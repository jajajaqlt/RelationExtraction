package demos;

import java.util.Scanner;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class StanfordWordTokenizer {

	public static void main(String[] args) {
		String grammar = args.length > 0 ? args[0]
				: "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		String[] options = { "-maxLength", "80", "-retainTmpSubcategories" };
		LexicalizedParser lp = LexicalizedParser.loadModel(grammar, options);
		TreebankLanguagePack tlp = lp.getOp().langpack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();

		while (true) {
			Scanner sc = new Scanner(System.in);
			String sentence = sc.nextLine();
			if (sentence.equals("quit"))
				break;
			else {
				Tree parse = lp.parse(sentence);
				// for (CoreLabel label : parse.taggedLabeledYield())
				// System.out.println(label.toString());
				for (TaggedWord word : parse.taggedYield())
					System.out.println(word.toString());
			}
		}
	}

}
