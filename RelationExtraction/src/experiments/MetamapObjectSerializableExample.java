package experiments;

import gov.nih.nlm.nls.metamap.AcronymsAbbrevs;
import gov.nih.nlm.nls.metamap.ConceptPair;
import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.Negation;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Position;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uses buffering, and abstract base classes. JDK 7+.
 */
public class MetamapObjectSerializableExample {

	public static void main(String... aArguments) throws Exception {
		// create a Serializable List
		// List<String> quarks = Arrays.asList("up", "down", "strange", "charm",
		// "top", "bottom");
		String abstractsFile = "input.txt";
		BufferedReader br = new BufferedReader(new FileReader(abstractsFile));
		String line;
		List<Result> results = new ArrayList<Result>();
		while ((line = br.readLine()) != null) {
			MetaMapApi api = new MetaMapApiImpl(0);
			api.setOptions("-y");
			List<Result> resultList = api.processCitationsFromString(line);
			results.addAll(resultList);
		}
		br.close();
		writeResults(results, "output1.ser");
		SerializableOuterClass ser = new SerializableOuterClass();
		ser.results = results;

		// serialize the List
		try (OutputStream file = new FileOutputStream("result.ser");
				OutputStream buffer = new BufferedOutputStream(file);
				ObjectOutput output = new ObjectOutputStream(buffer);) {
			output.writeObject(ser);
		} catch (IOException ex) {
			fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
		}

		results.clear();
		// deserialize the quarks.ser file
		try (InputStream file = new FileInputStream("result.ser");
				InputStream buffer = new BufferedInputStream(file);
				ObjectInput input = new ObjectInputStream(buffer);) {
			// deserialize the List
			Object obj = input.readObject();
			List<Result> recoveredResults = ((SerializableOuterClass) obj).results;
			// display its data
			results.addAll(recoveredResults);
		} catch (ClassNotFoundException ex) {
			fLogger.log(Level.SEVERE, "Cannot perform input. Class not found.",
					ex);
		} catch (IOException ex) {
			fLogger.log(Level.SEVERE, "Cannot perform input.", ex);
		}
		writeResults(results, "output2.ser");

	}

	// PRIVATE

	private static final Logger fLogger = Logger
			.getLogger(MetamapObjectSerializableExample.class.getPackage()
					.getName());

	private static void writeResults(List<Result> results, String outputFilename)
			throws Exception {
		for (Result result : results) {
			if (result != null) {
				PrintWriter pw = new PrintWriter(outputFilename);
				List<AcronymsAbbrevs> aaList = result.getAcronymsAbbrevsList();
				pw.println("Acronyms and Abbreviations:");
				if (aaList.size() > 0) {
					for (AcronymsAbbrevs e : aaList) {
						pw.println("Acronym: " + e.getAcronym());
						pw.println("Expansion: " + e.getExpansion());
						pw.println("Count list: " + e.getCountList());
						pw.println("CUI list: " + e.getCUIList());
					}
				} else {
					pw.println(" None.");
				}

				pw.println("Negations:");
				List<Negation> negList = result.getNegationList();
				if (negList.size() > 0) {
					for (Negation e : negList) {
						pw.println("type: " + e.getType());
						pw.print("Trigger: " + e.getTrigger() + ": [");
						for (Position pos : e.getTriggerPositionList()) {
							pw.print(pos + ",");
						}
						pw.println("]");
						pw.print("ConceptPairs: [");
						for (ConceptPair pair : e.getConceptPairList()) {
							pw.print(pair + ",");
						}
						pw.println("]");
						pw.print("ConceptPositionList: [");
						for (Position pos : e.getConceptPositionList()) {
							pw.print(pos + ",");
						}
						pw.println("]");
					}
				} else {
					pw.println(" None.");
				}
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
						pw.println("Candidates:");
						for (Ev ev : pcm.getCandidateList()) {
							pw.println(" Candidate:");
							pw.println("  Score: " + ev.getScore());
							pw.println("  Concept Id: " + ev.getConceptId());
							pw.println("  Concept Name: " + ev.getConceptName());
							pw.println("  Preferred Name: "
									+ ev.getPreferredName());
							pw.println("  Matched Words: "
									+ ev.getMatchedWords());
							pw.println("  Semantic Types: "
									+ ev.getSemanticTypes());
							pw.println("  is Head?: " + ev.isHead());
							pw.println("  is Overmatch?: " + ev.isOvermatch());
							pw.println("  Sources: " + ev.getSources());
							pw.println("  Positional Info: "
									+ ev.getPositionalInfo());
						}
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
