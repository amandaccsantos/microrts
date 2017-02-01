package ai.metabot.learning.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jdom.JDOMException;

import ai.abstraction.BuildBarracks;
import ai.abstraction.Expand;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.core.AI;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.SGDomain;
import rts.GameState;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

public class MicroRTSGame implements DomainGenerator {
	//action names
	public static final String WORKER_RUSH = WorkerRush.class.getSimpleName();
	public static final String LIGHT_RUSH = LightRush.class.getSimpleName();
	public static final String RANGED_RUSH = RangedRush.class.getSimpleName();
	public static final String EXPAND = Expand.class.getSimpleName();
	public static final String BUILD_BARRACKS = BuildBarracks.class.getSimpleName();
	
	
	public static final int MAXCYCLES = 3000;
	public static final int PERIOD = 20;
	
	boolean gameover;// = false;
	UnitTypeTable unitTypeTable;
	PhysicalGameState physicalGameState;
	GameState gs;

	
	public MicroRTSGame() throws JDOMException, IOException{
		
		unitTypeTable = new UnitTypeTable();
		physicalGameState = PhysicalGameState.load("maps/basesWorkers24x24.xml", unitTypeTable);
		// PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

		gs = new GameState(physicalGameState, unitTypeTable);
		
		gameover = false;
		
	}
	
	/**
	 * Returns the initial state for the game
	 * @return
	 */
	public State getInitialState(){
		return new MicroRTSState(gs);
	}
	
	@Override
	public Domain generateDomain() {
		SGDomain domain = new SGDomain();
		
		Map<String, AI> actions = new HashMap<>();	//actions correspond to selection of a behavior
		actions.put(WorkerRush.class.getSimpleName(), new WorkerRush(unitTypeTable));
		actions.put(LightRush.class.getSimpleName(), new LightRush(unitTypeTable));
		actions.put(RangedRush.class.getSimpleName(), new RangedRush(unitTypeTable));
		actions.put(Expand.class.getSimpleName(), new Expand(unitTypeTable));
		actions.put(BuildBarracks.class.getSimpleName(), new BuildBarracks(unitTypeTable));

		domain.addActionType(new UniversalActionType(WORKER_RUSH))
			.addActionType(new UniversalActionType(LIGHT_RUSH))
			.addActionType(new UniversalActionType(RANGED_RUSH))
			.addActionType(new UniversalActionType(EXPAND))
			.addActionType(new UniversalActionType(BUILD_BARRACKS));


		domain.setJointActionModel(new MicroRTSJointActionModel(actions));
		
		return domain;
	}

}
