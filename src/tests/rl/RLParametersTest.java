package tests.rl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.metagame.DummyPolicy;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.valuefunction.QFunction;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.world.World;
import rl.RLParamNames;
import rl.RLParameters;
import rl.adapters.gamenatives.PortfolioAIAdapter;
import rl.adapters.learners.SGQLearningAdapter;
import rl.models.aggregatediff.AggregateDifferencesDomain;
import rl.models.common.MicroRTSRewardFactory;
import tests.rl.adapters.learners.SGQLearningAdapterTest;

public class RLParametersTest {
	
	@Before
	public void setUp(){
		// reset is required, because the instance persists between tests
		RLParameters.getInstance().reset();
	}

	@Test
	/**
	 * Tests whether configurations in example.xml is properly loaded
	 */
	public void testExampleXML() throws SAXException, IOException, ParserConfigurationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		RLParameters rlParams = RLParameters.getInstance();
		
		Map<String, Object> parameters =  rlParams.loadFromFile("src/tests/rl/example.xml"); //may throw exceptions
		
		// tests parameter values
		assertEquals(100, (int)parameters.get(RLParamNames.EPISODES));
		assertEquals(MicroRTSRewardFactory.SIMPLE_WEIGHTED, parameters.get(RLParamNames.REWARD_FUNCTION));
		assertTrue((boolean) parameters.get(RLParamNames.QUIET_LEARNING));
		
		World w = (World) rlParams.getParameter(RLParamNames.ABSTRACTION_MODEL);
		assertTrue(w.getDomain() instanceof AggregateDifferencesDomain);
		
		@SuppressWarnings("unchecked")
		List<SGAgent> players = (List<SGAgent>) rlParams.getParameter(RLParamNames.PLAYERS);
		assertEquals(2, players.size());
		
		for(SGAgent player : players){
			
			//casts the player and tests its attributes 
			SGQLearningAdapter sgql = (SGQLearningAdapter) player;
			if (sgql.agentName().equals("learner")){ 
				
				QLearning ql = (QLearning) sgql.getSingleAgentLearner();
				//tests whether attributes were correctly loaded
				//code to test learning rate:
				Field lrField = revealField(ql, "learningRate");
				LearningRate lr = (LearningRate) lrField.get(ql);
				assertEquals(0.1, lr.peekAtLearningRate(null, null), 0.0000001);
				
				//code to test: initialQ
				Field initialQField = revealField(ql, "qInitFunction");
				QFunction initialQ = (QFunction) initialQField.get(ql);
				assertEquals(1, initialQ.value(null), 0.0000001);
				
				//code to test discount 
				Field discountField = revealField(MDPSolver.class, "gamma");
				double discount = (double) discountField.get(ql);
				assertEquals(0.9, discount, 0.0000001);
				
				// tests player knowledge
				SGQLearningAdapterTest test = new SGQLearningAdapterTest();
				test.assertLoadedKnowledgeInAggrDiffModel(sgql);
				
			}
			else if (player.agentName().equals("dummy")){ 
				//casts the player and tests its attributes 

				QLearning ql = (QLearning) sgql.getSingleAgentLearner();
				//tests whether we have a dummy policy
				Field policyField = revealField(ql, "learningPolicy");
				Policy thePolicy = (Policy) policyField.get(ql);
				assertTrue(thePolicy instanceof DummyPolicy);
				
			}
			else {
				fail(
					"Player name is neither learner or dummy. It is: " + 
					player.agentName()
				);
			}
		}
	}
	
	@Test
	/**
	 * Tests loading example_portfolioAI.xml
	 */
	public void testExamplePortfolioAI() throws Exception {
		RLParameters rlParams = RLParameters.getInstance();
		
		Map<String, Object> parameters = rlParams.loadFromFile("src/tests/rl/example_portfolioAI.xml"); //may throw exceptions
		
		//tests parameter values
		assertEquals(200, (int) rlParams.getParameter(RLParamNames.EPISODES));
		assertEquals(2, (int) rlParams.getParameter(RLParamNames.DEBUG_LEVEL));
		
		World w = (World) rlParams.getParameter(RLParamNames.ABSTRACTION_MODEL);
		assertTrue(w.getDomain() instanceof AggregateDifferencesDomain);
		
		assertEquals(MicroRTSRewardFactory.SIMPLE_WEIGHTED, parameters.get(RLParamNames.REWARD_FUNCTION));
		
		@SuppressWarnings("unchecked")
		List<SGAgent> players = (List<SGAgent>) rlParams.getParameter(RLParamNames.PLAYERS);
		assertEquals(2, players.size());
		
		for(SGAgent player : players){
			
			// tests 'learner' parameters
			if (player.agentName().equals("learner")){ 
				SGQLearningAdapter sgql = (SGQLearningAdapter) player;
				
				QLearning ql = (QLearning) sgql.getSingleAgentLearner();
				//tests whether attributes were correctly loaded
				//code to test learning rate:
				Field lrField = revealField(ql, "learningRate");
				LearningRate lr = (LearningRate) lrField.get(ql);
				assertEquals(0.1, lr.peekAtLearningRate(null, null), 0.0000001);
				
				//code to test: initialQ
				Field initialQField = revealField(ql, "qInitFunction");
				QFunction initialQ = (QFunction) initialQField.get(ql);
				assertEquals(1, initialQ.value(null), 0.0000001);
				
				//code to test discount 
				Field discountField = revealField(MDPSolver.class, "gamma");
				double discount = (double) discountField.get(ql);
				assertEquals(0.9, discount, 0.0000001);
				
			}
			
			// tests searcher parameters
			else if (player.agentName().equals("searcher")){ 
				PortfolioAIAdapter portfAI = (PortfolioAIAdapter) player;
				
				//tests the value of parameters
				assertEquals(50, portfAI.getTimeout());
				assertEquals(150, portfAI.getPlayouts());
				assertEquals(1000, portfAI.getLookahead());
				assertEquals(
					SimpleSqrtEvaluationFunction3.class.getSimpleName(), 
					portfAI.getEvaluationFunctionName()
				);
				
			}
			else {
				fail(
					"Player name is neither learner or dummy. It is: " + 
					player.agentName()
				);
			}
		}
	}
	
	/**
	 * Changes visibility (private -> public) of a specified object field
	 * and returns it  
	 * @param obj the object whose field will be revealed
	 * @param fieldName
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	private Field revealField(Object obj, String fieldName) throws NoSuchFieldException, SecurityException{
		Field theField = obj.getClass().getDeclaredField(fieldName);
		theField.setAccessible(true);
		return theField;
	}
	
	/**
	 * Changes visibility (private -> public) of a specified class field
	 * and returns it  
	 * @param cls
	 * @param fieldName
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	private Field revealField(Class<?> cls, String fieldName) throws NoSuchFieldException, SecurityException{
		Field theField = cls.getDeclaredField(fieldName);
		theField.setAccessible(true);
		return theField;
	}

}
