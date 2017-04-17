package ai.metagame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jdom.JDOMException;

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

public class BackwardInduction implements PersistentLearner {
	String name;
	SGAgentType type;
	int agentNumber;
	
	Map<MicroRTSState, Double> V;
	Map<MicroRTSState, Map<JointAction, Double>> Q;
	
	Set<MicroRTSState> visited;
	
	EnumerableSGDomain domain;
	TerminalFunction terminalFunction;
	
	public BackwardInduction(String name, EnumerableSGDomain domain, TerminalFunction terminal){
		this.name = name;
		this.type = new SGAgentType("BackwardInduction", domain.getActionTypes());
		
		V = new HashMap<>();
		Q = new HashMap<>();
		visited = new HashSet<>();
		
		this.domain = domain;
		terminalFunction = terminal;

		preAllocate();
	}
	
	public double solve(MicroRTSState s){
		
		// for a terminal state, store its value and return it
		if(terminalFunction.isTerminal(s)){
			// 0 or 1 if a player wins, or -1 for draw
			int winner = s.getUnderlyingState().winner();
			
			if (winner == -1){ // draw
				V.put(s, 0.);
			}
			else if (winner == 0){ // first player wins
				V.put(s, 1.);
			}
			else { // second player wins
				V.put(s, -1.); 
			}
			
			return V.get(s);
		}
		
		// finds the value of the state
		if(! visited.contains(s)){
			//System.out.print("\rEntering " + s +"     ");
			for(ActionType a : type.actions){
				for(ActionType o : type.actions){
					JointAction ja = new JointAction();
					ja.addAction(a.associatedAction(null));
					ja.addAction(o.associatedAction(null));
					
					MicroRTSState next = (MicroRTSState) domain.getJointActionModel().sample(s, ja);
					try {
						if(!Q.containsKey(s)){
							System.err.println("WARNING: missing key for " + s);
							Q.put(s, new HashMap<>());
						}
						Q.get(s).put(ja, solve(next));		// recursive call
					} catch (NullPointerException e) {
						System.out.println("NPE in state " + s);
						e.printStackTrace();
						System.exit(0);
					}
					
				}
			}
			try {
				V.put(s, calculateValue(s)); 
				visited.add(s);
			} catch (IOException|InterruptedException e) {
				System.err.println("Error while solving for state " + s);
				e.printStackTrace();
				System.exit(0);
			}
			
			
			System.out.print(String.format("\rClosed state number %7d", visited.size()));
		}
		
		// s is solved, return its value
		return V.get(s);
	}
	
	/**
	 * Creates entries in V and Q tables by enumerating states and joint actions
	 */
	public void preAllocate(){
		
		List<? extends State> allStates = domain.enumerate();
		
		System.out.println("Pre-allocating for " + allStates.size() + " states");
		
		// creates an entry in V for each state
		for(State s : allStates){
			MicroRTSState state = (MicroRTSState) s;
			V.put(state, 0.);
			
			// creates an entry in Q for each state and joint action
			Q.put(state, new HashMap<>());
			
			for(ActionType a : type.actions){
				for(ActionType o : type.actions){
					JointAction ja = new JointAction();
					ja.addAction(a.associatedAction(null));
					ja.addAction(o.associatedAction(null));
					
					Q.get(state).put(ja, 0.);
				}
			}
		}
		
		System.out.println("Done.");
		System.out.println("V entries: " + V.size());
		System.out.println("Q entries: " + Q.size());
	}
	
	/**
	 * Formulates the normal-form game contained in a state
	 * for Gambit to solve and returns its value
	 * @param s
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public double calculateValue(State s) throws IOException, InterruptedException{
		BufferedWriter fileWriter;
		fileWriter = new BufferedWriter(new FileWriter("/tmp/state.nfg"));
		
		fileWriter.write("NFG 1 R \"" + s + "\"\n");
		fileWriter.write(
			String.format("{\"Player0\" \"Player1\"}{%d %d}\n", type.actions.size(), type.actions.size())
		);
		
		//fills Q values for the given state 
		for(ActionType a : type.actions){
			for(ActionType o : type.actions){
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
		Process gambit = Runtime.getRuntime().exec("gambit-lcp -d 15 -q /tmp/state.nfg");
		BufferedReader bri = new BufferedReader (new InputStreamReader(gambit.getInputStream()));
		String result = bri.readLine();	//get the first equilibrium
	    bri.close();
		gambit.waitFor();
		//Scanner scanner = new Scanner(new File("/tmp/result.txt"));
		//String result = new String(Files.readAllBytes(Paths.get("/tmp/result.txt")));
		
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
		
		//TODO now we have the policy, how about the value?
		double stateValue = 0;
		int agentActionIndex = 0;
		for(ActionType a : type.actions){
			int opponentActionIndex = 0;
			for(ActionType o : type.actions){
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
		
		return stateValue;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction, double[] jointReward, State sprime,
			boolean isTerminal) {
		// do nothing
	}

	@Override
	public void gameTerminated() {
		// do nothing
		
	}

	@Override
	public void saveKnowledge(String path) {
		// TODO Auto-generated method stub
	}

	@Override
	public void loadKnowledge(String path) {
		// TODO Auto-generated method stub
		
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
		
		System.out.println("Solving...");
		bi.solve((MicroRTSState) domain.getInitialState());
		System.out.println("\nSolved.");
		
	}
}
