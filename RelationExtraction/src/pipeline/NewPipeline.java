package pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;

import pipeline.ClassUtilities.Candidate;

public class NewPipeline {
	public static void run(String relationMappingFile,
			String semanticNetworkFile, String semanticTypeAbbreviationFile,
			String metaRelationsFile, String abstractsFile, String outputFile,
			String errorLogFile, String wordDictFile, String tagDictFile,
			String depTypeDictFile, boolean isReadingFromJsonFile,
			boolean filterLongPath, int wordSeqThres, int depSeqThres)
			throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(abstractsFile));
		BufferedWriter bw;
		if (outputFile.equals(""))
			bw = new BufferedWriter(new OutputStreamWriter(System.out));
		else
			bw = new BufferedWriter(new FileWriter(outputFile));
		File errorLog = new File(errorLogFile);
		PrintStream ps = new PrintStream(errorLog);

		NewAbstractsToCandidates a2c = new NewAbstractsToCandidates(
				relationMappingFile, semanticNetworkFile,
				semanticTypeAbbreviationFile, metaRelationsFile,
				isReadingFromJsonFile, bw);
		NewCandidatesToFeatures c2f = new NewCandidatesToFeatures(bw,
				wordDictFile, tagDictFile, depTypeDictFile);

		ArrayList<Candidate> candidates;
		String line;
		int index = 0;

		while ((line = br.readLine()) != null) {
			try {
				bw.write("<abstract>");
				bw.newLine();
				bw.flush();
				index++;
				// System.out.println("Starts processing #" + index
				// + ". Time is: " + Time.getCurrentTime());
				candidates = a2c.getCandidates(line);
				c2f.getSentences(candidates);
				c2f.writeFeatures(index, filterLongPath, wordSeqThres,
						depSeqThres);
				// System.out.println("Finishes processing #" + index
				// + ". Time is: " + Time.getCurrentTime());
				bw.write("</abstract>");
				bw.newLine();
				bw.flush();
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
