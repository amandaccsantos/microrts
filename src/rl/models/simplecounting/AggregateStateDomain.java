package rl.models.simplecounting;

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
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import rl.adapters.domain.EnumerableSGDomain;
import rl.models.stages.GameStagesDomain;
import rts.GameState;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

public class AggregateStateDomain extends GameStagesDomain {

	
	/**
	 * Creates a default domain loading map maps/basesWorkers24x24.xml of microRTS
	 * @throws JDOMException
	 * @throws IOException
	 */
	public AggregateStateDomain() throws JDOMException, IOException {
		this("maps/basesWorkers24x24.xml");
		
	}
	
	public AggregateStateDomain(String pathToMap) throws JDOMException, IOException{
		super(pathToMap);
	
		//creates a map string -> AI for the joint action model
		//TODO encapsulate this code in a different class to be reused several times
		Map<String, AI> actions = new HashMap<>();	//actions correspond to selection of a behavior
		actions.put(WorkerRush.class.getSimpleName(), new WorkerRush(unitTypeTable));
		actions.put(LightRush.class.getSimpleName(), new LightRush(unitTypeTable));
		actions.put(RangedRush.class.getSimpleName(), new RangedRush(unitTypeTable));
		actions.put(Expand.class.getSimpleName(), new Expand(unitTypeTable));
		actions.put(BuildBarracks.class.getSimpleName(), new BuildBarracks(unitTypeTable));
		// end todo encapsulate
		
		//sets the joint action model containing the valid actions
		setJointActionModel(new AggregateStateJAM(actions));
	}
	
	/**
	 * Returns the initial state for the game
	 * @return
	 */
	public State getInitialState(){
		return new AggregateState(gs);
	}

	@Override
	public List<? extends State> enumerate() {
		//TODO implement enumerate!
		return null; //GameStage.allStates();
	}

}
