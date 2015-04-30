package pipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SemanticNetwork {

	// name is defined as the string of a semantic type
	String[] rootNodeNames = { "Physical Object", "Conceptual Entity",
			"Activity", "Phenomenon or Process" };
	HashMap<String, SemanticNetworkNode> nameNodeMap = new HashMap<String, SemanticNetworkNode>();
	String[] relations;
	// = { "treats", "prevents" };
	HashMap<String, ArrayList<RelationParticipants>> relationParticipantsMap = new HashMap<String, ArrayList<RelationParticipants>>();
	// isa is a hierachical relation for relation types and relation
	String isa = "isa";
	HashMap<String, ArrayList<String>> parentChildrenStringMap = new HashMap<String, ArrayList<String>>();
	HashMap<String, HashSet<String>> relationInstatiationMap = new HashMap<String, HashSet<String>>();
	HashMap<String, String> nameAbbreviationMap = new HashMap<String, String>();
	HashMap<String, String> relInvRelMap = new HashMap<String, String>();
	// = new HashMap<String, String>();
	// in form of <rel, "type1Abbr&type2Abbr">
	// treats = [medd&sosy, prog&podg, ...]
	HashMap<String, HashSet<String>> relationAbbrInstatiationMap = new HashMap<String, HashSet<String>>();
	HashMap<String, HashSet<String>> invRelationAbbrInstatiationMap = new HashMap<String, HashSet<String>>();

	// final outcome
	// <"medd&sosy","treats&prevents">
	HashMap<String, String> stypePairRelationMap = new HashMap<String, String>();

	boolean hasMultiRelations = false;

	/**
	 * 
	 * @param networkFile
	 *            'SRSTR'
	 * @param abbrFile
	 *            'SRDEF'
	 * @throws Exception
	 */
	public SemanticNetwork(String networkFile, String styRelFile,
			String[] relations) throws Exception {
		// this.relInvRelMap = relInvRelMap;
		// // String[] stringArray = Arrays.copyOf(objectArray,
		// objectArray.length,
		// // String[].class);
		// // relations = (String[]) relInvRelMap.keySet().toArray();
		// relations = Arrays.copyOf(relInvRelMap.keySet().toArray(),
		// relInvRelMap
		// .keySet().size(), String[].class);
		this.relations = relations;
		String relation;
		for (int i = 0; i < relations.length; i++) {
			relation = relations[i];
			relationParticipantsMap.put(relation,
					new ArrayList<RelationParticipants>());
		}
		BufferedReader br = new BufferedReader(new FileReader(new File(
				networkFile)));
		// in the format of 'Age Group|isa|Group|D|'
		String line;
		String[] lineSplits;
		String child, parent, participant1, participant2;
		SemanticNetworkNode childNode, parentNode;
		while ((line = br.readLine()) != null) {
			lineSplits = line.split("\\|");
			relation = lineSplits[1];
			if (relation.equals(isa)) {
				child = lineSplits[0];
				parent = lineSplits[2];
				if (!nameNodeMap.containsKey(child))
					nameNodeMap.put(child, new SemanticNetworkNode(child));
				if (!nameNodeMap.containsKey(parent))
					nameNodeMap.put(parent, new SemanticNetworkNode(parent));
				childNode = nameNodeMap.get(child);
				parentNode = nameNodeMap.get(parent);

				parentNode.children.add(childNode);
				childNode.parent = parentNode;
			} else if (Arrays.asList(relations).contains(relation)) {
				participant1 = lineSplits[0];
				participant2 = lineSplits[2];
				relationParticipantsMap.get(relation).add(
						new RelationParticipants(participant1, participant2));
			} else {
				// does nothing
			}
		}
		br.close();
		readStyRelDefFile(styRelFile);

		// constructs parentString to childrenStrings map
		parentChildrenStringMap = getParentChildrenStringMap();
		// correct until here
		ArrayList<RelationParticipants> instantiations;
		ArrayList<String> p1Descendents, p2Descendents;
		for (Map.Entry<String, ArrayList<RelationParticipants>> entry : relationParticipantsMap
				.entrySet()) {
			relation = entry.getKey();
			instantiations = entry.getValue();
			HashSet<String> set = new HashSet<String>();
			relationInstatiationMap.put(relation, set);
			for (RelationParticipants rp : instantiations) {
				participant1 = rp.participant1;
				participant2 = rp.participant2;
				p1Descendents = new ArrayList<String>();
				p1Descendents.add(participant1);
				p2Descendents = new ArrayList<String>();
				p2Descendents.add(participant2);
				p1Descendents.addAll(getDescendents(participant1));
				p2Descendents.addAll(getDescendents(participant2));
				set.addAll(completeBipartiteMatch(p1Descendents, p2Descendents));
			}
		}

		// fillStypePairRelationMap();
		substituteIntoAbbrs();

		boolean flag = true;
		flag = false;
	}

	// private void fillStypePairRelationMap() {
	// // HashMap<String, HashSet<String>> relationAbbrInstatiationMap = new
	// // HashMap<String, HashSet<String>>();
	// // HashMap<String, HashSet<String>> invRelationAbbrInstatiationMap = new
	// // HashMap<String, HashSet<String>>();
	// for (Map.Entry<String, HashSet<String>> entry :
	// relationAbbrInstatiationMap
	// .entrySet()) {
	//
	// }
	// }

	private void substituteIntoAbbrs() {
		// HashMap<String, HashSet<String>> relationInstatiationMap = new
		// HashMap<String, HashSet<String>>();
		String rel, invRel, sty1Abbr, sty2Abbr;
		String[] splits;
		HashSet<String> set, invSet;
		String stypePair, invStypePair, mappedRel, invMappedRel;
		// no possibility of repeated relations
		for (Map.Entry<String, HashSet<String>> entry : relationInstatiationMap
				.entrySet()) {
			rel = entry.getKey();
			invRel = relInvRelMap.get(rel);
			set = new HashSet<String>();
			invSet = new HashSet<String>();
			relationAbbrInstatiationMap.put(rel, set);
			invRelationAbbrInstatiationMap.put(invRel, invSet);

			for (String instance : entry.getValue()) {
				splits = instance.split("&");
				sty1Abbr = nameAbbreviationMap.get(splits[0]);
				sty2Abbr = nameAbbreviationMap.get(splits[1]);
				set.add(sty1Abbr + "&" + sty2Abbr);
				invSet.add(sty2Abbr + "&" + sty1Abbr);

				// for constructing stypePairRelationMap
				stypePair = sty1Abbr + "&" + sty2Abbr;
				invStypePair = sty2Abbr + "&" + sty1Abbr;

				mappedRel = stypePairRelationMap.get(stypePair);
				if (mappedRel != null) {
					stypePairRelationMap.put(stypePair, mappedRel + "&" + rel);
					hasMultiRelations = true;
				} else {
					stypePairRelationMap.put(stypePair, rel);
				}

				/**
				 * remove mappings from pairs of semantic types to inverse
				 * relations
				 */
				// invMappedRel = stypePairRelationMap.get(invStypePair);
				// if (invMappedRel != null) {
				// stypePairRelationMap.put(invStypePair, invMappedRel + "&"
				// + invRel);
				// hasMultiRelations = true;
				// } else {
				// stypePairRelationMap.put(invStypePair, invRel);
				// }
			}

		}
	}

	private void readStyRelDefFile(String styRelFile) throws Exception {
		// 0 RT: Record Type (STY = Semantic Type or RL = Relation).
		// 1 UI: Unique Identifier of the Semantic Type or Relation.
		// 2 STY/RL: Name of the Semantic Type or Relation.
		// 3 STN/RTN: Tree Number of the Semantic Type or Relation.
		// 4 DEF: Definition of the Semantic Type or Relation.
		// 5 EX: Examples of Metathesaurus concepts with this Semantic Type (STY
		// records only).
		// 6 UN: Usage note for Semantic Type assignment (STY records only).
		// 7 NH: The Semantic Type and its descendants allow the non-human flag
		// (STY records only).
		// 8 ABR: Abbreviation of the Relation Name or Semantic Type.
		// 9 RIN: Inverse of the Relation (RL records only).
		// constructs nameAbbreviationMap

		String line;

		// STY|T001|Organism|A1.1|Generally, a living individual, including all
		// plants and animals.||NULL||orgm||
		// RL|T154|treats|R3.1.2|Applies a remedy with the object of effecting a
		// cure or managing a condition.||||TS|treated_by|
		String[] splits;
		BufferedReader br = new BufferedReader(new FileReader(new File(
				styRelFile)));
		while ((line = br.readLine()) != null) {
			splits = line.split("\\|");
			if (splits[0].equals("STY")) {
				nameAbbreviationMap.put(splits[2], splits[8]);
			} else if (splits[0].equals("RL")) {
				relInvRelMap.put(splits[2], splits[9]);
			} else {

			}
		}
		br.close();

	}

	private HashSet<String> completeBipartiteMatch(
			ArrayList<String> p1Descendents, ArrayList<String> p2Descendents) {
		HashSet<String> set = new HashSet<>();
		for (String p1 : p1Descendents) {
			for (String p2 : p2Descendents) {
				set.add(p1 + "&" + p2);
			}
		}
		return set;
	}

	/**
	 * Returns a string array of the participant and its descendents
	 * 
	 * @param participant1
	 * @return
	 */
	private ArrayList<String> getDescendents(String participant1) {
		ArrayList<String> ret = new ArrayList<String>();
		if (!parentChildrenStringMap.containsKey(participant1))
			return ret;
		ArrayList<String> childrenStringMap = parentChildrenStringMap
				.get(participant1);
		ret.addAll(childrenStringMap);
		for (String childName : childrenStringMap) {
			ret.addAll(getDescendents(childName));
		}
		return ret;
	}

	private HashMap<String, ArrayList<String>> getParentChildrenStringMap() {
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		String rootNodeName;
		// SemanticNetworkNode rootNode;
		// rootNode = nameNodeMap.get(rootNodeName);
		for (int i = 0; i < rootNodeNames.length; i++) {
			rootNodeName = rootNodeNames[i];
			map.put(rootNodeName, getChildrenNames(rootNodeName, map));
		}
		return map;
	}

	private ArrayList<String> getChildrenNames(String nodeName,
			HashMap<String, ArrayList<String>> map) {
		ArrayList<String> ret = new ArrayList<String>();
		SemanticNetworkNode parent = nameNodeMap.get(nodeName);
		ArrayList<SemanticNetworkNode> children = parent.children;
		String childName;
		for (SemanticNetworkNode child : children) {
			childName = child.name;
			ret.add(childName);
			if (child.children.size() != 0)
				map.put(childName, getChildrenNames(childName, map));
		}
		return ret;
	}

	public class SemanticNetworkNode {
		SemanticNetworkNode parent;
		ArrayList<SemanticNetworkNode> children;
		String name;

		public SemanticNetworkNode(String name) {
			this.name = name;
			children = new ArrayList<SemanticNetworkNode>();
		}
	}

	public class RelationParticipants {
		String participant1;
		String participant2;

		public RelationParticipants(String p1, String p2) {
			participant1 = p1;
			participant2 = p2;
		}
	}
}
