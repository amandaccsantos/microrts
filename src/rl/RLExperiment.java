package rl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.stochasticgames.GameEpisode;
import burlap.behavior.valuefunction.QValue;
import burlap.debugtools.DPrint;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.world.World;
import rl.adapters.domain.EnumerableSGDomain;
import rl.adapters.learners.PersistentLearner;
import rl.adapters.learners.SGQLearningAdapter;

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
		
		
		//parses command line arguments
		CommandLine cmdLine = processCommandLine(args);
		if(! cmdLine.hasOption(RLParamNames.CONFIG_FILE)){
			System.err.println(
				"Please provide the configuration file with -c or " + RLParamNames.CONFIG_FILE
			);
			System.exit(0);
		}
		// loads parameters from file
		Map<String, Object> parameters = null;

		RLParameters rlParams = RLParameters.getInstance();
		
		try {
			parameters = rlParams.loadFromFile(cmdLine.getOptionValue(RLParamNames.CONFIG_FILE));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			System.err.println("An error has occurred...");
			e.printStackTrace();
			System.exit(0);
		}
		
		// overrides parameters with ones supplied in command line (if needed)
		parameters = rlParams.parametersFromCommandLine(cmdLine);

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
		DPrint.toggleCode(gameWorld.getDebugId(), false);
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
				for (State s : enumDomain.enumerate()) {
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

	private static CommandLine processCommandLine(String[] args) {
		// create Options object
		Options options = new Options();

		options.addOption("c", RLParamNames.CONFIG_FILE, true, "Path to configuration file.");
		options.addOption("o", RLParamNames.OUTPUT_DIR, true, "Directory to generate output.");
		
		CommandLine line = null;
		CommandLineParser parser = new DefaultParser();
		
		try {
	        // parse the command line arguments
	        line = parser.parse( options, args );
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Command line parsing failed.");
	        exp.printStackTrace();
	        System.err.println( "Exiting");
	        System.exit(0);
	    }
		
		return line;
		
	}

}
