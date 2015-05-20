package pipeline;

import java.io.StringReader; 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
//import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Utterance;
import pipeline.AbstractsToCandidates.Candidate;

public class CandidatesToFeatures {
	// MaxentTagger tagger = new
	// MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
	LexicalizedParser lp = LexicalizedParser
			.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
	TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
			new CoreLabelTokenFactory(), "");
	private ArrayList<Candidate> candidates;

	public CandidatesToFeatures(ArrayList<Candidate> candidates) {
		// TODO Auto-generated constructor stub
		this.candidates = candidates;
	}

	public void c2f() throws Exception {
		for (int i = 0; i < candidates.size(); i++) {
			Candidate cand = candidates.get(i);
			System.out.println("Candidate features: ");
			getMMFeatures(cand);
			// Get needed variables
			ArrayList<String> splits = getSplits(cand);
			// has to use stanford parser
			ArrayList<Integer> ifEnt = ifEntity(splits);
			ArrayList<String> phrases = getPhraseText(cand);
			// has to use stanford parser
			ArrayList<Integer> ifEntPhrases = ifEntityPhraseLevel(cand);

			// Sentence level features
			System.out.println();
			System.out.println("Sentence level features: ");
			ArrayList<String> POStags = getPOSFeatures(cand);
			ArrayList<String> POStagsWithSemTypes = getPOSFeaturesWithSemType(
					splits, cand, ifEnt);
			ArrayList<String> depParseFeature = getDepParseFeatures(cand);
			ArrayList<String> depArrows = getDepParseArrows(cand);
			ArrayList<String> depParseFeatureWithSemTypes = getDepParseFeaturesWithSemType(
					cand, ifEnt);
			ArrayList<String> depParseArrowsWithSemTypes = getDepParseArrowsWithSemType(
					cand, ifEnt);

			// Split level features
			startSplitLevelFeatures();
			int wordNum = 0;
			// POS features just tags
			System.out.print("POS Split Tags: ");
			for (int j = 0; j < splits.size(); j++) {
				getSplitLevelPOSFeatures(splits.get(j));
			}
			System.out.println();
			// POS features with semantic types
			System.out.print("POS Split Tags with SemTypes: ");
			wordNum = 0;
			for (int j = 0; j < splits.size(); j++) {
				wordNum = getSplitLevelFeaturesWithSemTypes(splits.get(j),
						POStagsWithSemTypes, wordNum, ifEnt);
			}
			System.out.println();
			// Dependency parse feature just tags
			wordNum = 0;
			System.out.print("Dep Parse Split tags: ");
			for (int j = 0; j < splits.size(); j++) {
				wordNum = getDepSplitLevelFeatures(splits.get(j),
						depParseFeature, wordNum);
			}
			System.out.println();
			// Dependency parse arrow feature
			wordNum = 0;
			System.out.print("Dep Parse Split Arrows: ");
			for (int j = 0; j < splits.size(); j++) {
				wordNum = getDepSplitLevelArrows(splits.get(j), depArrows,
						wordNum);
			}
			System.out.println();
			// Split level Dependency parse tags with semtypes
			wordNum = 0;
			System.out.print("Dep Parse Split tags w/SemTypes: ");
			for (int j = 0; j < splits.size(); j++) {
				wordNum = getSplitLevelFeaturesWithSemTypes(splits.get(j),
						depParseFeatureWithSemTypes, wordNum, ifEnt);
			}
			System.out.println();
			// Split level dependency parse arrows with semtypes
			wordNum = 0;
			System.out.print("Dep Parse Split Arrows w/SemTypes: ");
			for (int j = 0; j < splits.size(); j++) {
				wordNum = getSplitLevelFeaturesWithSemTypes(splits.get(j),
						depParseArrowsWithSemTypes, wordNum, ifEnt);
			}
			System.out.println();

			// MetaMap Phrase Level features
			startPhraseLevelFeatures();
			wordNum = 0;
			// Phrase level POS tags
			System.out.print("Phrase level POS tags: ");
			for (int j = 0; j < phrases.size(); j++) {
				wordNum = getPhraseLevelPOSFeatures(phrases.get(j), POStags,
						wordNum);
			}
			System.out.println();
			// Phrase level POS tags with semantic types
			wordNum = 0;
			System.out.print("Phrase level POS tags w/SemTypes: ");
			for (int j = 0; j < phrases.size(); j++) {
				wordNum = getPhraseLevelFeaturesWithSemTypes(phrases.get(j),
						POStagsWithSemTypes, wordNum, ifEntPhrases);
			}
			System.out.println();
			// Phrase level dependency tags
			wordNum = 0;
			System.out.print("Phrase level dependency tags: ");
			for (int j = 0; j < phrases.size(); j++) {
				wordNum = getPhraseLevelDepFeatures(phrases.get(j),
						depParseFeature, wordNum);
			}
			System.out.println();
			// Phrase level dependency arrows
			wordNum = 0;
			System.out.print("Phrase level dependency arrows: ");
			for (int j = 0; j < phrases.size(); j++) {
				wordNum = getPhraseLevelDepFeatures(phrases.get(j), depArrows,
						wordNum);
			}
			System.out.println();
			// Phrase level dependency tags with semtypes
			wordNum = 0;
			System.out.print("Phrase level dependency tags w/SemTypes: ");
			for (int j = 0; j < phrases.size(); j++) {
				wordNum = getPhraseLevelFeaturesWithSemTypes(phrases.get(j),
						depParseFeatureWithSemTypes, wordNum, ifEntPhrases);
			}
			System.out.println();
			// Phrase level dependency arrows with semtypes
			wordNum = 0;
			System.out.print("Phrase level dependency tags w/SemTypes: ");
			for (int j = 0; j < phrases.size(); j++) {
				wordNum = getPhraseLevelFeaturesWithSemTypes(phrases.get(j),
						depParseArrowsWithSemTypes, wordNum, ifEntPhrases);
			}
			System.out.println();
			endInstance();
		}
	}

	// Write the metamap features to a file
	private void getMMFeatures(Candidate cand) throws Exception {
		String utt = cand.utterance.getString();
		System.out.println("Entities: "
				+ utt.substring(cand.prev.pos.get(0).getX(),
						cand.prev.pos.get(0).getY()
								+ cand.prev.pos.get(0).getX())
				+ ", "
				+ utt.substring(cand.succ.pos.get(0).getX(),
						cand.succ.pos.get(0).getY()
								+ cand.succ.pos.get(0).getX()));
		System.out.println("CUIs: " + cand.prev.cui + " " + cand.succ.cui);
		System.out.println("Inverse_flag: " + cand.isInverse);
		System.out.println("Utterance: " + utt);
		System.out.println("SemNet relation: " + cand.netRelation);
		System.out.println("Positive instance: " + cand.isPositive);
		System.out.println("Meta relation: " + cand.metaRelation);
	}

	// Sentence level methods
	private ArrayList<String> getPOSFeatures(Candidate cand) throws Exception {
		// if utt = "Quinapril hydrochloride may treat heart failure.";
		String utt = cand.utterance.getString();
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
		ArrayList<String> tags = new ArrayList<String>();
		// All words tagged
		System.out.print("POS tags: ");
		for (int a = 0; a < parsedSent.size(); a++) {
			String tag = parsedSent.get(a).tag();
			System.out.print(tag + " ");
			tags.add(tag);
		}
		System.out.println();
		return tags;
	}

	private ArrayList<String> getPOSFeaturesWithSemType(
			ArrayList<String> phraseList, Candidate cand,
			ArrayList<Integer> ifEnt) throws Exception {
		String utt = cand.utterance.getString();
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(utt));
		List<CoreLabel> rawWords2 = tok.tokenize();
		Tree parse = lp.apply(rawWords2);
		ArrayList<TaggedWord> parsedSent = parse.taggedYield();
		ArrayList<String> tagsWithSemTypes = new ArrayList<String>();
		System.out.print("POS tags w/semtypes: ");
		// All words except entities tagged. Entities have metarelation
		int ent1 = 0;
		int ent2 = 0;
		for (int a = 0; a < parsedSent.size(); a++) {
			if (ifEnt.get(a) == 0) {
				String tag = parsedSent.get(a).tag();
				System.out.print(tag + " ");
				tagsWithSemTypes.add(tag + " ");
			} else if (ifEnt.get(a) == 1) {
				if (ent1 == 0) {
					System.out.print("|" + cand.prev.sType + "|" + " ");
					ent1 = 1;
					tagsWithSemTypes.add("|" + cand.prev.sType + "|" + " ");
				} else {
					continue;
				}
			} else {
				if (ent2 == 0) {
					System.out.print("|" + cand.succ.sType + "|" + " ");
					ent2 = 1;
					tagsWithSemTypes.add("|" + cand.succ.sType + "|" + " ");
				} else {
					continue;
				}
			}
		}
		System.out.println();
		return tagsWithSemTypes;
	}

	private ArrayList<String> getDepParseFeatures(Candidate cand)
			throws Exception {
		String utt = cand.utterance.getString();
		// Tokenize and create parse
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(utt));
		List<CoreLabel> rawWords2 = tok.tokenize();
		Tree parse = lp.apply(rawWords2);
		TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a
																// PennTreebankLanguagePack
																// for English
		GrammaticalStructureFactory gsf = null;
		gsf = tlp.grammaticalStructureFactory();

		// Get dependency arrows and chain of tags
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		ArrayList<String> depFeature = new ArrayList<String>();
		for (int j = 0; j < tdl.size(); j++) {
			if ((tdl.get(j).reln()).toString() == "root") {
				int b = j;
				System.out.print("Dep tags: ");
				for (int k = 0; k < tdl.size(); k++) {
					if (k < b) {
						System.out.print(tdl.get(k).reln().toString() + "->");
						depFeature.add(tdl.get(k).reln().toString() + "->");
					} else if (k == b) {
						System.out.print("root");
						depFeature.add("root");
					} else if (k > b) {
						System.out.print("<-" + tdl.get(k).reln().toString());
						depFeature.add("<-" + tdl.get(k).reln().toString());
					}
				}
			}
		}
		System.out.println();

		return depFeature;
	}

	private ArrayList<String> getDepParseArrows(Candidate cand)
			throws Exception {
		String utt = cand.utterance.getString();
		// Tokenize and create parse
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(utt));
		List<CoreLabel> rawWords2 = tok.tokenize();
		Tree parse = lp.apply(rawWords2);
		TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a
																// PennTreebankLanguagePack
																// for English
		GrammaticalStructureFactory gsf = null;
		gsf = tlp.grammaticalStructureFactory();

		// Get dependency arrows and chain of tags
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		ArrayList<String> depArrows = new ArrayList<String>();
		for (int j = 0; j < tdl.size(); j++) {
			if ((tdl.get(j).reln()).toString() == "root") {
				int b = j;
				System.out.print("Dep tags: ");
				for (int k = 0; k < tdl.size(); k++) {
					if (k < b) {
						System.out.print("->");
						depArrows.add("->");
					} else if (k == b) {
						System.out.print("root");
						depArrows.add("root");
					} else if (k > b) {
						System.out.print("<-");
						depArrows.add("<-");
					}
				}
			}
		}
		System.out.println();
		return depArrows;
	}

	private ArrayList<String> getDepParseArrowsWithSemType(Candidate cand,
			ArrayList<Integer> ifEnt) throws Exception {
		String utt = cand.utterance.getString();
		// Tokenize and create parse
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(utt));
		List<CoreLabel> rawWords2 = tok.tokenize();
		Tree parse = lp.apply(rawWords2);
		TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a
																// PennTreebankLanguagePack
																// for English
		GrammaticalStructureFactory gsf = null;
		gsf = tlp.grammaticalStructureFactory();

		// Get dependency arrows and chain of tags
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		ArrayList<String> depArrows = new ArrayList<String>();
		int ent1 = 0;
		int ent2 = 0;
		int rootInt = findRoot(tdl);
		System.out.print("Dep arrows w/semtypes: ");
		for (int j = 0; j < tdl.size(); j++) {
			if (ifEnt.get(j) == 0) {
				if (j < rootInt) {
					System.out.print("->");
					depArrows.add("->");
				} else if (j == rootInt) {
					depArrows.add("'root'");
					System.out.print("'root'");
				} else if (j > rootInt) {
					depArrows.add("<-");
					System.out.print("<-");
				}
			} else if (ifEnt.get(j) == 1) {
				if (ent1 == 0) {
					if (j < rootInt) {
						depArrows.add("|" + cand.prev.sType + "|");
						System.out.print("|" + cand.prev.sType + "|");
					} else if (j == rootInt) {
						depArrows.add("|" + cand.prev.sType + "|");
						System.out.print("|" + cand.prev.sType + "|");
					} else if (j > rootInt) {
						depArrows.add("|" + cand.prev.sType + "|");
						System.out.print("|" + cand.prev.sType + "|");
					}
					ent1 = 1;
				} else {
					continue;
				}
			} else {
				if (ent2 == 0) {
					if (j < rootInt) {
						depArrows.add("|" + cand.succ.sType + "|");
						System.out.print("|" + cand.prev.sType + "|");
					} else if (j == rootInt) {
						depArrows.add("|" + cand.succ.sType + "|");
						System.out.println("|" + cand.prev.sType + "|");
					} else if (j > rootInt) {
						depArrows.add("|" + cand.succ.sType + "|");
						System.out.print("|" + cand.prev.sType + "|");
					}
					ent2 = 1;
				} else {
					continue;
				}
			}
		}
		System.out.println();
		return depArrows;
	}

	private ArrayList<String> getDepParseFeaturesWithSemType(Candidate cand,
			ArrayList<Integer> ifEnt) throws Exception {
		String utt = cand.utterance.getString();
		// Tokenize and create parse
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(utt));
		List<CoreLabel> rawWords2 = tok.tokenize();
		Tree parse = lp.apply(rawWords2);
		TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a
																// PennTreebankLanguagePack
																// for English
		GrammaticalStructureFactory gsf = null;
		gsf = tlp.grammaticalStructureFactory();

		// Get dependency arrows and chain of tags
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		ArrayList<String> depFeature = new ArrayList<String>();
		int ent1 = 0;
		int ent2 = 0;
		int rootInt = findRoot(tdl);
		System.out.print("Dep tags w/semtypes: ");
		for (int j = 0; j < tdl.size(); j++) {
			if (ifEnt.get(j) == 0) {
				if (j < rootInt) {
					System.out.print(tdl.get(j).reln().toString() + "->");
					depFeature.add(tdl.get(j).reln().toString() + "->");
				} else if (j == rootInt) {
					depFeature.add(tdl.get(j).reln().toString());
					System.out.print(tdl.get(j).reln().toString());
				} else if (j > rootInt) {
					depFeature.add("<-" + tdl.get(j).reln().toString());
					System.out.print("<-" + tdl.get(j).reln().toString());
				}
			} else if (ifEnt.get(j) == 1) {
				if (ent1 == 0) {
					if (j < rootInt) {
						depFeature.add(tdl.get(j).reln().toString() + "|"
								+ cand.prev.sType + "|" + "->");
						System.out.print(tdl.get(j).reln().toString() + "|"
								+ cand.prev.sType + "|" + "->");
					} else if (j == rootInt) {
						depFeature.add(tdl.get(j).reln().toString() + "|"
								+ cand.prev.sType + "|");
						System.out.print(tdl.get(j).reln().toString() + "|"
								+ cand.prev.sType + "|");
					} else if (j > rootInt) {
						depFeature.add("<-" + tdl.get(j).reln().toString()
								+ "|" + cand.prev.sType + "|");
						System.out.print("<-" + tdl.get(j).reln().toString()
								+ "|" + cand.prev.sType + "|");
					}
					ent1 = 1;
				} else {
					continue;
				}
			} else {
				if (ent2 == 0) {
					if (j < rootInt) {
						depFeature.add(tdl.get(j).reln().toString() + "|"
								+ cand.succ.sType + "|" + "->");
						System.out.print(tdl.get(j).reln().toString() + "|"
								+ cand.succ.sType + "|" + "->");
					} else if (j == rootInt) {
						depFeature.add(tdl.get(j).reln().toString() + "|"
								+ cand.succ.sType + "|");
						System.out.println(tdl.get(j).reln().toString() + "|"
								+ cand.succ.sType + "|");
					} else if (j > rootInt) {
						depFeature.add("<-" + tdl.get(j).reln().toString()
								+ "|" + cand.succ.sType + "|");
						System.out.print("<-" + tdl.get(j).reln().toString()
								+ "|" + cand.succ.sType + "|");
					}
					ent2 = 1;
				} else {
					continue;
				}
			}
		}
		System.out.println();
		return depFeature;
	}

	// Split level methods
	private void getSplitLevelPOSFeatures(String str) {
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(str));
		List<CoreLabel> rawWords2 = tok.tokenize();
		Tree parse = lp.apply(rawWords2);
		ArrayList<TaggedWord> parsedSent = parse.taggedYield();
		String splitPOS = "";
		if (parsedSent.size() == 0) {
			splitPOS = "(),";
		} else if (parsedSent.get(0).toString() == "UH") {
			splitPOS = "()";
		} else {
			for (int a = 0; a < parsedSent.size(); a++) {
				if (a == 0 && a == parsedSent.size() - 1) {
					splitPOS += "(" + parsedSent.get(a).tag() + "),";
				} else if (a == 0) {
					splitPOS += "(" + parsedSent.get(a).tag() + ",";
				} else if (a == parsedSent.size() - 1) {
					splitPOS += parsedSent.get(a).tag() + "),";
				} else {
					splitPOS += parsedSent.get(a).tag() + " ";
				}
			}
		}
		System.out.print(splitPOS);
	}

	private int getSplitLevelFeaturesWithSemTypes(String thisSplit,
			ArrayList<String> feature, int wordNum, ArrayList<Integer> ifEnt) {
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(thisSplit));
		List<CoreLabel> rawWords = tok.tokenize();
		String splitPOSFeature = "";
		int num = wordNum;
		int ent1 = 0;
		int ent2 = 0;
		for (int k = 0; k < rawWords.size(); k++) {
			if (num >= feature.size()) {
				break;
			} else {
				if (ifEnt.get(k) == 0) {
					splitPOSFeature += feature.get(num);
					num += 1;
				} else if (ifEnt.get(k) == 1) {
					if (ent1 == 0) {
						splitPOSFeature += feature.get(num);
						num += 1;
						ent1 = 1;
					} else {
						continue;
					}
				} else {
					if (ent2 == 0) {
						splitPOSFeature += feature.get(num);
						num += 1;
						ent2 = 1;
					} else {
						continue;
					}
				}
			}
		}
		System.out.print("(" + splitPOSFeature + ") ");
		return num;
	}

	private int getDepSplitLevelFeatures(String thisSplit,
			ArrayList<String> depParseFeature, int wordNum) throws Exception {
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(thisSplit));
		List<CoreLabel> rawWords = tok.tokenize();
		String splitDepFeature = "";
		int num = wordNum;
		for (int k = 0; k < rawWords.size(); k++) {
			if (wordNum == depParseFeature.size()) {
				break;
			} else {
				splitDepFeature += depParseFeature.get(num);
				num += 1;
			}
		}
		System.out.print("(" + splitDepFeature + ") ");
		return num;
	}

	private int getDepSplitLevelArrows(String thisSplit,
			ArrayList<String> depArrows, int wordNum) throws Exception {
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(thisSplit));
		List<CoreLabel> rawWords = tok.tokenize();
		String splitDepFeature = "";
		int num = wordNum;
		for (int k = 0; k < rawWords.size(); k++) {
			if (wordNum >= depArrows.size()) {
				break;
			} else {
				splitDepFeature += depArrows.get(num);
				num += 1;
			}
		}
		System.out.print("(" + splitDepFeature + ") ");
		return num;
	}

	// MetaMap phrase level methods
	private int getPhraseLevelPOSFeatures(String thisPhrase,
			ArrayList<String> feature, int wordNum) {
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(thisPhrase));
		List<CoreLabel> rawWords = tok.tokenize();
		String splitPOSFeature = "";
		int num = wordNum;
		for (int k = 0; k < rawWords.size(); k++) {
			if (num >= feature.size()) {
				break;
			} else {
				splitPOSFeature += feature.get(num) + " ";
				num += 1;
			}
		}
		System.out.print("(" + splitPOSFeature + ") ");
		return num;
	}

	private int getPhraseLevelFeaturesWithSemTypes(String thisPhrase,
			ArrayList<String> feature, int wordNum,
			ArrayList<Integer> ifEntPhrases) {
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(thisPhrase));
		List<CoreLabel> rawWords = tok.tokenize();
		String phrasePOSFeature = "";
		int num = wordNum;
		int ent1 = 0;
		int ent2 = 0;
		for (int k = 0; k < rawWords.size(); k++) {
			if (num >= feature.size()) {
				break;
			} else {
				if (ifEntPhrases.get(k) == 0) {
					phrasePOSFeature += feature.get(num);
					num += 1;
				} else if (ifEntPhrases.get(k) == 1) {
					if (ent1 == 0) {
						phrasePOSFeature += feature.get(num);
						num += 1;
						ent1 = 1;
					} else {
						continue;
					}
				} else {
					if (ent2 == 0) {
						phrasePOSFeature += feature.get(num);
						num += 1;
						ent2 = 1;
					} else {
						continue;
					}
				}
			}
		}
		System.out.print("(" + phrasePOSFeature + ") ");
		return num;
	}

	private int getPhraseLevelDepFeatures(String thisPhrase,
			ArrayList<String> feature, int wordNum) {
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(thisPhrase));
		List<CoreLabel> rawWords = tok.tokenize();
		String phraseDepFeature = "";
		int num = wordNum;
		for (int k = 0; k < rawWords.size(); k++) {
			if (num >= feature.size()) {
				break;
			} else {
				phraseDepFeature += feature.get(num) + " ";
				num += 1;
			}
		}
		System.out.print("(" + phraseDepFeature + ") ");
		return num;
	}

	// Get necessary component methods
	/**
	 * changes original sentence "Quinapril hydrochloride may treat heart
	 * failure." into "() (Quinapril hydrochloride) ( may treat ) (heart
	 * failure) (.)"
	 */
	private ArrayList<String> getSplits(Candidate cand) throws Exception {
		String utt = cand.utterance.getString();
		int beginE1 = cand.prev.pos.get(0).getX();
		int endE1 = cand.prev.pos.get(0).getY() + beginE1;
		int beginE2 = cand.succ.pos.get(0).getX();
		int endE2 = cand.succ.pos.get(0).getY() + beginE2;
		// Get splits of strings
		String before = " ";
		String after = " ";
		if (beginE1 != 0) {
			before = utt.substring(0, beginE1);
		} else {
			before = "";
		}
		String ent1 = utt.substring(beginE1, endE1);
		String between = utt.substring(endE1, beginE2);
		String ent2 = utt.substring(beginE2, endE2);
		if (endE2 != utt.length()) {
			after = utt.substring(endE2, utt.length());
		} else {
			after = " ";
		}
		ArrayList<String> splits = new ArrayList<String>(Arrays.asList(before,
				ent1, between, ent2, after));
		for (int split = 0; split < splits.size(); split++) {
			System.out.print("(" + splits.get(split) + ") ");
		}

		return splits;
	}

	/**
	 * changes "Quinapril hydrochloride may treat heart failure." into
	 * "(Quinapril hydrochloride) (may) (treat) (heart failure.)"
	 */
	private ArrayList<String> getPhraseText(Candidate cand) throws Exception {
		Utterance utt = cand.utterance;
		ArrayList<PCM> phrases = (ArrayList<PCM>) utt.getPCMList();
		ArrayList<String> phraseText = new ArrayList<String>();
		for (int j = 0; j < phrases.size(); j++) {
			phraseText.add(phrases.get(j).getPhrase().getPhraseText());
			System.out.print("(" + phrases.get(j).getPhrase().getPhraseText()
					+ ") ");
		}
		return phraseText;
	}

	private ArrayList<Integer> ifEntity(ArrayList<String> splits) {
		ArrayList<Integer> ifEnt = new ArrayList<Integer>();
		for (int j = 0; j < splits.size(); j++) {
			Tokenizer<CoreLabel> tok = tokenizerFactory
					.getTokenizer(new StringReader(splits.get(j)));
			List<CoreLabel> rawWords = tok.tokenize();
			for (int k = 0; k < rawWords.size(); k++) {
				if (j == 0 || j == 2 || j == 4) {
					ifEnt.add(0);
				} else if (j == 1) {
					ifEnt.add(1);
				} else {
					ifEnt.add(2);
				}
			}
		}
		return ifEnt;
	}

	private ArrayList<Integer> ifEntityPhraseLevel(Candidate cand)
			throws Exception {
		ArrayList<PCM> phrases = (ArrayList<PCM>) cand.utterance.getPCMList();
		ArrayList<Integer> ifEnt = new ArrayList<Integer>();
		int ent1 = 0;
		int ent2 = 0;
		for (int j = 0; j < phrases.size(); j++) {
			if (ent1 == 0) {
				if (phrases.get(j).getPhrase().getPosition().getX() == cand.prev.pos
						.get(0).getX()) {
					Tokenizer<CoreLabel> tok = tokenizerFactory
							.getTokenizer(new StringReader(phrases.get(j)
									.getPhrase().getPhraseText()));
					List<CoreLabel> rawWords = tok.tokenize();
					for (int k = 0; k < rawWords.size(); k++) {
						ifEnt.add(1);
					}
					ent1 = 1;
				} else {
					Tokenizer<CoreLabel> tok = tokenizerFactory
							.getTokenizer(new StringReader(phrases.get(j)
									.getPhrase().getPhraseText()));
					List<CoreLabel> rawWords = tok.tokenize();
					for (int k = 0; k < rawWords.size(); k++) {
						ifEnt.add(0);
					}
				}
			} else if (ent1 == 1 && ent2 == 0) {
				if (phrases.get(j).getPhrase().getPosition().getX() == cand.succ.pos
						.get(0).getX()) {
					Tokenizer<CoreLabel> tok = tokenizerFactory
							.getTokenizer(new StringReader(phrases.get(j)
									.getPhrase().getPhraseText()));
					List<CoreLabel> rawWords = tok.tokenize();
					for (int k = 0; k < rawWords.size(); k++) {
						ifEnt.add(2);
					}
					ent2 = 1;
				} else {
					Tokenizer<CoreLabel> tok = tokenizerFactory
							.getTokenizer(new StringReader(phrases.get(j)
									.getPhrase().getPhraseText()));
					List<CoreLabel> rawWords = tok.tokenize();
					for (int k = 0; k < rawWords.size(); k++) {
						ifEnt.add(0);
					}
				}
			} else {
				Tokenizer<CoreLabel> tok = tokenizerFactory
						.getTokenizer(new StringReader(phrases.get(j)
								.getPhrase().getPhraseText()));
				List<CoreLabel> rawWords = tok.tokenize();
				for (int k = 0; k < rawWords.size(); k++) {
					ifEnt.add(0);
				}
			}

		}
		return ifEnt;
	}

	// Print out sentences methods
	private void startSplitLevelFeatures() throws Exception {
		System.out.println();
		System.out.println("Split Level Features:");
	}

	private void startPhraseLevelFeatures() throws Exception {
		System.out.println();
		System.out.println("Phrase Level Features:");
	}

	private void endInstance() throws Exception {
		System.out.println();
		System.out.print("------------");
		System.out.println();
	}

	private int findRoot(List<TypedDependency> tdl) {
		int rootInt = 0;
		for (int num = 0; num < tdl.size(); num++) {
			if (tdl.get(num).reln().toString() == "root") {
				rootInt = num;
			}
		}
		return rootInt;
	}

	// private void getPOSFeatures(String str) {
	// Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new
	// StringReader(str));
	// List<CoreLabel> rawWords2 = tok.tokenize();
	// Tree parse = lp.apply(rawWords2);
	// ArrayList<TaggedWord> parsedSent = parse.taggedYield();
	// System.out.print("POS tags: ");
	// for (int a=0; a<parsedSent.size(); a++){
	// System.out.print(parsedSent.get(a).tag() + " ");
	// }
	// System.out.println();
	// }

}
