package pipeline;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pipeline.AbstractsToCandidates.Candidate;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import gov.nih.nlm.nls.metamap.Utterance;

public class CandidatesToFeatures2 {
	// stanford parser tools
	LexicalizedParser lp;
	TokenizerFactory<CoreLabel> tokenizerFactory;
	GrammaticalStructureFactory gsf;

	BufferedWriter bw;

	public CandidatesToFeatures2(String outputFileName) throws Exception {
		lp = LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		tokenizerFactory = PTBTokenizer
				.factory(new CoreLabelTokenFactory(), "");
		gsf = new PennTreebankLanguagePack().grammaticalStructureFactory();
		bw = new BufferedWriter(new FileWriter(outputFileName));
	}

	// stanford parser usage
	// Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new
	// StringReader(sent2));
	// List<CoreLabel> rawWords2 = tok.tokenize();
	// parse = lp.apply(rawWords2);
	// ArrayList<TaggedWord> tw = parse.taggedYield();
	//
	// TreebankLanguagePack tlp = new PennTreebankLanguagePack();
	// GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	// GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
	// List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
	// System.out.println(tdl);

	public void getFeatures(ArrayList<Candidate> candidates) throws Exception {
		Utterance oldUtt = null, newUtt = null;
		String uttText;
		Tokenizer<CoreLabel> tok;
		List<CoreLabel> rawWords2;

		for (Candidate candidate : candidates) {
			oldUtt = newUtt;
			newUtt = candidate.utterance;
			if (oldUtt != newUtt) {
				uttText = newUtt.getString();
				tok = tokenizerFactory.getTokenizer(new StringReader(uttText));
				rawWords2 = tok.tokenize();
				// ***check whether discrepancy exists between tokenizations
				// generated from metamap and stanford parser***//
				
			}

		}
	}

	public class FeatureCollection {
		// feature1, feature2, ...
	}

}
