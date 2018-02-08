package rl.planners;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.jdom.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.world.World;
import rl.adapters.domain.EnumerableSGDomain;
import rl.adapters.learners.PersistentLearner;
import rl.models.aggregatediff.AggregateDifferencesDomain;
import rl.models.common.MicroRTSState;
import rl.models.common.MicroRTSTerminalFunction;
import util.Pair;

public class BackwardInduction implements PersistentLearner {
	
	/**
	 * Agent's name
	 */
	String name;
	
	/**
	 * Agent's type
	 */
	SGAgentType type;
	
	/**
	 * Directory to use for communicating with Gambit library
	 */
	String workingDir;
	
	/**
	 * Agent's number in a game (0 or 1) -- important when selecting the policy
	 */
	int agentNumber;
	
	/**
	 * State value function
	 */
	Map<MicroRTSState, Double> V;
	
	/**
	 * State-action value function
	 */
	Map<MicroRTSState, Map<JointAction, Double>> Q;
	
	/**
	 * Maps the state to a probability distribution over actions for the 1st player
	 */
	Map<MicroRTSState, Map<Action, Double>> p1Policy;

	/**
	 * Maps the state to a probability distribution over actions for the 2nd player
	 */
	Map<MicroRTSState, Map<Action, Double>> p2Policy;
	
	/**
	 * A transition function learned by the agent while it performs
	 * the backward induction
	 */
	Map<MicroRTSState, Map<JointAction, MicroRTSState>> T;
	
	
	/**
	 * Stores which states were already visited
	 */
	Set<MicroRTSState> visited;
	
	/**
	 * The domain in which this agent is in
	 */
	EnumerableSGDomain domain;
	
	/**
	 * A function to test whether state is terminal 
	 */
	TerminalFunction terminalFunction;
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param terminal
	 */
	public BackwardInduction(String name, EnumerableSGDomain domain, TerminalFunction terminal){
		this.name = name;
		this.type = new SGAgentType("BackwardInduction", domain.getActionTypes());
		
		V = new HashMap<>();
		Q = new HashMap<>();
		T = new HashMap<>();
		
		p1Policy = new HashMap<>();
		p2Policy = new HashMap<>();
		visited = new HashSet<>();
		
		this.domain = domain;
		terminalFunction = terminal;

		// uses a unique identifier for the working dir
		workingDir = "/tmp/bi_" + UUID.randomUUID().toString() + "/";
		
		// creates the working dir
		File dir = new File(workingDir);

		if(dir.mkdirs() == false){
			throw new RuntimeException("Unable to create working directory " + workingDir);
		}
		
		preAllocate();
	}
	
	/**
	 * Returns the value of a given state
	 * @param s
	 * @return
	 */
	public double value(State s){
		return V.get(s);
	}
	
	/**
	 * Returns the value of a given state-joint action pair
	 * @param s
	 * @param ja
	 * @return
	 */
	public double value(State s, JointAction ja){
		return Q.get(s).get(ja);
	}
	
	/**
	 * Returns the resulting state of taking joint action 
	 * ja in state s (if this transition has been learned, 
	 * otherwise returns null)
	 * @param s
	 * @param ja
	 * @return
	 */
	public State transition(State s, JointAction ja){
		return T.get(s).get(ja);
	}
	
	/**
	 * Returns the policy for player 1 or 2 depending on the received index
	 * @param playerIndex if 0, returns policy for player 1, otherwise, returns player 2 policy
	 * @return
	 */
	public Map<MicroRTSState, Map<Action, Double>> getPolicyFor(int playerIndex){
		return playerIndex == 0 ? p1Policy : p2Policy;
	}
	
	/**
	 * Returns the policy for a given state
	 * @param s
	 * @param playerIndex 0 or 1 indicating the first or second player
	 * @return
	 */
	public Map<Action, Double> policy(State s, int playerIndex){
		return getPolicyFor(playerIndex).get(s);
	}
	
	/**
	 * Returns whether a state was cached / solved during solving via backward induction
	 * @param s
	 * @return
	 */
	public boolean cached(State s){
		return visited.contains(s);
	}
	
