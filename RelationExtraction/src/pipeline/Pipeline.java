package pipeline;

import java.util.ArrayList;

import pipeline.ClassUtilities.Candidate;

public class Pipeline {
	public static void run(String relationMappingFile,
			String semanticNetworkFile, String semanticTypeAbbreviationFile,
			String metaRelationsFile, String abstractsFile, String outputFile,
			String wordDictFile, String tagDictFile, String depTypeDictFile)
			throws Exception {

		AbstractsToCandidates a2c = new AbstractsToCandidates(
				relationMappingFile, semanticNetworkFile,
				semanticTypeAbbreviationFile, abstractsFile, metaRelationsFile);
		ArrayList<Candidate> candidates = a2c.getCandidates();
		// uses wordDictFile, tagDictFile and depTypeDictFile here later
		CandidatesToFeatures c2f = new CandidatesToFeatures(outputFile);
		c2f.getSentences(candidates);
		c2f.writeFeatures();

	}
}
