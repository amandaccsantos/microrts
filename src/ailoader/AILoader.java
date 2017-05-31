package ailoader;

import java.util.Map;

import ai.core.AI;
import ai.portfolio.NashPortfolioAI;
import ai.portfolio.PortfolioAI;
import ai.portfolio.portfoliogreedysearch.PGSAI;
import ai.puppet.PuppetSearchMCTS;
import rl.models.common.ScriptActionTypes;
import rts.units.UnitTypeTable;

/**
 * Singleton class that takes care of loading AIs 
 * given a string with the AI name
 * @author anderson
 *
 */
public class AILoader {

	private AILoader instance;
	
	private AILoader() {
		
	}
	
	public AILoader getInstance(){
		if (instance == null){
			instance = new AILoader();
		}
		return instance;
	}
	
	/**
	 * Returns an AI whose name corresponds to the given string
	 * @param aiName
	 * @param utt
	 * @return
	 */
	public static AI loadAI(String aiName, UnitTypeTable utt){
		Map<String, AI> learnerAIs = ScriptActionTypes.getActionMapping(utt);
		AI toReturn = null;
		if(learnerAIs.containsKey(aiName)){
			toReturn = learnerAIs.get(aiName);
		}
		
		else if (aiName.equalsIgnoreCase("PGS") || 
					aiName.equalsIgnoreCase("PGSAI") || 
					aiName.equalsIgnoreCase("PortfolioGreedySearch") ){
				
			toReturn = new PGSAI(utt);
		}
		
		else if (aiName.equalsIgnoreCase("Puppet") || 
				aiName.equalsIgnoreCase("PuppetSearch") || 
				aiName.equalsIgnoreCase("PuppetMCTS") ){
			
			toReturn = new PuppetSearchMCTS(utt);
		}
		
		else if (aiName.equalsIgnoreCase("PortfolioAI")) {
			toReturn = new PortfolioAI(utt);
		}
		
		else if(aiName.equalsIgnoreCase("NashPortfolioAI") || 
				aiName.equalsIgnoreCase("NashPortfolio")){
			toReturn = new NashPortfolioAI(utt);
		}
		
		else { //name not found
			throw new RuntimeException("AI name " + aiName + " not found.");
		}
		
		return toReturn;
		
		/*AI theAI = null;
		try {
			theAI = (AI) Class.forName(aiName).newInstance();
		}
		catch (Exception e){
			System.err.println("An error has occurred while attempting to load an AI");
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
		return theAI;*/
	}

}
