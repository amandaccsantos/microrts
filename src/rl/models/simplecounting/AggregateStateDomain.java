package rl.models.simplecounting;

import java.io.IOException;
import java.util.List;

import org.jdom.JDOMException;

import burlap.mdp.core.state.State;
import rl.models.ScriptActionTypes;
import rl.models.stages.GameStagesDomain;

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
	
		//sets the joint action model containing the valid actions
		setJointActionModel(new AggregateStateJAM(ScriptActionTypes.getActionMapping(unitTypeTable)));
	}
	
	/**
	 * Returns the initial state for the game
	 * @return
	 */
	public State getInitialState(){
		return new AggregateState(gs);
	}

}