	/**
	 * Returns the probability of selecting an action in a given state
	 * @param s
	 * @param a
	 * @param playerIndex 0 or 1 indicating the first or second player
	 * @return
	 */
	public double probability(State s, Action a, int playerIndex){
		return getPolicyFor(playerIndex).get(s).get(a);
	}
	
	/**
	 * Runs the backward induction algorithm across all states
	 * (might take a lot of time and memory...)
	 * FIXME: does not work because states must have an associated 
	 * underlying microRTS GameState
	 * @param s
	 */
	public void solveAll(){
		List<? extends State> allStates = domain.enumerate();
		
		for(State s : allStates){
			MicroRTSState state = (MicroRTSState) s;
			solve(state);
		}
	}
	
	/**
	 * Uses the backward induction algorithm to solve and return the
	 * value of a state. Caches solved states so that they don't need
	 * to be solved twice
	 * @param s
	 * @return
	 */
	public double solve(MicroRTSState s){
		
		// for a terminal state, store its value
		if(terminalFunction.isTerminal(s)){
			// 0 or 1 if a player wins, or -1 for draw
			int winner = s.getUnderlyingState().winner();
			
			if (winner == -1){ // draw
				V.put(s, 0.);
			}
			else if (winner == 0){ // first player wins
				V.put(s, 1e6);
			}
			else { // second player wins
				V.put(s, -1e6); 
			}
			
			// marks as visited
			visited.add(s);
		}
		
		// finds the value of the state
		if(! visited.contains(s)){
			long start = System.currentTimeMillis();
			//System.out.print("\rEntering " + s +"     ");
			for(ActionType a : type.actions){
				for(ActionType o : type.actions){
					JointAction ja = new JointAction();
					ja.addAction(a.associatedAction(null));
					ja.addAction(o.associatedAction(null));
					
					//makes a copy of the state to be sampled (let's see if it improves BI)
					MicroRTSState next = (MicroRTSState) domain.getJointActionModel().sample(
						s.copy(), ja
					);
					
					//learns a transition
					T.get(s).put(ja, next);
					
					try {
						if(!Q.containsKey(s)){
							System.err.println("WARNING: missing key for " + s);
							Q.put(s, new HashMap<>());
						}
						
						Q.get(s).put(ja, solve(next));		// recursive call
					} catch (NullPointerException e) {
						System.err.println("NullPointerException in state " + s);
						e.printStackTrace();
						System.exit(0);
					}
					
				}
				long finish = System.currentTimeMillis();
				System.out.println("Took " + (finish - start) + " ms to solve " + s);
			}
			// sets the value and mark as visited
			V.put(s, calculateValue(s)); 
			visited.add(s);
			System.out.print(String.format("\rClosed state number %7d", visited.size()));
		}
		
		// value of s is determined and it is marked as visited. Return its value!
		return V.get(s);
	}
	
	/**
	 * Creates entries in V and Q tables by enumerating states and joint actions
	 */
	public void preAllocate(){
		
		List<? extends State> allStates = domain.enumerate();
		
		//System.out.println("Pre-allocating for " + allStates.size() + " states");
		
		// creates an entry in V for each state
		for(State s : allStates){
			MicroRTSState state = (MicroRTSState) s;
			V.put(state, 0.);
			
			// creates an entry in Q for each state and joint action
			Q.put(state, new HashMap<>());
			
			// creates an entry in the transition function for each state and joint action
			T.put(state, new HashMap<>());
			
			// creates an entry in policies for each state and action
			p1Policy.put(state, new HashMap<>());
			p2Policy.put(state, new HashMap<>());
			
			for(ActionType a : type.actions){
				
				// starts with equiprobable / fully random policy
				p1Policy.get(state).put(a.associatedAction(null), 1. / type.actions.size());
				
				for(ActionType o : type.actions){
					
					// starts with equiprobable / fully random policy
					p2Policy.get(state).put(o.associatedAction(null), 1. / type.actions.size());
					
					JointAction ja = new JointAction();
					ja.addAction(a.associatedAction(null));
					ja.addAction(o.associatedAction(null));
					
					Q.get(state).put(ja, 0.);
				}
			}
		}
		
		//System.out.println("Done.");
		//System.out.println("V entries: " + V.size());
		//System.out.println("Q entries: " + Q.size());
	}
	
