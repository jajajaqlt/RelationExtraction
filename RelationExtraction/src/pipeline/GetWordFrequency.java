package pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tartarus.snowball.SnowballStemmer;

public class GetWordFrequency {

	public static void main(String[] args) throws Exception {
		// start of program
		System.out.println("This is the start of program.");
		Date date = new Date();
		System.out.println("Current time is: " + date.toString());

		// just uses regular words
		Pattern wordPattern = Pattern.compile("[a-zA-Z][a-zA-Z0-9]+");
		Matcher myMatcher;

		Map<String, Integer> wordFrequencyMap = new TreeMap<String, Integer>();
		Integer frequency;

		Class stemClass = Class.forName("org.tartarus.snowball.ext."
				+ "english" + "Stemmer");
		SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
		// stemmer.setCurrent("sports");
		// stemmer.stem();
		// String your_stemmed_word = stemmer.getCurrent();
		// System.out.println(your_stemmed_word);

		String inputFile = "F:\\Data\\Medline_Abstracts\\all_abstracts.txt";
		BufferedReader br = new BufferedReader(new FileReader(new File(
				inputFile)));

		String line;
		String word;
		int index = 0;
		while ((line = br.readLine()) != null) {
			myMatcher = wordPattern.matcher(line);
			while (myMatcher.find()) {
				word = myMatcher.group().toLowerCase();

				// moves stemming to the end
				// stemming
				// stemmer.setCurrent(word);
				// stemmer.stem();
				// word = stemmer.getCurrent();

				frequency = wordFrequencyMap.get(word);
				frequency = (frequency == null) ? 1 : ++frequency;
				wordFrequencyMap.put(word, frequency);
			}
			index++;
			if (index % 1000 == 0)
				System.out.println("Current abstract is #" + index + ".");
		}
		br.close();

		Map<String, Integer> stemmedWordFrequencyMap = new TreeMap<String, Integer>();
		Integer newFrequency;

		for (Map.Entry<String, Integer> entry : wordFrequencyMap.entrySet()) {
			word = entry.getKey();
			frequency = entry.getValue();

			// stemming
			stemmer.setCurrent(word);
			stemmer.stem();
			word = stemmer.getCurrent();

			newFrequency = stemmedWordFrequencyMap.get(word);
			newFrequency = (newFrequency == null) ? frequency
					: (frequency + newFrequency);
			stemmedWordFrequencyMap.put(word, newFrequency);
		}

		String outputFile = "wordFrequency.txt";
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				outputFile)));
		for (Map.Entry<String, Integer> entry : stemmedWordFrequencyMap
				.entrySet()) {
			bw.write(entry.getKey() + " " + entry.getValue());
			bw.newLine();
		}
		bw.close();

		System.out.println("This is end of program.");
		date = new Date();
		System.out.println("Current time is: " + date.toString());

	}

}
