package wrapper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import edu.stanford.nlp.util.HashIndex;

public class Wrapper {

	public static String inputFile = "wrapper_input.txt";

	public static void main(String[] args) throws FileNotFoundException {
		// loads file into a buffered reader
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String line;
		String[] tokens;
		// separates name and value in the basic info of a instance
		String separator = ": ";
		Instance instance;
		HashIndex<String> sLvConjIndex = new HashIndex<String>();
		int index;
		ArrayList<Integer> featIndices;

		// boolean insideInstance = false;
		try {
			while ((line = br.readLine()) != null) {
				// if (!insideInstance) {
				if (line.substring(0, 10).equals("<instance>")) {
					try {
						// insideInstance = true;
						instance = new Instance();
						// index
						tokens = br.readLine().split(separator);
						instance.index = Integer.parseInt(tokens[1]);

						// cui1
						tokens = br.readLine().split(separator);
						instance.cui1 = tokens[1];
						// cui1-type
						tokens = br.readLine().split(separator);
						instance.cui1_type = tokens[1];
						// cui1_matched_words
						tokens = br.readLine().split(separator);
						instance.cui1_matched_words = tokens[1];
						// cui1_index
						tokens = br.readLine().split(separator);
						instance.cui1_index = Integer.parseInt(tokens[1]);

						// cui2
						tokens = br.readLine().split(separator);
						instance.cui2 = tokens[1];
						// cui2-type
						tokens = br.readLine().split(separator);
						instance.cui2_type = tokens[1];
						// cui2_matched_words
						tokens = br.readLine().split(separator);
						instance.cui2_matched_words = tokens[1];
						// cui2_index
						tokens = br.readLine().split(separator);
						instance.cui2_index = Integer.parseInt(tokens[1]);

						// positivity
						tokens = br.readLine().split(separator);
						instance.positivity = Boolean.parseBoolean(tokens[1]);
						// inverse
						tokens = br.readLine().split(separator);
						instance.inverse = Boolean.parseBoolean(tokens[1]);

						// net-relation
						tokens = br.readLine().split(separator);
						instance.net_relation = tokens[1];
						// meta-relation
						tokens = br.readLine().split(separator);
						instance.meta_relation = tokens[1];

						// sentence
						tokens = br.readLine().split(separator);
						instance.sentence = tokens[1];

						// <sentence-level-features>
						line = br.readLine();
						if (!line.equals("<sentence-level-features>"))
							throw new NotExpectedStringException(
									"Expects <sentence-level-features> but getting "
											+ line);
						// sentence-level conjunctive features
						featIndices = new ArrayList<Integer>();
						while (!(line = br.readLine())
								.equals("bag-of-words-features:")) {
							index = sLvConjIndex.indexOf(line);
							featIndices.add(index);
						}
						instance.sLvConjFeats = intList2Array(featIndices);

						// bow-word-feature
						// skips name
						br.readLine();
						instance.sLvWords = br.readLine().split(" ");

						// bow-tag-feature
						// skips name
						br.readLine();
						instance.sLvTags = strList2Array(br.readLine());

						// bow-dep-feature
						// skips name
						br.readLine();
						instance.sLvDeps = strList2Array(br.readLine());

						// bow-dep-feature
						// skips name
						br.readLine();
						instance.sLvDepWords = br.readLine().split(" ");

						// </sentence-level-features>
						// skips it
						br.readLine();
						
						// chunk-level features
						
						
					} catch (NotExpectedStringException e) {
						System.out.println(e.errorMessage);
						System.out.println("Looking for a new instance.");
					}

				}

				// }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	static class Instance {
		int index, cui1_index, cui2_index;
		String cui1, cui1_type, cui1_matched_words, cui2, cui2_type,
				cui2_matched_words, net_relation, meta_relation, sentence;
		boolean inverse, positivity;
		int[] sLvConjFeats;
		String[] sLvWords, sLvDepWords;
		int[] sLvTags, sLvDeps;
	}

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
