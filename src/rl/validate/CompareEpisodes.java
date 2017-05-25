package rl.validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import burlap.behavior.stochasticgames.GameEpisode;

public class CompareEpisodes {
	
	PrintStream outStream;
	
	public CompareEpisodes(PrintStream output){
		outStream = output;
	}
	
	/**
	 * Returns whether two files contain the same episode data
	 * @param file1
	 * @param file2
	 * @return
	 */
	public boolean compare(String file1, String file2){
		GameEpisode ge1 = null, ge2 = null;
		boolean equal = true;
		
		Yaml yaml = new Yaml();
		try {
			ge1 = (GameEpisode)yaml.load(new FileInputStream(file1));
			ge2 = (GameEpisode)yaml.load(new FileInputStream(file2));
		} catch(FileNotFoundException e) {
			System.err.println("Error while loading episode from file. Comparison aborted.");
			e.printStackTrace();
			return false;
		}
		
		// checks the states
		if(! listCompare(ge1.states, ge2.states, "state")){
		//if(! ge1.states.equals(ge2.states)){
			equal = false;
		}
		
		// checks the joint actions
		if(! listCompare(ge1.jointActions, ge2.jointActions, "joint action")){
		//if(!ge1.jointActions.equals(ge2.jointActions)){
			equal = false;
		}
		
		// checks the reward
		if(! listCompare(ge1.jointRewards, ge2.jointRewards, "joint reward")){
		//if(!ge1.jointRewards.equals(ge2.jointRewards)){
			equal = false;
		}
		
		return equal;
	}
	
	/**
	 * Traverses two lists entirely, printing out the different elements
	 * (this differs from {@link List#equals(Object)} because it points out
	 * different elements)
	 * @param first
	 * @param second
	 * @param itemName
	 * @return true if the lists have the same items, false otherwhise
	 */
	public boolean listCompare(List<?> first, List<?> second, String itemName){
		boolean equal = true;
		
		if(first.size() != second.size()){
			outStream.println("Different list sizes of " + itemName);
			equal = false;
		}
		
		for(int i = 0; i < first.size(); i++){
			try{
				
				if(! itemsEqual(first.get(i), second.get(i))){
					outStream.println(String.format(
						"Difference on %dth %s: %s vs %s", (i+1), itemName, first.get(i), second.get(i)
					));
					equal = false;
				}
			}
			catch(ArrayIndexOutOfBoundsException e){
				// we're safe
				break;
			}
		}
		
		return equal;
	}
	
	private boolean itemsEqual(Object first, Object second){
		
		// comparison for joint rewards (double[])
		if((first instanceof double[]) && (second instanceof double[])){
			return Arrays.equals((double[]) first, (double[]) second);
		}
		
		// comparison for other objects
		return first.equals(second);
	}

	public static void main(String[] args) {
		
		boolean allEqual = true;
		
		CompareEpisodes comparator = new CompareEpisodes(System.out);
		
		// retrieves the list of files in the given directory (in args[0])
		File[] listOfFiles = new File(args[0]).listFiles();
		
		// compares every pair of files, filtering with PathMatcher
		for(int i = 0; i < listOfFiles.length; i++){
			
			// ignores non .game files
			if(! listOfFiles[i].getName().endsWith(".game")){
				continue;
			}
			
			for (int j = i + 1; j < listOfFiles.length; j++){
				if(! listOfFiles[j].getName().endsWith(".game")){
					continue;
				}
				
				if(! comparator.compare(listOfFiles[i].getPath(), listOfFiles[j].getPath())){
					allEqual = false;
					System.out.println(String.format(
						"Files %s and %s are different", listOfFiles[i], listOfFiles[j]
					));
				}
			}
		}
		
		System.out.println("Comparison finished.");
		
		if (allEqual){
			System.out.println("All .game files have the same content.");
		}

	}

}
