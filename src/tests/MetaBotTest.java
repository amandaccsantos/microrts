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
        bots.add(new ai.rl.MetaBot("BackwardInduction", "/tmp/solution-winloss.xml", "aggregatediff"));
        //bots.add(new ai.rl.MetaBot());
        System.out.println("Added MetaBot.");
        
        bots.add(new LightRush(unitTypeTable));
        System.out.println("Added adversary.");
        
        PrintStream out = System.out;
        
        // prepares maps
        List<PhysicalGameState> maps = new LinkedList<PhysicalGameState>();        
        maps.add(PhysicalGameState.load("maps/basesWorkers24x24.xml", unitTypeTable));
        System.out.println("Maps prepared.");
        
        // runs the 'tournament'
        Experimenter.runExperiments(bots, maps, unitTypeTable, 1, 3000, 300, true, out);
        System.out.println("Done.");
	}
    
}
