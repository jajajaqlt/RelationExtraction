package pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.tartarus.snowball.SnowballStemmer;

public class GetWordDictionary {
	public final static int dictSize = 10000;

	public static void main(String[] args) throws Exception {
		String stopWordFile = "revised_snowball_stop_word_list.txt";
		String sortedWordFreqFile = "sortedWordFrequency.txt";
		Class stemClass = Class.forName("org.tartarus.snowball.ext."
				+ "english" + "Stemmer");
		SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
		ArrayList<String> stemmedWordList = new ArrayList<String>();

		BufferedReader br = new BufferedReader(new FileReader(stopWordFile));
		String line;
		String stemmedWord;
		while ((line = br.readLine()) != null) {
			stemmer.setCurrent(line);
			stemmer.stem();
			stemmedWord = stemmer.getCurrent();
			stemmedWordList.add(stemmedWord);
		}
		br.close();
		int stemmedWordListSize = stemmedWordList.size();

		LinkedHashMap<String, Integer> wordFreqMap = new LinkedHashMap<String, Integer>();
		br = new BufferedReader(new FileReader(sortedWordFreqFile));
		String[] splits;
		int freq;
		for (int i = 0; i < dictSize + stemmedWordListSize; i++) {
			line = br.readLine();
			splits = line.split(" ");
			stemmedWord = splits[0];
			freq = Integer.parseInt(splits[1]);
			wordFreqMap.put(stemmedWord, freq);
		}
		br.close();

		for (String word : stemmedWordList) {
			wordFreqMap.remove(word);
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter("wordDict.txt"));
		int i = 0;
		for (Map.Entry<String, Integer> entry : wordFreqMap.entrySet()) {
			if (i >= dictSize)
				break;
			else
				i++;
			// bw.write(entry.getKey() + " " + entry.getValue());
			bw.write(entry.getKey());
			bw.newLine();
		}
		bw.close();

	}

}
