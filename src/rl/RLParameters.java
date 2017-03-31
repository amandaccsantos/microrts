package rl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ai.metabot.DummyPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.stochasticgames.agents.interfacing.singleagent.LearningAgentToSGAgentInterface;
import burlap.behavior.stochasticgames.agents.maql.MultiAgentQLearning;
import burlap.behavior.stochasticgames.madynamicprogramming.backupOperators.MinMaxQ;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.world.World;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import rl.adapters.learners.PersistentLearner;
import rl.adapters.learners.PersistentMultiAgentQLearning;
import rl.adapters.learners.SGQLearningAdapter;


/**
 * Singleton class that takes care of RL experiment parameters
 * @author anderson
 *
 */
public class RLParameters {
	
	/**
	 * The singleton instance of this class
	 */
	private static RLParameters instance;
	
	/**
	 * A {@link Set} with names of integer parameters
	 */
	private  Set<String> integerParams = null;
	
	/**
	 * A {@link Set} with names of float parameters
	 */
	private  Set<String> floatParams = null;
	
	/**
	 * A {@link Map} String -> Object with parameter values
	 */
	private Map<String, Object> params;
	
	/**
	 * Initializes with default parameters
	 */
	private RLParameters(){
		params = defaultParameters();
	}
	
	/**
	 * Returns the singleton instance of this class
	 * @return
	 */
	public static RLParameters getInstance(){
		if (instance == null){
			instance = new RLParameters();
		}
		
		return instance;
	}
	
	/**
	 * Returns a {@link Set} with the name of integer parameters of the experiment
	 * @return
	 */
	public Set<String> integerParameters(){
		if (integerParams == null){
			integerParams = new HashSet<>();
			integerParams.add(RLParamNames.EPISODES);
			integerParams.add(RLParamNames.GAME_DURATION);
		}
		return integerParams;
	}
	
