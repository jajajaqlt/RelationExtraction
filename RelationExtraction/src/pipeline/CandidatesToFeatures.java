package pipeline;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pipeline.ClassUtilities.Candidate;
import pipeline.ClassUtilities.Edge;
import pipeline.ClassUtilities.PCMImpl2;
import pipeline.ClassUtilities.Phrase;
import pipeline.ClassUtilities.PhraseImpl2;
import pipeline.ClassUtilities.PositionImpl2;
import pipeline.ClassUtilities.PreCandidate;
import pipeline.ClassUtilities.Sentence;
import pipeline.ClassUtilities.TypedDependencyProperty;
import pipeline.ClassUtilities.Vertex;
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
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Position;
import gov.nih.nlm.nls.metamap.Utterance;

public class CandidatesToFeatures {
	// stanford parser tools
	private LexicalizedParser lp;
	private TokenizerFactory<CoreLabel> tokenizerFactory;
	private GrammaticalStructureFactory gsf;
	private BufferedWriter bw;

	private ArrayList<Sentence> sentences;

	private ArrayList<Phrase> phrases;
	private int revisedEntity1Index;
	private int revisedEntity2Index;

	private HashMap<Integer, HashMap<Integer, TypedDependencyProperty>> dependencies;
	private LinkedHashMap<Integer, ArrayList<TypedDependencyProperty>> fatPath;

	public CandidatesToFeatures(String outputFileName) throws Exception {
		lp = LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		tokenizerFactory = PTBTokenizer
				.factory(new CoreLabelTokenFactory(), "");
		gsf = new PennTreebankLanguagePack().grammaticalStructureFactory();
		bw = new BufferedWriter(new FileWriter(outputFileName));
		sentences = new ArrayList<Sentence>();
	}

	public void getSentences(ArrayList<Candidate> candidates) throws Exception {

		String uttText;
		Tokenizer<CoreLabel> tok;
		List<CoreLabel> rawWords = null;
		Tree parse;
		List<CoreLabel> taggedLabels = null;
		GrammaticalStructure gs;
		Collection<TypedDependency> tdl = null;

		Candidate oldCandid = null;
		Candidate newCandid = new Candidate();
		newCandid.utterance = null;

		Sentence sentence;

		for (Candidate candidate : candidates) {
			oldCandid = newCandid;
			newCandid = candidate;

			sentence = new Sentence();
			sentence.abstractIndex = candidate.abstractIndex;
			sentence.sentenceText = candidate.utterance.getString();
			sentence.netRelation = candidate.netRelation;
			sentence.isInverse = candidate.isInverse;
			sentence.isPositive = candidate.isPositive;
			sentence.metaRelation = candidate.metaRelation;
			sentence.entity1NE = candidate.prev.rootSType;
			sentence.entity2NE = candidate.succ.rootSType;

			if (oldCandid.utterance != newCandid.utterance) {
				// if new utterance appears, re-parse for words, tags and typed
				// dependencies and construct phrases
				uttText = newCandid.utterance.getString();
				tok = tokenizerFactory.getTokenizer(new StringReader(uttText));
				rawWords = tok.tokenize();
				parse = lp.apply(rawWords);
				taggedLabels = parse.taggedLabeledYield();
				gs = gsf.newGrammaticalStructure(parse);
				// TypedDependency: gov() dep() reln()
				// tdl = gs.typedDependenciesCCprocessed();
				tdl = gs.typedDependencies();
				// no index information inside taggedLabels
				constructPhrases(newCandid, rawWords, taggedLabels);
			} else {
				if (!checkCandidateIndicesEquality(oldCandid, newCandid))
					constructPhrases(newCandid, rawWords, taggedLabels);
			}

			sentence.phrases = phrases;
			sentence.words = taggedLabels;
			sentence.entity1Index = revisedEntity1Index;
			sentence.entity2Index = revisedEntity2Index;

			if (oldCandid.utterance != newCandid.utterance) {
				constructDependencyPath(sentence, tdl);
			} else {
				if (!checkCandidateIndicesEquality(oldCandid, newCandid))
					constructDependencyPath(sentence, tdl);
			}

			sentence.entity1Dependencies = dependencies.get(-1);
			sentence.entity2Dependencies = dependencies.get(-2);
			sentence.path = fatPath;

			sentences.add(sentence);
		}

	}

