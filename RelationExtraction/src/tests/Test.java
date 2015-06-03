package tests;

import java.util.ArrayList; 

import pipeline.AbstractsToCandidates;
import pipeline.AbstractsToCandidates.Candidate;
import pipeline.CandidatesToFeatures;

/**
 * 
 * @author lq4
 *
 */
public class Test {

	public static void main(String[] args) throws Exception {
		// // TODO Auto-generated method stub
		// // String str = "Age Group|isa|Group|D|";
		// // String[] splits = str.split("\\|");
		// // for (int i = 0; i < splits.length; i++) {
		// // System.out.println(i);
		// // System.out.println(splits[i]);
		// // }
		// // SemanticNetwork sn = new SemanticNetwork("SRSTR", "SRDEF");
		// ArrayList<String> arr = new ArrayList<String>();
		// arr.add("abc");
		// arr.add("def");
		// // String[] str = (String[]) arr.toArray();
		// String[] stringArray = Arrays.copyOf(arr.toArray(), arr.size(),
		// String[].class);
		// // String[] stringArray = Arrays.copyOf(objectArray,
		// objectArray.length,
		// // String[].class);
		// // get current date time with Calendar()
		// // DateFormat dateFormat = new
		// SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// // Date date = new Date();
		// // System.out.println(dateFormat.format(date));
		// // Thread.sleep(2000);
		// // date = new Date();
		// // System.out.println(dateFormat.format(date));
		// String[] str = { "1", "2", "3" };
		// TestClass t1 = new TestClass();
		// t1.testField = str;
		// str[2] = "4";
		// System.out.println(t1.testField.toString());

		// public static String relationMappingFile = "NETMETA";
		// public static String semanticNetworkFile = "SRSTR";
		// public static String semanticTypeAbbreviationFile = "SRDEF";

		// public AbstractsToCandidates(. relationMappingFile,
		// String semanticNetworkFile, String semanticTypeAbbreviationFile,
		// String abstractsFile, String metaRelationsFile) throws Exception {

		// System.out.println(System.currentTimeMillis());
		AbstractsToCandidates a2c = new AbstractsToCandidates("NETMETA",
				"SRSTR", "SRDEF", "input.txt", "COMPACT_MRREL.RRF");
		// System.out.println(System.currentTimeMillis());
		ArrayList<Candidate> candidates = a2c.getCandidates();

		CandidatesToFeatures c2f = new CandidatesToFeatures("output.txt");
		c2f.getSentences(candidates);
		boolean flag = false;
		flag = true;

		// System.out.println("Working Directory = "
		// + System.getProperty("user.dir"));

	}
}