	/**
	 * Solves the game for a state and returns its value.
	 * As a side effect, updates players' policies for the given state.
	 * It also updates the cached state value in V
	 * @param s
	 * @return
	 */
	public double calculateValue(State s) {
		Pair<double[], double[]> policies = null;
		try {
			policies = getPoliciesFor(s);
		} catch (IOException | InterruptedException e) {
			System.err.println("Error while calculating policy for state " + s);
			e.printStackTrace();
			System.exit(0);
		}
		
		// arrays filled with policies calculated from gambit
		double[] agentPolicy = policies.m_a;
		double[] opponentPolicy = policies.m_b;
		
		double stateValue = 0;
		int agentActionIndex = 0;
		for(ActionType a : type.actions){
			int opponentActionIndex = 0;
			
			// updates the probability for this state-action pair for player 1
			p1Policy.get(s).put(a.associatedAction(null), agentPolicy[agentActionIndex]);
			
			for(ActionType o : type.actions){
				// updates the prob. for this state-action pair for player 2
				p2Policy.get(s).put(o.associatedAction(null), opponentPolicy[opponentActionIndex]);
				
				JointAction ja = new JointAction();
				ja.addAction(a.associatedAction(null));
				ja.addAction(o.associatedAction(null));
				
				/*
				 *  adds the value of each outcome by the probability of its occurrence
				 *  which is dictated by the policies
				 */
				stateValue += agentPolicy[agentActionIndex] * opponentPolicy[opponentActionIndex]
						* Q.get(s).get(ja);
				
				opponentActionIndex++;
			}
			agentActionIndex++;
		}
		V.put((MicroRTSState) s, stateValue);
		visited.add((MicroRTSState) s);
		return stateValue;
	}
	
	/**
	 * Calculates the equilibrium of state and returns the policy
	 * @param s
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 * TODO extract the part that formulates the NFG so that it can be reused
	 */
	public Pair<double[], double[]>  getPoliciesFor(State s) throws IOException, InterruptedException{
		
		// writes a file with a normal-form game for Gambit
		String fileForGambit = workingDir + "/state.nfg";
		BufferedWriter fileWriter;
		fileWriter = new BufferedWriter(new FileWriter(fileForGambit));
		
		fileWriter.write("NFG 1 R \"" + s + "\"\n");
		fileWriter.write(
			String.format("{\"Player0\" \"Player1\"}{%d %d}\n", type.actions.size(), type.actions.size())
		);
		
		//fills Q values for the given state 
		// Fix: Player1 (opponent) actions must be in the outer loop, because
		// Gambit requires Player0 (agent) actions to roll over first
		for(ActionType o : type.actions){
			for(ActionType a : type.actions){
				JointAction ja = new JointAction();
				ja.addAction(a.associatedAction(null));
				ja.addAction(o.associatedAction(null));
				
				double myPayoff = Q.get(s).get(ja);
				fileWriter.write(
					String.format(Locale.ROOT, "%f %f ", myPayoff, -myPayoff)
				);
			}
		}
		fileWriter.close();
		
		// starts gambit and captures its output
		// TODO: checks for trivial games, avoiding unnecessary calls to gambit
		
		// using gambit-lcp because it returns a single equilibrium
		// 15-digit precision, hope numeric errors don't accumulate
		Process gambit = Runtime.getRuntime().exec("gambit-lcp -d 15 -q " + fileForGambit);
		BufferedReader bri = new BufferedReader (new InputStreamReader(gambit.getInputStream()));
		String result = bri.readLine();	//get the first equilibrium
	    bri.close();
		gambit.waitFor();
		
		// resulting String is NE,prob1a,prob1b,...,prob2a,prob2b 
		String[] parts = result.trim().split(",");
		
		double[] agentPolicy = new double[type.actions.size()];
		double[] opponentPolicy = new double[type.actions.size()];
		
		// reads policies
		for(int i = 0; i < agentPolicy.length; i++){
			// i+1 to skip the first token 'NE'
			agentPolicy[i] = Double.parseDouble(parts[i+1]);
			
			// opponent policy offset 'length' from agentPolicy 
			opponentPolicy[i] = Double.parseDouble(parts[i + agentPolicy.length + 1]);
		}
		
		return new Pair<double[], double[]>(agentPolicy, opponentPolicy);
	}
	
	