	/**
	 * Returns a {@link Set} with the name of float parameters of the experiment
	 * @return
	 */
	public Set<String> floatParameters(){
		if (floatParams == null){
			floatParams = new HashSet<>();
			floatParams.add(RLParamNames.DISCOUNT);
			floatParams.add(RLParamNames.LEARNING_RATE);
			floatParams.add(RLParamNames.INITIAL_Q);
		}
		return floatParams;
	}
	
	
	/**
	 * Returns the default parameters
	 * @return {@link Map}
	 */
	public Map<String, Object> defaultParameters(){
		Map<String, Object> params = new HashMap<>();
		
		params.put(RLParamNames.EPISODES, 1000);
		params.put(RLParamNames.GAME_DURATION, 3000);
		
		params.put(RLParamNames.DISCOUNT, 0.9f);
		params.put(RLParamNames.LEARNING_RATE, 0.1f);
		params.put(RLParamNames.INITIAL_Q, 1.0f);
		
		params.put(RLParamNames.OUTPUT_DIR, "/tmp/rl-experiment/");
		
		//instantiates the default world:
		World defaultWorld = AbstractionModels.stages();
		params.put(RLParamNames.ABSTRACTION_MODEL, defaultWorld);
		
		//adds the default players - their params: discount, StateFactory, defaultQ, learning rate
		List<SGAgent> players = new ArrayList<>();
		QLearning ql1 = new QLearning(null, 0.9f, new SimpleHashableStateFactory(false), 1, 0.1);
		QLearning ql2 = new QLearning(null, 0.9f, new SimpleHashableStateFactory(false), 1, 0.1);

		// create a single-agent interface for each of our learning algorithm
		LearningAgentToSGAgentInterface a1 = new LearningAgentToSGAgentInterface(
			defaultWorld.getDomain(), ql1, "agent0", 
			new SGAgentType("qlearning", defaultWorld.getDomain().getActionTypes())
		);
		LearningAgentToSGAgentInterface a2 = new LearningAgentToSGAgentInterface(
			defaultWorld.getDomain(), ql2, "agent1", 
			new SGAgentType("qlearning", defaultWorld.getDomain().getActionTypes())
		);
		
		players.add(a1);
		players.add(a2);
		
		params.put(RLParamNames.PLAYERS, params);
		
		return params;
	}

	
	/**
	 * Reads the parameters of a xml file
	 * @param path the path to the xml file
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public Map<String, Object> loadFromFile(String path) throws SAXException, IOException, ParserConfigurationException{
		//initializes default parameters
		params = defaultParameters();
		
		//initializes a list to load specified players, if there are any in the xml
		List<PersistentLearner> newPlayers = new ArrayList<>();
		
		//opens xml file
		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = dBuilder.parse(new File(path));
		
		//traverses all 1st level nodes of xml file (children of root node) 
		NodeList nList = doc.getDocumentElement().getChildNodes();
		
		for (int i = 0; i < nList.getLength(); i++){
			Node n = nList.item(i);

			//if node is 'parameters', traverses all its child nodes, setting experiment parameters accordingly
			if (n.getNodeName() == "parameters"){
				for(Node param = n.getFirstChild(); param != null; param = param.getNextSibling()){
					
					if (param.getNodeType() != Node.ELEMENT_NODE) continue;	//prevents ClassCastException
					
					//inserts the parameter on the map according to its type
					Element e = (Element) param;
					if (integerParameters().contains(param.getNodeName())){
						params.put(param.getNodeName(), Integer.parseInt(e.getAttribute("value")));
					}
					else if (floatParameters().contains(param.getNodeName())){
						params.put(param.getNodeName(), Float.parseFloat(e.getAttribute("value")));
					}
					//if node is 'abstraction-model', retrieves the appropriate model
					else if(param.getNodeName().equals(RLParamNames.ABSTRACTION_MODEL)){
						params.put(param.getNodeName(), AbstractionModels.fromString(e.getAttribute("value")));
					}
					else {	//parameter is an ordinary string (probably)
						params.put(param.getNodeName(), e.getAttribute("value"));
					}
				}
			}
			
			//if node is 'player', creates the specified player 
			else if (n.getNodeName().equals("player")){
				newPlayers.add(processPlayerNode(n));
			}
			
		}
		
		//replace default players if new ones were specified in xml
		if (! newPlayers.isEmpty()){
			params.put(RLParamNames.PLAYERS, newPlayers);
		}
		
		return params;
	}
	
	/**
	 * Extracts all relevant information about the player node
	 * @param playerNode
	 */
	private PersistentLearner processPlayerNode(Node playerNode){

		
		//retrieves the world model (needed for agent creation)
		World world = (World) params.get(RLParamNames.ABSTRACTION_MODEL);
		
		// tests which type of player is specified and properly loads an agent
		Element e = (Element) playerNode;
		
		if (e.getAttribute("type").equalsIgnoreCase("QLearning")){
			//loads parameters in a map
			Map<String, Object> qlParams = fillParameters(playerNode);
			
			
			QLearning ql = new QLearning(
				null, 
				(float) qlParams.get(RLParamNames.DISCOUNT), 
				new SimpleHashableStateFactory(false), 
				(float) qlParams.get(RLParamNames.INITIAL_Q), 
				(float) qlParams.get(RLParamNames.LEARNING_RATE)
			);

			// create a single-agent interface for the learning algorithm
			SGQLearningAdapter agent = new SGQLearningAdapter(
					world.getDomain(), ql, e.getAttribute("name"), 
					new SGAgentType("QLearning", world.getDomain().getActionTypes())
			);
			
			return agent;
		}
		
		else if (e.getAttribute("type").equalsIgnoreCase("Dummy")) {
			//dummy is QLearning with 'dummy' learning policy
			Map<String, Object> qlParams = fillParameters(playerNode);
			
			QLearning ql = new QLearning(
				null, 
				0, //(float) qlParams.get(RLParamNames.DISCOUNT), 
				new SimpleHashableStateFactory(false), 
				0, //(float) qlParams.get(RLParamNames.INITIAL_Q), 
				0 //(float) qlParams.get(RLParamNames.LEARNING_RATE)
			);
			
			ql.setLearningPolicy(
				new DummyPolicy((String) qlParams.get(RLParamNames.DUMMY_POLICY), ql)
			);
			
			Field policyField = null;
			try {
				policyField = ql.getClass().getDeclaredField("learningPolicy");
			} catch (NoSuchFieldException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			policyField.setAccessible(true);
			Policy thePolicy = null;
			try {
				thePolicy = (Policy) policyField.get(ql);
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// create a single-agent interface the learning algorithm
			SGQLearningAdapter agent = new SGQLearningAdapter(
					world.getDomain(), ql, e.getAttribute("name"), 
					new SGAgentType("Dummy", world.getDomain().getActionTypes())
			);
			return agent;
		}
		
		else if(e.getAttribute("type").equalsIgnoreCase("minimaxQ")) {
		
			//MinimaxQ example: https://groups.google.com/forum/#!topic/burlap-discussion/QYP6FKDGDnM
			PersistentMultiAgentQLearning mmq = new PersistentMultiAgentQLearning(
				world.getDomain(), .9, .1, new SimpleHashableStateFactory(),
				1, new MinMaxQ(), true, e.getAttribute("name"), 
				new SGAgentType("MiniMaxQ", world.getDomain().getActionTypes())
			);
			
			return mmq;
		}
		
		else if (e.getAttribute("type").equalsIgnoreCase("SGQLearningAdapter")){
			//loads parameters in a map
			Map<String, Object> qlParams = fillParameters(playerNode);
			
			QLearning ql = new QLearning(
				null, 
				(float) qlParams.get(RLParamNames.DISCOUNT), 
				new SimpleHashableStateFactory(false), 
				(float) qlParams.get(RLParamNames.INITIAL_Q), 
				(float) qlParams.get(RLParamNames.LEARNING_RATE)
			);

			// create a single-agent interface for the learning algorithm
			SGQLearningAdapter agent = new SGQLearningAdapter(
					world.getDomain(), ql, e.getAttribute("name"), 
					new SGAgentType("SGQLearningAdapter", world.getDomain().getActionTypes())
			);
			
			return agent;
		}
		
		throw new RuntimeException("Could not load player from file.");
	}

	/**
	 * Processes the children of a {@link Node}, and return their values in a Map (paramName -> value)
	 * @param node a node containing parameters as in <node> <param1 value="1"/> <param2 value="2"/> </node>
	 * @return
	 */
	private Map<String, Object> fillParameters(Node node) {
		Map<String, Object> parameters = new HashMap<>();
		for(Node parameter = node.getFirstChild(); parameter != null; parameter = parameter.getNextSibling()){
			
			if (parameter.getNodeType() != Node.ELEMENT_NODE) continue;	//prevents ClassCastException
			
			Element paramElement = (Element) parameter;
			if (integerParameters().contains(parameter.getNodeName())){
				parameters.put(parameter.getNodeName(), Integer.parseInt(paramElement.getAttribute("value")));
			}
			else if (floatParameters().contains(parameter.getNodeName())){
				parameters.put(parameter.getNodeName(), Float.parseFloat(paramElement.getAttribute("value")));
			}
			else {	//parameter is a string (probably)
				parameters.put(parameter.getNodeName(), paramElement.getAttribute("value"));
			}
		}
		return parameters;
	}
}
