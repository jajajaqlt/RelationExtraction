package pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SortWordFrequencyFile {

	public static void main(String[] args) throws Exception {
		String inputFile = "wordFrequency.txt";
		BufferedReader br = new BufferedReader(new FileReader(new File(
				inputFile)));
		HashMap<String, Integer> freqMap = new HashMap<String, Integer>();
		String line;
		String[] splits;
		String word;
		int freq;
		while ((line = br.readLine()) != null) {
			splits = line.split(" ");
			word = splits[0];
			freq = Integer.parseInt(splits[1]);
			freqMap.put(word, freq);
		}
		br.close();
		ValueComparator comp = new ValueComparator(freqMap);
		TreeMap<String, Integer> sortedMap = new TreeMap<String, Integer>(comp);
		sortedMap.putAll(freqMap);
		String outputFile = "sortedWordFrequency.txt";
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				outputFile)));
		for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
			bw.write(entry.getKey() + " " + entry.getValue());
			bw.newLine();
		}
		bw.close();
	}
}

class ValueComparator implements Comparator<String> {

	Map<String, Integer> base;

	public ValueComparator(Map<String, Integer> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	public int compare(String a, String b) {
		int valueA = base.get(a), valueB = base.get(b);
		if (valueA <= valueB) {
			return 1;
		} 
//		else if (valueA == valueB) {
//			return 0;
//		} 
		else {
			return -1;
		}
	}
}