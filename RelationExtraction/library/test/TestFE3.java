package test;

import se.sics.prologbeans.*;
import java.io.*;
import java.util.List;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;
import gov.nih.nlm.nls.metamap.Negation;
import gov.nih.nlm.nls.metamap.AcronymsAbbrevs;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.Position;
import gov.nih.nlm.nls.metamap.ConceptPair;

/**
 * An example of reading an input file to a string, passing the string to the
 * api, and then writing the result to an output file in a human readable form .
 * <p>
 * Created: Tue May 19 09:42:22 2009
 *
 * @author <a href="mailto:wrogers@nlm.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class TestFE3 {

	private PrologSession session = new PrologSession();

	/**
	 * Creates a new <code>TestFE2</code> instance.
	 *
	 */
	public TestFE3() {

	}

	void process(String inputFilename, String outputFilename) {
		try {
			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(
					new FileReader(inputFilename));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			br.close();
			String input = sb.toString();
			System.out.println("input: " + input);

			MetaMapApi api = new MetaMapApiImpl(0);
			api.setOptions("-y");
			// List<Result> resultList = api.processCitationsFromString("-y",
			// input);
			List<Result> resultList = api.processCitationsFromString(input);
			String phraseTokenization = "";
			for (Result result : resultList) {
				if (result != null) {
					PrintWriter pw = new PrintWriter(outputFilename);

					for (Utterance utterance : result.getUtteranceList()) {
						pw.println("Utterance:");
						pw.println(" Utterance text: " + utterance.getString());
						pw.println(" Position: " + utterance.getPosition());

						for (PCM pcm : utterance.getPCMList()) {
							phraseTokenization += pcm.getPhrase()
									.getPhraseText() + "/";
						}
						pw.println(phraseTokenization.substring(0,
								phraseTokenization.length() - 1));
					}
					pw.close();
				} else {
					System.out.println("NULL result instance! ");
				}
			}
		} catch (Exception e) {
			System.out.println("Error when querying Prolog Server: "
					+ e.getMessage() + '\n');
		}
	}

	public static void main(String[] args) {
		TestFE3 frontEnd = new TestFE3();

		if (args.length < 2) {
			System.out.println("usage: TestFE2 <inputfile> <outputfile>");
			System.exit(0);
		}
		frontEnd.process(args[0], args[1]);
	}
}
