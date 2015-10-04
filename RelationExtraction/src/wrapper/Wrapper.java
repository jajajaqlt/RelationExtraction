package wrapper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Wrapper {

	public static String inputFile = "wrapper_input.txt";

	public static void main(String[] args) throws FileNotFoundException {
		// loads file into a buffered reader
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		
	}

}
