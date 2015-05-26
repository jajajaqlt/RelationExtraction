package pipeline;

import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Position;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
//import java.lang.reflect.Array;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.CharMatcher;

//import java.util.Map;
/**
 * 
 * @author lq4
 *
 */
public class AbstractsToCandidates {

	// public static String relationMappingFile = "NETMETA";
	// public static String semanticNetworkFile = "SRSTR";
	// public static String semanticTypeAbbreviationFile = "SRDEF";
	public static String relationMappingFile;
	public static String semanticNetworkFile;
	public static String semanticTypeAbbreviationFile;
	public static String abstractsFile;
	public static String metaRelationsFile;
	// in the format of <"stype1&stype2","netRel1&netRel2&...">
	// the first type has relation to the second type
	public static HashMap<String, String> stypePairRelationMap;
	// in the format of <"cui1&cui2","metaRel1&metaRel2&...">
	// the first cui has relation to the second cui
	public static HashMap<String, ArrayList<String>> cuiPairRelationMap;
	// connects net relation to meta relations, in the format of
	// <"netRel1",["metaRel1","metaRel2",..]>
	public static HashMap<String, ArrayList<String>> netMetaRelMap;
	SemanticNetwork semanticNet;

	public AbstractsToCandidates(String relationMappingFile,
			String semanticNetworkFile, String semanticTypeAbbreviationFile,
			String abstractsFile, String metaRelationsFile) throws Exception {
		this.relationMappingFile = relationMappingFile;
		this.semanticNetworkFile = semanticNetworkFile;
		this.semanticTypeAbbreviationFile = semanticTypeAbbreviationFile;
		this.abstractsFile = abstractsFile;
		this.metaRelationsFile = metaRelationsFile;

		netMetaRelMap = getNetMetaRelMap(relationMappingFile);
		String[] netRelations = Arrays.copyOf(netMetaRelMap.keySet().toArray(),
				netMetaRelMap.keySet().size(), String[].class);
		// System.out.println(System.currentTimeMillis());
		semanticNet = new SemanticNetwork(semanticNetworkFile,
				semanticTypeAbbreviationFile, netRelations);
		// System.out.println(System.currentTimeMillis());
		stypePairRelationMap = semanticNet.stypePairRelationMap;

		ArrayList<String> tmp = new ArrayList<String>();
		for (String str : netMetaRelMap.keySet()) {
			tmp.addAll(netMetaRelMap.get(str));
		}
		// System.out.println(System.currentTimeMillis());
		Metathesaurus meta = new Metathesaurus(metaRelationsFile, tmp);
		// System.out.println(System.currentTimeMillis());
		cuiPairRelationMap = meta.cuiPairRelationMap;
	}

	/**
	 * Set MetaMap server options using a string of form:
	 *
	 * <pre>
	 * &quot;-option1 optional-argument1 -option2 optional-argument2&quot;
	 * </pre>
	 * 
	 * examples:
	 * 
	 * <pre>
	 *    "-yD" or "-y -D" or "-J SNOMEDCT" or "--restrict_to_sources SNOMEDCT"
	 * </pre>
	 * <p>
	 * Set MetaMap server options. State of options are preserved in subsequent
	 * invocations of processCitationsFromString(String),
	 * processCitationsFromReader(Reader), and processCitationsFromFile(String).
	 * <p>
	 * 
	 * @param optionString
	 *            a string of MetaMap options
	 */
	public ArrayList<Candidate> getCandidates() throws Exception {
		ArrayList<Candidate> candidates = new ArrayList<Candidate>();
		BufferedReader br = new BufferedReader(new FileReader(new File(
				abstractsFile)));
		// each line is an abstract
		String line;
		MetaMapApi api = new MetaMapApiImpl(0);
		api.setOptions("-y");
		int i = 0;
		Result result;
		ArrayList<Candidate> someCandidates;
		System.out.println(System.currentTimeMillis());
		while ((line = br.readLine()) != null) {
			boolean isAscii = CharMatcher.ASCII.matchesAllOf(line);
			if (isAscii) {
				System.out.println("" + i + "th abstract:");
				System.out.println("start time: " + System.currentTimeMillis());
				List<Result> resultList = api.processCitationsFromString(line);
				// one 'line' has one abstract and one result
				result = resultList.get(0);
				// splits into utterances
				for (Utterance utterance : result.getUtteranceList()) {
					someCandidates = processUtterance(utterance);
					candidates.addAll(someCandidates);
				}
				System.out.println("end time: " + System.currentTimeMillis());
				i++;
			}
		}
		br.close();
		System.out.println(System.currentTimeMillis());
		return candidates;
	}

