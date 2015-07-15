package experiments;

import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Position;

import java.util.ArrayList;
import java.util.List;

import pipeline.AbstractsToCandidates;
import pipeline.ClassUtilities.Candidate;
import pipeline.ClassUtilities.PreCandidate;

public class OneEvInOnePhraseDetailed {

	public static void main(String[] args) throws Exception {

		// System.out.println(System.currentTimeMillis());
		// AbstractsToCandidates a2c = new AbstractsToCandidates("NETMETA",
		// "SRSTR", "SRDEF", "sample_abstracts.txt", "COMPACT_MRREL.RRF");
		AbstractsToCandidates a2c = new AbstractsToCandidates("NETMETA",
				"SRSTR", "SRDEF", "input.txt", "COMPACT_MRREL.RRF", false);
		// System.out.println(System.currentTimeMillis());
		ArrayList<Candidate> candidates = a2c.getCandidates();
		PreCandidate prev, succ;
		PCM pcm;
		int prevCount = 0, succCount = 0;
		int size = candidates.size();
		String utt;
		int uttStartIndex;
		for (Candidate candid : candidates) {
			utt = candid.utterance.getString();
			uttStartIndex = candid.utterance.getPosition().getX();

			// public class PreCandidate {
			// public String cui;
			// // rootAbbr
			// public String rootSType;
			// public String sType;
			// public List<Position> pos;
			// public int phraseIndex;
			// public int mappingIndex;
			// public int evIndex;
			// public int sTypeIndex;
			// }
			//

			prev = candid.prev;
			pcm = candid.utterance.getPCMList().get(prev.phraseIndex);
			if (pcm.getMappingList().get(0).getEvList().size() != 1) {
				prevCount++;
				System.out.println("Prev: " + prevCount + "/" + size);
				System.out.println(candid.utterance.getString());
				System.out.println(pcm.getPhrase().getPhraseText());
				// prints out the mapped text
				for (Position pos : prev.pos) {
					System.out.println(pos);
					System.out.println(utt.substring(
							pos.getX() - uttStartIndex, pos.getX()
									- uttStartIndex + pos.getY()));
				}
				System.out.println("" + prev.phraseIndex + " "
						+ prev.mappingIndex + " " + prev.evIndex + " "
						+ prev.sTypeIndex);
				System.out.println(prev.cui + " " + prev.sType + " "
						+ prev.rootSType);

			}
			succ = candid.succ;
			pcm = candid.utterance.getPCMList().get(succ.phraseIndex);
			if (pcm.getMappingList().get(0).getEvList().size() != 1) {
				succCount++;
				System.out.println("Succ: " + succCount + "/" + size);
				System.out.println(candid.utterance.getString());
				System.out.println(pcm.getPhrase().getPhraseText());
				// prints out the mapped text
				for (Position pos : succ.pos) {
					System.out.println(pos);
					System.out.println(utt.substring(
							pos.getX() - uttStartIndex, pos.getX()
									- uttStartIndex + pos.getY()));
				}
				System.out.println("" + succ.phraseIndex + " "
						+ succ.mappingIndex + " " + succ.evIndex + " "
						+ succ.sTypeIndex);
				System.out.println(succ.cui + " " + succ.sType + " "
						+ succ.rootSType);
			}

		}
		boolean flag = false;
	}

}
