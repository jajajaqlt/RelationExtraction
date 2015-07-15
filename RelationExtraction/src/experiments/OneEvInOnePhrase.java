package experiments;

import gov.nih.nlm.nls.metamap.PCM; 

import java.util.ArrayList;

import pipeline.AbstractsToCandidates;
import pipeline.ClassUtilities.Candidate;
import pipeline.ClassUtilities.PreCandidate;

public class OneEvInOnePhrase {

	public static void main(String[] args) throws Exception {

		// System.out.println(System.currentTimeMillis());
		AbstractsToCandidates a2c = new AbstractsToCandidates("NETMETA",
				"SRSTR", "SRDEF", "sample_abstracts.txt", "COMPACT_MRREL.RRF",false);
		// System.out.println(System.currentTimeMillis());
		ArrayList<Candidate> candidates = a2c.getCandidates();
		PreCandidate prev, succ;
		PCM pcm;
		int prevCount = 0, succCount = 0;
		int size = candidates.size();
		for (Candidate candid : candidates) {
			prev = candid.prev;
			pcm = candid.utterance.getPCMList().get(prev.phraseIndex);
			if (pcm.getMappingList().get(0).getEvList().size() != 1) {
				prevCount++;
				System.out.println("Prev: " + prevCount + "/" + size);
				System.out.println(candid.utterance.getString());
				System.out.println(pcm.getPhrase().getPhraseText());
			}
			succ = candid.succ;
			pcm = candid.utterance.getPCMList().get(succ.phraseIndex);
			if (pcm.getMappingList().get(0).getEvList().size() != 1) {
				succCount++;
				System.out.println("Succ: " + succCount + "/" + size);
				System.out.println(candid.utterance.getString());
				System.out.println(pcm.getPhrase().getPhraseText());
			}

		}
		boolean flag = false;
	}

}
