package rl.adapters.gamenatives;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ai.core.AI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.portfolio.PortfolioAI;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.world.World;
import rl.models.common.MicroRTSState;
import rl.models.common.ScriptActionTypes;
import rts.units.UnitTypeTable;

public class PortfolioAIAdapter implements SGAgent {
	
	String name;
	SGAgentType type;
	PortfolioAI portfolioAI;
	Collection<AI> portfolio;
	
	int agentNum;
	
	public PortfolioAIAdapter(String agentName, SGAgentType agentType){
		this.name = agentName;
		this.type = agentType;
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
		this.agentNum = agentNum;
	}

	@Override
	public Action action(State s) {
		if(!(s instanceof MicroRTSState)){
			throw new RuntimeException("PortfolioAIAdapter works only with MicroRTSStates. I received " + s);
		}
		
		MicroRTSState state = (MicroRTSState) s;
		if(portfolioAI == null){
			initializePortfolioAI(state.getUnderlyingState().getUnitTypeTable());
		}
		
		//TODO resume implementation here
		return null;
	}

	/**
	 * Initializes our portfolioAI object according to a unit type table
	 * @param unitTypeTable
	 */
	protected void initializePortfolioAI(UnitTypeTable unitTypeTable) {
		// retrieves the list of AIs and transforms it in an array
		Map<String, AI> actionMapping = ScriptActionTypes.getActionMapping(unitTypeTable);
		portfolio = actionMapping.values();
		AI[] portfolioArray = portfolio.toArray(new AI[portfolio.size()]);
		
		// creates an array stating that all AIs in the portfolio are not deterministic
		// (we're being conservative by not making assumptions on deterministic-ness of AIs)
		boolean[] deterministic = new boolean[portfolio.size()];
		for(int i = 0; i < deterministic.length; i++){
			deterministic[i] = false;
		}
		
		// finally creates the PortfolioAI object with default timeout, #playouts, lookahead and evaluation functions
		portfolioAI = new PortfolioAI(
			portfolioArray, deterministic, 100, -1, 100, 
			new SimpleSqrtEvaluationFunction3()
		);
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction, double[] jointReward, State sprime,
			boolean isTerminal) {
		// TODO Auto-generated method stub

	}

	@Override
	public void gameTerminated() {
		// TODO Auto-generated method stub

	}

}
