package tests.rl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import ai.metabot.learning.model.MicroRTSGame;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.valuefunction.QFunction;
import burlap.mdp.stochasticgames.agent.SGAgent;
import rl.RLParamNames;
import rl.RLParameters;
import rl.adapters.PersistentMultiAgentQLearning;
import rl.adapters.SGQLearningAdapter;

public class RLParametersTest {

	@Test
	/**
	 * Tests whether configurations in example.xml is properly loaded
	 */
	public void testExampleXML() throws SAXException, IOException, ParserConfigurationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		RLParameters rlParams = RLParameters.getInstance();
		
		Map<String, Object> parameters =  rlParams.loadFromFile("experiments/example.xml"); //may throw exceptions
		
		//tests parameter values
		assertEquals(100, (int)parameters.get(RLParamNames.EPISODES));
		
		//TODO test the parameters.get(RLParamNames.ABSTRACTION_MODEL) 
		
		List<SGAgent> players = (List<SGAgent>) parameters.get(RLParamNames.PLAYERS);
		assertEquals(2, players.size());
		
		for(SGAgent player : players){
			
			
			if (player instanceof PersistentMultiAgentQLearning){
				//casts the player and tests its attributes 
				PersistentMultiAgentQLearning pmaq = (PersistentMultiAgentQLearning) player;
				
				//tests whether attributes were correctly loaded
				//first is learning rate
				Field lrField = revealField(pmaq, "learningRate");
				LearningRate lr = (LearningRate) lrField.get(pmaq);
				assertEquals(0.1, lr.peekAtLearningRate(null, null), 0.00000001);
				
				//second is discount
				Field discountField = revealField(pmaq, "discount");
				double discount = (double) discountField.get(pmaq);
				assertEquals(0.9, discount, 0.00000001);
				
				//last is initial-q
				Field initialQField = revealField(pmaq, "qInit");
				QFunction initialQ = (QFunction) initialQField.get(pmaq);
				assertEquals(1, initialQ.value(null), 0.00000001);
				
			}
			else if (player instanceof SGQLearningAdapter){
				//casts the player and tests its attributes 
				SGQLearningAdapter sgql = (SGQLearningAdapter) player;
				
			}
			else {
				fail(
					"Player does not belong to neither expected class. It is a: " + 
					player.getClass().getName()
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

}
