package tests.rl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ai.metabot.learning.model.GameStage;
import ai.metabot.learning.model.MetaBotAction;
import ai.metabot.learning.model.MicroRTSState;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.action.Action;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.world.World;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import rl.AbstractionModels;
import rl.RLParamNames;
import rl.adapters.SGQLearningAdapter;
import rts.GameState;

public class SGQLearningAdapterTest {
	
	SGQLearningAdapter sgql;
	World world;
	
	@Before
	public void setUp(){
		world = AbstractionModels.stages();
		
		QLearning ql = new QLearning(
			null, 0.9, new SimpleHashableStateFactory(false), 
			1.0, 0.1
		);
		
		sgql = new SGQLearningAdapter(
			world.getDomain(), ql, "QLearning", 
			new SGAgentType("Dummy", world.getDomain().getActionTypes())
		);
		
	}

	@Test
	public void testSaveKnowledge() {
		
		
		//TODO: saveKnowledge prior to any training yields an empty file.
		
		fail("Not yet implemented");
	}
	
	@Test
	public void testLoadKnowledge(){
		sgql.loadKnowledge("src/tests/rl/qtable-example.yaml");
		
		MicroRTSState state = new MicroRTSState();
		QLearning qLearner = (QLearning) sgql.getSingleAgentLearner();
		
		//retrieves objects regarding game actions
		Action lightRush = world.getDomain().getActionType("LightRush").associatedAction(null);
		Action buildBarracks = world.getDomain().getActionType("BuildBarracks").associatedAction(null);
		Action rangedRush = world.getDomain().getActionType("RangedRush").associatedAction(null);
		Action expand = world.getDomain().getActionType("Expand").associatedAction(null);
		Action workerRush = world.getDomain().getActionType("WorkerRush").associatedAction(null);
		
		/*
		 * Expected values of actions for stage 'OPENING' are:
		 * LightRush: 0.0
		 * BuildBarracks: 0.5
		 * RangedRush: 0.0
		 * Expand: -0.5
		 * WorkerRush: 0.75
		 */
		state.setStage(GameStage.OPENING);
		assertEquals(0.0, qLearner.qValue(state, lightRush), 0.00001);
		assertEquals(0.5, qLearner.qValue(state, buildBarracks), 0.00001);
		assertEquals(0.0, qLearner.qValue(state, rangedRush), 0.00001);
		assertEquals(-0.5, qLearner.qValue(state, expand), 0.00001);
		assertEquals(0.75, qLearner.qValue(state, workerRush), 0.00001);
		
		
		/*
		 * Expected values of actions for stage 'EARLY' are:
		 * LightRush: 0.5
		 * BuildBarracks: 0.0
		 * RangedRush: 0.6
		 * Expand: 0.3
		 * WorkerRush: -0.1
		 */
		state.setStage(GameStage.EARLY);
		assertEquals(0.5, qLearner.qValue(state, lightRush), 0.00001);
		assertEquals(0.0, qLearner.qValue(state, buildBarracks), 0.00001);
		assertEquals(0.6, qLearner.qValue(state, rangedRush), 0.00001);
		assertEquals(0.3, qLearner.qValue(state, expand), 0.00001);
		assertEquals(-0.1, qLearner.qValue(state, workerRush), 0.00001);
		

		/*
		 * Expected values for other states are not specified in file,
		 * thus they should keep the default (1.0) 
		 */
		
		for(MicroRTSState s : MicroRTSState.allStates()){
			//skips OPENING and EARLY (previously tested)
			if (s.getStage().equals(GameStage.OPENING) || s.getStage().equals(GameStage.EARLY)){
				continue;
			}
			
			for (QValue q : qLearner.qValues(s)) {
				assertEquals(1.0, q.q, 0.00001);
			}
		}
	}

}
