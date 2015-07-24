package tests;

import pipeline.NewPipeline;

public class MainTestThruNewPipeline {
	public static void main(String[] args) throws Exception {
		NewPipeline.run("NETMETA", "SRSTR", "SRDEF", "COMPACT_MRREL.RRF",
				"input.txt", "output.txt", "errorLog.txt", "wordDict.txt",
				"tagDict.txt", "depDict.txt", false);
	}
}
