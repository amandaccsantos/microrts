package tests;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import ai.abstraction.HeavyRush;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.core.AI;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;


public class MetaBotTest {
	public static void main(String args[]) throws Exception {
		List<AI> bots = new LinkedList<>();
        UnitTypeTable unitTypeTable = new UnitTypeTable();
        
        //bots.add(new MetaBot(timeBudget, iterationsBudget, unitTypeTable, "/tmp/qltest/qtable0_99"));
        //bots.add(new ai.rl.MetaBot("MinimaxQ", "/tmp/mmq-vs-workerrush/q_learner_final.txt", "stages"));
        bots.add(new ai.rl.MetaBot());
        bots.add(new WorkerRush(unitTypeTable));
        PrintStream out = System.out;
        
        // Separate the matchs by map:
        List<PhysicalGameState> maps = new LinkedList<PhysicalGameState>();        
        //maps.add(PhysicalGameState.load("maps/basesWorkers8x8.xml", unitTypeTable));
        maps.add(PhysicalGameState.load("maps/24x24/basesWorkers24x24A.xml", unitTypeTable));
        Experimenter.runExperiments(bots, maps, unitTypeTable, 1, 3000, 300, true, out);
	}
    
}
