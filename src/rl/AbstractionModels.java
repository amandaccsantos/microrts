package rl;

import java.io.IOException;

import org.jdom.JDOMException;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.world.World;
import rl.models.MicroRTSTerminalFunction;
import rl.models.WinLossRewardFunction;
import rl.models.stages.GameStagesDomain;
import rl.models.stages.StagesDomainGenerator;

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
		
		throw new RuntimeException("Unknown abstraction model: " + model);
	}
	
	/**
	 * In this abstraction, microRTS game states are differentiated
	 * only by the game stage they represent (e.g. early, mid, late)
	 * @return {@link World}
	 */
	public static World stages() {
		GameStagesDomain stagesDomain = null;
		try {
			stagesDomain = new GameStagesDomain();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JointRewardFunction rwdFunc = new WinLossRewardFunction();
		TerminalFunction terminalFunc = new MicroRTSTerminalFunction();

		World w = new World(stagesDomain, rwdFunc, terminalFunc, stagesDomain.getInitialState());
		return w;
	}

}
