package tests;

import java.util.ArrayList;

import pipeline.AbstractsToCandidates;
import pipeline.CandidatesToFeatures;
import pipeline.ClassUtilities.Candidate;
import pipeline.Pipeline;

public class MainTestThruLib {

	public static void main(String[] args) throws Exception {
		// AbstractsToCandidates a2c = new AbstractsToCandidates("NETMETA",
		// "SRSTR", "SRDEF", "input.txt", "COMPACT_MRREL.RRF");
		// ArrayList<Candidate> candidates = a2c.getCandidates();
		// CandidatesToFeatures c2f = new CandidatesToFeatures("");
		// c2f.getSentences(candidates);
		// //c2f.writeDemoFeatures();
		// c2f.writeFeatures();
		Pipeline.run("NETMETA", "SRSTR", "SRDEF", "COMPACT_MRREL.RRF",
				"input.txt", "", "wordDict.txt", "tagDict.txt", "depDict.txt");
	}

}
