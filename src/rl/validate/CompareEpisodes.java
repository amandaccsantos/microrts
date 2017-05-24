package rl.validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import burlap.behavior.stochasticgames.GameEpisode;

public class CompareEpisodes {
	
	/**
	 * Returns whether two files contain the same episode data
	 * @param file1
	 * @param file2
	 * @return
	 */
	public static boolean compare(String file1, String file2){
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
			equal = false;
		}
		
		// checks the joint actions
		if(! listCompare(ge1.jointActions, ge2.jointActions, "joint action")){
			equal = false;
		}
		
		// checks the reward
		if(! listCompare(ge1.jointRewards, ge2.jointRewards, "joint reward")){
			equal = false;
		}
		
		return equal;
	}
	
	/**
	 * Traverses two lists entirely, printing out the different elements
	 * @param l1
	 * @param l2
	 * @param itemName
	 * @return true if the lists have the same items, false otherwhise
	 */
	public static boolean listCompare(List<?> l1, List<?> l2, String itemName){
		boolean equal = true;
		
		for(int i = 0; i < l1.size(); i++){
			try{
				if(! l1.get(i).equals(l1.get(i))){
					System.out.println(String.format(
						"Difference on %dth %s: %s vs %s", (i+1), itemName, l1.get(i), l2.get(i)
					));
					equal = false;
				}
			}
			catch(ArrayIndexOutOfBoundsException e){
				System.out.println("Different sizes of " + itemName);
				equal = false;
			}
		}
		
		return equal;
	}

	public static void main(String[] args) {
		// args: contain the input directory
		
		PathMatcher matcher =
			    FileSystems.getDefault().getPathMatcher("glob:" + Paths.get(args[0], "*.game"));
		
		
		// retrieves the list of files in the given directory
		File[] listOfFiles = new File(args[0]).listFiles();
		
		// compares every pair of files, filtering with PathMatcher
		for(int i = 0; i < listOfFiles.length; i++){
			/*if(! matcher.matches(Paths.get(listOfFiles[i].getName()))){
				continue;
			}*/
			
			// ignores non .game files
			if(! listOfFiles[i].getName().endsWith(".game")){
				continue;
			}
			
			for (int j = i + 1; j < listOfFiles.length; j++){
				/*if(! matcher.matches(Paths.get(listOfFiles[j].getName()))){
					continue;
				}*/
				if(! listOfFiles[j].getName().endsWith(".game")){
					continue;
				}
				
				if(! compare(listOfFiles[i].getName(), listOfFiles[j].getName())){
					System.out.println(String.format(
						"Files %s and %s are different", listOfFiles[i], listOfFiles[j]
					));
				}
			}
		}
		
		System.out.println("Comparison finished.");

	}

}
