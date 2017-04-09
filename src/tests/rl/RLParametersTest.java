package tests.rl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.metabot.DummyPolicy;
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
import rl.models.aggregate.AggregateStateDomain;
import rl.models.aggregatediff.AggregateDiffState;
import rl.models.aggregatediff.AggregateDifferencesDomain;
import rl.models.common.MicroRTSRewardFactory;

public class RLParametersTest {

	@Test
	/**
	 * Tests whether configurations in example.xml is properly loaded
	 */
	public void testExampleXML() throws SAXException, IOException, ParserConfigurationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		RLParameters rlParams = RLParameters.getInstance();
		
		Map<String, Object> parameters =  rlParams.loadFromFile("src/tests/rl/example.xml"); //may throw exceptions
		
		//tests parameter values
		assertEquals(100, (int)parameters.get(RLParamNames.EPISODES));
		
		World w = (World)parameters.get(RLParamNames.ABSTRACTION_MODEL);
		assertTrue(w.getDomain() instanceof AggregateStateDomain);
		
		assertEquals(MicroRTSRewardFactory.SIMPLE_WEIGHTED, parameters.get(RLParamNames.REWARD_FUNCTION));
		
		
		@SuppressWarnings("unchecked")
		List<SGAgent> players = (List<SGAgent>) parameters.get(RLParamNames.PLAYERS);
		assertEquals(2, players.size());
		
		for(SGAgent player : players){
			
			//casts the player and tests its attributes 
			SGQLearningAdapter sgql = (SGQLearningAdapter) player;
			if (sgql.agentName().equals("learner")){ // instanceof PersistentMultiAgentQLearning){
				
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
			else if (player.agentName().equals("dummy")){ // instanceof SGQLearningAdapter){	//both agents in example are SGQLearningAdapter
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
		assertEquals(200, (int)parameters.get(RLParamNames.EPISODES));
		
		World w = (World)parameters.get(RLParamNames.ABSTRACTION_MODEL);
		assertTrue(w.getDomain() instanceof AggregateDifferencesDomain);
		
		assertEquals(MicroRTSRewardFactory.SIMPLE_WEIGHTED, parameters.get(RLParamNames.REWARD_FUNCTION));
		
		// tests 'learner' parameters
		assertEquals(0.9, (float) parameters.get(RLParamNames.DISCOUNT), 0.00001);
		assertEquals(0.1, (float) parameters.get(RLParamNames.LEARNING_RATE), 0.00001);
		assertEquals(1., (float) parameters.get(RLParamNames.INITIAL_Q), 0.00001);
		
		// tests 'searcher' parameters
		assertEquals(50, (int) parameters.get(RLParamNames.TIMEOUT));
		assertEquals(150, (int) parameters.get(RLParamNames.PLAYOUTS));
		assertEquals(1000, (int) parameters.get(RLParamNames.LOOKAHEAD));
		assertEquals(
			SimpleSqrtEvaluationFunction3.class.getSimpleName(), 
			parameters.get(RLParamNames.EVALUATION_FUNCTION)
		);
		
		@SuppressWarnings("unchecked")
		List<SGAgent> players = (List<SGAgent>) parameters.get(RLParamNames.PLAYERS);
		assertEquals(2, players.size());
		
		for(SGAgent player : players){
			
			//casts the player and tests its attributes 
			
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
			else if (player.agentName().equals("searcher")){ // instanceof SGQLearningAdapter){	//both agents in example are SGQLearningAdapter
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
