package tests;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.Result;

import java.io.StringReader;
import java.util.List;

public class TestC2F2 {

	public static void main(String[] args) {
		String input = "";
		
		MetaMapApi api = new MetaMapApiImpl(0);
		api.setOptions("-y");
		List<Result> resultList = api.processCitationsFromString("-y",
				input);
		
		LexicalizedParser lp = LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
				new CoreLabelTokenFactory(), "");
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(input));
		List<CoreLabel> rawWords2 = tok.tokenize();
		Tree parse = lp.apply(rawWords2);
		
		
	}

	
}
