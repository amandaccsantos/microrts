package rl.models;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.JDOMException;

import ai.abstraction.BuildBarracks;
import ai.abstraction.Expand;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.core.AI;
import ai.metabot.learning.model.MicroRTSJointActionModel;
import ai.metabot.learning.model.MicroRTSState;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import rl.adapters.domain.EnumerableSGDomain;
import rts.GameState;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

public class GameStagesDomain extends EnumerableSGDomain {

	//action names
	public static final String WORKER_RUSH = WorkerRush.class.getSimpleName();
	public static final String LIGHT_RUSH = LightRush.class.getSimpleName();
	public static final String RANGED_RUSH = RangedRush.class.getSimpleName();
	public static final String EXPAND = Expand.class.getSimpleName();
	public static final String BUILD_BARRACKS = BuildBarracks.class.getSimpleName();
	
	//some game parameters
	public static final int MAXCYCLES = 3000;
	public static final int PERIOD = 20;
	
	UnitTypeTable unitTypeTable;
	PhysicalGameState physicalGameState;
	GameState gs;
	
	/**
	 * Creates a default domain loading map maps/basesWorkers24x24.xml of microRTS
	 * @throws JDOMException
	 * @throws IOException
	 */
	public GameStagesDomain() throws JDOMException, IOException {
		this("maps/basesWorkers24x24.xml");
		
	}
	
	public GameStagesDomain(String pathToMap) throws JDOMException, IOException{
		unitTypeTable = new UnitTypeTable();
		physicalGameState = PhysicalGameState.load(pathToMap, unitTypeTable);

		gs = new GameState(physicalGameState, unitTypeTable);
		
		this.addActionType(new UniversalActionType(WORKER_RUSH))
			.addActionType(new UniversalActionType(LIGHT_RUSH))
			.addActionType(new UniversalActionType(RANGED_RUSH))
			.addActionType(new UniversalActionType(EXPAND))
			.addActionType(new UniversalActionType(BUILD_BARRACKS));
	
		//creates a map string -> AI for the joint action model
		Map<String, AI> actions = new HashMap<>();	//actions correspond to selection of a behavior
		actions.put(WorkerRush.class.getSimpleName(), new WorkerRush(unitTypeTable));
		actions.put(LightRush.class.getSimpleName(), new LightRush(unitTypeTable));
		actions.put(RangedRush.class.getSimpleName(), new RangedRush(unitTypeTable));
		actions.put(Expand.class.getSimpleName(), new Expand(unitTypeTable));
		actions.put(BuildBarracks.class.getSimpleName(), new BuildBarracks(unitTypeTable));
	
		//sets the joint action model containing the valid actions
		setJointActionModel(new MicroRTSJointActionModel(actions));
	}
	
	/**
	 * Returns the initial state for the game
	 * @return
	 */
	public State getInitialState(){
		return new MicroRTSState(gs);
	}

	@Override
	public List<? extends State> enumerate() {
		return MicroRTSState.allStates();
	}

}
