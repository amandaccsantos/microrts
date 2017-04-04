package tests.rl.adapters.learners;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.action.Action;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.world.World;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import rl.WorldFactory;
import rl.adapters.learners.SGQLearningAdapter;
import rl.models.stages.GameStages;
import rl.models.stages.GameStage;

public class SGQLearningAdapterTest {
	
	SGQLearningAdapter sgql;
	World world;
	final static String EXAMPLE_QTABLE_FILE = "src/tests/rl/qtable-example.yaml";
	
	@Before
	public void setUp(){
		world = WorldFactory.stages();
		
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
	public void testLoadKnowledgeWithStagesModel(){
		sgql.loadKnowledge(EXAMPLE_QTABLE_FILE);
		
		GameStage state = new GameStage();
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
		state.setStage(GameStages.OPENING);
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
		state.setStage(GameStages.EARLY);
		assertEquals(0.5, qLearner.qValue(state, lightRush), 0.00001);
		assertEquals(0.0, qLearner.qValue(state, buildBarracks), 0.00001);
		assertEquals(0.6, qLearner.qValue(state, rangedRush), 0.00001);
		assertEquals(0.3, qLearner.qValue(state, expand), 0.00001);
		assertEquals(-0.1, qLearner.qValue(state, workerRush), 0.00001);
		

		/*
		 * Expected values for other states are not specified in file,
		 * thus they should keep the default (1.0) 
		 */
		
		for(GameStage s : GameStage.allStates()){
			//skips OPENING and EARLY (previously tested)
			if (s.getStage().equals(GameStages.OPENING) || s.getStage().equals(GameStages.EARLY)){
				continue;
			}
			
			for (QValue q : qLearner.qValues(s)) {
				assertEquals(
					String.format("state %s / action %s", s, q.a), 
					1.0, q.q, 0.00001
				);
			}
		}
	}
	
	@Test
	/**
	 * This test assumes {@link testLoadKnowledge} is working properly
	 * @throws FileNotFoundException
	 */
	public void testSaveKnowledgeWithStagesModel() throws FileNotFoundException {
		//if loadKnowledge is working properly, saveKnowledge should yield the same file
		
		/* 
		 * TODO test whether knowledge loaded from the saved file is equivalent to the 
		 * one before saving instead of exact file content
		 */
		
		String tmpQTableFile = "/tmp/test-save-knowledge-sgql.yaml";
		sgql.loadKnowledge(EXAMPLE_QTABLE_FILE);
		sgql.saveKnowledge(tmpQTableFile);
		
		String expected = new Scanner(new File(EXAMPLE_QTABLE_FILE)).useDelimiter("\\Z").next();
		String actual = new Scanner(new File(tmpQTableFile)).useDelimiter("\\Z").next();
		
		assertEquals(expected, actual);
	}

}
