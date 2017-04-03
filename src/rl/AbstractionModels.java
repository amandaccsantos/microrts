package rl;

import java.io.IOException;

import org.jdom.JDOMException;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.world.World;
import rl.models.common.MicroRTSTerminalFunction;
import rl.models.common.WinLossRewardFunction;
import rl.models.simplecounting.AggregateStateDomain;
import rl.models.stages.GameStagesDomain;

/**
 * Concentrates different World models
 * @author anderson
 *
 */
public class AbstractionModels {

	
	public static World fromString(String model){
		if(model.equalsIgnoreCase("stages")){
			return stages();
		}
		else if(model.equalsIgnoreCase("aggregate")){
			return aggregateStateFeatures();
		}
		
		throw new RuntimeException("Unknown abstraction model: " + model);
	}
	
	/**
	 * In this abstraction, microRTS game states are differentiated
	 * only by the game stage they represent (e.g. early, mid, late)
	 * Reward and terminal functions are default: {@link WinLossRewardFunction} and
	 * {@link MicroRTSTerminalFunction}
	 * @return {@link World}
	 */
	public static World stages() {
		return stages(new WinLossRewardFunction(), new MicroRTSTerminalFunction());
	}
	
	public static World stages(JointRewardFunction rwdFunc, TerminalFunction terminalFunc) {
		GameStagesDomain stagesDomain = null;
		try {
			stagesDomain = new GameStagesDomain();
		} catch (JDOMException|IOException e) {
			e.printStackTrace();
		}

		World w = new World(stagesDomain, rwdFunc, terminalFunc, stagesDomain.getInitialState());
		return w;
	}
	
	/**
	 * In this abstraction, microRTS states are differentiated by
	 * quantities of entities (few, fair or many)
	 * Reward and terminal functions are default: {@link WinLossRewardFunction} and
	 * {@link MicroRTSTerminalFunction}
	 * @return
	 */
	public static World aggregateStateFeatures(){
		return aggregateStateFeatures(new WinLossRewardFunction(), new MicroRTSTerminalFunction());
	}
	
	public static World aggregateStateFeatures(JointRewardFunction rwdFunc, TerminalFunction terminalFunc) {
		AggregateStateDomain aggrDomain = null;
		try {
			aggrDomain = new AggregateStateDomain();
		} catch (JDOMException | IOException e) {
			System.err.println("An error happened! Will exit...");
			e.printStackTrace();
			System.exit(0);
		}
		
		World w = new World(aggrDomain, rwdFunc, terminalFunc, aggrDomain.getInitialState());
		return w;
	}

}