	@Override
	public String agentName() {
		return name;
	}

	@Override
	public SGAgentType agentType() {
		return type;
	}

	@Override
	public void gameStarting(World w, int agentNum) {
		this.agentNumber = agentNum;
	}

	@Override
	public Action action(State s) {
		
		double [] policyArray = null;
		if(!visited.contains(s)){
			System.out.println("State is not cached, will solve for its' policy " + s);
			/*Pair<double[], double[]> policies = null;
			try {
				policies = getPoliciesFor(s);
			} catch (IOException | InterruptedException e) {
				System.err.println("Error while getting action for state " + s);
				e.printStackTrace();
				return null;
			}*/
			solve((MicroRTSState) s);
			
			// uses the policy of row or column player depending on my number
			//policyArray = agentNumber == 0 ? policies.m_a : policies.m_b;
			
		}
		
		//policy is cached! just convert to an array of doubles...
		Map<MicroRTSState, Map<Action, Double>> thePolicy = getPolicyFor(agentNumber);
		policyArray =  ArrayUtils.toPrimitive(
			thePolicy.get(s).values().toArray(new Double[type.actions.size()])
		);
		
		
		// use the policy array to perform a roulette selection
		return rouletteSelection(policyArray);
		
	}
	
	/**
	 * Selects an action according to the given probability vector
	 * Code from http://stackoverflow.com/a/10949834
	 * @param probabilities
	 * @return
	 */
	public Action rouletteSelection(double[] probabilities) {
	    float totalScore = 0;
	    float runningScore = 0;
	    for (double prob : probabilities) {
	        totalScore += prob;
	    }

	    float rnd = (float) (Math.random() * totalScore);

	    List<ActionType> actionTypes = this.type.actions;
	    
	    for(int i = 0; i < probabilities.length; i++){
	        if (rnd >= runningScore && rnd <= runningScore+ probabilities[i]){
	            //selected
	        	return actionTypes.get(i).associatedAction(null);
	        }
	        runningScore += probabilities[i];
	    }
	    
	    // wut? didn't find an action?
	    return null;
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction, double[] jointReward, State sprime,
			boolean isTerminal) {
		// checks if the transition I knew is the same that happened
		// (it can be different if the underlying state on the abstract states is different)
		// disabled because we don't load the learned transitions to play a game
		if(T.containsKey(s)){
			if(!T.get(s).containsKey(jointAction)){
				System.out.println("I don't have a transition for " + s + " and " + jointAction);
				System.out.println("This is  " + sprime);
				
			}
			else if(!T.get(s).get(jointAction).equals(sprime)){
				System.out.println("I had a different transition for " + s + " and " + jointAction);
				System.out.println("Mine was " + T.get(s).get(jointAction));
				System.out.println("This is  " + sprime);
			}
		}
	}

	@Override
	public void gameTerminated() {
		// do nothing
	}

