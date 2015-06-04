package tests;

import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Position;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

import java.util.ArrayList;
import java.util.List;

import pipeline.ClassUtilities.PCMImpl2;
import pipeline.ClassUtilities.PhraseImpl2;
import pipeline.ClassUtilities.PositionImpl2;

public class TestMetamap {

	public static void main(String[] args) throws Exception {
		testSplittingEntity();
	}

	private static void testSplittingEntity() throws Exception {
		// example
		String input = "I love you. Analysis of a NirI-deficient mutant strain revealed that NirI is involved in transcription activation of the nir gene cluster in response to oxygen limitation and the presence of N-oxides.";
		int prevPhraseIndex = 0, prevMapIndex = 0, prevEvIndex = 2, prevTypeIndex = 0;
		int succPhraseIndex = 5, succMapIndex = 0, succEvIndex = 2, succTypeIndex = 1;
		int uttIndex = 1;

		MetaMapApi api = new MetaMapApiImpl(0);
		api.setOptions("-y");
		List<Result> resultList = api.processCitationsFromString("-y", input);

		Utterance utt = resultList.get(0).getUtteranceList().get(uttIndex);

		// int phraseStartIndex, phraseLength;
		int uttStartIndex = utt.getPosition().getX();
		List<PCM> PCMList = utt.getPCMList();

		System.out.println("initial:");
		printPCMList(utt.getString(), uttStartIndex, PCMList);

		int prevPhraseStartIndex, prevEvStartIndex, prevEvEndIndex, prevPhraseEndIndex;
		int succPhraseStartIndex, succEvStartIndex, succEvEndIndex, succPhraseEndIndex;

		Position prevPhrasePos = PCMList.get(prevPhraseIndex).getPhrase()
				.getPosition();
		// inclusive
		prevPhraseStartIndex = prevPhrasePos.getX();
		// exclusive
		prevPhraseEndIndex = prevPhraseStartIndex + prevPhrasePos.getY();

		Position succPhrasePos = PCMList.get(succPhraseIndex).getPhrase()
				.getPosition();
		// inclusive
		succPhraseStartIndex = succPhrasePos.getX();
		// exclusive
		succPhraseEndIndex = succPhraseStartIndex + succPhrasePos.getY();

		List<Position> prevPosList = PCMList.get(prevPhraseIndex)
				.getMappingList().get(prevMapIndex).getEvList()
				.get(prevEvIndex).getPositionalInfo();
		// inclusive
		prevEvStartIndex = prevPosList.get(0).getX();
		// exclusive
		prevEvEndIndex = prevPosList.get(prevPosList.size() - 1).getX()
				+ prevPosList.get(prevPosList.size() - 1).getY();

		List<Position> succPosList = PCMList.get(succPhraseIndex)
				.getMappingList().get(succMapIndex).getEvList()
				.get(succEvIndex).getPositionalInfo();
		// inclusive
		succEvStartIndex = succPosList.get(0).getX();
		succEvEndIndex = succPosList.get(succPosList.size() - 1).getX()
				+ succPosList.get(succPosList.size() - 1).getY();

		PCM originalEntity1Phrase, originalEntity2Phrase;
		List<PCM> entity1PCMCollection = new ArrayList<PCM>();
		List<PCM> entity2PCMCollection = new ArrayList<PCM>();

		if (prevPhraseStartIndex != prevEvEndIndex)
			entity1PCMCollection.add(new PCMImpl2(new PhraseImpl2(
					new PositionImpl2(prevPhraseStartIndex, prevEvStartIndex
							- prevPhraseStartIndex))));
		entity1PCMCollection.add(new PCMImpl2(new PhraseImpl2(
				new PositionImpl2(prevEvStartIndex, prevEvEndIndex
						- prevEvStartIndex))));
		if (prevEvEndIndex != prevPhraseEndIndex)
			entity1PCMCollection.add(new PCMImpl2(new PhraseImpl2(
					new PositionImpl2(prevEvEndIndex, prevPhraseEndIndex
							- prevEvEndIndex))));
		PCMList.addAll(prevPhraseIndex + 1, entity1PCMCollection);
		originalEntity1Phrase = PCMList.remove(prevPhraseIndex);

		succPhraseIndex += entity1PCMCollection.size() - 1;
		if (succPhraseStartIndex != succEvEndIndex)
			entity2PCMCollection.add(new PCMImpl2(new PhraseImpl2(
					new PositionImpl2(succPhraseStartIndex, succEvStartIndex
							- succPhraseStartIndex))));
		entity2PCMCollection.add(new PCMImpl2(new PhraseImpl2(
				new PositionImpl2(succEvStartIndex, succEvEndIndex
						- succEvStartIndex))));
		if (succEvEndIndex != succPhraseEndIndex)
			entity2PCMCollection.add(new PCMImpl2(new PhraseImpl2(
					new PositionImpl2(succEvEndIndex, succPhraseEndIndex
							- succEvEndIndex))));
		PCMList.addAll(succPhraseIndex + 1, entity2PCMCollection);
		originalEntity2Phrase = PCMList.remove(succPhraseIndex);

		System.out.println("after splitting:");
		printPCMList(utt.getString(), uttStartIndex, PCMList);

		/**
		 * Restores PCMList in case next candidate uses the same candidate and
		 * PCMList.
		 */
		
		// order is very important, do not change
		PCMList.add(succPhraseIndex, originalEntity2Phrase);
		PCMList.add(prevPhraseIndex, originalEntity1Phrase);		
		
		PCMList.removeAll(entity1PCMCollection);
		PCMList.removeAll(entity2PCMCollection);

		System.out.println("after restoration:");
		printPCMList(utt.getString(), uttStartIndex, PCMList);
	}

	private static void printPCMList(String uttText, int uttStartIndex,
			List<PCM> PCMList) throws Exception {
		String str = "";
		int s, l;
		for (PCM pcm : PCMList) {
			s = pcm.getPhrase().getPosition().getX() - uttStartIndex;
			l = pcm.getPhrase().getPosition().getY();
			str += uttText.substring(s, s + l) + "/";
		}
		System.out.println(str.substring(0, str.length() - 1));
	}

}
