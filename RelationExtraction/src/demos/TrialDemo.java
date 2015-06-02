package demos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.io.StringReader;

import pipeline.AbstractsToCandidates.Candidate;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class TrialDemo {

	/**
	 * The main method demonstrates the easiest way to load a parser. Simply
	 * call loadModel and specify the path of a serialized grammar model, which
	 * can be a file, a resource on the classpath, or even a URL. For example,
	 * this demonstrates loading from the models jar file, which you therefore
	 * need to include in the classpath for ParserDemo to work.
	 */
	public static void main(String[] args) {
		LexicalizedParser lp = LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		// if (args.length > 0) {
		// demoDP(lp, args[0]);
		// } else {
		demoAPI(lp);
		// }
	}

	/**
	 * demoDP demonstrates turning a file into tokens and then parse trees. Note
	 * that the trees are printed by calling pennPrint on the Tree object. It is
	 * also possible to pass a PrintWriter to pennPrint if you want to capture
	 * the output.
	 */
	public static void demoDP(LexicalizedParser lp, String filename) {
		// This option shows loading, sentence-segmenting and tokenizing
		// a file using DocumentPreprocessor.
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		// You could also create a tokenizer here (as below) and pass it
		// to DocumentPreprocessor
		for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {
			Tree parse = lp.apply(sentence);
			parse.pennPrint();
			System.out.println();

			GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
			Collection tdl = gs.typedDependenciesCCprocessed();
			System.out.println(tdl);
			System.out.println();
		}
	}

	// function getting pos tags
	// private ArrayList<String> getPOSFeatures(Candidate cand) throws Exception
	// {
	// // if utt = "Quinapril hydrochloride may treat heart failure.";
	// String utt = cand.utterance.getString();
	// Tokenizer<CoreLabel> tok = tokenizerFactory
	// .getTokenizer(new StringReader(utt));
	// // [Quinapril, hydrochloride, may, treat, heart, failure, .]
	// List<CoreLabel> rawWords2 = tok.tokenize();
	// // (ROOT (S (NP (JJ Quinapril) (NN hydrochloride)) (VP (MD may) (VP (VB
	// // treat) (NP (NN heart) (NN failure)))) (. .)))
	// Tree parse = lp.apply(rawWords2);
	// // [Quinapril/JJ, hydrochloride/NN, may/MD, treat/VB, heart/NN,
	// // failure/NN, ./.]
	// // http://en.wikipedia.org/wiki/Parse_tree
	// ArrayList<TaggedWord> parsedSent = parse.taggedYield();
	// ArrayList<String> tags = new ArrayList<String>();
	// // All words tagged
	// System.out.print("POS tags: ");
	// for (int a = 0; a < parsedSent.size(); a++) {
	// String tag = parsedSent.get(a).tag();
	// System.out.print(tag + " ");
	// tags.add(tag);
	// }
	// System.out.println();
	// return tags;
	// }

	/**
	 * demoAPI demonstrates other ways of calling the parser with already
	 * tokenized text, or in some cases, raw text that needs to be tokenized as
	 * a single sentence. Output is handled with a TreePrint object. Note that
	 * the options used when creating the TreePrint can determine what results
	 * to print out. Once again, one can capture the output by passing a
	 * PrintWriter to TreePrint.printTree.
	 */
	public static void demoAPI(LexicalizedParser lp) {
		// This option shows parsing a list of correctly tokenized words
		// Quinapril hydrochloride may treat heart failure.
		// // String[] sent = { "This", "is", "an", "easy", "sentence", "." };
		// String[] sent = { "Quinapril hydrochloride", "may", "treat",
		// "heart failure", "." };
		// List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
		// Tree parse = lp.apply(rawWords);
		// ArrayList<TaggedWord> tags = parse.taggedYield();
		// System.out.println(tags.toString());
		// parse.pennPrint();
		// System.out.println();

		// This option shows loading and using an explicit tokenizer
		// String sent2 = "This is another sentence.";
		// TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
		// new CoreLabelTokenFactory(), "");
		// Tokenizer<CoreLabel> tok = tokenizerFactory
		// .getTokenizer(new StringReader(sent2));
		// List<CoreLabel> rawWords2 = tok.tokenize();
		// parse = lp.apply(rawWords2);
		//
		// TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		// GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		// GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		// List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		// System.out.println(tdl);
		// System.out.println();
		//
		// // You can also use a TreePrint object to print trees and
		// dependencies
		// TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
		// tp.printTree(parse);
		String sent2 = "I love you. You love me.";
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
				new CoreLabelTokenFactory(), "");
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(sent2));
		List<CoreLabel> rawWords2 = tok.tokenize();
		Tree parse = lp.apply(rawWords2);
		String sentenceTokenized = "";
		for (CoreLabel label : rawWords2) {
			 System.out.println(label.word());
			// System.out.println(label.lemma());
			// System.out.println(label.tag());
			System.out.println(label.beginPosition());
			System.out.println(label.endPosition());
			// System.out.println(label.ner());
			// System.out.println();
			sentenceTokenized += label.word() + "/";
		}
		System.out.println(sentenceTokenized.substring(0,
				sentenceTokenized.length() - 1));
	}

	private TrialDemo() {
	} // static methods only

}
