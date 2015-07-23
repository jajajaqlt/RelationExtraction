package tests;

import info.olteanu.interfaces.StringFilter;
import info.olteanu.utils.TextNormalizer;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;

public class ArbitraryTest {
	public static void main(String[] args) throws Exception {
		// ArrayList<Integer> ints = new ArrayList<Integer>();
		// ints.add(1);
		// ints.add(2);
		// System.out.println(ints.contains(1));
		// System.out.println(ints.contains(0));

		// String line = "süme string";
		// boolean isAscii = CharMatcher.ASCII.matchesAllOf(line);
		// System.out.println("Is the sentence in ascii?");
		// System.out.println(isAscii);
		// System.out.println("Converted:");
		// String normalizedText = normalizeString(line);
		// System.out.println(normalizedText);
		// System.out.println("Question mark replaced:");
		// String asciiText2 = filterNonAscii(line);
		// System.out.println(asciiText2);
		// String asciiText = filterNonAscii(normalizedText);
		// Method m[] = Dijkstra.class.getDeclaredMethods();
		// for (int i = 0; i < m.length; i++)
		// {
		// System.out.println(m[i].toString());
		// }
		// String a = "ABC";
		// String b = a;
		// System.out.println(a);
		// System.out.println(b);
		// String word = "word";
		// ArrayList<String> words = new ArrayList<String>();
		// words.add(word);
		// words.add(word);
		// words.add(word);
		// for (String word1 : words) {
		//
		// }
		// String word1 = "word1";
		// String str = "//";
		// System.out.println(str.length());
		// List<Result> results = ConvertMetamapParsingResultsToJson
		// .readJson("output.json");
		// ConvertMetamapParsingResultsToJson.writeResults(results,
		// "output.txt");
		// System.out.println(Time.getCurrentTime());
		// Number num = new Number();
		// num.num = 10;
		// Number num2 = new Number();
		// System.out.println(num2.num);
		// BufferedWriter log = new BufferedWriter(new OutputStreamWriter(
		// System.out));
		// for (int i = 0; i < 5; i++) {
		// log.write("This is " + i + "!\n");
		// log.flush();
		// }

		LexicalizedParser lp;
		TokenizerFactory<CoreLabel> tokenizerFactory;
		GrammaticalStructureFactory gsf;

		lp = LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		tokenizerFactory = PTBTokenizer
				.factory(new CoreLabelTokenFactory(), "");
		gsf = new PennTreebankLanguagePack().grammaticalStructureFactory();

		String uttText;
		Tokenizer<CoreLabel> tok;
		List<CoreLabel> rawWords = null;
		Tree parse;
		List<CoreLabel> taggedLabels = null;
		GrammaticalStructure gs;
		Collection<TypedDependency> tdl = null;

		uttText = "The obstructed area may be bypassed by attaching the vas to any part of the epididymis on the testicular side of the obstruction or to the vasa efferentia.";
		tok = tokenizerFactory.getTokenizer(new StringReader(uttText));
		rawWords = tok.tokenize();
		parse = lp.apply(rawWords);
		taggedLabels = parse.taggedLabeledYield();
		gs = gsf.newGrammaticalStructure(parse);
		// TypedDependency: gov() dep() reln()
		// tdl = gs.typedDependenciesCCprocessed();
		tdl = gs.typedDependencies();
		// no index information inside taggedLabels

		// log.flush();
		// log.close();
	}

	public static class Number {
		public static int num;
	}

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
}
