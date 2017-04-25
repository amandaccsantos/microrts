package tests.rl.models.stages;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import rl.models.common.ScriptActionTypes;
import rl.models.stages.GameStage;
import rl.models.stages.GameStages;
import rl.models.stages.GameStagesDomain;
import rl.models.stages.StagesJointActionModel;

public class StagesJAMTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSample() throws JDOMException, IOException {
		GameStagesDomain domain = new GameStagesDomain(); 
		StagesJointActionModel jointActionModel = (StagesJointActionModel) domain.getJointActionModel();
		
		// gets the initial state
		GameStage initial = (GameStage) domain.getInitialState();
		
		// retrieves the available actions
		List<UniversalActionType> portfolio = ScriptActionTypes.getActionTypes(); 
		
		// tests for all action combinations
		for (UniversalActionType uat1 : portfolio){
			for(UniversalActionType uat2 : portfolio){
				JointAction ja = new JointAction();
				ja.addAction(uat1.associatedAction(null));
				ja.addAction(uat2.associatedAction(null));
				
				State newState = jointActionModel.sample(initial, ja);
				
				// regardless of which pair of actions is issued, I should get a different state than initial
				assertFalse(newState.equals(initial));
				
				//System.out.println("Stage is " + newState.get("stage") + ", action: " + ja);
				// moreover, next state should be 'EARLY' or 'FINISHED'
				assertTrue(
					"Stage is " + newState.get("stage") + ", action: " + ja,
					newState.get("stage") == GameStages.EARLY || 
					newState.get("stage") == GameStages.FINISHED 
				);
			}
		}
	}

}
