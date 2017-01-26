package ai.metabot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ai.abstraction.BuildBarracks;
import ai.abstraction.Expand;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ParameterSpecification;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

public class MetaBot extends AIWithComputationBudget {

	//private Map<String, AI> portfolio;
	//private String currentAI;
	private Map<String, AI> portfolio;
	private String currentAIName;
	private int currentAIIndex;
	
	private UnitTypeTable unitTypeTable;
	private int timeBudget;
	private int iterationsBudget;
	
	/**
	 * Default constructor with same parameters as AIWithComputationBudget.
	 * Initializes a default portfolio
	 * @param timeBudget
	 * @param iterationsBudget
	 */
	public MetaBot(int timeBudget, int iterationsBudget, UnitTypeTable unitTypeTable){
		super(timeBudget, iterationsBudget);
		
		this.unitTypeTable = unitTypeTable;
		this.timeBudget = timeBudget;
		this.iterationsBudget = iterationsBudget;
		
		portfolio = new HashMap<>();
		
		
		portfolio.put(WorkerRush.class.getSimpleName(), new WorkerRush(unitTypeTable));
		portfolio.put(LightRush.class.getSimpleName(), new LightRush(unitTypeTable));
		portfolio.put(RangedRush.class.getSimpleName(), new RangedRush(unitTypeTable));
		portfolio.put(Expand.class.getSimpleName(), new Expand(unitTypeTable));
		portfolio.put(BuildBarracks.class.getSimpleName(), new BuildBarracks(unitTypeTable));
		
		currentAIName = "WorkerRush";
		
	}
		
	@Override
	public void reset() {
		for(AI ai : portfolio.values()){
			ai.reset();
		}

	}

	@Override
	public PlayerAction getAction(int player, GameState gs) throws Exception {
		if (gs.canExecuteAnyAction(player)) {
			//cycles through AIs every 200 frames
			if(gs.getTime() % 200 == 0){
				currentAIIndex = (currentAIIndex + 1) % portfolio.values().size();
				currentAIName = (String)portfolio.keySet().toArray()[currentAIIndex];
				System.out.println("MetaBot: changed to " + currentAIName);
			}
			return portfolio.get(currentAIName).getAction(player, gs); 
        } else {
            return new PlayerAction();        
        }       
	}

	@Override
	public AI clone() {
		return new MetaBot(timeBudget, iterationsBudget, unitTypeTable);
	}

	@Override
	public List<ParameterSpecification> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}
