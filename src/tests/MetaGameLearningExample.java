package tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jdom.JDOMException;

import ai.abstraction.LightRush;
import ai.metabot.DummyPolicy;
import ai.metabot.learning.model.MicroRTSGame;
import ai.metabot.learning.model.MicroRTSJointRewardFunction;
import ai.metabot.learning.model.MicroRTSState;
import ai.metabot.learning.model.MicroRTSTerminalFunction;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.stochasticgames.GameEpisode;
import burlap.behavior.stochasticgames.agents.interfacing.singleagent.LearningAgentToSGAgentInterface;
import burlap.behavior.stochasticgames.agents.maql.MultiAgentQLearning;
import burlap.behavior.stochasticgames.auxiliary.GameSequenceVisualizer;
import burlap.behavior.stochasticgames.madynamicprogramming.SGBackupOperator;
import burlap.behavior.valuefunction.QValue;
import burlap.debugtools.DPrint;
import burlap.domain.stochasticgames.gridgame.GGVisualizer;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.world.World;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;
import rl.adapters.PersistentMultiAgentQLearning;

/**
 * An example of the Algorithm Selection Metagame in microRTS
 * 
 * @author anderson
 *
 */
public class MetaGameLearningExample {
	public MetaGameLearningExample() {
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
		SGAgentType agentType = new SGAgentType("agent", microRTSDomain.getActionTypes());

		JointRewardFunction rwdFunc = new MicroRTSJointRewardFunction();
		TerminalFunction terminalFunc = new MicroRTSTerminalFunction();

		// learning parameters
		final double discount = 0.95;
		final double learningRate = 0.1;
		final double defaultQ = 1;

		int ngames = 100;
		
		World w = new World(microRTSDomain, rwdFunc, terminalFunc, microRTSGame.getInitialState());

		// single agent Q-learning algorithms which will operate in our stochastic game
		// don't need to specify the domain, because the single agent interface will provide it
		QLearning ql1 = new QLearning(null, discount, new SimpleHashableStateFactory(false), defaultQ, learningRate);
		QLearning ql2 = new QLearning(null, discount, new SimpleHashableStateFactory(false), defaultQ, learningRate);

		PersistentMultiAgentQLearning ql3 = new PersistentMultiAgentQLearning(null, discount, 
				learningRate, new SimpleHashableStateFactory(false), defaultQ, null,
				false, "agent2", agentType);
		
		// loads previous match values to initialize function
		for (int i = 0; i < ngames; i++) {							
			ql1.loadQTable("/tmp/qltest/qtable0_" + i);
			ql1.setQInitFunction(ql1);
		}	
		
		// ql2 will be a dummy, always selecting the same behavior
		ql2.setLearningPolicy(new DummyPolicy(MicroRTSGame.RANGED_RUSH, ql2));

		// create a single-agent interface for each of our learning algorithm instances
		LearningAgentToSGAgentInterface a1 = new LearningAgentToSGAgentInterface(microRTSDomain, ql1, "agent0",
				agentType);
		LearningAgentToSGAgentInterface a2 = new LearningAgentToSGAgentInterface(microRTSDomain, ql2, "agent1",
				agentType);
		
		//LearningAgentToSGAgentInterface a3 = new LearningAgentToSGAgentInterface(microRTSDomain, ql3, "agent2", agentType);

		w.join(a1);
		w.join(a2);

		// don't have the world print out debug info (comment out if you want to
		// see it!)
		DPrint.toggleCode(w.getDebugId(), false);

		System.out.println("Starting training");
		
		List<GameEpisode> episodes = new ArrayList<GameEpisode>(ngames);
		PrintWriter output = null;
		
		try {
			output = new PrintWriter(new BufferedWriter(new FileWriter("output.txt", false)));
			for (int i = 0; i < ngames; i++) {
				GameEpisode episode = w.runGame();
				
				episodes.add(episode);
				if (i % 10 == 0) {
					System.out.println("Game: " + i + ": " + episode.maxTimeStep());
				}
				episode.write("/tmp/qltest/qltest_" + i);
				ql1.writeQTable("/tmp/qltest/qtable0_" + i);
				ql2.writeQTable("/tmp/qltest/qtable1_" + i);
				ql3.saveKnowledge("/tmp/qltest/qtable2_" + i);

				output.println("Game: " + i);
				output.println("Value functions for agent 0");

				for (MicroRTSState s : MicroRTSState.allStates()) {
					output.println(String.format("%s: %.3f", s, ql1.value(s)));
					for (QValue q : ql1.qValues(s)) {
						output.println(String.format("%s: %.3f", q.a, q.q));
					}
				}

				output.println("Value functions for agent 1");
				for (MicroRTSState s : MicroRTSState.allStates()) {
					output.println(String.format("%s: %.3f", s, ql2.value(s)));
					for (QValue q : ql2.qValues(s)) {
						output.println(String.format("%s: %.3f", q.a, q.q));
					}
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		output.close();
	}

	public static void main(String[] args) {

		new MetaGameLearningExample();

		try {
			Runtime.getRuntime().exec("python python/plot_actions.py");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
