package tests;

import java.io.StringReader;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;

public class TestNLPCoreLabelTaggedWord {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		getPOSFeatures();
	}

	private static ArrayList<String> getPOSFeatures() throws Exception {
		LexicalizedParser lp = LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
				new CoreLabelTokenFactory(), "");
		String utt = "Quinapril hydrochloride may treat heart failure.";
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(utt));
		// [Quinapril, hydrochloride, may, treat, heart, failure, .]
		List<CoreLabel> rawWords2 = tok.tokenize();
		// (ROOT (S (NP (JJ Quinapril) (NN hydrochloride)) (VP (MD may) (VP (VB
		// treat) (NP (NN heart) (NN failure)))) (. .)))
		Tree parse = lp.apply(rawWords2);
		// [Quinapril/JJ, hydrochloride/NN, may/MD, treat/VB, heart/NN,
		// failure/NN, ./.]
		// http://en.wikipedia.org/wiki/Parse_tree
		ArrayList<TaggedWord> parsedSent = parse.taggedYield();
//		List<CoreLabel> parsedSent = parse.taggedLabeledYield();
		ArrayList<String> tags = new ArrayList<String>();
		// All words tagged
		// System.out.print("POS tags: ");
		CoreLabel label;
		TaggedWord taggedWord;
		for (int a = 0; a < parsedSent.size(); a++) {
			System.out.println("raw words:");
			label = rawWords2.get(a);
			System.out.println(label.word() + " " + label.beginPosition() + " "
					+ label.endPosition());
			// String tag = parsedSent.get(a).tag();
			System.out.println("tagged words:");
			taggedWord = parsedSent.get(a);
			System.out.println(taggedWord.word() + " "
					+ taggedWord.beginPosition() + " "
					+ taggedWord.endPosition() + " " + taggedWord.tag());

		}
		System.out.println();
		return tags;
	}

}
