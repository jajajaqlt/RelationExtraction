package pipeline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Metathesaurus {
	// global indicator of multiple relations per semantic type pair
	boolean hasMultiRelations = false;
	HashMap<String, HashSet<String>> cuiPairRelationMapTmp = new HashMap<String, HashSet<String>>();
	HashMap<String, ArrayList<String>> cuiPairRelationMap = new HashMap<String, ArrayList<String>>();

	// Metathesaurus meta = new Metathesaurus("MRREL", metaRelations);
	public Metathesaurus(String MRRELFile, ArrayList<String> relations)
			throws Exception {
		BufferedReader br = null;
		MRRELRecord rec = null;
		int index = 0;

		String cui1, rela, cui2, cuiPair;
		HashSet<String> set;
		String sCurrentLine;
		br = new BufferedReader(new FileReader(MRRELFile));
		while ((sCurrentLine = br.readLine()) != null) {
			rec = new MRRELRecord();
			rec.items = sCurrentLine.split("\\|", 17);
			// ??? the case of cui1 == cui2
			rela = rec.getRELA();
			if (!rela.equals("") && relations.contains(rela)) {
				// cui2 has the relation to cui1
				cui1 = rec.getCUI1();
				cui2 = rec.getCUI2();
				cuiPair = cui2 + "&" + cui1;
				set = cuiPairRelationMapTmp.get(cuiPair);
				if (set == null) {
					set = new HashSet<String>();
					set.add(rela);
					cuiPairRelationMapTmp.put(cuiPair, set);
				} else {
					set.add(rela);
					hasMultiRelations = true;
				}
			}

		}
		br.close();

		// HashMap<String, HashSet<String>> cuiPairRelationMapTmp
		// String rels;
		ArrayList<String> arr;
		for (Map.Entry<String, HashSet<String>> entry : cuiPairRelationMapTmp
				.entrySet()) {
			cuiPair = entry.getKey();
			set = entry.getValue();
			arr = new ArrayList<String>();
			// rels = "";
			for (String setElement : set) {
				// rels += setElement + "&";
				arr.add(setElement);
			}
			// rels = rels.substring(0, rels.length() - 1);
			cuiPairRelationMap.put(cuiPair, arr);
		}
	}
}
