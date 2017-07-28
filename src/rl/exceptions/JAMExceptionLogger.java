package rl.exceptions;

import java.util.ArrayList;
import java.util.List;

public class JAMExceptionLogger {
	/**
	 * Singleton instance
	 */
	private static JAMExceptionLogger instance;
	
	private List<ExceptionListEntry> exceptions;
	
	/**
	 * Private constructor (for singleton)
	 */
	private JAMExceptionLogger(){
		exceptions = new ArrayList<>();
	}
	
	/**
	 * Returns the singleton instance of this class
	 * @return
	 */
	public static JAMExceptionLogger getInstance(){
		if(instance == null){
			instance = new JAMExceptionLogger();
		}
		return instance;
	}
	
	/**
	 * Adds an exception to the list
	 * @param e
	 * @param playerIndex
	 * @param extraInfo
	 */
	public void registerException(Exception e, int playerIndex, String extraInfo){
		exceptions.add(
			new ExceptionListEntry(playerIndex, e, extraInfo)
		);
	}
	
	/**
	 * Counts the number of exceptions thrown by a player
	 * @param player
	 * @return
	 */
	public int countExceptionsOfPlayer(int player){
		int count = 0;
		for (ExceptionListEntry entry : exceptions){
			if(entry.playerIndex == player) count++;
		}
		return count;
	}
}
