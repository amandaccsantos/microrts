package rl.models.aggregate;

import java.io.IOException;
import java.util.List;

import org.jdom.JDOMException;

import burlap.mdp.core.state.State;
import rl.models.common.ScriptActionTypes;
import rl.models.stages.GameStagesDomain;

/**
 * A domain with abstract representation of a microRTS state
 * quantities are 'discretized' according to empirical amounts
 * 
 * TODO record map file and reload initial state from it
 * @author anderson
 *
 */
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
