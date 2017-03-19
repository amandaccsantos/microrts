package rl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ai.metabot.learning.model.MicroRTSState;
import burlap.behavior.stochasticgames.GameEpisode;
import burlap.behavior.stochasticgames.agents.interfacing.singleagent.LearningAgentToSGAgentInterface;
import burlap.behavior.valuefunction.QValue;
import burlap.debugtools.DPrint;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.world.World;
import rl.adapters.PersistentLearner;

/**
 * Manages a Reinforcement Learning experiment in microRTS
 * The RL experiment has two players (or learning agents), the 'world' which
 * consists of an microRTS abstraction and some experiment parameters
 * @author anderson
 *
 */
public class RLExperiment {
	
	public static void main(String[] args){
		//loads parameters from file
		Map<String, Object> parameters = null;
		
		try {
			parameters = RLParameters.getInstance().loadFromFile(args[1]);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			System.err.println("An error has occurred...");
			e.printStackTrace();
			System.exit(0);
		}
		
		//adds players to the world
		World gameWorld = (World) parameters.get(RLParamNames.ABSTRACTION_MODEL);
		List<PersistentLearner> agents = (List<PersistentLearner>) parameters.get(RLParamNames.PLAYERS);
		
		if(agents.size() < 2){
			throw new RuntimeException("Less than 2 players were specified for the experiment");
		}
		
		for(SGAgent agent : agents){
			gameWorld.join(agent);
		}
		
		
		//performs training
		System.out.println("Starting training");
		
		// don't have the world print out debug info (uncomment if you want to see it!)
		//DPrint.toggleCode(theWorld.getDebugId(), false);
		int numEpisodes = (int) parameters.get(RLParamNames.EPISODES);
		List<GameEpisode> episodes = new ArrayList<GameEpisode>(numEpisodes);
		
		//retrieves output dir
		String outDir = (String) parameters.get(RLParamNames.OUTPUT_DIR);
		
		for (int episodeNumber = 0; episodeNumber < numEpisodes; episodeNumber++) {
			GameEpisode episode = gameWorld.runGame();
			episodes.add(episode);
			
			System.out.print(String.format("\bEpisode #%7d finished.", episodeNumber));
			
			
			//writes episode data and q-values
			episode.write(String.format("%s/episode_%d", outDir, episodeNumber));
			for(PersistentLearner agent : agents){
				
				agent.saveKnowledge(
					String.format("%s/q_%s_%d.txt", outDir, agent.agentName(), episodeNumber)
				);
			}
		}
		//finished training
		System.out.println("\nTraining finished"); //has leading \n because previous print has no trailing \n
	}
		
}