	@Override
	public void saveKnowledge(String path) {
		if (!(domain instanceof EnumerableSGDomain)){
			System.err.println("Can only save knowledge for EnumerableSGDomains, this one is: " + domain.getClass().getName());
			return;
		}
		
		BufferedWriter fileWriter;
		try {
			fileWriter = new BufferedWriter(new FileWriter(path));

			EnumerableSGDomain enumDomain = (EnumerableSGDomain) domain;
			
			// xml root node
			fileWriter.write("<knowledge>\n\n"); 
			
			// information about who is saving knowledge, i.e., myself
			// this might be useful when retrieving the joint action later
			fileWriter.write(String.format(
				"<learner name='%s' type='%s' id='%d' />\n\n", 
				agentName(), this.getClass().getName(), agentNumber
			));
			
			// a friendly remark
			fileWriter.write(
				"<!-- Note: "
				+ "'ja' stands for joint action\n"
				+ "Joint action name is agent0Action;agent1Action;... "
				+ " always in this order.\n\n"
				+ "pi_0 stands for the policy of player 1 and likewise for pi_1. \n"
				+ "This indicates the probability of selecting each action -->\n\n"
			);
			
			for (State s : enumDomain.enumerate()) {
				// opens state tag
				fileWriter.write(
					String.format(
						Locale.ROOT, 
						"<state id='%s' value='%f' visited='%s'>\n", 
						s, V.get(s), visited.contains(s)? "true" : "false"
					)
				); 
				
				for(ActionType playerAction : this.type.actions){
					for(ActionType opponentAction : this.type.actions){
						JointAction ja = new JointAction();
						ja.addAction(playerAction.associatedAction(null));
						ja.addAction(opponentAction.associatedAction(null));
						
						// writes the joint action tag
						// action name is agent0Action;agent1Action;...
						// always in this order
						fileWriter.write(String.format(
							Locale.ROOT,
							"\t<ja name='%s' value='%f' pi_1='%f' pi_2='%f' />\n",
							ja.actionName(),
							Q.get(s).get(ja),
							getPolicyFor(0).get(s).get(playerAction.associatedAction(null)),
							getPolicyFor(1).get(s).get(opponentAction.associatedAction(null))
						));
						
						/*if (visited.contains(s)){
							System.out.println("Saving " + s);
							System.out.println("pi1: " + getPolicyFor(0).get(s));
							System.out.println("pi2: " + getPolicyFor(1).get(s));
						}*/
						
					}
				}
				
					
				// closes state tag
				fileWriter.write("</state>\n\n");
			}
			
			// closes xml root
			fileWriter.write("</knowledge>\n"); 
			fileWriter.close();
		
		} catch (IOException e) {
			System.err.println("ERROR: Unable to save knowledge to file " + path);
			e.printStackTrace();
		}
	}

	@Override
	public void loadKnowledge(String path) {
		if (! (domain instanceof EnumerableSGDomain)){
			throw new RuntimeException("Cannot loadKnowledge if domain is not a EnumerableSGDomain");
		}
		
		//opens xml file
		DocumentBuilder dBuilder = null;
		Document doc = null;
		try {
			dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = dBuilder.parse(new File(path));
		} catch (ParserConfigurationException|SAXException|IOException e) {
			System.err.println("ERROR when parsing " + path +". knowledge NOT LOADED!");
			e.printStackTrace();
			return;
		}
		
		//retrieves the game states, they'll be useful for filling the knowledge in
		Map<String, State> nameToState = ((EnumerableSGDomain)domain).namesToStates();
		
		//traverses all 1st level nodes of xml file (children of root node) 
		NodeList nList = doc.getDocumentElement().getChildNodes();
		
		for (int i = 0; i < nList.getLength(); i++){
			Node n = nList.item(i);

			//if node is 'learner', checks if ID is the same as mine
			if (n.getNodeName() == "learner"){
				Element e = (Element) n;
				if(Integer.parseInt(e.getAttribute("id")) != agentNumber){
					System.err.println(
						"WARNING! Loading knowledge with agent with different ID. "
						+ "I'll be probably loading a transposed reward matrix."
					);
				}
			}
			
			//if node is 'state', loads its value and that of joint actions for it
			else if (n.getNodeName() == "state"){
				Element e = (Element) n;
				
				String stateID = e.getAttribute("id");
				MicroRTSState state = (MicroRTSState) nameToState.get(stateID);
				V.put(state, Double.parseDouble(e.getAttribute("value")));
				
				// checks whether the state was visited during game solving
				String visitedAttr = e.getAttribute("visited");
				if (visitedAttr.equalsIgnoreCase("true")){
					visited.add(state);
				}
				
				
				// checks if need to allocate Q and the policies
				if(! Q.containsKey(state)){
					Q.put(state, new HashMap<>());
				}
				if(! p1Policy.containsKey(state)){
					p1Policy.put(state, new HashMap<>());
				}
				if(! p2Policy.containsKey(state)){
					p2Policy.put(state, new HashMap<>());
				}
				
				//System.out.println("State: " + state);
				
				//jaNode stands for joint action node
				for(Node jaNode = n.getFirstChild(); jaNode != null; jaNode = jaNode.getNextSibling()){
					
					if (jaNode.getNodeType() != Node.ELEMENT_NODE) continue;	//prevents ClassCastException
					
					// process the node, filling in the joint action value
					Element jaElement = (Element) jaNode;
					
					// names of action components are separated by semicolon
					String names[] = jaElement.getAttribute("name").split(";");
					
					// fills the list of joint action components
					List<Action> components = new ArrayList<>();
					for (String name : names){
						components.add(
							this.domain.getActionType(name).associatedAction(null)
						);
					}
					
					// retrieves the joint action and sets its value
					JointAction ja = new JointAction(components);
					//JAQValue jaq = myQSource.getQValueFor(s, ja); 
					Q.get(state).put(ja, Double.parseDouble(jaElement.getAttribute("value")));
					
					//retrieves the policies of the players
					double pi_1 = Double.parseDouble(jaElement.getAttribute("pi_1")); 
					double pi_2 = Double.parseDouble(jaElement.getAttribute("pi_2"));
					
					//System.out.println("Putting " + pi_1 + " into " + ja.action(0));
					
					p1Policy.get(state).put(ja.action(0), pi_1);
					p2Policy.get(state).put(ja.action(1), pi_2);
					
					//System.out.println("Put " + p1Policy.get(state).get(ja.action(0)) + " into " + ja.action(0) );
				}
			}
		}
		// TODO: this should be temporary, to test whether solving via BI covers all metagame states:
		System.out.println("Loading transitions...");
		loadTransitions("/tmp/transitions.xml");
	}
	
