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
	PersistentMultiAgentQLearning rival;
	
	World microRTSStages;
	
	@Before
	public void setUp() throws Exception {
		
		microRTSStages = AbstractionModels.stages();
		
		//agent to be tested
		maql = new PersistentMultiAgentQLearning(
			microRTSStages.getDomain(), .9, .1, new SimpleHashableStateFactory(),
			1, new MinMaxQ(), true, "MAQL", 
			new SGAgentType("MiniMaxQ", microRTSStages.getDomain().getActionTypes())
		);
		
		//another agent
		rival = new PersistentMultiAgentQLearning(
			microRTSStages.getDomain(), .9, .1, new SimpleHashableStateFactory(),
			1, new MinMaxQ(), true, "RIVAL", 
			new SGAgentType("MiniMaxQ", microRTSStages.getDomain().getActionTypes())
		);
		
		//must 'prepare' a match so that agents initialize their structures
		microRTSStages.join(maql);
		microRTSStages.join(rival);
		
		for (int i = 0; i < 20; i++)
			microRTSStages.runGame();	//run a game with zero stages, so that functions are not updated (?)
		
		
	}

	@Test
	public void testLoadKnowledge() {
		//maql.loadKnowledge("/tmp/saved.sav");
	}
	
	@Test
	public void testSaveKnowledge() {
		maql.saveKnowledge("/tmp/maql.xml");
		rival.saveKnowledge("/tmp/rival.xml");
		fail("not tested yet");
	}

	@Test
	public void testAction() {
		fail("Not yet implemented"); // TODO
	}

}
