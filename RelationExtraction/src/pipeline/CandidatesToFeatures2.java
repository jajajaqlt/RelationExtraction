package pipeline;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pipeline.AbstractsToCandidates.Candidate;
import pipeline.ClassUtilities.Phrase;
import pipeline.ClassUtilities.Sentence;
import pipeline.ClassUtilities.TypedDependencyProperty;
import pipeline.ClassUtilities.Word;
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
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

public class CandidatesToFeatures2 {
	// stanford parser tools
	public static LexicalizedParser lp;
	public static TokenizerFactory<CoreLabel> tokenizerFactory;
	public static GrammaticalStructureFactory gsf;
	public static BufferedWriter bw;

	// those fields are updated only in checkTokenizationDiscrepancy() and
	// assigned to each sentence in getSentences()
	public static ArrayList<Phrase> phrases;
	public static int revisedEntity1Index;
	public static int revisedEntity2Index;
	public static List<TypedDependency> tdl;

	public static ArrayList<Sentence> sentences;
	public static List<CoreLabel> taggedLabels;
	public static Tree parse;
	public static GrammaticalStructure gs;

	public CandidatesToFeatures2(String outputFileName) throws Exception {
		lp = LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		tokenizerFactory = PTBTokenizer
				.factory(new CoreLabelTokenFactory(), "");
		gsf = new PennTreebankLanguagePack().grammaticalStructureFactory();
		bw = new BufferedWriter(new FileWriter(outputFileName));
	}

	public void getSentences(ArrayList<Candidate> candidates) throws Exception {
		sentences = new ArrayList<Sentence>();

		Utterance oldUtt = null;
		Utterance newUtt = null;
		int oldEntity1Index = -1, oldEntity2Index = -1, newEntity1Index = -1, newEntity2Index = -1;
		boolean discrepancyExists = false;
		String uttText;
		Tokenizer<CoreLabel> tok;
		List<CoreLabel> rawWords;
		Sentence sentence;
		// ArrayList<DependencyCollection> dependencies;
		ArrayList<Integer> entity1WordIndices, entity2WordIndices;
		HashMap<Integer, Vertex> vertices;
		HashMap<Integer, TypedDependencyProperty> tdpMap;
		// the following three are result of syntactic analysis
		HashMap<Integer, HashMap<Integer, TypedDependencyProperty>> dependencies = null;
		List<Vertex> path = null;
		LinkedHashMap<Integer, ArrayList<TypedDependencyProperty>> fatPath = null;
		boolean syntacticAnalysis;
		int fromVertexIndex, toVertexIndex;
		TypedDependencyProperty tdp;
		ArrayList<TypedDependencyProperty> tmp;

		for (Candidate candidate : candidates) {
			syntacticAnalysis = true;

			oldUtt = newUtt;
			newUtt = candidate.utterance;
			oldEntity1Index = newEntity1Index;
			oldEntity2Index = newEntity2Index;
			newEntity1Index = candidate.prev.phraseIndex;
			newEntity2Index = candidate.succ.phraseIndex;

			// initialize sentences
			sentence = new Sentence();
			sentence.sentenceText = candidate.utterance.getString();
			sentence.netRelation = candidate.netRelation;
			sentence.isInverse = candidate.isInverse;
			sentence.isPositive = candidate.isPositive;
			sentence.metaRelation = candidate.metaRelation;
			sentence.entity1NE = candidate.prev.rootSType;
			sentence.entity2NE = candidate.succ.rootSType;

			if (oldUtt != newUtt) {
				uttText = newUtt.getString();
				tok = tokenizerFactory.getTokenizer(new StringReader(uttText));
				rawWords = tok.tokenize();
				// ***check whether discrepancy exists between tokenizations
				// generated from metamap and stanford parser***//
				// discrepancyExists = checkTokenizationDiscrepancy(newUtt,
				// rawWords);
				checkTokenizationDiscrepancy(newUtt, rawWords, newEntity1Index,
						newEntity2Index);
			} else {
				if (oldEntity1Index == newEntity1Index
						&& oldEntity2Index == newEntity2Index) {
					syntacticAnalysis = false;
				}
			}

			// deals with discrepancy

			// for if-clause
			sentence.phrases = phrases;
			sentence.words = taggedLabels;
			sentence.entity1Index = revisedEntity1Index;
			sentence.entity2Index = revisedEntity2Index;

			// for else-clause
			if (syntacticAnalysis) {
				// shortest dependency path
				entity1WordIndices = new ArrayList<Integer>();
				for (Word w : sentence.phrases.get(sentence.entity1Index).words) {
					entity1WordIndices.add(w.index + 1);
				}
				entity2WordIndices = new ArrayList<Integer>();
				for (Word w : sentence.phrases.get(sentence.entity2Index).words) {
					entity2WordIndices.add(w.index + 1);
				}
				// dependencies
				dependencies = new HashMap<Integer, HashMap<Integer, TypedDependencyProperty>>();
				vertices = new HashMap<Integer, Vertex>();
				for (int i = -2; i < sentence.words.size() + 1; i++) {
					// 0 for root dummy node, hence +1 for the rest
					// "-1" for entity1, "-2" for entity2
					vertices.put(i, new Vertex("" + i));
					dependencies.put(i,
							new HashMap<Integer, TypedDependencyProperty>());
				}

				int govIndex, depIndex;
				String reln;
				for (TypedDependency td : tdl) {
					govIndex = td.gov().index();
					depIndex = td.dep().index();
					if (entity1WordIndices.contains(govIndex))
						govIndex = -1;
					if (entity1WordIndices.contains(depIndex))
						depIndex = -1;
					if (entity2WordIndices.contains(govIndex))
						govIndex = -2;
					if (entity2WordIndices.contains(depIndex))
						depIndex = -2;

					// entities are always in different phrases
					if (govIndex != depIndex) {
						reln = td.reln().toString();
						dependencies.get(govIndex).put(depIndex,
								new TypedDependencyProperty(true, reln));
						dependencies.get(depIndex).put(govIndex,
								new TypedDependencyProperty(false, reln));
					}
				}

				// run dijkstra's algorithm
				Edge[] adjacencies;
				Vertex v;
				int j;
				int distance = 1;
				for (int i = -2; i < sentence.words.size() + 1; i++) {
					v = vertices.get(i);
					tdpMap = dependencies.get(i);
					adjacencies = new Edge[tdpMap.keySet().size()];
					j = 0;
					for (int in : tdpMap.keySet()) {
						adjacencies[j] = new Edge(vertices.get(in), distance);
						j++;
					}
					v.adjacencies = adjacencies;
				}
				Dijkstra.computePaths(vertices.get(-1));
				path = Dijkstra.getShortestPathTo(vertices.get(-2));
				fatPath = new LinkedHashMap<Integer, ArrayList<TypedDependencyProperty>>();
				// processes path into fatPath
				for (int i = 1; i < path.size(); i++) {
					fromVertexIndex = Integer.parseInt(path.get(i - 1).name);
					toVertexIndex = Integer.parseInt(path.get(i).name);
					tdp = dependencies.get(fromVertexIndex).get(toVertexIndex);
					// right
					if (tdp.direction) {
						tmp = fatPath.get(fromVertexIndex);
						if (tmp == null)
							tmp = new ArrayList<TypedDependencyProperty>();
						tdp.position = false;
						tmp.add(tdp);
						fatPath.put(fromVertexIndex, tmp);
					} else {
						// left
						tmp = fatPath.get(toVertexIndex);
						if (tmp == null)
							tmp = new ArrayList<TypedDependencyProperty>();
						tdp.position = true;
						tmp.add(tdp);
						fatPath.put(toVertexIndex, tmp);
					}
				}
			}

			sentence.entity1Dependencies = dependencies.get(-1);
			sentence.entity2Dependencies = dependencies.get(-2);
			sentence.path = fatPath;

			sentences.add(sentence);
		}

		printSentencesFeatures(sentences);

	}