	private ArrayList<Candidate> processUtterance(Utterance utterance)
			throws Exception {
		ArrayList<Candidate> someCandidates = new ArrayList<Candidate>();
		ArrayList<Candidate> aFewCandidates = new ArrayList<Candidate>();
		List<PCM> pcmList = utterance.getPCMList();
		int pcmListSize = pcmList.size();
		PCM phrase1, phrase2;
		for (int i = 0; i < pcmListSize; i++) {
			phrase1 = pcmList.get(i);
			while (phrase1.getMappingList().size() == 0) {
				i++;
				if (i < pcmListSize - 1)
					phrase1 = pcmList.get(i);
				else
					break;
			}
			mainLoop: for (int j = i + 1; j < pcmListSize; j++) {
				phrase2 = pcmList.get(j);
				while (phrase2.getMappingList().size() == 0) {
					j++;
					if (j < pcmListSize)
						phrase2 = pcmList.get(j);
					else
						break mainLoop;
				}
				aFewCandidates = processTwoPhrases(phrase1, phrase2, utterance,
						i, j);
				someCandidates.addAll(aFewCandidates);
			}
		}
		return someCandidates;
	}

	private ArrayList<Candidate> processTwoPhrases(PCM phrase1, PCM phrase2,
			Utterance utterance, int phrase1Index, int phrase2Index)
			throws Exception {
		ArrayList<Candidate> aFewCandidates = new ArrayList<Candidate>();
		Candidate template, candidate;
		List<Mapping> mappingList1, mappingList2;
		// Mapping mapping1, mapping2;
		// Ev ev1, ev2;
		// String semanticType1, semanticType2;
		String sTypePair, invSTypePair, cuiPair, invCUIPair;
		String[] netRelationSplits;
		String netRelation;

		mappingList1 = phrase1.getMappingList();
		mappingList2 = phrase2.getMappingList();

		int mappingIndex, evIndex, sTypeIndex;
		PreCandidate preCandidate1;
		ArrayList<PreCandidate> list1 = new ArrayList<PreCandidate>();
		mappingIndex = 0;
		evIndex = 0;
		sTypeIndex = 0;
		for (Mapping map1 : mappingList1) {
			for (Ev ev1 : map1.getEvList()) {
				for (String type1 : ev1.getSemanticTypes()) {
					preCandidate1 = new PreCandidate();
					preCandidate1.sType = type1;
					preCandidate1.cui = ev1.getConceptId();
					preCandidate1.pos = ev1.getPositionalInfo();
					preCandidate1.phraseIndex = phrase1Index;
					preCandidate1.mappingIndex = mappingIndex;
					preCandidate1.evIndex = evIndex;
					preCandidate1.sTypeIndex = sTypeIndex;
					list1.add(preCandidate1);
					sTypeIndex++;
				}
				evIndex++;
			}
			mappingIndex++;
		}

		PreCandidate preCandidate2;
		ArrayList<PreCandidate> list2 = new ArrayList<PreCandidate>();
		mappingIndex = 0;
		evIndex = 0;
		sTypeIndex = 0;
		for (Mapping map2 : mappingList2) {
			for (Ev ev2 : map2.getEvList()) {
				for (String type2 : ev2.getSemanticTypes()) {
					preCandidate2 = new PreCandidate();
					preCandidate2.sType = type2;
					preCandidate2.cui = ev2.getConceptId();
					preCandidate2.pos = ev2.getPositionalInfo();
					preCandidate2.phraseIndex = phrase2Index;
					preCandidate2.mappingIndex = mappingIndex;
					preCandidate2.evIndex = evIndex;
					preCandidate2.sTypeIndex = sTypeIndex;
					list2.add(preCandidate2);
					sTypeIndex++;
				}
				evIndex++;
			}
			mappingIndex++;
		}

		ArrayList<String> metaRelations;
		for (PreCandidate preCandid1 : list1) {
			for (PreCandidate preCandid2 : list2) {
				sTypePair = preCandid1.sType + "&" + preCandid2.sType;
				invSTypePair = preCandid2.sType + "&" + preCandid1.sType;
				cuiPair = preCandid1.cui + "&" + preCandid2.cui;
				invCUIPair = preCandid2.cui + "&" + preCandid1.cui;
				if (stypePairRelationMap.get(sTypePair) != null) {
					// forward
					template = new Candidate(utterance, null, false,
							preCandid1, preCandid2, false, null);
					netRelationSplits = stypePairRelationMap.get(sTypePair)
							.split("&");
					for (int i = 0; i < netRelationSplits.length; i++) {
						netRelation = netRelationSplits[i];
						candidate = deepCopyCandidate(template);
						candidate.netRelation = netRelation;
						for (String metaRel : netMetaRelMap.get(netRelation)) {
							metaRelations = cuiPairRelationMap.get(cuiPair);
							// ???
							if (metaRelations != null) {
								if (cuiPairRelationMap.get(cuiPair).contains(
										metaRel)) {
									candidate.isPositive = true;
									candidate.metaRelation = metaRel;
									break;
								}
							}

						}
						// add root abbr semantic type
						candidate.prev.rootSType = semanticNet.abbrRootAbbrMap.get(candidate.prev.sType);
						candidate.succ.rootSType = semanticNet.abbrRootAbbrMap.get(candidate.succ.sType);
						aFewCandidates.add(candidate);
					}
				}
				if (stypePairRelationMap.get(invSTypePair) != null) {
					// inverse
					template = new Candidate(utterance, null, true, preCandid1,
							preCandid2, false, null);
					netRelationSplits = stypePairRelationMap.get(invSTypePair)
							.split("&");
					for (int i = 0; i < netRelationSplits.length; i++) {
						netRelation = netRelationSplits[i];
						candidate = deepCopyCandidate(template);
						candidate.netRelation = netRelation;
						for (String metaRel : netMetaRelMap.get(netRelation)) {
							// ???
							metaRelations = cuiPairRelationMap.get(invCUIPair);
							// ???
							if (metaRelations != null) {
								if (cuiPairRelationMap.get(invCUIPair)
										.contains(metaRel)) {
									candidate.isPositive = true;
									candidate.metaRelation = metaRel;
									break;
								}
							}

						}
						
						aFewCandidates.add(candidate);
					}

				}
			}
		}

		return aFewCandidates;
	}

