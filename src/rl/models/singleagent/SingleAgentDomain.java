package rl.models.singleagent;

import java.io.IOException;
import java.util.List;

import org.jdom.JDOMException;

import ai.abstraction.RangedRush;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import rl.adapters.domain.EnumerableSGDomain;
import rl.models.common.ScriptActionTypes;
import rts.GameState;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

public class SingleAgentDomain extends EnumerableSGDomain {
	
	// some game parameters
	public static final int MAXCYCLES = 3000;
	public static final int PERIOD = 20;

	protected UnitTypeTable unitTypeTable;
	protected PhysicalGameState physicalGameState;
	protected GameState gs;

	/**
	 * Creates a default domain loading map maps/basesWorkers24x24.xml of microRTS
	 * @throws JDOMException
	 * @throws IOException
	 */
	public SingleAgentDomain() throws JDOMException, IOException {
		this("maps/basesWorkers24x24.xml");

	}

	public SingleAgentDomain(String pathToMap) throws JDOMException, IOException {
		unitTypeTable = new UnitTypeTable();
		physicalGameState = PhysicalGameState.load(pathToMap, unitTypeTable);

		gs = new GameState(physicalGameState, unitTypeTable);

		for (UniversalActionType actionType : ScriptActionTypes.getActionTypes()) {
			this.addActionType(actionType);
		}

		// sets the joint action model containing the valid actions
		// TODO: find opponent AI 
		// setJointActionModel(new SingleAgentJAM(ScriptActionTypes.getActionMapping(unitTypeTable));
	}

	/**
	 * Returns the initial state for the game
	 * @return
	 */
	public State getInitialState() {
		return new SingleAgent(gs);
	}

	@Override
	public List<? extends State> enumerate() {
		return SingleAgent.allStates();
	}

}
