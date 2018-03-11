package tests.rl.models.singleagentstages;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import ailoader.AILoader;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import rl.RLParamNames;
import rl.RLParameters;
import rl.models.common.MicroRTSTerminalFunction;
import rl.models.common.ScriptActionTypes;
import rl.models.singleagentstages.SingleAgentStagesJAM;
import rl.models.stages.GameStage;
import rl.models.stages.GameStages;
import rts.GameState;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

public class SingleAgentStagesJAMTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSampleChangesStage() throws JDOMException, IOException {
		// defines the state
		UnitTypeTable unitTypeTable = new UnitTypeTable();
		PhysicalGameState physicalGameState = PhysicalGameState.load(
			"src/tests/rl/models/aggregatediff/basesWorkers24x24.xml", 
			unitTypeTable
		);
		GameState gs = new GameState(physicalGameState, unitTypeTable);
		GameStage currentState = new GameStage(gs);

		
		// retrieves possible actions and defines the joint action
		Map<String, UniversalActionType> actionMapping = ScriptActionTypes.getMapToLearnerActionTypes();
		List<Action> theActions = new ArrayList<>();
		theActions.add(actionMapping.get(ScriptActionTypes.WORKER_RUSH).associatedAction(null));
		theActions.add(actionMapping.get(ScriptActionTypes.WORKER_RUSH).associatedAction(null));
		JointAction ja = new JointAction(theActions);
		
		SingleAgentStagesJAM jointActionModel = new SingleAgentStagesJAM(
			ScriptActionTypes.getLearnerActionMapping(unitTypeTable), 
			AILoader.loadAI(ScriptActionTypes.PORTFOLIO_GREEDY_SEARCH, unitTypeTable)
		);
		
		State newState = jointActionModel.sample(currentState, ja);
		
		assertTrue(newState instanceof GameStage);
		GameStage newAbstractState = (GameStage) newState;
		
		assertFalse(newAbstractState.equals(currentState));
		
	}

	//@Test //commented out because game is over before timeout
	public void testSampleUntilTimeout() throws JDOMException, IOException{
		// defines the state
		UnitTypeTable unitTypeTable = new UnitTypeTable();
		PhysicalGameState physicalGameState = PhysicalGameState.load(
			"src/tests/rl/models/aggregatediff/basesWorkers24x24.xml", 
			unitTypeTable
		);
		GameState gs = new GameState(physicalGameState, unitTypeTable);
		GameStage currentState = new GameStage(gs);

		
		// retrieves possible actions and defines the joint action
		Map<String, UniversalActionType> actionMapping = ScriptActionTypes.getMapToLearnerActionTypes();
		List<Action> theActions = new ArrayList<>();
		
		//will pair worker rush vs worker rush, who run until timeout
		theActions.add(actionMapping.get(ScriptActionTypes.WORKER_RUSH).associatedAction(null));
		theActions.add(actionMapping.get(ScriptActionTypes.WORKER_RUSH).associatedAction(null));
		JointAction ja = new JointAction(theActions);
		
		SingleAgentStagesJAM jointActionModel = new SingleAgentStagesJAM(
			ScriptActionTypes.getLearnerActionMapping(unitTypeTable), 
			AILoader.loadAI(ScriptActionTypes.PUPPET_SEARCH, unitTypeTable)
		);
		
		// samples a new state until timeout
		State newState;
		while(true){
			newState = jointActionModel.sample(currentState, ja);
			
			assertTrue(newState instanceof GameStage);
			GameStage newAbstractState = (GameStage) newState;
			
			// new state should be different from previous
			assertFalse(newAbstractState.equals(currentState));
			
			GameState underlyingState = newAbstractState.getUnderlyingState();

			// not game over yet
			assertFalse("Found gameover in "+ underlyingState, underlyingState.gameover());
			
			// tests whether timeout was reached
			if(underlyingState.getTime() >= 
					(int) RLParameters.getInstance().getParameter(RLParamNames.GAME_DURATION)){
				break;
			}
			currentState = newAbstractState;
		}
		
		TerminalFunction tf = new MicroRTSTerminalFunction();
		assertTrue(((GameStage) newState).getStage() == GameStages.FINISHED);
		assertTrue(tf.isTerminal(newState));
	}
}
