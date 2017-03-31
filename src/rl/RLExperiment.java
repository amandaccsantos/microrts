package rl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ai.metabot.learning.model.MicroRTSState;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.stochasticgames.GameEpisode;
import burlap.behavior.stochasticgames.agents.interfacing.singleagent.LearningAgentToSGAgentInterface;
import burlap.behavior.stochasticgames.agents.maql.MultiAgentQLearning;
import burlap.behavior.stochasticgames.madynamicprogramming.MultiAgentQSourceProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.debugtools.DPrint;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.world.World;
import rl.adapters.domain.EnumerableSGDomain;
import rl.adapters.learners.PersistentLearner;
import rl.adapters.learners.SGQLearningAdapter;
import rl.models.GameStagesDomain;
import sun.security.jca.GetInstance;

/**
 * Manages a Reinforcement Learning experiment in microRTS The RL experiment has
 * two players (or learning agents), the 'world' which consists of an microRTS
 * abstraction and some experiment parameters
 * 
 * @author anderson
 *
 */
public class RLExperiment {

	public static void main(String[] args) {
		// loads parameters from file
		Map<String, Object> parameters = null;

		RLParameters rlParams = RLParameters.getInstance();

		try {
			parameters = rlParams.loadFromFile(args[0]);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			System.err.println("An error has occurred...");
			e.printStackTrace();
			System.exit(0);
		}

		// adds players to the world
		World gameWorld = (World) parameters.get(RLParamNames.ABSTRACTION_MODEL);
		List<PersistentLearner> agents = (List<PersistentLearner>) parameters.get(RLParamNames.PLAYERS);

		if (agents.size() < 2) {
			throw new RuntimeException("Less than 2 players were specified for the experiment");
		}

		for (SGAgent agent : agents) {
			gameWorld.join(agent);
		}

		// performs training
		System.out.println("Starting training");

		// don't have the world print out debug info (uncomment if you want to see it!)
		// DPrint.toggleCode(theWorld.getDebugId(), false);
		int numEpisodes = (int) parameters.get(RLParamNames.EPISODES);
		List<GameEpisode> episodes = new ArrayList<GameEpisode>(numEpisodes);

		// retrieves output dir
		String outDir = (String) parameters.get(RLParamNames.OUTPUT_DIR);
		PrintWriter output = null;
		
		try {
			output = new PrintWriter(new BufferedWriter(new FileWriter("output.txt", false)));
			for (int episodeNumber = 0; episodeNumber < numEpisodes; episodeNumber++) {
				GameEpisode episode = gameWorld.runGame();
				episodes.add(episode);
	
				System.out.print(String.format("\rEpisode #%7d finished.", episodeNumber));
			
				// writes episode data and q-values
				episode.write(String.format("%s/episode_%d", outDir, episodeNumber));
				for (PersistentLearner agent : agents) {
					agent.saveKnowledge(String.format("%s/q_%s_%d.txt", outDir, agent.agentName(), episodeNumber));
				}
				
				EnumerableSGDomain enumDomain = (EnumerableSGDomain) gameWorld.getDomain();
				for (MicroRTSState s : enumDomain.enumerate()) {
                	for (PersistentLearner agent : agents) {
                		QLearning qLearner = (QLearning) ((SGQLearningAdapter) agent).getSingleAgentLearner();
	                    output.println(String.format("%s: %.3f", s, qLearner.value(s)));
	                    for (QValue q : qLearner.qValues(s)) {
	                        output.println(String.format("%s: %.3f", q.a, q.q));
	                    }
                	}
                }
				
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		output.close();

		// finished training
		System.out.println("\nTraining finished"); // has leading \n because previous print has no trailing \n

		/*try {
			Runtime.getRuntime().exec("python python/plot_actions.py");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Plot finished.");*/
	}

}
