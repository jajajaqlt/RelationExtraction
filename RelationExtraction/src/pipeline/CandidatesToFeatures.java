package pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.tartarus.snowball.SnowballStemmer;

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
import edu.stanford.nlp.util.HashIndex;
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
	private HashMap<Integer, TypedDependencyProperty> entity1Dependencies;
	private HashMap<Integer, TypedDependencyProperty> entity2Dependencies;

	// all indices here are 1-based
	private HashMap<Integer, HashMap<Integer, TypedDependencyProperty>> dependencies;
	private LinkedHashMap<Integer, ArrayList<TypedDependencyProperty>> fatPath;

	// true for writing to the output file, false for outputting to the standard
	// output
	private boolean outputMethod;

	private HashIndex<String> wordIndex;
	private HashIndex<String> tagIndex;
	private HashIndex<String> depIndex;

	private SnowballStemmer stemmer;

	public CandidatesToFeatures(String outputFileName, String wordDictFile,
			String tagDictFile, String depTypeDictFile) throws Exception {
		lp = LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		tokenizerFactory = PTBTokenizer
				.factory(new CoreLabelTokenFactory(), "");
		gsf = new PennTreebankLanguagePack().grammaticalStructureFactory();

		if (!outputFileName.equals("")) {
			bw = new BufferedWriter(new FileWriter(outputFileName));
			outputMethod = true;
		} else {
			outputMethod = false;
		}

		initializeIndices(wordDictFile, tagDictFile, depTypeDictFile);

		Class stemClass = Class.forName("org.tartarus.snowball.ext."
				+ "english" + "Stemmer");
		stemmer = (SnowballStemmer) stemClass.newInstance();

		sentences = new ArrayList<Sentence>();
	}

	private void initializeIndices(String wordDictFile, String tagDictFile,
			String depTypeDictFile) throws Exception {
		String line;
		BufferedReader br = new BufferedReader(new FileReader(wordDictFile));
		wordIndex = new HashIndex<String>();
		while ((line = br.readLine()) != null) {
			wordIndex.add(line);
		}
		br.close();

		br = new BufferedReader(new FileReader(tagDictFile));
		tagIndex = new HashIndex<String>();
		while ((line = br.readLine()) != null) {
			tagIndex.add(line);
		}
		br.close();

		br = new BufferedReader(new FileReader(depTypeDictFile));
		depIndex = new HashIndex<String>();
		while ((line = br.readLine()) != null) {
			depIndex.add(line);
		}
		br.close();
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
			sentence.entity1Cui = candidate.prev.cui;
			sentence.entity2Cui = candidate.succ.cui;
			sentence.abstractIndex = candidate.abstractIndex;
			sentence.sentenceText = candidate.utterance.getString();
			sentence.netRelation = candidate.netRelation;
			sentence.isInverse = candidate.isInverse;
			sentence.isPositive = candidate.isPositive;
			sentence.metaRelation = candidate.metaRelation;
			sentence.entity1NE = candidate.prev.rootSType.toUpperCase();
			sentence.entity2NE = candidate.succ.rootSType.toUpperCase();

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

			sentence.entity1Dependencies = entity1Dependencies;
			sentence.entity2Dependencies = entity2Dependencies;
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
			// det ( area-3 , The-1 ); arrow is from 'area' to 'The'
			govIndex = td.gov().index();
			depIndex = td.dep().index();
			// root(ROOT-0, bypassed-6)
			if (govIndex != 0) {
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
			// always gives the edge to the node where it is origniated
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

		// uses path to generate entity1Dependencies and entity2Dependencies
		int index;

		entity1Dependencies = new HashMap<Integer, TypedDependencyProperty>();
		fromVertexIndex = -1;
		entity1Dependencies = dependencies.get(fromVertexIndex);
		toVertexIndex = Integer.parseInt(path.get(1).name);
		entity1Dependencies.remove(toVertexIndex);
		for (Map.Entry<Integer, TypedDependencyProperty> entry : entity1Dependencies
				.entrySet()) {
			index = entry.getKey();
			tdp = entry.getValue();
			if (tdp.direction)
				tdp.position = true;
			else
				tdp.position = false;
		}

		entity2Dependencies = new HashMap<Integer, TypedDependencyProperty>();
		fromVertexIndex = -2;
		entity2Dependencies = dependencies.get(fromVertexIndex);
		toVertexIndex = Integer.parseInt(path.get(path.size() - 2).name);
		entity2Dependencies.remove(toVertexIndex);
		for (Map.Entry<Integer, TypedDependencyProperty> entry : entity2Dependencies
				.entrySet()) {
			index = entry.getKey();
			tdp = entry.getValue();
			if (tdp.direction)
				tdp.position = false;
			else
				tdp.position = true;
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

	public void writeFeatures() throws Exception {
		String instance;
		String newLine = "\n";
		String inverseFlag, inverseFlagAbbr;
		int countOfWindowNodes = 2;
		int entity1FirstWordIndex, entity2LastWordIndex;
		Phrase entity1, entity2;
		String middleWords, middleTags;
		// closer word has smaller index
		ArrayList<Word> leftWords, rightWords;
		CoreLabel label;
		int index;
		ArrayList<TypedDependencyProperty> tdpArr;
		String entity1MatchedWords, entity2MatchedWords;
		boolean includePadInFeatures = true;
		String unitDelimiter = "\n";
		int unitDelimiterLength = unitDelimiter.length();

		// sentence-level bag of words features
		String sLvWordIndices, sLvTagIndices, sLvDepTypeIndices, sLvDepWordIndices;

		for (Sentence s : sentences) {
			try {
				String header = "", footer = "", sentenceLvFeats = "", chunkLvFeats = "", phraseLvFeats = "", wordLvFeats = "";
				// feat1's are for word unit compound, feat2's are for tag unit
				// compound
				String chunkLvFeat1 = "", chunkLvFeat2 = "", phraseLvFeat1 = "", phraseLvFeat2 = "", wordLvFeat1 = "", wordLvFeat2 = "";
				// header
				header += "<instance>" + newLine;
				header += "index: " + s.abstractIndex + newLine;
				header += "cui1: " + s.entity1Cui + newLine;
				header += "cui1-type: " + s.entity1NE + newLine;
				entity1MatchedWords = "";
				for (Word w : s.phrases.get(s.entity1Index).words)
					entity1MatchedWords += " " + w.wText;
				header += "cui1-matched-words:" + entity1MatchedWords + newLine;
				header += "cui2: " + s.entity2Cui + newLine;
				header += "cui2-type: " + s.entity2NE + newLine;
				entity2MatchedWords = "";
				for (Word w : s.phrases.get(s.entity2Index).words)
					entity2MatchedWords += " " + w.wText;
				header += "cui2-matched-words:" + entity2MatchedWords + newLine;
				header += "positivity: " + (s.isPositive ? "true" : "false")
						+ newLine;
				header += "inverse: " + (s.isInverse ? "true" : "false")
						+ newLine;

				// a flag indicating which entity came first in the sentence
				if (s.isInverse) {
					inverseFlag = "inverse_true";
					inverseFlagAbbr = inverseFlag + "|";
				} else {
					inverseFlag = "inverse_false";
					inverseFlagAbbr = inverseFlag + "|";
				}

				header += "net-relation: " + s.netRelation + newLine;
				header += "meta-relation: "
						+ (s.metaRelation == null ? "null" : s.metaRelation)
						+ newLine;
				header += "sentence: " + s.sentenceText + newLine;

				// sentence-level features
				sentenceLvFeats += "<sentence-level-features>" + newLine;

				// the sequence of words between the two entities
				// the part-of-speech tags of these words
				/**
				 * Sets a threshold here in the future to deal with a long
				 * sequence of words or tags. i.e. putting a ""
				 */
				// sets the value of central nodes of other resolution levels
				wordLvFeat1 += inverseFlagAbbr + s.entity1NE + unitDelimiter;
				wordLvFeat2 += inverseFlagAbbr + s.entity1NE + unitDelimiter;
				phraseLvFeat1 += inverseFlagAbbr + s.entity1NE + unitDelimiter;
				phraseLvFeat2 += inverseFlagAbbr + s.entity1NE + unitDelimiter;
				chunkLvFeat1 += inverseFlagAbbr + s.entity1NE + unitDelimiter;
				chunkLvFeat2 += inverseFlagAbbr + s.entity1NE + unitDelimiter;
				middleWords = "";
				middleTags = "";
				for (int i = s.entity1Index + 1; i < s.entity2Index; i++) {
					phraseLvFeat1 += inverseFlagAbbr;
					phraseLvFeat2 += inverseFlagAbbr;
					for (Word w : s.phrases.get(i).words) {
						wordLvFeat1 += inverseFlagAbbr;
						wordLvFeat2 += inverseFlagAbbr;
						middleWords += w.wText + " ";
						middleTags += w.tag + " ";
						wordLvFeat1 += w.wText;
						wordLvFeat2 += w.tag;
						wordLvFeat1 += unitDelimiter;
						wordLvFeat2 += unitDelimiter;

						phraseLvFeat1 += w.wText + " ";
						phraseLvFeat2 += w.tag + " ";
					}
					phraseLvFeat1 = phraseLvFeat1.substring(0,
							phraseLvFeat1.length() - 1)
							+ unitDelimiter;
					phraseLvFeat2 = phraseLvFeat2.substring(0,
							phraseLvFeat2.length() - 1)
							+ unitDelimiter;
				}
				middleWords = middleWords
						.substring(0, middleWords.length() - 1);
				middleTags = middleTags.substring(0, middleTags.length() - 1);

				wordLvFeat1 += inverseFlagAbbr + s.entity2NE;
				wordLvFeat2 += inverseFlagAbbr + s.entity2NE;
				phraseLvFeat1 += inverseFlagAbbr + s.entity2NE;
				phraseLvFeat2 += inverseFlagAbbr + s.entity2NE;
				chunkLvFeat1 += inverseFlagAbbr + middleWords + unitDelimiter
						+ inverseFlagAbbr + s.entity2NE;
				chunkLvFeat2 += inverseFlagAbbr + middleTags + unitDelimiter
						+ inverseFlagAbbr + s.entity2NE;

				// a window of k words to the left/right of entity1/2 and their
				// part
				// of speech tags
				entity1 = s.phrases.get(s.entity1Index);
				entity1FirstWordIndex = entity1.words.get(0).index;
				entity2 = s.phrases.get(s.entity2Index);
				entity2LastWordIndex = entity2.words
						.get(entity2.words.size() - 1).index;

				leftWords = new ArrayList<Word>();
				rightWords = new ArrayList<Word>();

				wordLvFeat1 = unitDelimiter + wordLvFeat1 + unitDelimiter;
				wordLvFeat2 = unitDelimiter + wordLvFeat2 + unitDelimiter;
				chunkLvFeat1 = unitDelimiter + chunkLvFeat1 + unitDelimiter
						+ inverseFlagAbbr;
				chunkLvFeat2 = unitDelimiter + chunkLvFeat2 + unitDelimiter
						+ inverseFlagAbbr;

				for (int i = 1; i <= countOfWindowNodes; i++) {
					index = entity1FirstWordIndex - i;
					if (index >= 0) {
						label = s.words.get(index);
						leftWords
								.add(new Word(label.tag(), index, label.word()));
						wordLvFeat1 = unitDelimiter + inverseFlagAbbr
								+ label.word() + wordLvFeat1;
						wordLvFeat2 = unitDelimiter + inverseFlagAbbr
								+ label.tag() + wordLvFeat2;
						chunkLvFeat1 = " " + label.word() + chunkLvFeat1;
						chunkLvFeat2 = " " + label.tag() + chunkLvFeat2;
					} else {
						leftWords.add(new Word("#PAD#", index, "#PAD#"));
						if (includePadInFeatures) {
							wordLvFeat1 = unitDelimiter + inverseFlagAbbr
									+ "#PAD#" + wordLvFeat1;
							wordLvFeat2 = unitDelimiter + inverseFlagAbbr
									+ "#PAD#" + wordLvFeat2;
							chunkLvFeat1 = " " + "#PAD#" + chunkLvFeat1;
							chunkLvFeat2 = " " + "#PAD#" + chunkLvFeat2;
						}
					}

					index = entity2LastWordIndex + i;
					if (index < s.words.size()) {
						label = s.words.get(index);
						rightWords.add(new Word(label.tag(), index, label
								.word()));
						wordLvFeat1 = wordLvFeat1 + inverseFlagAbbr
								+ label.word() + unitDelimiter;
						wordLvFeat2 = wordLvFeat2 + inverseFlagAbbr
								+ label.tag() + unitDelimiter;
						chunkLvFeat1 = chunkLvFeat1 + label.word() + " ";
						chunkLvFeat2 = chunkLvFeat2 + label.tag() + " ";
					} else {
						rightWords.add(new Word("#PAD#", index, "#PAD#"));
						if (includePadInFeatures) {
							wordLvFeat1 = wordLvFeat1 + inverseFlagAbbr
									+ "#PAD#" + unitDelimiter;
							wordLvFeat2 = wordLvFeat2 + inverseFlagAbbr
									+ "#PAD#" + unitDelimiter;
							chunkLvFeat1 = chunkLvFeat1 + "#PAD#" + " ";
							chunkLvFeat2 = chunkLvFeat2 + "#PAD#" + " ";
						}
					}

				}

				// final processing of chunk-level features
				int inverseFlagAbbrLength = inverseFlagAbbr.length();

				if (!chunkLvFeat1.substring(0, unitDelimiterLength).equals(
						unitDelimiter))
					chunkLvFeat1 = inverseFlagAbbr + chunkLvFeat1.substring(1);
				else
					chunkLvFeat1 = chunkLvFeat1.substring(unitDelimiterLength);
				if (chunkLvFeat1.substring(
						chunkLvFeat1.length() - inverseFlagAbbrLength).equals(
						inverseFlagAbbr))
					chunkLvFeat1 = chunkLvFeat1.substring(0,
							chunkLvFeat1.length() - inverseFlagAbbrLength);
				else
					chunkLvFeat1 = chunkLvFeat1.substring(0,
							chunkLvFeat1.length() - 1);
				if (!chunkLvFeat2.substring(0, unitDelimiterLength).equals(
						unitDelimiter))
					chunkLvFeat2 = inverseFlagAbbr + chunkLvFeat2.substring(1);
				else
					chunkLvFeat2 = chunkLvFeat2.substring(unitDelimiterLength);
				if (chunkLvFeat2.substring(
						chunkLvFeat2.length() - inverseFlagAbbrLength).equals(
						inverseFlagAbbr))
					chunkLvFeat2 = chunkLvFeat2.substring(0,
							chunkLvFeat2.length() - inverseFlagAbbrLength);
				else
					chunkLvFeat2 = chunkLvFeat2.substring(0,
							chunkLvFeat2.length() - 1);

				// final processing of phrase-level features
				int accumulate = 0, cur, diff;
				String before1, before2, before, after;
				Phrase p;
				Word w2;
				if (s.entity1Index != 0) {
					accumulate = 0;
					cur = s.entity1Index - 1;
					while (accumulate < countOfWindowNodes && cur >= 0) {
						phraseLvFeat1 = unitDelimiter + phraseLvFeat1;
						phraseLvFeat2 = unitDelimiter + phraseLvFeat2;
						before1 = "";
						before2 = "";
						p = s.phrases.get(cur);
						diff = countOfWindowNodes - accumulate;
						if (diff >= p.words.size()) {
							for (Word w : p.words) {
								before1 += w.wText + " ";
								before2 += w.tag + " ";
							}
							phraseLvFeat1 = inverseFlagAbbr
									+ before1
											.substring(0, before1.length() - 1)
									+ phraseLvFeat1;
							phraseLvFeat2 = inverseFlagAbbr
									+ before2
											.substring(0, before2.length() - 1)
									+ phraseLvFeat2;
						} else {
							for (int i = 0; i < diff; i++) {
								w2 = p.words.get(p.words.size() - 1 - i);
								phraseLvFeat1 = " " + w2.wText + phraseLvFeat1;
								phraseLvFeat2 = " " + w2.tag + phraseLvFeat2;
							}
							phraseLvFeat1 = inverseFlagAbbr
									+ phraseLvFeat1.substring(1);
							phraseLvFeat2 = inverseFlagAbbr
									+ phraseLvFeat2.substring(1);
						}

						// not true in else-clause, but works fine
						accumulate += p.words.size();
						cur--;
					}
				}

				// padding
				if (includePadInFeatures) {
					diff = countOfWindowNodes - accumulate;
					if (diff > 0) {
						before = inverseFlagAbbr;
						for (int i = 0; i < diff; i++) {
							before += "#PAD# ";
						}
						phraseLvFeat1 = before
								.substring(0, before.length() - 1)
								+ unitDelimiter + phraseLvFeat1;
						phraseLvFeat2 = before
								.substring(0, before.length() - 1)
								+ unitDelimiter + phraseLvFeat2;
					}
				}

				int pSize = s.phrases.size();
				if (s.entity2Index != pSize) {
					accumulate = 0;
					cur = s.entity2Index + 1;
					while (accumulate < countOfWindowNodes && cur < pSize) {
						phraseLvFeat1 = phraseLvFeat1 + unitDelimiter
								+ inverseFlagAbbr;
						phraseLvFeat2 = phraseLvFeat2 + unitDelimiter
								+ inverseFlagAbbr;
						p = s.phrases.get(cur);
						diff = countOfWindowNodes - accumulate;
						if (diff >= p.words.size()) {
							for (Word w : p.words) {
								phraseLvFeat1 = phraseLvFeat1 + w.wText + " ";
								phraseLvFeat2 = phraseLvFeat2 + w.tag + " ";
							}
						} else {
							for (int i = 0; i < diff; i++) {
								w2 = p.words.get(i);
								phraseLvFeat1 = phraseLvFeat1 + w2.wText + " ";
								phraseLvFeat2 = phraseLvFeat2 + w2.tag + " ";
							}
						}
						phraseLvFeat1 = phraseLvFeat1.substring(0,
								phraseLvFeat1.length() - 1);
						phraseLvFeat2 = phraseLvFeat2.substring(0,
								phraseLvFeat2.length() - 1);
						// not true in else-clause, but works fine
						accumulate += p.words.size();
						cur++;
					}
				}

				// padding
				if (includePadInFeatures) {
					diff = countOfWindowNodes - accumulate;
					if (diff > 0) {
						after = inverseFlagAbbr;
						for (int i = 0; i < diff; i++) {
							after += "#PAD# ";
						}
						phraseLvFeat1 += unitDelimiter
								+ after.substring(0, after.length() - 1);
						phraseLvFeat2 += unitDelimiter
								+ after.substring(0, after.length() - 1);
					}
				}

				// final processing of word-level features
				wordLvFeat1 = wordLvFeat1
						.substring(1, wordLvFeat1.length() - 1);
				wordLvFeat2 = wordLvFeat2
						.substring(1, wordLvFeat2.length() - 1);

				String wordFeatureStem, tagFeatureStem;
				// sentence-level lexical conjunction features
				// word features
				// example feature:
				// "inverse_true|Brothers ,|ORGANIZATION|, Bear Stearns and|ORGANIZATION|. B_1"
				wordFeatureStem = s.entity1NE + "|" + middleWords + "|"
						+ s.entity2NE;
				sentenceLvFeats += "feature: " + inverseFlag + "|"
						+ wordFeatureStem + newLine;
				for (int i = 0; i < countOfWindowNodes; i++) {
					if (i == 0)
						wordFeatureStem = leftWords.get(i).wText + "|"
								+ wordFeatureStem + "|"
								+ rightWords.get(i).wText;
					else
						wordFeatureStem = leftWords.get(i).wText + " "
								+ wordFeatureStem + " "
								+ rightWords.get(i).wText;
					sentenceLvFeats += "feature: " + inverseFlag + "|"
							+ wordFeatureStem + newLine;
				}

				// tag features
				// example feature:
				// "inverse_true|Brothers ,|ORGANIZATION|, NNP NNP CC|ORGANIZATION|. B_1"
				// comment: changes left/right words to left/right tags
				tagFeatureStem = s.entity1NE + "|" + middleTags + "|"
						+ s.entity2NE;
				sentenceLvFeats += "feature: " + inverseFlag + "|"
						+ tagFeatureStem + newLine;
				for (int i = 0; i < countOfWindowNodes; i++) {
					if (i == 0)
						tagFeatureStem = leftWords.get(i).tag + "|"
								+ tagFeatureStem + "|" + rightWords.get(i).tag;
					else
						tagFeatureStem = leftWords.get(i).tag + " "
								+ tagFeatureStem + " " + rightWords.get(i).tag;
					sentenceLvFeats += "feature: " + inverseFlag + "|"
							+ tagFeatureStem + newLine;
				}

				// sentence-level bag of words features
				// word indices and tag indices
				String[] splits1, splits2;
				String word, tag, stemmedWord;
				sLvWordIndices = "";
				sLvTagIndices = "";
				splits1 = middleWords.split(" ");
				splits2 = middleTags.split(" ");

				for (int i = 0; i < splits1.length; i++) {
					word = splits1[i];
					sLvWordIndices += getIndex(word, "w");

					tag = splits2[i];
					sLvTagIndices += getIndex(tag, "t");
				}

				for (int i = 0; i < leftWords.size(); i++) {
					word = leftWords.get(i).wText;
					sLvWordIndices = getIndex(word, "w") + sLvWordIndices;

					tag = leftWords.get(i).tag;
					sLvTagIndices = getIndex(tag, "t") + sLvTagIndices;
				}

				for (int i = 0; i < rightWords.size(); i++) {
					word = rightWords.get(i).wText;
					sLvWordIndices += getIndex(word, "w");

					tag = rightWords.get(i).tag;

					sLvTagIndices += getIndex(tag, "t");
				}

				// sentence-level bag of words features: dep indices
				sLvDepTypeIndices = "";
				sLvDepWordIndices = "";

				// sentence-level syntactic conjunction features
				String strFeatureStem = "", depFeatureStem = "", dirFeatureStem = "";
				/**
				 * Sets a threshold here in the future to deal with a long
				 * dependency path, and *LONG-PATH* for str and dir syntactic
				 * features, *LONG-TYPE* for dep syntactic features.
				 */

				/**
				 * Reasons for not doing long-path: 1. Takes time and effect is
				 * not clear. 2. On average, a sentence is not very long due to
				 * nature of abstract.
				 */
				String arrow, type;
				for (Map.Entry<Integer, ArrayList<TypedDependencyProperty>> m : s.path
						.entrySet()) {
					index = m.getKey();
					tdpArr = m.getValue();
					// ←↑→↓↖↙↗↘↕
					// format: "[dependency-type]""arrow(<-/->)""word"...
					if (index == -1) {
						strFeatureStem += "[" + tdpArr.get(0).relation + "]"
								+ "->";
						depFeatureStem += "[" + tdpArr.get(0).relation + "]"
								+ "->";
						dirFeatureStem += "->";
						sLvDepTypeIndices += getIndex(tdpArr.get(0).relation,
								"d");
					} else if (index == -2) {
						strFeatureStem += "[" + tdpArr.get(0).relation + "]"
								+ "<-";
						depFeatureStem += "[" + tdpArr.get(0).relation + "]"
								+ "<-";
						dirFeatureStem += "<-";
						sLvDepTypeIndices += getIndex(tdpArr.get(0).relation,
								"d");
					} else {
						word = s.words.get(index - 1).word();
						sLvDepWordIndices += getIndex(word, "w");
						for (TypedDependencyProperty tdp : tdpArr) {
							if (tdp.position) {
								// left case
								// word = "<--" + tdp.relation + "-" + word;
								word = "[" + tdp.relation + "]" + "<-" + word;
								depFeatureStem += "[" + tdp.relation + "]"
										+ "<-";
								dirFeatureStem += "<-";
							} else {
								// right case
								// word = word + "-" + tdp.relation + "-->";
								word = word + "[" + tdp.relation + "]" + "->";
								depFeatureStem += "[" + tdp.relation + "]"
										+ "->";
								dirFeatureStem += "->";
							}
							sLvDepTypeIndices += getIndex(tdp.relation, "d");
						}
						strFeatureStem += word;
					}
				}

				strFeatureStem = s.entity1NE + "|" + strFeatureStem + "|"
						+ s.entity2NE;
				depFeatureStem = s.entity1NE + "|" + depFeatureStem + "|"
						+ s.entity2NE;
				dirFeatureStem = s.entity1NE + "|" + dirFeatureStem + "|"
						+ s.entity2NE;

				// no window node
				sentenceLvFeats += "feature: " + "str:" + inverseFlag + "|"
						+ strFeatureStem + newLine;
				sentenceLvFeats += "feature: " + "dep:" + inverseFlag + "|"
						+ depFeatureStem + newLine;
				sentenceLvFeats += "feature: " + "dir:" + inverseFlag + "|"
						+ dirFeatureStem + newLine;

				// having left window nodes
				TypedDependencyProperty tdp;
				for (Map.Entry<Integer, TypedDependencyProperty> entry : s.entity1Dependencies
						.entrySet()) {
					index = entry.getKey();
					tdp = entry.getValue();
					word = s.words.get(index - 1).word();
					sLvDepWordIndices = getIndex(word, "w") + sLvDepWordIndices;
					// format "word""[dependency-type]""arrow""feature-stem"
					if (tdp.position)
						arrow = "<-";
					else
						arrow = "->";
					type = "[" + tdp.relation + "]";
					sLvDepTypeIndices = getIndex(tdp.relation, "d")
							+ sLvDepTypeIndices;
					sentenceLvFeats += "feature: " + "str:" + inverseFlag + "|"
							+ word + type + arrow + "|" + strFeatureStem
							+ newLine;
					sentenceLvFeats += "feature: " + "dep:" + inverseFlag + "|"
							+ type + arrow + "|" + depFeatureStem + newLine;
					sentenceLvFeats += "feature: " + "dir:" + inverseFlag + "|"
							+ arrow + "|" + dirFeatureStem + newLine;
				}

				// having right window nodes
				for (Map.Entry<Integer, TypedDependencyProperty> entry : s.entity2Dependencies
						.entrySet()) {
					index = entry.getKey();
					tdp = entry.getValue();
					word = s.words.get(index - 1).word();
					sLvDepWordIndices += getIndex(word, "w");
					// format "word""[dependency-type]""arrow""feature-stem"
					if (tdp.position)
						arrow = "<-";
					else
						arrow = "->";
					type = "[" + tdp.relation + "]";
					sLvDepTypeIndices += getIndex(word, "d");
					sentenceLvFeats += "feature: " + "str:" + inverseFlag + "|"
							+ strFeatureStem + "|" + type + arrow + word
							+ newLine;
					sentenceLvFeats += "feature: " + "dep:" + inverseFlag + "|"
							+ depFeatureStem + "|" + type + arrow + newLine;
					sentenceLvFeats += "feature: " + "dir:" + inverseFlag + "|"
							+ dirFeatureStem + "|" + arrow + newLine;
				}

				// having window nodes on both sides
				int index1, index2;
				String word1, word2, arrow1, arrow2, type1, type2;
				TypedDependencyProperty tdp1, tdp2;
				for (Map.Entry<Integer, TypedDependencyProperty> entry1 : s.entity1Dependencies
						.entrySet()) {
					index1 = entry1.getKey();
					tdp1 = entry1.getValue();
					word1 = s.words.get(index1 - 1).word();
					if (tdp1.position)
						arrow1 = "<-";
					else
						arrow1 = "->";
					type1 = "[" + tdp1.relation + "]";
					for (Map.Entry<Integer, TypedDependencyProperty> entry2 : s.entity2Dependencies
							.entrySet()) {
						index2 = entry2.getKey();
						tdp2 = entry2.getValue();
						word2 = s.words.get(index2 - 1).word();
						if (tdp2.position)
							arrow2 = "<-";
						else
							arrow2 = "->";
						type2 = "[" + tdp2.relation + "]";

						sentenceLvFeats += "feature: " + "str:" + inverseFlag
								+ "|" + word1 + type1 + arrow1 + "|"
								+ strFeatureStem + "|" + type2 + arrow2 + word2
								+ newLine;
						sentenceLvFeats += "feature: " + "dep:" + inverseFlag
								+ "|" + type1 + arrow1 + "|" + depFeatureStem
								+ "|" + type2 + arrow2 + newLine;
						sentenceLvFeats += "feature: " + "dir:" + inverseFlag
								+ "|" + arrow1 + "|" + dirFeatureStem + "|"
								+ arrow2 + newLine;
					}
				}

				// sentence-level bag of words features
				sentenceLvFeats += "bag-of-words-features:" + newLine;
				sentenceLvFeats += "bow-word-feature: " + sLvWordIndices
						+ newLine;
				sentenceLvFeats += "bow-tag-feature: " + sLvTagIndices
						+ newLine;
				sentenceLvFeats += "bow-dep-feature: " + sLvDepTypeIndices
						+ newLine;
				sentenceLvFeats += "bow-dep-word-feature: " + sLvDepWordIndices
						+ newLine;
				sentenceLvFeats += "</sentence-level-features>" + newLine;

				// chunk-level features
				chunkLvFeats += "<chunk-level-features>" + newLine;
				// both chunkLvFeat1 and chunkLvFeat2 are '' ended
				chunkLvFeats += "word-features:"
						+ newLine
						+ addIndicesToFeatureGroup(chunkLvFeat1, "w",
								inverseFlagAbbr, newLine) + newLine;
				chunkLvFeats += "tag-features:"
						+ newLine
						+ addIndicesToFeatureGroup(chunkLvFeat2, "t",
								inverseFlagAbbr, newLine) + newLine;
				chunkLvFeats += "</chunk-level-features>" + newLine;

				// phrase-level features
				phraseLvFeats += "<phrase-level-features>" + newLine;
				phraseLvFeats += "word-features:"
						+ newLine
						+ addIndicesToFeatureGroup(phraseLvFeat1, "w",
								inverseFlagAbbr, newLine) + newLine;
				phraseLvFeats += "tag-features:"
						+ newLine
						+ addIndicesToFeatureGroup(phraseLvFeat2, "t",
								inverseFlagAbbr, newLine) + newLine;
				phraseLvFeats += "</phrase-level-features>" + newLine;

				// word-level features
				wordLvFeats += "<word-level-features>" + newLine;
				// both wordLvFeat1 and wordLvFeat2 are '' ended
				wordLvFeats += "word-features:"
						+ newLine
						+ addIndicesToFeatureGroup(wordLvFeat1, "w",
								inverseFlagAbbr, newLine) + newLine;
				wordLvFeats += "tag-features:"
						+ newLine
						+ addIndicesToFeatureGroup(wordLvFeat2, "t",
								inverseFlagAbbr, newLine) + newLine;
				wordLvFeats += "</word-level-features>" + newLine;

				// footer
				footer += "</instance>" + newLine;

				// System.out.print(header);
				// System.out.print(sentenceLvFeats);
				// System.out.print(chunkLvFeats);
				// System.out.print(phraseLvFeats);
				// System.out.print(wordLvFeats);
				// System.out.print(footer);
				instance = header + sentenceLvFeats + chunkLvFeats
						+ phraseLvFeats + wordLvFeats + footer;
				if (outputMethod)
					bw.write(instance);
				else
					System.out.print(instance);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error sentence is: " + s.sentenceText);
			}
		}
		if (outputMethod)
			bw.close();
	}

	// featGroup is "" ended
	private String addIndicesToFeatureGroup(String featGroup, String type,
			String invFlag, String delimiter) {
		// ret is "" ended
		String ret = "";
		String[] lines, tokens;
		String line, token;
		int flagSize = invFlag.length();
		String index;

		lines = featGroup.split(delimiter);
		for (int i = 0; i < lines.length; i++) {
			line = lines[i];
			ret += line + delimiter;
			ret += "bow: ";
			line = line.substring(flagSize);
			tokens = line.split(" ");
			for (int j = 0; j < tokens.length; j++) {
				token = tokens[j];
				index = getIndex(token, type);
				ret += index;
			}
			ret += delimiter;
		}

		ret = ret.substring(0, ret.length() - delimiter.length());

		return ret;
	}

	public void writeDemoFeatures() {
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
					// sf1 += "→" + tdpArr.get(0).relation + " ";
					sf1 += "-" + tdpArr.get(0).relation + "-->" + " ";
				else if (index == -2)
					// sf1 += "←" + tdpArr.get(0).relation + " ";
					sf1 += "<--" + tdpArr.get(0).relation + "-" + " ";
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

	private String getIndex(String token, String type) {
		int index = -1;
		if (type.equals("w")) {
			String stemmedWord;
			stemmer.setCurrent(token);
			stemmer.stem();
			stemmedWord = stemmer.getCurrent();
			index = wordIndex.indexOf(stemmedWord);
		}

		if (type.equals("t")) {
			index = tagIndex.indexOf(token);
		}

		if (type.equals("d")) {
			index = depIndex.indexOf(token);
		}

		if (index != -1)
			return "" + index + " ";
		else
			return "";
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
