package rl.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionListEntry {
	public int playerIndex;
	public String playerName;
	
	public String extraInfo;
	public Exception exception;
	
	/**
	 * 
	 * @param playerIndex
	 * @param playerName
	 * @param e
	 * @param extraInfo
	 */
	public ExceptionListEntry(int playerIndex, String playerName, Exception e, String extraInfo){
		this.playerIndex = playerIndex;
		this.playerName = playerName;
		this.extraInfo = (extraInfo == "" || extraInfo == null) ? "nothing" : extraInfo;
		this.exception = e;
	}
	
	public String toString(){
		// stores the stack trace in a StringWriter to retrieve a string afterwards
		StringWriter errors = new StringWriter();
		exception.printStackTrace(new PrintWriter(errors));
		
		return String.format(
			"Player: %d (%s)\nAdditional Info: %s\nStack trace: %s", 
			playerIndex, playerName, extraInfo, errors.toString()
		);
	}
}
