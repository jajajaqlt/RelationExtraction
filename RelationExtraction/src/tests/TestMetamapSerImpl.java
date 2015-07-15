package tests;

import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Phrase;
import gov.nih.nlm.nls.metamap.Position;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import pipeline.MetamapSerImpl;
import pipeline.MetamapSerImpl.EvSerImpl;
import pipeline.MetamapSerImpl.MappingSerImpl;
import pipeline.MetamapSerImpl.PCMSerImpl;
import pipeline.MetamapSerImpl.PhraseSerImpl;
import pipeline.MetamapSerImpl.PositionSerImpl;
import pipeline.MetamapSerImpl.ResultSerImpl;
import pipeline.MetamapSerImpl.UtteranceSerImpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

public class TestMetamapSerImpl {

	public static void main(String[] args) throws Exception {

		Result resultOriginal;
		Result resultTransformed;
		BufferedReader br = new BufferedReader(new FileReader("input1.txt"));
		String line = br.readLine();
		br.close();

		// line =
		// "None of the 15 patients with supradiaphragmatic disease who received limited-field radiotherapy to regions that did not include the mediastinal or hilar nodes subsequently experienced relapse there. Aspirin treats heart disease.";
		MetaMapApi api = new MetaMapApiImpl(0);
		api.setOptions("-y");
		List<Result> resultList = api.processCitationsFromString(line);
		System.out.println("Finishes metamap processing. Time is: "
				+ System.currentTimeMillis());
		resultOriginal = resultList.get(0);
		// List<Result> results = new ArrayList<Result>();
		// results.add(resultOriginal);
		// writeResults(results, "output2.txt");
		// results.clear();
		System.out.println("Starts converting. Time is: "
				+ System.currentTimeMillis());
		resultTransformed = MetamapSerImpl.convertToSerImpl(resultOriginal);
		System.out.println("Finishes converting. TIme is: "
				+ System.currentTimeMillis());
		// results.add(resultTransformed);
		// writeResults(results, "output3.txt");

		// GsonBuilder gb = new GsonBuilder();
		// gb.registerTypeAdapter(ResultSerImpl.class,
		// new InterfaceAdapter<ResultSerImpl>());
		// gb.registerTypeAdapter(UtteranceSerImpl.class,
		// new InterfaceAdapter<UtteranceSerImpl>());
		// gb.registerTypeAdapter(PositionSerImpl.class,
		// new InterfaceAdapter<PositionSerImpl>());
		// gb.registerTypeAdapter(PCMSerImpl.class,
		// new InterfaceAdapter<PCMSerImpl>());
		// gb.registerTypeAdapter(PhraseSerImpl.class,
		// new InterfaceAdapter<PhraseSerImpl>());
		// gb.registerTypeAdapter(MappingSerImpl.class,
		// new InterfaceAdapter<MappingSerImpl>());
		// gb.registerTypeAdapter(EvSerImpl.class,
		// new InterfaceAdapter<EvSerImpl>());

		GsonBuilder gb = new GsonBuilder();
		gb.registerTypeAdapter(Result.class, new InterfaceAdapter<Result>());
		gb.registerTypeAdapter(Utterance.class,
				new InterfaceAdapter<Utterance>());
		gb.registerTypeAdapter(Position.class, new InterfaceAdapter<Position>());
		gb.registerTypeAdapter(PCM.class, new InterfaceAdapter<PCM>());
		gb.registerTypeAdapter(Phrase.class, new InterfaceAdapter<Phrase>());
		gb.registerTypeAdapter(Mapping.class, new InterfaceAdapter<Mapping>());
		gb.registerTypeAdapter(Ev.class, new InterfaceAdapter<Ev>());

		// GsonBuilder gb = new GsonBuilder();
		// gb.registerTypeAdapter(Result.class, new InstanceCreator<Result>() {
		//
		// @Override
		// public Result createInstance(Type arg0) {
		// return new ResultSerImpl();
		// }
		//
		// });
		// gb.registerTypeAdapter(Utterance.class,
		// new InstanceCreator<Utterance>() {
		//
		// @Override
		// public Utterance createInstance(Type arg0) {
		// return new UtteranceSerImpl();
		// }
		//
		// });
		// gb.registerTypeAdapter(Position.class, new
		// InstanceCreator<Position>() {
		//
		// @Override
		// public Position createInstance(Type arg0) {
		// return new PositionSerImpl();
		// }
		//
		// });
		// gb.registerTypeAdapter(PCM.class, new InstanceCreator<PCM>() {
		//
		// @Override
		// public PCM createInstance(Type arg0) {
		// return new PCMSerImpl();
		// }
		//
		// });
		// gb.registerTypeAdapter(Phrase.class, new InstanceCreator<Phrase>() {
		//
		// @Override
		// public Phrase createInstance(Type arg0) {
		// return new PhraseSerImpl();
		// }
		//
		// });
		// gb.registerTypeAdapter(Mapping.class, new InstanceCreator<Mapping>()
		// {
		//
		// @Override
		// public Mapping createInstance(Type arg0) {
		// return new MappingSerImpl();
		// }
		//
		// });
		// gb.registerTypeAdapter(Ev.class, new InstanceCreator<Ev>() {
		//
		// @Override
		// public Ev createInstance(Type arg0) {
		// return new EvSerImpl();
		// }
		//
		// });

		// gb.registerTypeAdapter(Utterance.class, new
		// UtteranceInstanceCreater());
		// Gson gson = gb.create();
		// gb.registerTypeAdapter(Utterance.class, new
		// UtteranceInstanceCreater());
		Gson gson = gb.create();
		// gson = new Gson();
		String json = gson.toJson(resultTransformed);
		// System.out.println(json);
		Result finalResult = gson.fromJson(json, ResultSerImpl.class);
		writeSingleResult(finalResult, "output4.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter("output.json"));
		bw.write(json);
		bw.close();
	}

	public static class UtteranceInstanceCreater implements
			InstanceCreator<Utterance> {

		@Override
		public Utterance createInstance(Type arg0) {
			return new UtteranceSerImpl();
		}

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
