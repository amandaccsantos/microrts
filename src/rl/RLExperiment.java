package rl;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import burlap.behavior.stochasticgames.GameEpisode;
import burlap.debugtools.DPrint;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.world.World;

/**
 * Manages a Reinforcement Learning experiment in microRTS
 * The RL experiment has two players (or learning agents), the 'world' which
 * consists of an microRTS abstraction and some experiment parameters
 * @author anderson
 *
 */
public class RLExperiment {

	List<SGAgent> players;
	World microRTSAbstraction;
	public RLExperiment(List<SGAgent> players, World microRTSAbstraction) {
		
		if (players.size() < 2){
			throw new RuntimeException("Less than 2 players were specified for the experiment");
		}
		
		//makes the players join the 'game' 
		this.players = players;
		this.microRTSAbstraction = microRTSAbstraction;
		for(SGAgent player : players) {
			microRTSAbstraction.join(player);
		}
	}
	
	public void run(){
		//DPrint.toggleCode(microRTSAbstraction.getDebugId(), false);

		System.out.println("Starting training");
		int ngames = 100;
		List<GameEpisode> episodes = new ArrayList<GameEpisode>(ngames);
		PrintWriter output = null;
		File file = new File("output.txt");
		if (file.exists()) {
			file.delete();
		}
	}

}
