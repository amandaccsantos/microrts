package tests.rl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import burlap.behavior.stochasticgames.madynamicprogramming.backupOperators.MinMaxQ;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.world.World;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import rl.AbstractionModels;
import rl.adapters.learners.PersistentMultiAgentQLearning;

public class PersistentMultiAgentQLearningTest {

	PersistentMultiAgentQLearning maql;
	World microRTSStages;
	
	@Before
	public void setUp() throws Exception {
		
		microRTSStages = AbstractionModels.stages();
		
		maql = new PersistentMultiAgentQLearning(
			microRTSStages.getDomain(), .9, .1, new SimpleHashableStateFactory(),
			1, new MinMaxQ(), true, "MAQL", 
			new SGAgentType("MiniMaxQ", microRTSStages.getDomain().getActionTypes())
		);
	}

	@Test
	public void testLoadKnowledge() {
		fail("Not yet implemented"); // TODO
	}
	
	@Test
	public void testSaveKnowledge() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testAction() {
		fail("Not yet implemented"); // TODO
	}

}
