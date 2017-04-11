package rl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.metabot.DummyPolicy;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.stochasticgames.madynamicprogramming.backupOperators.MinMaxQ;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.world.World;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import rl.adapters.gamenatives.PortfolioAIAdapter;
import rl.adapters.learners.PersistentLearner;
import rl.adapters.learners.PersistentMultiAgentQLearning;
import rl.adapters.learners.SGQLearningAdapter;
import rl.models.common.MicroRTSRewardFactory;


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
	 * Stores the world specified in parameters
	 */
	private World world;
	
	/**
	 * Stores the joint reward function
	 */
	private JointRewardFunction jointRwd;
	
	/**
	 * Stores players' information contained in config. file
	 */
	List<Node> playerNodes; 
	
	/**
	 * Stores the actual player objects
	 */
	List<PersistentLearner> players;
	
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
			integerParams.add(RLParamNames.TIMEOUT);
			integerParams.add(RLParamNames.PLAYOUTS);
			integerParams.add(RLParamNames.LOOKAHEAD);
			integerParams.add(RLParamNames.DEBUG_LEVEL);
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
		
		// experiment parameters
		params.put(RLParamNames.EPISODES, 100);
		params.put(RLParamNames.GAME_DURATION, 5000);
		params.put(RLParamNames.OUTPUT_DIR, "/tmp/rl-experiment/");
		
		params.put(RLParamNames.REWARD_FUNCTION, MicroRTSRewardFactory.WIN_LOSS);
		params.put(RLParamNames.ABSTRACTION_MODEL, WorldFactory.STAGES);
		params.put(RLParamNames.DEBUG_LEVEL, 0); // currently only affects PortfolioAI
		
		// parameters of RL methods
		params.put(RLParamNames.DISCOUNT, 0.9f);
		params.put(RLParamNames.LEARNING_RATE, 0.1f);
		params.put(RLParamNames.INITIAL_Q, 1.0f);
		
		// parameters of search methods
		params.put(RLParamNames.TIMEOUT, 100);
		params.put(RLParamNames.PLAYOUTS, -1);
		params.put(RLParamNames.LOOKAHEAD, 100);
		params.put(RLParamNames.EVALUATION_FUNCTION, SimpleSqrtEvaluationFunction3.class.getSimpleName());

		return params;
	}
	
	/**
	 * Resets parameters to their default values and 
	 * cleans up internal structures
	 */
	public void reset(){
		// resets stuff
		params = defaultParameters();
		players = null;
		world = null;
		jointRwd = null;
		playerNodes = null;
	}

	/*public Map<String, Object> defaultRLParameters(){
		Map<String, Object> params = new HashMap<>();
		params.put(RLParamNames.DISCOUNT, 0.9f);
		params.put(RLParamNames.LEARNING_RATE, 0.1f);
		params.put(RLParamNames.INITIAL_Q, 1.0f);
	}*/
	
	/**
	 * Reads the parameters of a xml file
	 * @param path the path to the xml file
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public Map<String, Object> loadFromFile(String path) throws SAXException, IOException, ParserConfigurationException{
		// initializes player nodes that will be parsed from file
		playerNodes = new ArrayList<>();
		
		// opens xml file
		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = dBuilder.parse(new File(path));
		
		// traverses all 1st level nodes of xml file (children of root node) 
		NodeList nList = doc.getDocumentElement().getChildNodes();
		
		for (int i = 0; i < nList.getLength(); i++){
			Node n = nList.item(i);

			//if node is 'parameters', traverses all its child nodes, setting experiment parameters accordingly
			if (n.getNodeName() == "parameters"){
				fillParameters(n, params);
			}
			
			//if node is 'player', stores its node for processing afterwards
			else if (n.getNodeName().equals("player")){
				playerNodes.add(n.cloneNode(true));
			}
			
		}
		
		return params;
	}
	
	/**
	 * Returns the value of a parameter identified by its name
	 * @param name
	 * @return
	 */
	public Object getParameter(String name){
		// some parameters require initialization before returning
		if(name.equalsIgnoreCase(RLParamNames.ABSTRACTION_MODEL)){
			return getWorld();
		}
		else if (name.equalsIgnoreCase(RLParamNames.REWARD_FUNCTION)){
			return getJointReward();
		}
		else if (name.equalsIgnoreCase(RLParamNames.PLAYERS)){
			return getPlayers();
		}
		
		// ordinary parameters are retrieved directly from params
		return params.get(name);
	}
	
	/**
	 * Returns the actual {@link World} object instead of its name  
	 * stored in {@link #params} map.
	 * @return
	 */
	public World getWorld(){
		// initializes world if necessary
		if(world == null){ 
			String worldModelName = (String) params.get(RLParamNames.ABSTRACTION_MODEL);
			world = WorldFactory.fromString(worldModelName, getJointReward());
		}
		return world;
	}
	
	/**
	 * Returns the actual {@link JointRewardFunction} object instead of its name  
	 * stored in {@link #params} map.
	 * @return
	 */
	public JointRewardFunction getJointReward(){
		// initializes joint reward if necessary
		if(jointRwd == null){
			jointRwd = MicroRTSRewardFactory.getRewardFunction(
				(String) params.get(RLParamNames.REWARD_FUNCTION)
			);
		}
		return jointRwd;
	}
	
	public List<PersistentLearner> getPlayers(){
		
		// initializes list of players if needed 
		if(players == null){
			
			if(playerNodes.size() < 2){
				throw new RuntimeException("You must specify at least two players.");
			}
			
			// process previously stored player nodes and creates players accordingly
			players = new ArrayList<>();
			for(Node n : playerNodes){
				players.add(processPlayerNode(n));
			}
		}
		return players;
	}
	
	/**
	 * Processes some parameters from command line
	 * @param line
	 * @return
	 */
	public Map<String, Object> parametersFromCommandLine(CommandLine line) {
		
		//Map<String, Object> params = defaultParameters();
		
		if(line.hasOption(RLParamNames.OUTPUT_DIR)){
			params.put(
				RLParamNames.OUTPUT_DIR, 
				line.getOptionValue(RLParamNames.OUTPUT_DIR)
			);
		}
		
		return params;
	}
	
	/**
	 * Extracts all relevant information about the player node
	 * @param playerNode
	 */
	private PersistentLearner processPlayerNode(Node playerNode){
		
		// retrieves the world model (needed for agent creation)
		World world = getWorld();
		
		// tests which type of player is specified and properly loads an agent
		Element e = (Element) playerNode;
		
		// loads parameters in a map
		Map<String, Object> playerParams = fillParameters(playerNode, defaultParameters());
		
		// QLearning or SGQLearningAdapter
		if ((e.getAttribute("type").equalsIgnoreCase("QLearning")) || 
				(e.getAttribute("type").equalsIgnoreCase("SGQLearningAdapter"))){
			
			QLearning ql = new QLearning(
				null, 
				(float) playerParams.get(RLParamNames.DISCOUNT), 
				new SimpleHashableStateFactory(false), 
				(float) playerParams.get(RLParamNames.INITIAL_Q), 
				(float) playerParams.get(RLParamNames.LEARNING_RATE)
			);

			// create a single-agent interface for the learning algorithm
			SGQLearningAdapter agent = new SGQLearningAdapter(
					world.getDomain(), ql, e.getAttribute("name"), 
					new SGAgentType("QLearning", world.getDomain().getActionTypes())
			);
			
			return agent;
		}
		
		// Dummy
		else if (e.getAttribute("type").equalsIgnoreCase("Dummy")) {
			//dummy is QLearning with 'dummy' learning policy
			
			QLearning ql = new QLearning(
				null, 
				0, //(float) qlParams.get(RLParamNames.DISCOUNT), 
				new SimpleHashableStateFactory(false), 
				0, //(float) qlParams.get(RLParamNames.INITIAL_Q), 
				0 //(float) qlParams.get(RLParamNames.LEARNING_RATE)
			);
			
			ql.setLearningPolicy(
				new DummyPolicy((String) playerParams.get(RLParamNames.DUMMY_POLICY), ql)
			);
			
			/*Field policyField = null;
			try {
				policyField = ql.getClass().getDeclaredField("learningPolicy");
			} catch (NoSuchFieldException | SecurityException e1) {
				e1.printStackTrace();
			}
			policyField.setAccessible(true);
		 	*/
			
			// create a single-agent interface the learning algorithm
			SGQLearningAdapter agent = new SGQLearningAdapter(
				world.getDomain(), ql, e.getAttribute("name"), 
				new SGAgentType("Dummy", world.getDomain().getActionTypes())
			);
			return agent;
		}
		
		// minimax-Q
		else if(e.getAttribute("type").equalsIgnoreCase("minimaxQ")) {
		
			//MinimaxQ example: https://groups.google.com/forum/#!topic/burlap-discussion/QYP6FKDGDnM
			PersistentMultiAgentQLearning mmq = new PersistentMultiAgentQLearning(
				world.getDomain(), 
				(float) playerParams.get(RLParamNames.DISCOUNT), 
				(float) playerParams.get(RLParamNames.LEARNING_RATE), 
				new SimpleHashableStateFactory(),
				(float) playerParams.get(RLParamNames.INITIAL_Q), 
				new MinMaxQ(), false, 
				e.getAttribute("name"), 
				new SGAgentType("MiniMaxQ", world.getDomain().getActionTypes())
			);
			
			return mmq;
		}
		
		// PortfolioAI or PortfolioAIAdapter
		else if(e.getAttribute("type").equalsIgnoreCase("PortfolioAI") || 
				e.getAttribute("type").equalsIgnoreCase("PortfolioAIAdapter")) {
			
			PortfolioAIAdapter agent = new PortfolioAIAdapter(
				e.getAttribute("name"), 
				new SGAgentType("PortfolioAI", world.getDomain().getActionTypes()),
				(int) playerParams.get(RLParamNames.TIMEOUT),
				(int) playerParams.get(RLParamNames.PLAYOUTS),
				(int) playerParams.get(RLParamNames.LOOKAHEAD),
				(String) playerParams.get(RLParamNames.EVALUATION_FUNCTION)
			);
			
			return agent;
		}
		
		throw new RuntimeException("Unrecognized player type: " + e.getAttribute("type"));
	}

	/**
	 * Processes the children of a {@link Node}, and return their values in a Map (paramName -> value)
	 * @param node a node containing parameters as in <node> <param1 value="1"/> <param2 value="2"/> </node>
	 * @return
	 *
	private Map<String, Object> fillParameters(Node node) {
		return fillParameters(node, new HashMap<String, Object>());
	}*/
	
	/**
	 * Processes the children of a {@link Node}, and return their values in a Map (paramName -> value)
	 * @param node a node containing parameters as in <node> <param1 value="1"/> <param2 value="2"/> </node>
	 * @param initialParameters a map containing the parameters, so that new values are overwritten
	 * @return
	 */
	private Map<String, Object> fillParameters(Node node, Map<String, Object> initialParameters) {
		
		for(Node parameter = node.getFirstChild(); parameter != null; parameter = parameter.getNextSibling()){
			
			if (parameter.getNodeType() != Node.ELEMENT_NODE) continue;	//prevents ClassCastException
			
			Element paramElement = (Element) parameter;
			if (integerParameters().contains(parameter.getNodeName())){
				initialParameters.put(parameter.getNodeName(), Integer.parseInt(paramElement.getAttribute("value")));
			}
			else if (floatParameters().contains(parameter.getNodeName())){
				initialParameters.put(parameter.getNodeName(), Float.parseFloat(paramElement.getAttribute("value")));
			}
			else {	//parameter is a string (probably)
				initialParameters.put(parameter.getNodeName(), paramElement.getAttribute("value"));
			}
		}
		return initialParameters;
	}
}
