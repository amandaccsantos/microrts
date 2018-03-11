package rl.models.singleagentstages;

import java.io.IOException;
import java.util.List;

import org.jdom.JDOMException;

import ailoader.AILoader;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import rl.RLParameters;
import rl.adapters.domain.EnumerableSGDomain;
import rl.models.common.ScriptActionTypes;
import rl.models.stages.GameStage;
import rl.models.stages.GameStagesDomain;
import rts.GameState;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

/**
 * The embedded-adversary domain (single agent), but with
 * state abstraction based on the game stage
 * @author anderson
 *
 */
public class SingleAgentStagesDomain extends EnumerableSGDomain {
	
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
	public SingleAgentStagesDomain() throws JDOMException, IOException {
		this("maps/basesWorkers24x24.xml");

	}

	public SingleAgentStagesDomain(String pathToMap) throws JDOMException, IOException {
		unitTypeTable = new UnitTypeTable();
		physicalGameState = PhysicalGameState.load(pathToMap, unitTypeTable);

		gs = new GameState(physicalGameState, unitTypeTable);

		for (UniversalActionType actionType : ScriptActionTypes.getLearnerActionTypes()) {
			this.addActionType(actionType);
		}
		
		String opponent = RLParameters.getInstance().getOpponentName();
		//String opponent = "WorkerRush"; //TODO test
		
		// sets the joint action model containing the valid actions
		setJointActionModel(new SingleAgentStagesJAM(
			ScriptActionTypes.getLearnerActionMapping(unitTypeTable), 
			AILoader.loadAI(opponent, unitTypeTable))
		);
	}

	/**
	 * Returns the initial state for the game
	 * @return
	 */
	public State getInitialState() {
		return new GameStage(gs);
	}

	@Override
	public List<? extends State> enumerate() {
		GameStagesDomain underlyingDomain = null;
		try {
			underlyingDomain = new GameStagesDomain();
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
			return null;
		}
		return underlyingDomain.enumerate();
	}

}
