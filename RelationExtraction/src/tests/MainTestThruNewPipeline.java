package tests;

import pipeline.NewPipeline;

public class MainTestThruNewPipeline {
	public static void main(String[] args) throws Exception {
		NewPipeline.run("NETMETA", "SRSTR", "SRDEF", "COMPACT_MRREL.RRF",
				"input3.txt", "output1.txt", "errorLog.txt", "wordDict.txt",
				"tagDict.txt", "depDict.txt", false, true, 11, 1);
	}
}
