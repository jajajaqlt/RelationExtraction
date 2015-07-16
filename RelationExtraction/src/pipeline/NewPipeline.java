package pipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;

import pipeline.ClassUtilities.Candidate;
import tools.Time;

public class NewPipeline {
	public static void run(String relationMappingFile,
			String semanticNetworkFile, String semanticTypeAbbreviationFile,
			String metaRelationsFile, String abstractsFile, String outputFile,
			String errorLogFile, String wordDictFile, String tagDictFile,
			String depTypeDictFile, boolean isReadingFromJsonFile)
			throws Exception {

		NewAbstractsToCandidates a2c = new NewAbstractsToCandidates(
				relationMappingFile, semanticNetworkFile,
				semanticTypeAbbreviationFile, metaRelationsFile,
				isReadingFromJsonFile);
		NewCandidatesToFeatures c2f = new NewCandidatesToFeatures(outputFile,
				wordDictFile, tagDictFile, depTypeDictFile);
		File errorLog = new File(errorLogFile);
		PrintStream ps = new PrintStream(errorLog);
		// e.printStackTrace(ps);
		// ps.println("Error instance is:");
		BufferedReader br = new BufferedReader(new FileReader(abstractsFile));
		ArrayList<Candidate> candidates;
		String line;
		int index = 0;
		while ((line = br.readLine()) != null) {
			try {
				index++;
				// System.out.println("Starts processing #" + index
				// + ". Time is: " + Time.getCurrentTime());
				candidates = a2c.getCandidates(line);
				c2f.getSentences(candidates);
				c2f.writeFeatures(index);
				// System.out.println("Finishes processing #" + index
				// + ". Time is: " + Time.getCurrentTime());
			} catch (Exception e) {
				// System.out.println("Unable to finishes processing #" + index
				// + ". Time is: " + Time.getCurrentTime());
				e.printStackTrace(ps);
				ps.println("Error instance is: #" + index + " " + line);
			}

		}
		br.close();
		ps.close();
	}

}