	private void printSentencesFeatures(ArrayList<Sentence> sentences2) {
		String lf1;
		String sf1;
		int index;
		String word;
		ArrayList<TypedDependencyProperty> tdpArr;
		// TypedDependencyProperty tdp;

		for (Sentence s : sentences) {
			System.out.println(s.sentenceText);
			// lexical feature #1, nothing is on left and right windows
			lf1 = "";
			lf1 += s.entity1NE + "[";
			for (int i = s.entity1Index + 1; i < s.entity2Index; i++) {
				for (Word w : s.phrases.get(i).words) {
					lf1 += w.wText + "/" + w.tag + " ";
				}
			}
			lf1 = lf1.substring(0, lf1.length() - 1) + "]" + s.entity2NE;
			System.out.println(lf1);

			// syntactic feature #1, nothing is on left and right windows
			sf1 = "";
			sf1 += s.entity1NE + "[";
			for (Map.Entry<Integer, ArrayList<TypedDependencyProperty>> m : s.path
					.entrySet()) {
				index = m.getKey();
				tdpArr = m.getValue();
				// ←↑→↓↖↙↗↘↕
				if (index == -1)
					sf1 += "→" + tdpArr.get(0).relation + " ";
				else if (index == -2)
					sf1 += "←" + tdpArr.get(0).relation + " ";
				else {
					word = s.words.get(index - 1).word();
					for (TypedDependencyProperty tdp : tdpArr) {
						if (tdp.position) {
							// left case
							word = "<--" + tdp.relation + "-" + word;
						} else {
							// right case
							word = word + "-" + tdp.relation + "-->";
						}
					}
					sf1 += word + " ";
				}
			}
			sf1 = sf1.substring(0, sf1.length() - 1) + "]" + s.entity2NE;
			System.out.println(sf1);
		}

	}

