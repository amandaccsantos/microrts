package ai.metagame;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import rl.WorldFactory;
import rl.adapters.domain.EnumerableSGDomain;
import rl.adapters.learners.PersistentLearner;
import rl.models.aggregatediff.AggregateDiffState;
import rl.models.aggregatediff.AggregateDifferencesDomain;
import rl.models.common.MicroRTSState;
import rl.models.common.MicroRTSTerminalFunction;
import rl.models.common.ScriptActionTypes;
import rts.GameState;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

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
			
			for(ActionType a : type.actions){
				for(ActionType o : type.actions){
					JointAction ja = new JointAction();
					ja.addAction(a.associatedAction(null));
					ja.addAction(o.associatedAction(null));
					
					MicroRTSState next = (MicroRTSState) domain.getJointActionModel().sample(s, ja);
					try {
						if(!Q.containsKey(s)){
							System.out.println("WARNING: missing key for " + s);
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
			
			V.put(s, 0.); //TODO solve s from Q(s)
			visited.add(s);
			
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
		EnumerableSGDomain domain = new AggregateDifferencesDomain();
		
		BackwardInduction bi = new BackwardInduction("test", domain, tf);
		
		State initialState = ((AggregateDifferencesDomain) domain).getInitialState();
		
		System.out.println("Solving...");
		bi.solve((MicroRTSState) initialState);
		System.out.println("Solved.");
		
	}
}
