package tests.rl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ai.metabot.learning.model.GameStage;
import ai.metabot.learning.model.MicroRTSState;
import burlap.behavior.stochasticgames.madynamicprogramming.backupOperators.MinMaxQ;
import burlap.debugtools.DPrint;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.world.World;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import rl.AbstractionModels;
import rl.adapters.domain.EnumerableSGDomain;
import rl.adapters.learners.PersistentMultiAgentQLearning;

public class PersistentMultiAgentQLearningTest {

	PersistentMultiAgentQLearning player;
	PersistentMultiAgentQLearning rival;
	
	World microRTSStages;
	
	@Before
	public void setUp() throws Exception {
		
		microRTSStages = AbstractionModels.stages();
		
		//agent to be tested
		player = new PersistentMultiAgentQLearning(
			microRTSStages.getDomain(), .9, .1, new SimpleHashableStateFactory(),
			1, new MinMaxQ(), true, "PLAYER", 
			new SGAgentType("MiniMaxQ", microRTSStages.getDomain().getActionTypes())
		);
		
		//another agent
		rival = new PersistentMultiAgentQLearning(
			microRTSStages.getDomain(), .9, .1, new SimpleHashableStateFactory(),
			1, new MinMaxQ(), true, "RIVAL", 
			new SGAgentType("MiniMaxQ", microRTSStages.getDomain().getActionTypes())
		);
		
		//must 'prepare' a match so that agents initialize their structures
		microRTSStages.join(player);
		microRTSStages.join(rival);
		
		DPrint.toggleCode(microRTSStages.getDebugId(), false);
		//for (int i = 0; i < 20; i++)
			microRTSStages.runGame(0);	//run a game with zero stages, so that functions are not updated (?)
		
		
	}

	@Test
	public void testLoadKnowledge() {
		player.loadKnowledge("src/tests/rl/maql-player-example.xml");
		
		/*
		 * most states and joint actions have value 1.0 in the file
		 * but here we test the ones whose value is different
		 */
		
		// retrieves a map of names to states so that we're able to query them
		EnumerableSGDomain domain = (EnumerableSGDomain) microRTSStages.getDomain();
		Map<String, State> nameToState = domain.namesToStates();
		
		// declares the state and the joint action which are used throughout this test
		MicroRTSState state = new MicroRTSState();
		JointAction ja;
		
		
		// testing state 'OPENING' and joint action 'LightRush;LightRush'
		state.setStage(GameStage.OPENING);
		ja = constructJointAction("LightRush", "LightRush");
		assertEquals(.7, player.getMyQSource().getQValueFor(state, ja).q, 0.00001);
		
		
		// testing state 'EARLY' and joint action 'RangedRush;Expand'
		state.setStage(GameStage.EARLY);
		ja = constructJointAction("RangedRush", "Expand");
		assertEquals(-.33, player.getMyQSource().getQValueFor(state, ja).q, 0.00001);
		
		
		// testing state 'MID' and joint action 'WorkerRush;BuildBarracks' value='0.89'
		state.setStage(GameStage.MID);
		ja = constructJointAction("WorkerRush", "BuildBarracks");
		assertEquals(.89, player.getMyQSource().getQValueFor(state, ja).q, 0.00001);
		
		// testing state 'LATE' and joint action 'WorkerRush;RangedRush' value='-1'
		state.setStage(GameStage.LATE);
		ja = constructJointAction("WorkerRush", "RangedRush");
		assertEquals(-1, player.getMyQSource().getQValueFor(state, ja).q, 0.00001);
		
		// testing state 'END' and joint action 'WorkerRush;WorkerRush' value='0' 
		state.setStage(GameStage.END);
		ja = constructJointAction("WorkerRush", "WorkerRush");
		assertEquals(0, player.getMyQSource().getQValueFor(state, ja).q, 0.00001);
		
		
		
	}
	
	@Test
	public void testSaveKnowledge() {
		player.saveKnowledge("/tmp/maql.xml");
		rival.saveKnowledge("/tmp/rival.xml");
		fail("not tested yet");
	}

	private JointAction constructJointAction(String component1, String component2) {

		List<Action> components = new ArrayList<>();
		components.add(
			microRTSStages.getDomain().getActionType(component1).associatedAction(null)
		);
		components.add(
			microRTSStages.getDomain().getActionType(component2).associatedAction(null)
		);
		
		return new JointAction(components);
	}

}
