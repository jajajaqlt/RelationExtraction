package pipeline;

import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;
import info.olteanu.interfaces.StringFilter;
import info.olteanu.utils.TextNormalizer;

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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import pipeline.ClassUtilities.Candidate;
import pipeline.ClassUtilities.PreCandidate;

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
	public static HashMap<String, String> metaNetRelMap;
	SemanticNetwork semanticNet;

	public AbstractsToCandidates(String relationMappingFile,
			String semanticNetworkFile, String semanticTypeAbbreviationFile,
			String abstractsFile, String metaRelationsFile) throws Exception {
		this.relationMappingFile = relationMappingFile;
		this.semanticNetworkFile = semanticNetworkFile;
		this.semanticTypeAbbreviationFile = semanticTypeAbbreviationFile;
		this.abstractsFile = abstractsFile;
		this.metaRelationsFile = metaRelationsFile;

		processRelationMappingFile(relationMappingFile);
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
			// deals with non-ascii characters here
			boolean isAscii = CharMatcher.ASCII.matchesAllOf(line);
			if (!isAscii)
				line = normalizeString(line);

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
			// gets first phrase since i that has a mapping
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
		List<Mapping> mappingList1, mappingList2;

		mappingList1 = phrase1.getMappingList();
		mappingList2 = phrase2.getMappingList();

		int mappingIndex, evIndex, sTypeIndex;

		// Generates all pre-candidates for phrase1
		PreCandidate preCandidate1;
		ArrayList<PreCandidate> tempList;
		// ArrayList<PreCandidate> list1 = new ArrayList<PreCandidate>();
		ArrayList<ArrayList<PreCandidate>> EvList1 = new ArrayList<ArrayList<PreCandidate>>();
		mappingIndex = 0;
		for (Mapping map1 : mappingList1) {
			evIndex = 0;
			for (Ev ev1 : map1.getEvList()) {
				sTypeIndex = 0;
				tempList = new ArrayList<PreCandidate>();
				for (String type1 : ev1.getSemanticTypes()) {
					preCandidate1 = new PreCandidate();
					preCandidate1.sType = type1;
					preCandidate1.cui = ev1.getConceptId();
					preCandidate1.pos = ev1.getPositionalInfo();
					preCandidate1.phraseIndex = phrase1Index;
					preCandidate1.mappingIndex = mappingIndex;
					preCandidate1.evIndex = evIndex;
					preCandidate1.sTypeIndex = sTypeIndex;
					tempList.add(preCandidate1);
					sTypeIndex++;
				}
				EvList1.add(tempList);
				evIndex++;
			}
			mappingIndex++;
		}

		// Generates all pre-candidates for phrase2
		PreCandidate preCandidate2;
		// ArrayList<PreCandidate> list2 = new ArrayList<PreCandidate>();
		ArrayList<ArrayList<PreCandidate>> EvList2 = new ArrayList<ArrayList<PreCandidate>>();
		mappingIndex = 0;
		for (Mapping map2 : mappingList2) {
			evIndex = 0;
			for (Ev ev2 : map2.getEvList()) {
				sTypeIndex = 0;
				tempList = new ArrayList<PreCandidate>();
				for (String type2 : ev2.getSemanticTypes()) {
					preCandidate2 = new PreCandidate();
					preCandidate2.sType = type2;
					preCandidate2.cui = ev2.getConceptId();
					preCandidate2.pos = ev2.getPositionalInfo();
					preCandidate2.phraseIndex = phrase2Index;
					preCandidate2.mappingIndex = mappingIndex;
					preCandidate2.evIndex = evIndex;
					preCandidate2.sTypeIndex = sTypeIndex;
					tempList.add(preCandidate2);
					sTypeIndex++;
				}
				EvList2.add(tempList);
				evIndex++;
			}
			mappingIndex++;
		}

		String sTypePair, invSTypePair, cuiPair = null, invCUIPair = null;
		ArrayList<String> metaRelations;
		String netRelations;
		String[] netRelationSplits;
		String netRelation;
		Set<String> heldNetRelationsSet = null, invHeldNetRelationsSet = null, potentialNetRelationSet;
		boolean cuiPairFirstAppearance;

		// PreCandidate preCandid1, preCandid2;
		for (ArrayList<PreCandidate> tempList1 : EvList1) {
			for (ArrayList<PreCandidate> tempList2 : EvList2) {
				cuiPairFirstAppearance = true;

				for (PreCandidate preCandid1 : tempList1) {
					for (PreCandidate preCandid2 : tempList2) {
						Candidate candidate;
						preCandid1.rootSType = semanticNet.abbrRootAbbrMap
								.get(preCandid1.sType);
						preCandid2.rootSType = semanticNet.abbrRootAbbrMap
								.get(preCandid2.sType);

						sTypePair = preCandid1.sType + "&" + preCandid2.sType;
						invSTypePair = preCandid2.sType + "&"
								+ preCandid1.sType;

						if (cuiPairFirstAppearance) {
							cuiPair = preCandid1.cui + "&" + preCandid2.cui;
							invCUIPair = preCandid2.cui + "&" + preCandid1.cui;

							// positive examples - forward direction
							metaRelations = cuiPairRelationMap.get(cuiPair);
							heldNetRelationsSet = new HashSet<String>();
							if (metaRelations != null) {
								for (String metaRel : metaRelations) {
									netRelation = metaNetRelMap.get(metaRel);
									heldNetRelationsSet.add(netRelation);
									candidate = new Candidate(utterance,
											preCandid1, preCandid2, true,
											false, netRelation, metaRel);
									aFewCandidates.add(candidate);
								}
							}

							// positive examples - inverse direction
							metaRelations = cuiPairRelationMap.get(invCUIPair);
							invHeldNetRelationsSet = new HashSet<String>();
							if (metaRelations != null) {
								for (String metaRel : metaRelations) {
									netRelation = metaNetRelMap.get(metaRel);
									invHeldNetRelationsSet.add(netRelation);
									candidate = new Candidate(utterance,
											preCandid1, preCandid2, true, true,
											netRelation, metaRel);
									aFewCandidates.add(candidate);
								}
							}

							cuiPairFirstAppearance = false;
						}

						// checks negative examples of other relations - forward
						// direction
						netRelations = stypePairRelationMap.get(sTypePair);
						if (netRelations != null) {
							netRelationSplits = netRelations.split("&");
							potentialNetRelationSet = new HashSet<String>(
									Arrays.asList(netRelationSplits));
							potentialNetRelationSet
									.removeAll(heldNetRelationsSet);
							for (String rel : potentialNetRelationSet) {
								netRelation = rel;
								candidate = new Candidate(utterance,
										preCandid1, preCandid2, false, false,
										netRelation, null);
								aFewCandidates.add(candidate);
							}
						}

						// checks negative examples of other relations - inverse
						// direction
						netRelations = stypePairRelationMap.get(invSTypePair);
						if (netRelations != null) {
							netRelationSplits = netRelations.split("&");
							potentialNetRelationSet = new HashSet<String>(
									Arrays.asList(netRelationSplits));
							potentialNetRelationSet
									.removeAll(invHeldNetRelationsSet);
							for (String rel : potentialNetRelationSet) {
								netRelation = rel;
								candidate = new Candidate(utterance,
										preCandid1, preCandid2, false, true,
										netRelation, null);
								aFewCandidates.add(candidate);
							}
						}
						// end of of checking negative examples of other
						// relations - inverse direction
					}
				}
				// end of pre-candidate double loop
			}
		}
		// end of ev-list double loop
		return aFewCandidates;
	}

	public Candidate deepCopyCandidate(Candidate candid) {
		// Candidate candidate = new Candidate(candid.utteranceText,
		// candid.netRelation, candid.isInverse, candid.prevCUI,
		// candid.succCUI, candid.prevConceptPosition,
		// candid.succConceptPosition, candid.isPositive,
		// candid.metaRelation);
		Candidate candidate = new Candidate(candid.utterance, candid.prev,
				candid.succ, candid.isPositive, candid.isInverse,
				candid.netRelation, candid.metaRelation);
		return candidate;
	}

	public void processRelationMappingFile(String relationMappingFile)
			throws Exception {
		netMetaRelMap = new HashMap<String, ArrayList<String>>();
		metaNetRelMap = new HashMap<String, String>();
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
				netMetaRelMap.put(netRelation, arr);
				for (String metaRel : arr)
					metaNetRelMap.put(metaRel, netRelation);
			}
			isFirst = !isFirst;
		}
		br.close();
	}

	/**
	 *
	 * @param str
	 * @return Normalized version of str with accented characters replaced by
	 *         unaccented version and with diacritics removed. E.g. Ö -> O
	 */
	public static String normalizeString(String str)
			throws ClassNotFoundException {
		// TextNormalizer code from phramer.org
		// Allows compilation under both Java 5 and Java 6
		StringFilter stringFilter = TextNormalizer
				.getNormalizationStringFilter();
		String nfdNormalizedString = stringFilter.filter(str);

		// Normalizer is Java 6 only
		// String nfdNormalizedString = java.text.Normalizer.normalize(str,
		// Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}
}
