package experiments;

import edu.stanford.nlp.ling.CoreAnnotations.NormalizedNamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;

//TextNormalizer code from phramer.org
//Allows compilation under both Java 5 and Java 6
import info.olteanu.utils.*;
import info.olteanu.interfaces.StringFilter;
import demos.TrialDemo;

/**
 * This class is used to check whether every phrase token given back by Metamap
 * consists of only word tokens given back by Stanford Parser.
 * 
 * @author lq4
 *
 */
public class TokenizationDiscrepancy {
	public static String inputFileName = "input.txt";

	/**
	 *
	 * @param text
	 * @return ASCII encoding of text with non ASCII characters replaced by ?
	 * @throws UnsupportedEncodingException
	 */
	public static String filterNonAscii(String text) throws Exception {
		String aText;
		byte[] b = text.getBytes("US-ASCII");
		aText = new String(b, "US-ASCII");
		return aText;
	}

	/**
	 *
	 * @param str
	 * @return Normalized version of str with accented characters replaced by
	 *         unaccented version and with diacritics removed. E.g. Ö -> O
	 */
	public static String normalizeString(String str)
			throws ClassNotFoundException {
		// TextNormalizer code from phramer.org
		// Allows compilation under both Java 5 and Java 6
		StringFilter stringFilter = TextNormalizer
				.getNormalizationStringFilter();
		String nfdNormalizedString = stringFilter.filter(str);

		// Normalizer is Java 6 only
		// String nfdNormalizedString = java.text.Normalizer.normalize(str,
		// Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}

	public static void main(String[] args) throws Exception {
		// Metamap environment
		MetaMapApi api = new MetaMapApiImpl(0);
		api.setOptions("-y");
		// Metamap usage
		// List<Result> resultList = api.processCitationsFromString(input);

		// Stanford Parser environment
		LexicalizedParser lp = LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
				new CoreLabelTokenFactory(), "");
		// Stanford Parser usage
		// Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new
		// StringReader(sent2));
		// List<CoreLabel> rawWords = tok.tokenize();

		BufferedReader br = new BufferedReader(new FileReader(inputFileName));
		String line, sentence;
		int uttStartIndex;
		// all exclusive
		int[] phraseEndingIndices;
		int phraseEndingIndicesCursor;
		// ArrayList<Phrase> phrases;
		// Phrase phrase;
		int phraseStartIndex, phraseLength;
		List<Result> resultList;
		Result result;
		List<PCM> PCMList;
		Tokenizer<CoreLabel> tok;
		List<CoreLabel> rawWords;
		// inclusive
		int wordStartIndex;
		// exclusive
		int wordEndIndex;
		CoreLabel label;
		int discrepencyCount = 0;
		int sentenceCount = 0;

		// every line is an abstract, consisting of one or more sentences
		while ((line = br.readLine()) != null) {
			// After reviewing Bj枚rnsson's classification of drug action and the
			// notion of contributory causality, this commentary defines an
			// ideal drug from the perspectives of pharmacodynamics,
			// pharmacokinetics, and therapeutics.
			// String normalizedText = normalizeString(line);
			// String asciiText2 = filterNonAscii(line);
			// String asciiText = filterNonAscii(normalizedText);

			boolean isAscii = CharMatcher.ASCII.matchesAllOf(line);
			if (isAscii) {
				resultList = api.processCitationsFromString(line);
				// one abstract
				result = resultList.get(0);

				// Metamap utterance indexing
				// (start-0-based-index, length(separating whitespace
				// inclusive)):=
				// (0, 223) (223, 231)
				// each phrase starts and ends (both inclusively) with
				// non-whitespace characters
				// phrase with no matching also has position info
				// ending period goes with the last phrase

				for (Utterance utt : result.getUtteranceList()) {
					// utt: one sentence
					sentenceCount++;
					System.out.println("This is sentence #" + sentenceCount);
					sentence = utt.getString();
					uttStartIndex = utt.getPosition().getX();
					PCMList = utt.getPCMList();
					phraseEndingIndices = new int[PCMList.size()];
					for (int i = 0; i < PCMList.size(); i++) {
						phraseStartIndex = PCMList.get(i).getPhrase()
								.getPosition().getX();
						phraseLength = PCMList.get(i).getPhrase().getPosition()
								.getY();
						phraseEndingIndices[i] = phraseStartIndex
								- uttStartIndex + phraseLength;
					}

					// Stanford Parser Indexing
					// every word token has an Id, 1-based
					// symbols are not on the dependency path
					// tagger will assign 0-based indices to tokens
					// dependency parser will assign 1-based indices to tokens

					tok = tokenizerFactory.getTokenizer(new StringReader(
							sentence));
					rawWords = tok.tokenize();
					tok = tokenizerFactory.getTokenizer(new StringReader(
							sentence));
					rawWords = tok.tokenize();
					phraseEndingIndicesCursor = 0;
					for (int i = 0; i < rawWords.size(); i++) {
						label = rawWords.get(i);
						wordStartIndex = label.beginPosition();
						wordEndIndex = label.endPosition();
						while (wordStartIndex >= phraseEndingIndices[phraseEndingIndicesCursor]) {
							phraseEndingIndicesCursor++;
						}
						if (wordEndIndex > phraseEndingIndices[phraseEndingIndicesCursor]) {
							discrepencyCount++;
							System.out
									.println("There is discrepency between both tokenization methods!");
							System.out.println(sentence);
							System.out.println("Error label: " + label.word());
							System.out.println("Error percentage: "
									+ discrepencyCount + "/" + sentenceCount);
							String phraseTokenization = "";
							for (PCM pcm : utt.getPCMList()) {
								phraseTokenization += pcm.getPhrase()
										.getPhraseText() + "/";
							}
							System.out.println(phraseTokenization.substring(0,
									phraseTokenization.length() - 1));
							String sentenceTokenized = "";
							for (CoreLabel label2 : rawWords) {
								// System.out.println(label.word());
								// System.out.println(label.lemma());
								// System.out.println(label.tag());
								// System.out.println(label.beginPosition());
								// System.out.println(label.endPosition());
								// System.out.println(label.ner());
								// System.out.println();
								sentenceTokenized += label2.word() + "/";
							}
							System.out.println(sentenceTokenized.substring(0,
									sentenceTokenized.length() - 1));
							// break;
						}

					}

				}
			}

		}

		br.close();
	}
}
