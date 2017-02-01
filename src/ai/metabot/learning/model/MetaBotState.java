package ai.metabot.learning.model;

enum GameStage{
	OPENING, 	//0 - 599 frames
	EARLY,		//600 - 1199
	MID,		//1200 - 1799
	LATE,		//1800 - 2399
	END			//2400 - 3000
}

public class MetaBotState {
	int frameNumber;
	
	GameStage stage;
	
	
	public MetaBotState(int frameNumber){
		this.frameNumber = frameNumber;
		
		if(frameNumber < 600){
			stage = GameStage.OPENING;
		}
		else if (frameNumber < 1200){
			stage = GameStage.EARLY;
		}
		else if (frameNumber < 1800){
			stage = GameStage.MID;
		}
		else if (frameNumber < 2400){
			stage = GameStage.LATE;
		}
		else {
			stage = GameStage.END;
		}
	}
	
	public GameStage getStage(){
		return stage;
	}
	
	public boolean equals(Object other){
		if(! (other instanceof MetaBotState) ){
			return false;
		}
		return stage == ((MetaBotState)other).getStage();
	}
	
	
}
