package tests;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import ai.RandomBiasedAI;
import ai.abstraction.BuildBarracks;
import ai.abstraction.Expand;
import ai.abstraction.HeavyRush;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.core.AI;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.portfolio.NashPortfolioAI;
import ai.portfolio.PortfolioAI;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;


public class MetaBotTest {
	public static void main(String args[]) throws Exception {
		List<AI> bots = new LinkedList<>();
        UnitTypeTable unitTypeTable = new UnitTypeTable();
        
        //bots.add(new MetaBot(timeBudget, iterationsBudget, unitTypeTable, "/tmp/qltest/qtable0_99"));
        //bots.add(new ai.rl.MetaBot("BackwardInduction", "/tmp/solution-winloss.xml", "aggregatediff"));
        //bots.add(new ai.rl.MetaBot("MinimaxQ", "/tmp/solution-winloss.xml", "aggregatediff"));
        //bots.add(new ai.rl.MetaBot());
        AI player1 = new NashPortfolioAI(
    		new AI[]{
	    		new WorkerRush(unitTypeTable),
	            new LightRush(unitTypeTable),
	            new RangedRush(unitTypeTable),
	            new HeavyRush(unitTypeTable),
	            //new BuildBarracks(unitTypeTable),
	            //new Expand(unitTypeTable)
            },
           new boolean[]{true,true,true,true,},
           100, -1, 100,
           new SimpleSqrtEvaluationFunction3()
        );
        
        AI player2 = new PortfolioAI(
        		new AI[]{
    	    		new WorkerRush(unitTypeTable),
    	            new LightRush(unitTypeTable),
    	            new RangedRush(unitTypeTable),
    	            new HeavyRush(unitTypeTable),
    	            //new BuildBarracks(unitTypeTable),
    	            //new Expand(unitTypeTable)
                },
               new boolean[]{true,true,true,true,},
               100, -1, 100,
               new SimpleSqrtEvaluationFunction3()
            );
        
        bots.add(player1);
        System.out.println("Added first player.");
        
        bots.add(player2);
        System.out.println("Added second player.");
        
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