	/**
	 * Dumps the set of visited states to a file
	 * @param path
	 */
	public void dumpVisited(String path){
		BufferedWriter fileWriter;
		try {
			fileWriter = new BufferedWriter(new FileWriter(path));
			String lineSeparated = visited.stream()
		        .map( v -> v.toString() )
		        .collect( Collectors.joining("\n") 
    		);	
			//fileWriter.write("" + visited);
			fileWriter.write(lineSeparated);
			
			fileWriter.close();
		} catch (IOException e) {
			System.err.println("ERROR: Unable to dump visited to " + path);
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Dumps the learned transitions to a file
	 * @param path
	 */
	public void dumpTransitions(String path){
		/*BufferedWriter fileWriter;
		try {
			fileWriter = new BufferedWriter(new FileWriter(path));
			String lineSeparated = T.entrySet().stream()
		        .map( t -> t.toString() )
		        .collect( Collectors.joining("\n") 
    		);	
			fileWriter.write(lineSeparated);
			
			fileWriter.close();
		} catch (IOException e) {
			System.err.println("ERROR: Unable to dump learned transitions to " + path);
			e.printStackTrace();
			return;
		}*/
		try {
			XStream xs = new XStream(); 
			ObjectOutputStream oos = new ObjectOutputStream( 
				new FileOutputStream(path,false)
			);
			xs.toXML(T, oos);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadTransitions(String path){
		try {
			XStream xs = new XStream(); 
                
			ObjectInputStream oos = new ObjectInputStream(                                 
			        new FileInputStream(path)
			);
			T = (Map<MicroRTSState, Map<JointAction, MicroRTSState>>) xs.fromXML(oos);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Runs a test of Backward Induction
	 * @param args
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	public static void main(String[] args) throws JDOMException, IOException{
		TerminalFunction tf = new MicroRTSTerminalFunction();
		AggregateDifferencesDomain domain = new AggregateDifferencesDomain();
		
		BackwardInduction bi = new BackwardInduction("test", domain, tf);
		
		//System.out.println(args[0]);
		
		if(args.length > 0 && args[0].equals("--all")){
			System.out.println("Solving for all states! Might take a lot of time and memory...");
			bi.solveAll();
			System.out.println("\nSolved for all!.");
		}
		else {
			System.out.println("Solving from initial state...");
			bi.solve((MicroRTSState) domain.getInitialState());
			System.out.println("\nSolved.");
		}
		
		System.out.println("Saving knowledge...");
		bi.saveKnowledge("/tmp/solution.xml");
		
		System.out.println("Dumping visited...");
		bi.dumpVisited("/tmp/visited.txt");
		
		System.out.println("Dumping transitions...");
		bi.dumpTransitions("/tmp/transitions.xml");
		
		System.out.println("Done. Saved knowledge in /tmp/solution.xml.");
		System.out.println(
			"Check out visited in /tmp/visited.txt and transitions "
			+ "in /tmp/transitions.xml."
		);
		
	}
}
