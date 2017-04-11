package ai.rl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.core.AI;
import ai.core.ParameterSpecification;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.mdp.core.action.Action;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.world.World;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import rl.WorldFactory;
import rl.adapters.learners.PersistentLearner;
import rl.adapters.learners.SGQLearningAdapter;
import rl.models.common.MicroRTSState;
import rl.models.common.ScriptActionTypes;
import rl.models.common.SimpleWeightedFeatures;
import rts.GameState;
import rts.PlayerAction;

public class MetaBot extends AI {
	
	PersistentLearner learner;
	String pathToKnowledge;
	String worldModelName;
	Map<String, AI> portfolio;
	
	public MetaBot(){
		//domain <- world <- reward function
		// agent name
		// agent type
		worldModelName = WorldFactory.AGGREGATE_DIFF;
		JointRewardFunction rwdFunc = new SimpleWeightedFeatures();
		World w = WorldFactory.fromString(worldModelName, rwdFunc);
		
		QLearning ql = new QLearning(null, 0.9, new SimpleHashableStateFactory(false), 1000, 0);
		SGQLearningAdapter agent = new SGQLearningAdapter(
			w.getDomain(), ql, "QLearning", 
			new SGAgentType("QLearning", w.getDomain().getActionTypes())
		);
		
		initialize(agent, null, worldModelName);
	}
	
	public MetaBot(PersistentLearner learner, String pathToKnowledge, String worldModelName){
		this.initialize(learner, pathToKnowledge, worldModelName);
	}
	
	private void initialize(PersistentLearner learner, String pathToKnowledge, String worldModelName){
		this.learner = learner;
		this.pathToKnowledge = pathToKnowledge;
		this.worldModelName = worldModelName;
		this.reset();
	}

	@Override
	/**
	 * Re-loads knowledge from file
	 */
	public void reset() {
		if (pathToKnowledge != null){
			try {
				this.learner.loadKnowledge(pathToKnowledge);
			}
			catch(Exception e){
				System.err.println("Unable to load knowledge! Will proceed with 'dumb' agent");
				e.printStackTrace();
			}
			
		}
	}

	@Override
	public PlayerAction getAction(int player, GameState gs) throws Exception {
		// initializes the portfolio if needed
		if(portfolio == null){
			portfolio = ScriptActionTypes.getActionMapping(gs.getUnitTypeTable());
		}
		
		// retrieves the abstract state representation and retrieves an action
		MicroRTSState s = MicroRTSStateFactory.fromString(worldModelName, gs);
		Action a = learner.action(s);
		
		// returns the underlying game action dictated by the chosen script
		AI currentScript = portfolio.get(a.actionName());
		
		return currentScript.getAction(player, gs);
	}

	@Override
	public AI clone() {
		return new MetaBot(learner, pathToKnowledge, worldModelName);
	}

	@Override
	public List<ParameterSpecification> getParameters() {
		List<ParameterSpecification> parameters = new ArrayList<>();
		
		parameters.add(new ParameterSpecification("pathToKnowledge", String.class, pathToKnowledge));
		parameters.add(new ParameterSpecification("worldModelName", String.class, worldModelName));
		
		return parameters;
	}

}
