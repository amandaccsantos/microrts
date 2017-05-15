package tests.rl.models.singlestate;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import rl.models.common.ScriptActionTypes;
import rl.models.singlestate.SingleState;
import rl.models.singlestate.SingleStateJAM;
import rl.models.stages.GameStages;
import rts.GameState;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

public class SingleStateJAMTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws JDOMException, IOException {
		// defines the state (both underlying and abstract)
		UnitTypeTable unitTypeTable = new UnitTypeTable();
		PhysicalGameState physicalGameState = PhysicalGameState.load(
			"src/tests/rl/models/singlestate/basesWorkers24x24.xml", 
			unitTypeTable
		);
		GameState gs = new GameState(physicalGameState, unitTypeTable);
		SingleState currentState = new SingleState(gs);
		
		// instantiates the JAM
		SingleStateJAM jointActionModel = new SingleStateJAM(
			ScriptActionTypes.getActionMapping(unitTypeTable)
		);
		
		// retrieves possible actions and defines the joint action
		Map<String, UniversalActionType> actionMapping = ScriptActionTypes.getMapToActionTypes();
		
		//regardless of the joint actions, the next state should be FINISHED
		for (UniversalActionType type1 : actionMapping.values()){
			for (UniversalActionType type2 : actionMapping.values()){
				// creates the joint action with current types
				List<Action> theActions = new ArrayList<>();
				theActions.add(type1.associatedAction(null));
				theActions.add(type2.associatedAction(null));
				JointAction ja = new JointAction(theActions);
				
				State newState = jointActionModel.sample(currentState, ja);
				
				assertTrue(newState instanceof SingleState);
				SingleState newSingleState = (SingleState) newState;
				assertTrue(newSingleState.getStage() == GameStages.FINISHED);
			}
		}
	}
}
