package pipeline;

import java.util.ArrayList;

import pipeline.ClassUtilities.Candidate;

public class Pipeline {
	public static void run(String relationMappingFile,
			String semanticNetworkFile, String semanticTypeAbbreviationFile,
			String metaRelationsFile, String abstractsFile, String outputFile,
			String errorLogFile, String wordDictFile, String tagDictFile,
			String depTypeDictFile, boolean isReadingFromJsonFile)
			throws Exception {

		AbstractsToCandidates a2c = new AbstractsToCandidates(
				relationMappingFile, semanticNetworkFile,
				semanticTypeAbbreviationFile, abstractsFile, metaRelationsFile,
				isReadingFromJsonFile);
		ArrayList<Candidate> candidates = a2c.getCandidates();
		// uses wordDictFile, tagDictFile and depTypeDictFile here later
		CandidatesToFeatures c2f = new CandidatesToFeatures(outputFile,
				errorLogFile, wordDictFile, tagDictFile, depTypeDictFile);
		c2f.getSentences(candidates);
		c2f.writeFeatures();

	}
}
