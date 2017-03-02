package A4.resources;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class ListOfServers {
	private static ArrayList<String> serversList = new ArrayList<String>();

	public static ArrayList<String> getList(){
		return serversList;
	}
	
	public static void initializeNodes(String pathname){
		try{
			BufferedReader br = new BufferedReader(new FileReader(pathname));
		    for(String line; (line = br.readLine()) != null; ) {
		    	serversList.add(line);
		    }
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
