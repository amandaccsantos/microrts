package rl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.world.World;
import rl.adapters.domain.EnumerableSGDomain;
import rl.adapters.learners.PersistentLearner;
import rl.adapters.learners.SGQLearningAdapter;
import rl.models.common.MicroRTSState;

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
		
		// checks for the quiet parameter
		boolean quiet = cmdLine.hasOption("quiet");
		
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
		//List<GameEpisode> episodes = new ArrayList<GameEpisode>(numEpisodes);

		// retrieves output dir
		String outDir = (String) parameters.get(RLParamNames.OUTPUT_DIR);
		File dir = new File(outDir);
		if (!dir.exists()) {
			dir.mkdir();
		}
		PrintWriter output = null;
		
		// declares episode variable (will store final episode info after loop finishes)
		GameEpisode episode = null;
		
		try {
			output = new PrintWriter(new BufferedWriter(new FileWriter(outDir + "/output.txt", false)));
			for (int episodeNumber = 0; episodeNumber < numEpisodes; episodeNumber++) {
				episode = gameWorld.runGame();
				//episodes.add(episode);
	
				System.out.print(String.format("\rEpisode #%7d finished.", episodeNumber));
			
				// writes episode data and q-values
				if (!quiet){
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
				
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

		// finished training
		System.out.println("\nTraining finished"); // has leading \n because previous print has no trailing \n

		// if I did not print during training, print now:
		//if(quiet){
			//episode.write(String.format("%s/episode_%d", outDir, episodeNumber));
		for (PersistentLearner agent : agents) {
			agent.saveKnowledge(String.format("%s/q_%s_final.txt", outDir, agent.agentName()));
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
		//}
		
		//prints results for final episode
		printEpisodeInfo(numEpisodes - 1, episode, outDir + "/final_episode.txt");
		//printEpisodeInfo(episodes.size() - 1, episodes.get(episodes.size() - 1), outDir + "/final_episode.txt");
		//printEpisodesInfo(episodes, outDir + "/episodes.txt"); 
	}

	private static CommandLine processCommandLine(String[] args) {
		// create Options object
		Options options = new Options();

		options.addOption("c", RLParamNames.CONFIG_FILE, true, "Path to configuration file.");
		options.addOption("o", RLParamNames.OUTPUT_DIR, true, "Directory to generate output.");
		options.addOption("q", "quiet", false, "Don't generate enormous ammount of files");
		
		
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
	
	private static void printEpisodesInfo(List<GameEpisode> episodes, String path){
		
		for(int i = 0; i < episodes.size(); i++){
			printEpisodeInfo(i, episodes.get(i), path);
		}
	}
	
	private static void printEpisodeInfo(int episodeNumber, GameEpisode episode, String path){
		PrintWriter out;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		episode.write(String.format("%s.ep", path));
		
		out.println("Episode " + episodeNumber);
		out.println("Duration: " + episode.numTimeSteps());
		out.println("States visited: " + episode.states.size());
		out.println("\nJoint actions: ");
		
		for(JointAction ja : episode.jointActions){
			out.println(ja);
		}
		
		out.println("\nFinal state:");
		MicroRTSState finalState = (MicroRTSState) episode.states.get(episode.states.size() - 1);
		out.println(finalState);
		out.println("\nFinal state dump: ");
		out.println(finalState.dump());
		
		/*for(State s : episode.states){
			out.println("state: " + s);
		}
		*/
		List<double[]> jointRewards = episode.jointRewards;
		double[] finalRewards = jointRewards.get(jointRewards.size() -1);
		
		// Locale.ROOT ensures the use of '.' as decimal separator
		out.println(String.format(Locale.ROOT, "\nFinal rewards: %f,%f", finalRewards[0], finalRewards[1] ));
		
		out.close();
	}

}