	private void constructDependencyPath(Sentence sentence,
			Collection<TypedDependency> tdl) {

		ArrayList<Integer> entity1WordIndices, entity2WordIndices;
		HashMap<Integer, Vertex> vertices;
		HashMap<Integer, TypedDependencyProperty> tdpMap;
		int fromVertexIndex, toVertexIndex;
		TypedDependencyProperty tdp;
		ArrayList<TypedDependencyProperty> tmp;

		// the following three are result of syntactic analysis
		dependencies = null;
		List<Vertex> path = null;
		fatPath = null;

		// treats entity1 phrase and entity2 phrase as single nodes
		entity1WordIndices = new ArrayList<Integer>();
		for (Word w : sentence.phrases.get(sentence.entity1Index).words) {
			entity1WordIndices.add(w.index + 1);
		}
		entity2WordIndices = new ArrayList<Integer>();
		for (Word w : sentence.phrases.get(sentence.entity2Index).words) {
			entity2WordIndices.add(w.index + 1);
		}

		// typed dependencies
		dependencies = new HashMap<Integer, HashMap<Integer, TypedDependencyProperty>>();
		vertices = new HashMap<Integer, Vertex>();
		for (int i = -2; i < sentence.words.size() + 1; i++) {
			// 0 for root dummy node, hence +1 for the rest
			// "-1" for entity1, "-2" for entity2
			vertices.put(i, new Vertex("" + i));
			dependencies
					.put(i, new HashMap<Integer, TypedDependencyProperty>());
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
		ClassUtilities.computePaths(vertices.get(-1));
		path = ClassUtilities.getShortestPathTo(vertices.get(-2));
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

	private boolean checkCandidateIndicesEquality(Candidate oldCandid,
			Candidate newCandid) {
		PreCandidate oldPrev = oldCandid.prev, oldSucc = oldCandid.succ, newPrev = newCandid.prev, newSucc = newCandid.succ;
		if (oldPrev.phraseIndex == newPrev.phraseIndex
				&& oldSucc.phraseIndex == newSucc.phraseIndex
				&& oldPrev.evIndex == newPrev.evIndex
				&& oldSucc.evIndex == newSucc.evIndex
				&& oldPrev.mappingIndex == newPrev.mappingIndex
				&& oldSucc.mappingIndex == newSucc.mappingIndex)
			return true;
		return false;
	}

	public void writeFeatures() {
		// lexical feature 1
		String lf1;
		// syntactic feature 1
		String sf1;
		int index;
		String word;
		ArrayList<TypedDependencyProperty> tdpArr;
		// TypedDependencyProperty tdp;

		// features for an instance

		for (Sentence s : sentences) {
			String features = "";
			// System.out.println(s.sentenceText);
			// lexical feature #1, nothing is on left and right windows
			lf1 = "";
			lf1 += s.entity1NE + "[";
			for (int i = s.entity1Index + 1; i < s.entity2Index; i++) {
				for (Word w : s.phrases.get(i).words) {
					lf1 += w.wText + "/" + w.tag + " ";
				}
			}
			lf1 = lf1.substring(0, lf1.length() - 1) + "]" + s.entity2NE;
			// System.out.println(lf1);
			features += lf1 + "\n";

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
			// System.out.println(sf1);
			features += sf1;
			System.out.println(features);
		}

	}

	private void printPhrases() {
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

	private void constructPhrases(Candidate candid, List<CoreLabel> rawWords,
			List<CoreLabel> taggedLabels) throws Exception {

		phrases = new ArrayList<ClassUtilities.Phrase>();
		Phrase phrase;

		Utterance utt = candid.utterance;
		int entity1Index;
		int entity2Index;

		int[] phraseEndingIndices;
		int phraseEndingIndicesCursor;
		// inclusive
		int wordStartIndex;
		// exclusive
		int wordEndIndex;

		CoreLabel taggedLabel;
		CoreLabel rawWord;

		phraseEndingIndices = getRevisedPhraseEndingIndices(candid);
		entity1Index = candid.prev.revisedPhraseIndex;
		entity2Index = candid.succ.revisedPhraseIndex;

		/**
		 * Revision: extend phrases to deal with tokenization discrepancy.
		 */

		ArrayList<Integer> gulpedPhraseIndices = new ArrayList<Integer>();
		boolean increaseCursorFlag = false;

		phraseEndingIndicesCursor = 0;
		phrase = new Phrase();
		phrases.add(phrase);

		for (int i = 0; i < rawWords.size(); i++) {
			rawWord = rawWords.get(i);
			wordStartIndex = rawWord.beginPosition();
			wordEndIndex = rawWord.endPosition();

			while (wordStartIndex >= phraseEndingIndices[phraseEndingIndicesCursor]) {
				if (increaseCursorFlag)
					gulpedPhraseIndices.add(wordStartIndex);
				phraseEndingIndicesCursor++;
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

	/**
	 * Not the fastest solution. Good for debugging. Might be subject to
	 * optimization.
	 */
	private static int[] getRevisedPhraseEndingIndices(Candidate candid)
			throws Exception {
		/**
		 * Splits entities (of interest) into multiple phrases and updates
		 * PCMList.
		 */

		int[] phraseEndingIndices;
		int phraseStartIndex, phraseLength;

		Utterance utt = candid.utterance;
		int uttStartIndex = utt.getPosition().getX();

		PreCandidate prev = candid.prev, succ = candid.succ;
		int prevPhraseIndex = prev.phraseIndex, prevMapIndex = prev.mappingIndex, prevEvIndex = prev.evIndex;
		int succPhraseIndex = succ.phraseIndex, succMapIndex = succ.mappingIndex, succEvIndex = succ.evIndex;
		int revisedEntity1Index, revisedEntity2Index;

		List<PCM> PCMList = utt.getPCMList();
		PCM pcm;

		// System.out.println("initial:");
		// printPCMList(utt.getString(), uttStartIndex, PCMList);

		int prevPhraseStartIndex, prevEvStartIndex, prevEvEndIndex, prevPhraseEndIndex;
		int succPhraseStartIndex, succEvStartIndex, succEvEndIndex, succPhraseEndIndex;

		Position prevPhrasePos = PCMList.get(prevPhraseIndex).getPhrase()
				.getPosition();
		// inclusive
		prevPhraseStartIndex = prevPhrasePos.getX();
		// exclusive
		prevPhraseEndIndex = prevPhraseStartIndex + prevPhrasePos.getY();

		Position succPhrasePos = PCMList.get(succPhraseIndex).getPhrase()
				.getPosition();
		// inclusive
		succPhraseStartIndex = succPhrasePos.getX();
		// exclusive
		succPhraseEndIndex = succPhraseStartIndex + succPhrasePos.getY();

		List<Position> prevPosList = PCMList.get(prevPhraseIndex)
				.getMappingList().get(prevMapIndex).getEvList()
				.get(prevEvIndex).getPositionalInfo();
		// inclusive
		prevEvStartIndex = prevPosList.get(0).getX();
		// exclusive
		prevEvEndIndex = prevPosList.get(prevPosList.size() - 1).getX()
				+ prevPosList.get(prevPosList.size() - 1).getY();

		List<Position> succPosList = PCMList.get(succPhraseIndex)
				.getMappingList().get(succMapIndex).getEvList()
				.get(succEvIndex).getPositionalInfo();
		// inclusive
		succEvStartIndex = succPosList.get(0).getX();
		succEvEndIndex = succPosList.get(succPosList.size() - 1).getX()
				+ succPosList.get(succPosList.size() - 1).getY();

		PCM originalEntity1Phrase, originalEntity2Phrase;
		List<PCM> entity1PCMCollection = new ArrayList<PCM>();
		List<PCM> entity2PCMCollection = new ArrayList<PCM>();

		revisedEntity1Index = prevPhraseIndex;
		if (prevPhraseStartIndex != prevEvStartIndex) {
			entity1PCMCollection.add(new PCMImpl2(new PhraseImpl2(
					new PositionImpl2(prevPhraseStartIndex, prevEvStartIndex
							- prevPhraseStartIndex))));
			revisedEntity1Index++;
		}
		entity1PCMCollection.add(new PCMImpl2(new PhraseImpl2(
				new PositionImpl2(prevEvStartIndex, prevEvEndIndex
						- prevEvStartIndex))));
		if (prevEvEndIndex != prevPhraseEndIndex)
			entity1PCMCollection.add(new PCMImpl2(new PhraseImpl2(
					new PositionImpl2(prevEvEndIndex, prevPhraseEndIndex
							- prevEvEndIndex))));
		PCMList.addAll(prevPhraseIndex + 1, entity1PCMCollection);
		originalEntity1Phrase = PCMList.remove(prevPhraseIndex);
		succPhraseIndex += entity1PCMCollection.size() - 1;
		revisedEntity2Index = succPhraseIndex;

		if (succPhraseStartIndex != succEvStartIndex) {
			entity2PCMCollection.add(new PCMImpl2(new PhraseImpl2(
					new PositionImpl2(succPhraseStartIndex, succEvStartIndex
							- succPhraseStartIndex))));
			revisedEntity2Index++;
		}
		entity2PCMCollection.add(new PCMImpl2(new PhraseImpl2(
				new PositionImpl2(succEvStartIndex, succEvEndIndex
						- succEvStartIndex))));
		if (succEvEndIndex != succPhraseEndIndex)
			entity2PCMCollection.add(new PCMImpl2(new PhraseImpl2(
					new PositionImpl2(succEvEndIndex, succPhraseEndIndex
							- succEvEndIndex))));
		PCMList.addAll(succPhraseIndex + 1, entity2PCMCollection);
		originalEntity2Phrase = PCMList.remove(succPhraseIndex);

		candid.prev.revisedPhraseIndex = revisedEntity1Index;
		candid.succ.revisedPhraseIndex = revisedEntity2Index;

		// System.out.println("after splitting:");
		// printPCMList(utt.getString(), uttStartIndex, PCMList);

		// making ending indices array
		phraseEndingIndices = new int[PCMList.size()];
		for (int i = 0; i < PCMList.size(); i++) {
			pcm = PCMList.get(i);
			phraseStartIndex = pcm.getPhrase().getPosition().getX();
			phraseLength = pcm.getPhrase().getPosition().getY();
			// should be exclusive
			phraseEndingIndices[i] = phraseStartIndex - uttStartIndex
					+ phraseLength;
		}

		/**
		 * Restores PCMList in case next candidate uses the same candidate and
		 * PCMList.
		 */

		// order is very important, do not change
		PCMList.add(succPhraseIndex, originalEntity2Phrase);
		PCMList.add(prevPhraseIndex, originalEntity1Phrase);

		PCMList.removeAll(entity1PCMCollection);
		PCMList.removeAll(entity2PCMCollection);

		// System.out.println("after restoration:");
		// printPCMList(utt.getString(), uttStartIndex, PCMList);

		return phraseEndingIndices;
	}

	private static void printPCMList(String uttText, int uttStartIndex,
			List<PCM> PCMList) throws Exception {
		String str = "";
		int s, l;
		for (PCM pcm : PCMList) {
			s = pcm.getPhrase().getPosition().getX() - uttStartIndex;
			l = pcm.getPhrase().getPosition().getY();
			str += uttText.substring(s, s + l) + "/";
		}
		System.out.println(str.substring(0, str.length() - 1));
	}

	// public static void main(String[] args) throws Exception {
	// String input =
	// "This photochemical property was utilized in the development of hydrazones as photo-induced DNA-cleaving agents. ";
	//
	// MetaMapApi api = new MetaMapApiImpl(0);
	// api.setOptions("-y");
	// List<Result> resultList = api.processCitationsFromString("-y", input);
	//
	// lp = LexicalizedParser
	// .loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
	// gsf = new PennTreebankLanguagePack().grammaticalStructureFactory();
	// tokenizerFactory = PTBTokenizer
	// .factory(new CoreLabelTokenFactory(), "");
	//
	// Tokenizer<CoreLabel> tok = tokenizerFactory
	// .getTokenizer(new StringReader(input));
	// List<CoreLabel> rawWords = tok.tokenize();
	//
	// checkTokenizationDiscrepancy(resultList.get(0).getUtteranceList()
	// .get(0), rawWords, 0, 1);
	// printPhrases();
	// }
}
