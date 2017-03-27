package rl;

import java.io.IOException;

import org.jdom.JDOMException;

import ai.metabot.learning.model.MicroRTSGame;
import ai.metabot.learning.model.MicroRTSJointRewardFunction;
import ai.metabot.learning.model.MicroRTSTerminalFunction;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.world.World;

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
		MicroRTSGame microRTSGame = null;
		try {
			microRTSGame = new MicroRTSGame();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SGDomain microRTSDomain = (SGDomain) microRTSGame.generateDomain();

		JointRewardFunction rwdFunc = new MicroRTSJointRewardFunction();
		TerminalFunction terminalFunc = new MicroRTSTerminalFunction();

		World w = new World(microRTSDomain, rwdFunc, terminalFunc, microRTSGame.getInitialState());
		return w;
	}

}
