package tests.rl.models.aggregatediff;

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
import rl.models.aggregatediff.AggregateDiffState;
import rl.models.aggregatediff.AggregateDiffStateJAM;
import rl.models.common.ScriptActionTypes;
import rl.models.stages.GameStage;
import rts.GameState;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

public class AggregateDiffStateJAMTest {

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
		AggregateDiffState currentState = new AggregateDiffState(gs);

		
		// retrieves possible actions and defines the joint action
		Map<String, UniversalActionType> actionMapping = ScriptActionTypes.getMapToActionTypes();
		List<Action> theActions = new ArrayList<>();
		theActions.add(actionMapping.get(ScriptActionTypes.WORKER_RUSH).associatedAction(null));
		theActions.add(actionMapping.get(ScriptActionTypes.WORKER_RUSH).associatedAction(null));
		JointAction ja = new JointAction(theActions);
		
		AggregateDiffStateJAM jointActionModel = new AggregateDiffStateJAM(
			ScriptActionTypes.getActionMapping(unitTypeTable)
		);
		
		State newState = jointActionModel.sample(currentState, ja);
		
		assertTrue(newState instanceof AggregateDiffState);
		AggregateDiffState newAggrState = (AggregateDiffState) newState;
		
		assertFalse(newAggrState.equals(currentState));
		
		//System.out.println(newAggrState.variableKeys());
		//System.out.println(newAggrState);
	}

}
