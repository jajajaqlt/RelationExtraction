package pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.PriorityQueue;

import edu.stanford.nlp.ling.CoreLabel;

public class ClassUtilities {
	public static class Sentence {
		public ArrayList<Phrase> phrases;
		public List<CoreLabel> words;

		// fields inherited from Candidate class
		public String netRelation;
		public boolean isInverse;
		public boolean isPositive;
		public String metaRelation;
		// phrase index of precandidate prev, physically before
		public int entity1Index;
		// phrase index of precandidate succ, physically after
		public int entity2Index;
		// prev.rootSType
		public String entity1NE;
		// succ.rootSType
		public String entity2NE;

		// Mintz09: For each entity, one 'window' node that is not part of the
		// dependency path
		HashMap<Integer, TypedDependencyProperty> entity1Dependencies;
		HashMap<Integer, TypedDependencyProperty> entity2Dependencies;

		// map data structure keeps insertion order
		LinkedHashMap<Integer, ArrayList<TypedDependencyProperty>> path;
	}

	// only saves lexical information
	public static class Phrase {
		public ArrayList<Word> words;
		public String pText;

		public Phrase(String t) {
			pText = t;
			words = new ArrayList<ClassUtilities.Word>();
		}
	}

	// only saves lexical information
	public static class Word {
		public String tag;
		// after adding tag => 0-based indices; after adding typed dependencies
		// 1-based indices;
		public int index;
		public String wText;

		public Word(String t, int i, String w) {
			tag = t;
			index = i;
			wText = w;
			// dependencies = new ArrayList<ClassUtilities.Dependency>();
		}
	}

	public static class TypedDependencyProperty {
		// true for to direction, false otherwise
		public boolean direction;
		public String relation;

		public TypedDependencyProperty(boolean d, String r) {
			direction = d;
			relation = r;
		}
	}

	public static class Vertex implements Comparable<Vertex> {
		public final String name;
		public Edge[] adjacencies;
		public double minDistance = Double.POSITIVE_INFINITY;
		public Vertex previous;

		public Vertex(String argName) {
			name = argName;
		}

		public String toString() {
			return name;
		}

		public int compareTo(Vertex other) {
			return Double.compare(minDistance, other.minDistance);
		}
	}

	public static class Edge {
		public final Vertex target;
		public final double weight;

		public Edge(Vertex argTarget, double argWeight) {
			target = argTarget;
			weight = argWeight;
		}
	}

	public static void computePaths(Vertex source) {
		source.minDistance = 0.;
		PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
		vertexQueue.add(source);

		while (!vertexQueue.isEmpty()) {
			Vertex u = vertexQueue.poll();

			// Visit each edge exiting u
			for (Edge e : u.adjacencies) {
				Vertex v = e.target;
				double weight = e.weight;
				double distanceThroughU = u.minDistance + weight;
				if (distanceThroughU < v.minDistance) {
					vertexQueue.remove(v);
					v.minDistance = distanceThroughU;
					v.previous = u;
					vertexQueue.add(v);
				}
			}
		}
	}

	public static List<Vertex> getShortestPathTo(Vertex target) {
		List<Vertex> path = new ArrayList<Vertex>();
		for (Vertex vertex = target; vertex != null; vertex = vertex.previous)
			path.add(vertex);
		Collections.reverse(path);
		return path;
	}

	public static void main(String[] args) {
		Vertex v0 = new Vertex("Redvile");
		Vertex v1 = new Vertex("Blueville");
		Vertex v2 = new Vertex("Greenville");
		Vertex v3 = new Vertex("Orangeville");
		Vertex v4 = new Vertex("Purpleville");

		v0.adjacencies = new Edge[] { new Edge(v1, 5), new Edge(v2, 10),
				new Edge(v3, 8) };
		v1.adjacencies = new Edge[] { new Edge(v0, 5), new Edge(v2, 3),
				new Edge(v4, 7) };
		v2.adjacencies = new Edge[] { new Edge(v0, 10), new Edge(v1, 3) };
		v3.adjacencies = new Edge[] { new Edge(v0, 8), new Edge(v4, 2) };
		v4.adjacencies = new Edge[] { new Edge(v1, 7), new Edge(v3, 2) };
		Vertex[] vertices = { v0, v1, v2, v3, v4 };
		computePaths(v1);
		for (Vertex v : vertices) {
			System.out.println("Distance to " + v + ": " + v.minDistance);
			List<Vertex> path = getShortestPathTo(v);
			System.out.println("Path: " + path);
		}
	}

	// public static class DependencyCollection {
	// public String mainIndex;
	// ArrayList<Destination> destinations;
	//
	// public DependencyCollection() {
	// destinations = new ArrayList<Destination>();
	// }
	// }
	//
	// public static class Destination {
	// public int subIndex;
	// // true for to direction, false otherwise
	// public boolean direction;
	// public String relation;
	//
	// public Destination(int s, boolean d, String r) {
	// subIndex = s;
	// direction = d;
	// relation = r;
	// }
	// }

	// public static class leftRightTDP {
	// public int index;
	// TypedDependencyProperty left;
	// TypedDependencyProperty right;
	//
	// public leftRightTDP(){
	//
	// }
	//
	// public leftRightTDP(TypedDependencyProperty l, TypedDependencyProperty
	// r){
	// left = l;
	// right = r;
	// }
	// }
}
