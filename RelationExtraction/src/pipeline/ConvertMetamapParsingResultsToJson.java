package pipeline;

import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Phrase;
import gov.nih.nlm.nls.metamap.Position;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;
import info.olteanu.interfaces.StringFilter;
import info.olteanu.utils.TextNormalizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import pipeline.MetamapSerImpl.ResultSerImpl;
import tests.InterfaceAdapter;
import tools.Time;

import com.google.common.base.CharMatcher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConvertMetamapParsingResultsToJson {

	public static void main(String[] args) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(
				"all_abstracts_1-2000.txt"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("output.json"));

		MetaMapApi api = new MetaMapApiImpl(0);
		api.setOptions("-y");

		GsonBuilder gb = new GsonBuilder();
		gb.registerTypeAdapter(Result.class, new InterfaceAdapter<Result>());
		gb.registerTypeAdapter(Utterance.class,
				new InterfaceAdapter<Utterance>());
		gb.registerTypeAdapter(Position.class, new InterfaceAdapter<Position>());
		gb.registerTypeAdapter(PCM.class, new InterfaceAdapter<PCM>());
		gb.registerTypeAdapter(Phrase.class, new InterfaceAdapter<Phrase>());
		gb.registerTypeAdapter(Mapping.class, new InterfaceAdapter<Mapping>());
		gb.registerTypeAdapter(Ev.class, new InterfaceAdapter<Ev>());
		Gson gson = gb.create();

		Result result, resultTransferred;
		String line, json;
		boolean isAscii;
		int index = 0;
		while ((line = br.readLine()) != null) {
			try {
				index++;
				isAscii = CharMatcher.ASCII.matchesAllOf(line);
				if (!isAscii)
					line = normalizeString(line);
				System.out.println("Starts parsing #" + index + " abstract."
						+ " " + Time.getCurrentTime());
				List<Result> resultList = api.processCitationsFromString(line);
				System.out.println("Finishes parsing." + " "
						+ Time.getCurrentTime());
				result = resultList.get(0);
				System.out.println("Starts converting." + " "
						+ Time.getCurrentTime());
				resultTransferred = MetamapSerImpl.convertToSerImpl(result);
				System.out.println("Finishes converting." + " "
						+ Time.getCurrentTime());
				System.out.println("Starts jsoning." + " "
						+ Time.getCurrentTime());
				json = gson.toJson(resultTransferred);
				System.out.println("Finishes jsoning." + " "
						+ Time.getCurrentTime());
				bw.write(json);
				bw.newLine();
			} catch (Exception e) {
				// does nothing and 
			}
		}
		br.close();
		bw.close();

		// transfers back to Result object
		// Result finalResult = gson.fromJson(json, ResultSerImpl.class);

	}

	/**
	 *
	 * @param text
	 * @return ASCII encoding of text with non ASCII characters replaced by ?
	 * @throws UnsupportedEncodingException
	 */
	public static String normalizeString(String text)
			throws UnsupportedEncodingException {
		String aText;
		byte[] b = text.getBytes("US-ASCII");
		aText = new String(b, "US-ASCII");
		return aText;
	}

	// Normalized version of str with accented characters replaced by unaccented
	// version and with diacritics removed. E.g. Ö -> O
	// private static String normalizeString(String str)
	// throws ClassNotFoundException {
	// // TextNormalizer code from phramer.org
	// // Allows compilation under both Java 5 and Java 6
	// StringFilter stringFilter = TextNormalizer
	// .getNormalizationStringFilter();
	// String nfdNormalizedString = stringFilter.filter(str);
	//
	// // Normalizer is Java 6 only
	// // String nfdNormalizedString = java.text.Normalizer.normalize(str,
	// // Normalizer.Form.NFD);
	// Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	// return pattern.matcher(nfdNormalizedString).replaceAll("");
	// }

	public static List<Result> readJson(String inputFilename) throws Exception {
		GsonBuilder gb = new GsonBuilder();
		gb.registerTypeAdapter(Result.class, new InterfaceAdapter<Result>());
		gb.registerTypeAdapter(Utterance.class,
				new InterfaceAdapter<Utterance>());
		gb.registerTypeAdapter(Position.class, new InterfaceAdapter<Position>());
		gb.registerTypeAdapter(PCM.class, new InterfaceAdapter<PCM>());
		gb.registerTypeAdapter(Phrase.class, new InterfaceAdapter<Phrase>());
		gb.registerTypeAdapter(Mapping.class, new InterfaceAdapter<Mapping>());
		gb.registerTypeAdapter(Ev.class, new InterfaceAdapter<Ev>());
		Gson gson = gb.create();

		List<Result> results = new ArrayList<Result>();
		BufferedReader br = new BufferedReader(new FileReader(inputFilename));
		String json;
		Result result;
		while ((json = br.readLine()) != null) {
			result = gson.fromJson(json, ResultSerImpl.class);
			results.add(result);
		}
		return results;
	}

	public static void writeResults(List<Result> results, String outputFilename)
			throws Exception {
		for (Result result : results) {
			if (result != null) {
				PrintWriter pw = new PrintWriter(outputFilename);
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
