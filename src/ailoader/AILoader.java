package ailoader;

import ai.core.AI;

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
	
	public static AI loadAI(String aiName){
		AI theAI = null;
		try {
			theAI = (AI) Class.forName(aiName).newInstance();
		}
		catch (Exception e){
			System.err.println("An error has occurred while attempting to load an AI");
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
		return theAI;
	}

}
