package tests;

import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import pipeline.MetamapSerImpl;
import pipeline.MetamapSerImpl.ResultSerImpl;

import com.google.gson.Gson;

public class TestMetamapSerImpl {

	public static void main(String[] args) throws Exception {

		Result resultOriginal;
		Result resultTransformed;
		String line = "None of the 15 patients with supradiaphragmatic disease who received limited-field radiotherapy to regions that did not include the mediastinal or hilar nodes subsequently experienced relapse there.";

		MetaMapApi api = new MetaMapApiImpl(0);
		api.setOptions("-y");
		List<Result> resultList = api.processCitationsFromString(line);
		resultOriginal = resultList.get(0);
		List<Result> results = new ArrayList<Result>();
		results.add(resultOriginal);
		writeResults(results, "output2.txt");
		results.clear();
		resultTransformed = MetamapSerImpl.convertToSerImpl(resultOriginal);
		results.add(resultTransformed);
		writeResults(results, "output3.txt");

		Gson gson = new Gson();
		String json = gson.toJson(resultTransformed);
		System.out.println(json);
		Result finalResult = gson.fromJson(json, ResultSerImpl.class);
		writeSingleResult(finalResult, "output4.txt");
	}

	private static void writeSingleResult(Result result, String outputFileName)
			throws Exception {
		List<Result> results = new ArrayList<Result>();
		results.add(result);
		writeResults(results, outputFileName);
	}

	private static void writeResults(List<Result> results, String outputFilename)
			throws Exception {
		for (Result result : results) {
			if (result != null) {
				PrintWriter pw = new PrintWriter(outputFilename);
				// List<AcronymsAbbrevs> aaList =
				// result.getAcronymsAbbrevsList();
				// pw.println("Acronyms and Abbreviations:");
				// if (aaList.size() > 0) {
				// for (AcronymsAbbrevs e : aaList) {
				// pw.println("Acronym: " + e.getAcronym());
				// pw.println("Expansion: " + e.getExpansion());
				// pw.println("Count list: " + e.getCountList());
				// pw.println("CUI list: " + e.getCUIList());
				// }
				// } else {
				// pw.println(" None.");
				// }

				// pw.println("Negations:");
				// List<Negation> negList = result.getNegationList();
				// if (negList.size() > 0) {
				// for (Negation e : negList) {
				// pw.println("type: " + e.getType());
				// pw.print("Trigger: " + e.getTrigger() + ": [");
				// for (Position pos : e.getTriggerPositionList()) {
				// pw.print(pos + ",");
				// }
				// pw.println("]");
				// pw.print("ConceptPairs: [");
				// for (ConceptPair pair : e.getConceptPairList()) {
				// pw.print(pair + ",");
				// }
				// pw.println("]");
				// pw.print("ConceptPositionList: [");
				// for (Position pos : e.getConceptPositionList()) {
				// pw.print(pos + ",");
				// }
				// pw.println("]");
				// }
				// } else {
				// pw.println(" None.");
				// }
				for (Utterance utterance : result.getUtteranceList()) {
					pw.println("Utterance:");
					pw.println(" Id: " + utterance.getId());
					pw.println(" Utterance text: " + utterance.getString());
					pw.println(" Position: " + utterance.getPosition());

					for (PCM pcm : utterance.getPCMList()) {
						pw.println("Phrase:");
						pw.println(" text: " + pcm.getPhrase().getPhraseText());
						pw.println(" phrase_positional_info: "
								+ pcm.getPhrase().getPosition());
						// pw.println("syntactic analysis: " +
						// pcm.getPhrase().getMincoManAsString());
						// pw.println("Candidates:");
						// for (Ev ev : pcm.getCandidateList()) {
						// pw.println(" Candidate:");
						// pw.println("  Score: " + ev.getScore());
						// pw.println("  Concept Id: " + ev.getConceptId());
						// pw.println("  Concept Name: " + ev.getConceptName());
						// pw.println("  Preferred Name: "
						// + ev.getPreferredName());
						// pw.println("  Matched Words: "
						// + ev.getMatchedWords());
						// pw.println("  Semantic Types: "
						// + ev.getSemanticTypes());
						// pw.println("  is Head?: " + ev.isHead());
						// pw.println("  is Overmatch?: " + ev.isOvermatch());
						// pw.println("  Sources: " + ev.getSources());
						// pw.println("  Positional Info: "
						// + ev.getPositionalInfo());
						// }
						pw.println("Mappings:");
						for (Mapping map : pcm.getMappingList()) {
							pw.println(" Mapping:");
							pw.println("  Map Score: " + map.getScore());
							for (Ev mapEv : map.getEvList()) {
								pw.println("   Score: " + mapEv.getScore());
								pw.println("   Concept Id: "
										+ mapEv.getConceptId());
								pw.println("   Concept Name: "
										+ mapEv.getConceptName());
								pw.println("   Preferred Name: "
										+ mapEv.getPreferredName());
								pw.println("   Matched Words: "
										+ mapEv.getMatchedWords());
								pw.println("   Semantic Types: "
										+ mapEv.getSemanticTypes());
								pw.println("   is Head?: " + mapEv.isHead());
								pw.println("   is Overmatch?: "
										+ mapEv.isOvermatch());
								pw.println("   Sources: " + mapEv.getSources());
								pw.println("   Positional Info: "
										+ mapEv.getPositionalInfo());
							}
						}
					}
				}
				pw.close();
			} else {
				System.out.println("NULL result instance! ");
			}
		}
	}

}
