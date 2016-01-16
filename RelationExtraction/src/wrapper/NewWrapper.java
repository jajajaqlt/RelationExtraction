package wrapper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.nlp.util.HashIndex;

public class NewWrapper {

	public static void main(String[] args) throws IOException {

		String file = "output_160114_temp_wrapper_input.txt";
		BufferedReader br = new BufferedReader(new FileReader(file));

		ArrayList<Instance> instances = new ArrayList<>();
		Instance instance;

		// stanford nlp staff
		HashIndex<String> sLvConjIndex = new HashIndex<String>();
		Map<Integer, Integer> sLvConjIndexCnt = new HashMap<>();
		HashIndex<String> cLvConjIndex = new HashIndex<String>();
		Map<Integer, Integer> cLvConjIndexCnt = new HashMap<>();
		HashIndex<String> pLvConjIndex = new HashIndex<String>();
		Map<Integer, Integer> pLvConjIndexCnt = new HashMap<>();
		HashIndex<String> wLvConjIndex = new HashIndex<String>();
		Map<Integer, Integer> wLvConjIndexCnt = new HashMap<>();
		
		HashIndex<String> wordIndex = new HashIndex<String>();
		Map<Integer, Integer> wordIndexCnt = new HashMap<>();
		HashIndex<String> tagIndex = new HashIndex<String>();
		HashIndex<String> depIndex = new HashIndex<String>();
		HashIndex<String> depWordIndex = new HashIndex<String>();
		Map<Integer, Integer> depWordIndexCnt = new HashMap<>();
		
		
		String line;
		String[] splits;
		int index;
		int count;
		
		/**
		 * According to output_160114_temp_wrapper_input.txt
		 */
		while (true) {
			try {
				line = br.readLine();
				if (!line.equals("<abstract>"))
					continue;
				line = br.readLine();
				if (!line.equals("<metamap-output>"))
					continue;
				line = br.readLine();
				if (line.charAt(0) != '{')
					continue;
				line = br.readLine();
				if (!line.equals("</metamap-output>"))
					continue;
				sentenceLoop: while (true) {
					line = br.readLine();
					if (!line.equals("<sentence>"))
						break;
					line = br.readLine();
					if (!line.equals("<stanford-parser-result>"))
						break;
					for (int i = 0; i < 4; i++) {
						line = br.readLine();
						if (line.charAt(0) != '[')
							break sentenceLoop;
					}
					line = br.readLine();
					if (!line.equals("</stanford-parser-result>"))
						break;
					line = br.readLine();
					if (!line.equals("<phrase-chunking-result>"))
						break;
					line = br.readLine();
					if (line.charAt(0) != '[')
						break;
					line = br.readLine();
					if (!line.equals("</phrase-chunking-result>"))
						break;
					instanceLoop: while (true) {
						line = br.readLine();
						if (!line.equals("<instance>"))
							break;
						instance = new Instance();
						for (int i = 0; i < 11; i++) {
							line = br.readLine();
						}
						line = br.readLine();
						if (!line.substring(0, 3).equals("net"))
							break;
						splits = line.split(": ");
						instance.netRelation = splits[1];
						line = br.readLine();
						if (!line.substring(0, 4).equals("meta"))
							break;
						splits = line.split(": ");
						instance.metaRelation = splits[1];
						int[] temp = new int[4];
						for (int i = 0; i < 4; i++) {
							line = br.readLine();
							if (!line.substring(0, 6).equals("number"))
								break instanceLoop;
							splits = line.split(": ");
							temp[i] = Integer.parseInt(splits[1]);
						}
						instance.sLvFeatCnt = temp[0];
						instance.chunksCnt = temp[1];
						instance.phrasesCnt = temp[2];
						instance.wordsCnt = temp[3];
						line = br.readLine();
						if(!line.substring(0, 8).equals("sentence"))
							break;
						line = br.readLine();
						if(!line.equals("<sentence-level-features>"))
							break;
						instance.sLvFeats = new int[instance.sLvFeatCnt];
						for(int i = 0; i < instance.sLvFeatCnt; i++){
							line = br.readLine();
							if(!line.substring(0, 7).equals("feature"))
								break instanceLoop;
							index = sLvConjIndex.addToIndex(line);
							instance.sLvFeats[i] = index;
							count = sLvConjIndexCnt.getOrDefault(index, 0);
							sLvConjIndexCnt.put(index, count + 1);
						}
						line = br.readLine();
						if(!line.equals("bag-of-words-features:"))
							break;
						line = br.readLine();
						if(!line.substring(4, 8).equals("word"))
							break;
						line = br.readLine();
						splits = line.split(" ");
						instance.bowWords = new int[splits.length];
						for(int i = 0; i < splits.length; i++){
							index = wordIndex.addToIndex(splits[i]);
							instance.bowWords[i] = index;
							count = wordIndexCnt.getOrDefault(index, 0);
							wordIndexCnt.put(index, count + 1);
						}						
						line = br.readLine();
						if(!line.substring(4, 7).equals("tag"))
							break;
						line = br.readLine();
						splits = line.split(" ");
						instance.bowTags = new int[splits.length];
						for(int i = 0; i < splits.length; i++){
							index = tagIndex.addToIndex(splits[i]);
							instance.bowTags[i] = index;
						}
						line = br.readLine();
						if(!line.substring(4, 7).equals("dep"))
							break;
						line = br.readLine();
						splits = line.split(" ");
						instance.bowDeps = new int[splits.length];
						for(int i = 0; i < splits.length; i++){
							index = depIndex.addToIndex(splits[i]);
							instance.bowTags[i] = index;
						}
						line = br.readLine();
						if(!line.substring(4, 12).equals("dep-word"))
							break;
						line = br.readLine();
						splits = line.split(" ");
						instance.bowDepWords = new int[splits.length];
						for(int i = 0; i < splits.length; i++){
							index = depWordIndex.addToIndex(splits[i]);
							instance.bowTags[i] = index;
							count = depWordIndexCnt.getOrDefault(index, 0);
							depWordIndexCnt.put(index, count + 1);
						}
						line = br.readLine();
						if(!line.equals("</sentence-level-features>"))
							break;
						line = br.readLine();
						if(!line.equals("<chunk-level-features>"))
							break;
						line = br.readLine();
						if(!line.equals("word-features:"))
							break;
						instance.cLvWords = new int[instance.chunksCnt];
						for(int i = 0; i < instance.chunksCnt; i++){
							line = br.readLine();
							if(!line.substring(0, 3).equals("inv"))
								break instanceLoop;
							index = cLvConjIndex.addToIndex(line);
							instance.cLvWords[i] = index;
							count = cLvConjIndexCnt.getOrDefault(index, 0);
							cLvConjIndexCnt.put(index, count + 1);
						}
						line = br.readLine();
						if(!line.equals("tag-features:"))
							break;
						instance.cLvTags = new int[instance.chunksCnt];
						for(int i = 0; i < instance.chunksCnt; i++){
							line = br.readLine();
							if(!line.substring(0, 3).equals("inv"))
								break instanceLoop;
							index = cLvConjIndex.addToIndex(line);
							instance.cLvTags[i] = index;
							count = cLvConjIndexCnt.getOrDefault(index, 0);
							cLvConjIndexCnt.put(index, count + 1);
						}
						for(int i = 0; i < 2; i++)
							br.readLine();
						line = br.readLine();
						if(!line.equals("word-features:"))
							break;
						instance.pLvWords = new int[instance.phrasesCnt];
						for(int i = 0; i < instance.phrasesCnt; i++){
							line = br.readLine();
							if(!line.substring(0, 3).equals("inv"))
								break instanceLoop;
							index = pLvConjIndex.addToIndex(line);
							instance.pLvWords[i] = index;
							count = pLvConjIndexCnt.getOrDefault(index, 0);
							pLvConjIndexCnt.put(index, count + 1);
						}
						line = br.readLine();
						if(!line.equals("tag-features:"))
							break;
						instance.pLvTags = new int[instance.phrasesCnt];
						for(int i = 0; i < instance.phrasesCnt; i++){
							line = br.readLine();
							if(!line.substring(0, 3).equals("inv"))
								break instanceLoop;
							index = pLvConjIndex.addToIndex(line);
							instance.pLvTags[i] = index;
							count = pLvConjIndexCnt.getOrDefault(index, 0);
							pLvConjIndexCnt.put(index, count + 1);
						}
						for(int i = 0; i < 2; i++)
							br.readLine();
						line = br.readLine();
						if(!line.equals("word-features:"))
							break;
						instance.wLvWords = new int[instance.wordsCnt];
						for(int i = 0; i < instance.wordsCnt; i++){
							line = br.readLine();
							if(!line.substring(0, 3).equals("inv"))
								break instanceLoop;
							index = wLvConjIndex.addToIndex(line);
							instance.wLvWords[i] = index;
							count = wLvConjIndexCnt.getOrDefault(index, 0);
							wLvConjIndexCnt.put(index, count + 1);
						}
						line = br.readLine();
						if(!line.equals("tag-features:"))
							break;
						instance.wLvTags = new int[instance.wordsCnt];
						for(int i = 0; i < instance.wordsCnt; i++){
							line = br.readLine();
							if(!line.substring(0, 3).equals("inv"))
								break instanceLoop;
							index = wLvConjIndex.addToIndex(line);
							instance.wLvTags[i] = index;
							count = wLvConjIndexCnt.getOrDefault(index, 0);
							wLvConjIndexCnt.put(index, count + 1);
						}
						br.readLine();
						line = br.readLine();
						if(!line.equals("</instance>"))
							break;
						instances.add(instance);
					}
					if (!line.equals("</sentence>"))
						break;
				}
				if (!line.equals("</abstract>"))
					continue;

			} catch (NullPointerException e) {
//				e.printStackTrace();
				System.out.println("End of file!");
				break;
			}
		}
		
		System.out.println("Test. Num of instances is: " + instances.size());
		br.close();
	}

	// printInstancesInSerializationForm
	// printIndex
	
	private static class Instance {
		String netRelation;
		String metaRelation;
		int sLvFeatCnt;
		int chunksCnt;
		int phrasesCnt;
		int wordsCnt;
		int[] sLvFeats;
		int[] bowWords;
		int[] bowTags;
		int[] bowDeps;
		int[] bowDepWords;
		int[] cLvWords;
		int[] cLvTags;
		int[] pLvWords;
		int[] pLvTags;
		int[] wLvWords;
		int[] wLvTags;
	}
}
