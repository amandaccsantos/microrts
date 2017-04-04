package tests.rl.models.simplecounting;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import ai.core.AI;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import rl.models.aggregate.AggregateState;
import rl.models.aggregate.AggregateStateJAM;
import rl.models.common.ScriptActionTypes;
import rl.models.stages.GameStage;
import rts.GameState;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

public class AggregateJAMTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSampleChangesStage() throws JDOMException, IOException {
		// defines the state
		UnitTypeTable unitTypeTable = new UnitTypeTable();
		PhysicalGameState physicalGameState = PhysicalGameState.load("maps/basesWorkers24x24.xml", unitTypeTable);
		GameState gs = new GameState(physicalGameState, unitTypeTable);
		AggregateState currentState = new AggregateState(gs);

		
		// retrieves possible actions and defines the joint action
		Map<String, UniversalActionType> actionMapping = ScriptActionTypes.getMapToActionTypes();
		List<Action> theActions = new ArrayList<>();
		theActions.add(actionMapping.get(ScriptActionTypes.WORKER_RUSH).associatedAction(null));
		theActions.add(actionMapping.get(ScriptActionTypes.WORKER_RUSH).associatedAction(null));
		JointAction ja = new JointAction(theActions);
		
		AggregateStateJAM jointActionModel = new AggregateStateJAM(
			ScriptActionTypes.getActionMapping(unitTypeTable)
		);
		
		State newState = jointActionModel.sample(currentState, ja);
		
		assertTrue(newState instanceof AggregateState);
		AggregateState newAggrState = (AggregateState) newState;
		
		assertFalse(newAggrState.equals(currentState));
		
		System.out.println(newAggrState.variableKeys());
		System.out.println(newAggrState);
	}

}
