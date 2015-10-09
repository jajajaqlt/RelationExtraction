package wrapper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import edu.stanford.nlp.util.HashIndex;
import edu.stanford.nlp.util.Pair;

public class Wrapper {

	public static String inputFile = "output_ex_151007_test.txt";

	public static void main(String[] args) throws FileNotFoundException {
		// loads file into a buffered reader
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String line;
		// separates name and value in the basic info of a instance
		int sLvFeatsCount, chunksCount, phrasesCount, wordsCount;
		Instance instance;
		HashIndex<String> sLvConjIndex = new HashIndex<String>();
		HashIndex<String> cLvConjIndex = new HashIndex<String>();
		HashIndex<String> pLvConjIndex = new HashIndex<String>();
		HashIndex<String> wLvConjIndex = new HashIndex<String>();
		// ArrayList<Integer> featIndices;
		Pair<Integer, Integer> pair;

		// boolean insideInstance = false;
		try {
			while ((line = br.readLine()) != null) {
				// if (!insideInstance) {
				if (line.substring(0, 10).equals("<instance>")) {
					try {
						// header
						instance = readBasicInfoAndInitializeInstance(br);

						// sentence-level features
						readSentenceLevelFeatures(br, instance, sLvConjIndex);

						// chunks
						pair = readFeatureBlock(br, cLvConjIndex,
								"<chunk-level-features>", instance.chunksCnt,
								instance, instance.cLvConjFeats,
								instance.cLvBOWFeats);
						instance.cLvNEIndex1 = pair.first;
						instance.cLvNEIndex2 = pair.second;

						// phrases
						pair = readFeatureBlock(br, pLvConjIndex,
								"<phrase-level-features>", instance.phrasesCnt,
								instance, instance.pLvConjFeats,
								instance.pLvBOWFeats);
						instance.pLvNEIndex1 = pair.first;
						instance.pLvNEIndex2 = pair.second;

						// words
						pair = readFeatureBlock(br, wLvConjIndex,
								"<word-level-features>", instance.wordsCnt,
								instance, instance.wLvConjFeats,
								instance.wLvBOWFeats);
						instance.wLvNEIndex1 = pair.first;
						instance.wLvNEIndex2 = pair.second;

						boolean breakPoint = true;
					} catch (NotExpectedStringException e) {
						System.out.println(e.errorMessage);
						// System.out.println("Looking for a new instance.");
					}
				}

				// }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	static void readSentenceLevelFeatures(BufferedReader charStream,
			Instance instance, HashIndex<String> featIndex) throws IOException,
			NotExpectedStringException {
		String line;
		int index;
		// <sentence-level-features>
		line = charStream.readLine();
		if (!line.equals("<sentence-level-features>"))
			throw new NotExpectedStringException(
					"Expects <sentence-level-features> but getting " + line
							+ ". Looking for a new instance.");
		// sentence-level conjunctive features
		instance.sLvConjFeats = new int[instance.sLvFeatsCnt];
		for (int i = 0; i < instance.sLvFeatsCnt; i++) {
			line = charStream.readLine();
			index = featIndex.indexOf(line, true);
			instance.sLvConjFeats[i] = index;
		}
		// skips "bag-of-words-features"
		charStream.readLine();
		// skips "bow-word-feature"
		charStream.readLine();
		instance.sLvWords = charStream.readLine().split(" ");
		// skips "bow-tag-feature"
		charStream.readLine();
		instance.sLvTags = strList2Array(charStream.readLine());
		// skips "bow-dep-feature"
		charStream.readLine();
		instance.sLvDeps = strList2Array(charStream.readLine());
		// skips "bow-dep-feature"
		charStream.readLine();
		instance.sLvDepWords = charStream.readLine().split(" ");
		// skips "</sentence-level-features>"
		charStream.readLine();
	}

	static Instance readBasicInfoAndInitializeInstance(BufferedReader charStream)
			throws IOException {
		Instance instance = new Instance();

		String separator = ": ";
		String[] tokens;
		// index
		tokens = charStream.readLine().split(separator);
		instance.index = Integer.parseInt(tokens[1]);
		// cui1
		tokens = charStream.readLine().split(separator);
		instance.cui1 = tokens[1];
		// cui1-type
		tokens = charStream.readLine().split(separator);
		instance.cui1_type = tokens[1];
		// cui1-matched_words
		tokens = charStream.readLine().split(separator);
		instance.cui1_matched_words = tokens[1];
		// cui1-global-phrase-index
		tokens = charStream.readLine().split(separator);
		instance.cui1_index = Integer.parseInt(tokens[1]);
		// cui2
		tokens = charStream.readLine().split(separator);
		instance.cui2 = tokens[1];
		// cui2-type
		tokens = charStream.readLine().split(separator);
		instance.cui2_type = tokens[1];
		// cui2-matched-words
		tokens = charStream.readLine().split(separator);
		instance.cui2_matched_words = tokens[1];
		// cui2-global-phrase-index
		tokens = charStream.readLine().split(separator);
		instance.cui2_index = Integer.parseInt(tokens[1]);
		// positivity
		tokens = charStream.readLine().split(separator);
		instance.positivity = Boolean.parseBoolean(tokens[1]);
		// inverse
		tokens = charStream.readLine().split(separator);
		instance.inverse = Boolean.parseBoolean(tokens[1]);
		// net-relation
		tokens = charStream.readLine().split(separator);
		instance.net_relation = tokens[1];
		// meta-relation
		tokens = charStream.readLine().split(separator);
		instance.meta_relation = tokens[1];
		// number-of-sentence-level-features
		tokens = charStream.readLine().split(separator);
		instance.sLvFeatsCnt = Integer.parseInt(tokens[1]);
		// number-of-chunks
		tokens = charStream.readLine().split(separator);
		instance.chunksCnt = Integer.parseInt(tokens[1]);
		// number-of-phrases
		tokens = charStream.readLine().split(separator);
		instance.phrasesCnt = Integer.parseInt(tokens[1]);
		// number-of-words
		tokens = charStream.readLine().split(separator);
		instance.wordsCnt = Integer.parseInt(tokens[1]);
		// sentence
		tokens = charStream.readLine().split(separator);
		instance.sentence = tokens[1];

		instance.cLvConjFeats = new int[instance.chunksCnt * 2];
		instance.cLvBOWFeats = new int[instance.chunksCnt * 2][];

		instance.pLvConjFeats = new int[instance.phrasesCnt * 2];
		instance.pLvBOWFeats = new int[instance.phrasesCnt * 2][];

		instance.wLvConjFeats = new int[instance.wordsCnt * 2];
		instance.wLvBOWFeats = new int[instance.wordsCnt * 2][];

		return instance;
	}

	static Pair<Integer, Integer> readFeatureBlock(BufferedReader charStream,
			HashIndex<String> featIndex, String elementName, int length,
			Instance instance, int[] conjFeats, int[][] bowFeats)
			throws NotExpectedStringException {
		Pair<Integer, Integer> pair = new Pair<Integer, Integer>();
		String line, substring;
		String[] tokens;
		String ne1Type = instance.cui1_type, ne2Type = instance.cui2_type;
		int index;
		ArrayList<String> substrings = new ArrayList<String>();

		try {
			line = charStream.readLine();
			// elementName is the first line
			if (!line.equals(elementName))
				throw new NotExpectedStringException("Expects " + elementName
						+ " but getting " + line
						+ ". Looking for a new instance.");

			// skips "word-features:"
			charStream.readLine();
			for (int i = 0; i < length; i++) {
				line = charStream.readLine();
				index = featIndex.indexOf(line,true);
				conjFeats[i] = index;
				tokens = line.split("\\|");
				substring = tokens[1];
				substrings.add(substring);
				// no bow feature yet
			}

			pair.first = substrings.indexOf(ne1Type);
			pair.second = substrings.lastIndexOf(ne2Type);

			// skips "tag-features: "
			charStream.readLine();
			for (int i = 0; i < length; i++) {
				line = charStream.readLine();
				index = featIndex.indexOf(line,true);
				line = charStream.readLine();
				if (i != pair.first && i != pair.second) {
					tokens = line.split(": ");
					bowFeats[length + i] = strList2Array(tokens[1]);
				}

			}

			// skips "</...>"
			charStream.readLine();
		} catch (IOException e) {
			throw new NotExpectedStringException(
					"IOExpection catched for feature block " + elementName);
		}

		return pair;
	}

	static class Instance {
		int index, cui1_index, cui2_index, sLvFeatsCnt, chunksCnt, phrasesCnt,
				wordsCnt;
		String cui1, cui1_type, cui1_matched_words, cui2, cui2_type,
				cui2_matched_words, net_relation, meta_relation, sentence;
		boolean inverse, positivity;
		int[] sLvConjFeats, cLvConjFeats, pLvConjFeats, wLvConjFeats;
		int[][] cLvBOWFeats, pLvBOWFeats, wLvBOWFeats;
		int cLvNEIndex1, cLvNEIndex2, pLvNEIndex1, pLvNEIndex2, wLvNEIndex1,
				wLvNEIndex2;
		String[] sLvWords, sLvDepWords;
		int[] sLvTags, sLvDeps;
	}

	// used for all exceptions in this class
	static class NotExpectedStringException extends Exception {
		String errorMessage;

		NotExpectedStringException(String errorMessage) {
			this.errorMessage = errorMessage;
		}
	}

	static int[] intList2Array(ArrayList<Integer> integers) {
		int[] ints = new int[integers.size()];
		for (int i = 0; i < ints.length; i++) {
			ints[i] = integers.get(0);
		}
		return ints;
	}

	static int[] strList2Array(String strList) {
		String[] nums = strList.split(" ");
		int[] ints = new int[nums.length];
		for (int i = 0; i < nums.length; i++) {
			ints[i] = Integer.parseInt(nums[i]);
		}
		return ints;
	}
}