	public static void main(String[] args) throws Exception {
		String input = "This photochemical property was utilized in the development of hydrazones as photo-induced DNA-cleaving agents. ";

		MetaMapApi api = new MetaMapApiImpl(0);
		api.setOptions("-y");
		List<Result> resultList = api.processCitationsFromString("-y", input);

		lp = LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		gsf = new PennTreebankLanguagePack().grammaticalStructureFactory();
		tokenizerFactory = PTBTokenizer
				.factory(new CoreLabelTokenFactory(), "");

		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(input));
		List<CoreLabel> rawWords = tok.tokenize();

		checkTokenizationDiscrepancy(resultList.get(0).getUtteranceList()
				.get(0), rawWords, 0, 1);
		printPhrases();
	}

	private static void printPhrases() {
		String str = "";
		for (Phrase p : phrases) {
			str += "/";
			for (Word w : p.words) {
				str += w.wText + " ";
			}
			str = str.substring(0, str.length() - 1);
		}
		System.out.println(str);
	}

	/**
	 * Checks and resolves discrepancy. Assigns phrases, tdl,
	 * revisedEntity1Index, revisedEntity2Index;
	 * 
	 * @param utt
	 * @param rawWords
	 * @param newEntity2Index
	 * @param newEntity1Index
	 * @return true if discrepancy exists, false otherwise
	 * @throws Exception
	 */
	public static void checkTokenizationDiscrepancy(Utterance utt,
			List<CoreLabel> rawWords, int entity1Index, int entity2Index)
			throws Exception {
		phrases = new ArrayList<ClassUtilities.Phrase>();
		Phrase phrase;
		CoreLabel taggedLabel;
		int uttStartIndex;
		int[] phraseEndingIndices;
		int phraseEndingIndicesCursor;
		int phraseStartIndex, phraseLength;
		List<PCM> PCMList;
		PCM pcm;
		// inclusive
		int wordStartIndex;
		// exclusive
		int wordEndIndex;
		CoreLabel label;

		/**
		 * Revision: extend phrases to deal with tokenization discrepancy.
		 */
		// making ending indices array
		uttStartIndex = utt.getPosition().getX();
		PCMList = utt.getPCMList();
		phraseEndingIndices = new int[PCMList.size()];
		for (int i = 0; i < PCMList.size(); i++) {
			pcm = PCMList.get(i);
			phraseStartIndex = pcm.getPhrase().getPosition().getX();
			phraseLength = pcm.getPhrase().getPosition().getY();
			// should be exclusive
			phraseEndingIndices[i] = phraseStartIndex - uttStartIndex
					+ phraseLength;
			// move this to the iteration of going through words
			// phrases.add(new Phrase(pcm.getPhrase().getPhraseText()));
		}

		parse = lp.apply(rawWords);
		taggedLabels = parse.taggedLabeledYield();
		gs = gsf.newGrammaticalStructure(parse);
		// TypedDependency: gov() dep() reln()
		tdl = gs.typedDependenciesCCprocessed();
		// for (CoreLabel cl : parse.taggedLabeledYield())

		phraseEndingIndicesCursor = 0;
		phrase = new Phrase();
		phrases.add(phrase);
		// phrase = phrases.get(phraseEndingIndicesCursor);
		ArrayList<Integer> gulpedPhraseIndices = new ArrayList<Integer>();
		boolean increaseCursorFlag = false;
		for (int i = 0; i < rawWords.size(); i++) {
			label = rawWords.get(i);
			wordStartIndex = label.beginPosition();
			wordEndIndex = label.endPosition();

			while (wordStartIndex >= phraseEndingIndices[phraseEndingIndicesCursor]) {
				if (increaseCursorFlag)
					gulpedPhraseIndices.add(wordStartIndex);
				phraseEndingIndicesCursor++;
				// phrase = phrases.get(phraseEndingIndicesCursor);
				increaseCursorFlag = true;
			}
			if (increaseCursorFlag) {
				phrase = new Phrase();
				phrases.add(phrase);
				increaseCursorFlag = false;
			}

			taggedLabel = taggedLabels.get(i);
			phrase.words.add(new Word(taggedLabel.tag(), taggedLabel.index(),
					taggedLabel.word()));
			// if (wordEndIndex >
			// phraseEndingIndices[phraseEndingIndicesCursor]) {
			// return true;
			// }
		}

		// updates revisedEntity1Index, revisedEntity2Index
		if (gulpedPhraseIndices.size() == 0) {
			revisedEntity1Index = entity1Index;
			revisedEntity2Index = entity2Index;
		} else {
			int beforeFirstCount = 0, middleSecondCount = 0;
			for (int num : gulpedPhraseIndices) {
				if (num <= entity1Index)
					beforeFirstCount++;
				else if (num <= entity2Index)
					middleSecondCount++;
				else {

				}
			}
			revisedEntity1Index = entity1Index - beforeFirstCount;
			revisedEntity2Index = entity2Index - beforeFirstCount
					- middleSecondCount;
		}

	}
}