	public class PreCandidate {
		public String cui;
		// rootAbbr
		public String rootSType;
		public String sType;
		public List<Position> pos;
		public int phraseIndex;
		public int mappingIndex;
		public int evIndex;
		public int sTypeIndex;
	}

	public class Candidate {
		// String utteranceText;
		public Utterance utterance;
		public String netRelation;
		public boolean isInverse;
		public PreCandidate prev;
		public PreCandidate succ;
		// String prevCUI, succCUI;
		// used in the future
		// int prevPhraseIndex, prevMappingIndex, prevEvIndex, prevSTypeIndex;
		// int succPhraseIndex, succMappingIndex, succEvIndex, succSTypeIndex;
		// List<Position> prevConceptPosition, succConceptPosition;
		public boolean isPositive;
		// assumption: cuisOrder is always same as isInverse
		// boolean cuisOrder
		public String metaRelation;

		// public Candidate(String text, String netRel, boolean isInv,
		// String prevCUI, String succCUI,
		// List<Position> prevConceptPosition,
		// List<Position> succConceptPosition, boolean isPos,
		// String metaRel)
		public Candidate(Utterance utt, String netRel, boolean isInv,
				PreCandidate prev, PreCandidate succ, boolean isPos,
				String metaRel) {
			utterance = utt;
			netRelation = netRel;
			isInverse = isInv;
			this.prev = prev;
			this.succ = succ;
			// this.prevCUI = prevCUI;
			// this.succCUI = succCUI;
			// this.prevConceptPosition = prevConceptPosition;
			// this.succConceptPosition = succConceptPosition;
			isPositive = isPos;
			metaRelation = metaRel;
		}

	}

	public Candidate deepCopyCandidate(Candidate candid) {
		// Candidate candidate = new Candidate(candid.utteranceText,
		// candid.netRelation, candid.isInverse, candid.prevCUI,
		// candid.succCUI, candid.prevConceptPosition,
		// candid.succConceptPosition, candid.isPositive,
		// candid.metaRelation);
		Candidate candidate = new Candidate(candid.utterance,
				candid.netRelation, candid.isInverse, candid.prev, candid.succ,
				candid.isPositive, candid.metaRelation);
		return candidate;
	}

	public HashMap<String, ArrayList<String>> getNetMetaRelMap(
			String relationMappingFile) throws Exception {
		HashMap<String, ArrayList<String>> ret = new HashMap<String, ArrayList<String>>();
		ArrayList<String> arr;
		BufferedReader br = new BufferedReader(new FileReader(new File(
				relationMappingFile)));
		String line;
		boolean isFirst = true;
		String netRelation = "";
		String[] metaRelations;
		while ((line = br.readLine()) != null) {
			if (isFirst) {
				netRelation = line;
			} else {
				metaRelations = line.split(" ");
				arr = new ArrayList<>(Arrays.asList(metaRelations));
				ret.put(netRelation, arr);
			}
			isFirst = !isFirst;
		}
		br.close();
		return ret;
	}

}
