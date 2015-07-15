package tests;

import java.util.ArrayList;

import pipeline.AbstractsToCandidates;
import pipeline.CandidatesToFeatures;
import pipeline.ClassUtilities.Candidate;

/**
 * 
 * @author lq4
 *
 */
public class MainTest {

	public static void main(String[] args) throws Exception {

		AbstractsToCandidates a2c = new AbstractsToCandidates("NETMETA",
				"SRSTR", "SRDEF", "input.txt", "COMPACT_MRREL.RRF", true);
		ArrayList<Candidate> candidates = a2c.getCandidates();

		CandidatesToFeatures c2f = new CandidatesToFeatures("", "errorLog.txt",
				"wordDict.txt", "tagDict.txt", "depDict.txt");
		c2f.getSentences(candidates);
		// c2f.writeDemoFeatures();
		c2f.writeFeatures();
		boolean flag = false;
		flag = true;
	}
}