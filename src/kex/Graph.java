package kex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Wrapper class for DefaultDirectedGraph. Contains functionality for finding
 * strongly connected components, calculating the adjacency matrix, finding
 * cycles, and translating vertex labels between graphs.
 * 
 * @author Bastian Fredriksson
 * @author Edvin Lundberg
 */
public class Graph {
	private DefaultDirectedGraph<Integer, DefaultEdge> graph;
	private ArrayList<LinkedList<Integer>> adjacencyList;
	private int lowerBound, upperBound, estimate;
	private int[] indegree, translator;

	/**
	 * Create a new graph from an adjacency list.
	 * @param adjacencyList A list containing the neighbours for each vertex.
	 */
	public Graph(ArrayList<LinkedList<Integer>> adjacencyList) {
		this.graph = buildGraph(adjacencyList);
		this.adjacencyList = adjacencyList;
	}
	
	/**
	 * Create a new subgraph from an adjacency list and a translator
	 * given as arguments.
	 */
	public Graph(ArrayList<LinkedList<Integer>> adjacencyList, int[] translator) {
		this.graph = buildGraph(adjacencyList);
		this.adjacencyList = adjacencyList;
		this.translator = translator;
	}

	/**
	 * Create a new graph from an adjacency matrix.
	 * @param adjacencyMatrix A matrix M with M[v][u]=1 if
	 * there is an edge u->v and zero otherwise.
	 */
	public Graph(int[][] adjacencyMatrix) {
		/* Create adjacency list */
		ArrayList<LinkedList<Integer>> adjacencyList = new ArrayList<LinkedList<Integer>>(adjacencyMatrix.length);
		for (int i = 0; i < adjacencyMatrix.length; i++) adjacencyList.add(new LinkedList<Integer>());
		for (int v = 0; v < adjacencyMatrix.length; v++) {
			for (int u = 0; u < adjacencyMatrix.length; u++) {
				if (adjacencyMatrix[v][u] == 1) {
					adjacencyList.get(u).add(v);
				}
			}
		}

		this.graph = buildGraph(adjacencyList);
		this.adjacencyList = adjacencyList;
	}

	/**
	 * Build a directed graph from an adjacency list.
	 * @return A directed graph.
	 */
	private DefaultDirectedGraph<Integer, DefaultEdge> buildGraph(ArrayList<LinkedList<Integer>> adjacencyList) {
		DefaultDirectedGraph<Integer, DefaultEdge> mGraph = new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);

		/* Add vertices */
		for (int vertex = 0; vertex < adjacencyList.size(); vertex++) {
			mGraph.addVertex(vertex);
		}

		/* Add edges u->v */
		for (int u = 0; u < adjacencyList.size(); u++) {
			for (int v : adjacencyList.get(u)) {
				mGraph.addEdge(u, v);
			}
		}

		return mGraph;
	}

	/**
	 * Get the number of vertices for this graph.
	 * @return The number of vertices.
	 */
	public int getVertexCount() {
		return this.adjacencyList.size();
	}

	/**
	 * Calculate the adjacency matrix M for this graph. M[v][u]=1 if
	 * there is an edge u->v and zero otherwise. 
	 */
	public int[][] getAdjacencyMatrix() {
		int[][] M = new int[this.getVertexCount()][this.getVertexCount()];
		for (int u = 0; u < this.getVertexCount(); u++) {
			for (int v : this.getNeighbours(u)) {
				M[v][u] = 1;
			}
		}
		return M;
	}

	/**
	 * Get a list of all neighbours to the vertex given as argument.
	 * @param vertex The vertex whose neighbours should be sought.
	 * @return A list of neighbours.
	 */
	public LinkedList<Integer> getNeighbours(int vertex) {
		return this.adjacencyList.get(vertex);
	}

	/**
	 * Determine if this graph is a singleton graph.
	 * @return True if the graph consists of exactly one vertex, false otherwise.
	 */
	public boolean isSingleton() {
		return this.getVertexCount() == 1 ? true : false;
	}

	/**
	 * Calculates a lower bound for the search number of this graph.
	 * @return A lower bound for the search number.
	 */
	public int getLowerBound() {
		/* Return previously calculated lower bound, if it exists */
		if (this.lowerBound != 0) {
			return this.lowerBound;
		}

		/* Check if indegree already calculated */
		if (this.indegree == null) {
			getIndegree();
		}

		this.lowerBound = Math.max(1, min(indegree));
		return this.lowerBound;
	}

	public int getUpperBound() {
		/* Return previously calculated, if it exists */
		if (this.upperBound != 0) {
			return this.upperBound;
		}

		// TODO
		this.upperBound = this.getVertexCount();
		return this.upperBound;
	}

	/**
	 * Calculates an estimate of the search number based on Edvin's estimate.
	 * The estimate E returned is in the interval getLowerBound() <= E <= getUpperBound().  
	 * @return An estimate of the search number.
	 */
	public int getEstimate() {
		/* Return previously calculated estimate, if it exists */
		if (estimate != 0) {
			return this.estimate;
		}

		/* Check if indegree already calculated */
		if (this.indegree == null) {
			getIndegree();
		}

		Integer[] vertices = new Integer[this.getVertexCount()];
		for (int i = 0; i < vertices.length; i++) {
			vertices[i] = i;
		}
		Arrays.sort(vertices, new Comparator<Integer>() {
			@Override
			public int compare(Integer a, Integer b) {
				return indegree[a]-indegree[b];
			}
		});

		int maxIndegree = max(indegree);

		/* Process vertices with low indegree first. */
		for (int i = 0; i < this.getVertexCount(); i++) {
			int v_0 = vertices[i]; // Source vertex
			int x = indegree[v_0]; // Indegree for source vertex
			if (validEstimatePathExists(v_0, x, maxIndegree, indegree)) {
				this.estimate = x;
				break;
			}
		}

		return Math.max(1, this.estimate);
	}

	/**
	 * Returns true if there exists a path v_0, v_1... v_k where 
	 * indegree[v_i] <= x+i for 0 <= i <= k and indegree[v_k] == maxDegree.
	 * @param v_0 The first vertex in the path.
	 * @param x Indegree for v_0
	 * @param maxDegree The maximum indegree for any vertex in the graph.
	 * @param indegree An array with indegree for the vertices in the graph.
	 * @return True if there exists a valid path according to Edvin's conjecture.
	 */
	private boolean validEstimatePathExists(int v_0, int x, int maxIndegree, int[] indegree) {
		return dfs(v_0, x, 0, maxIndegree, new boolean[this.getVertexCount()], indegree);
	}

	/**
	 * Depth-first traversal of this graph used to find a path v_0, v_1... v_k in Edvin's
	 * conjecture.
	 * @param v_i The current vertex.
	 * @param x The indegree of v_0.
	 * @param i Number of steps taken in this depth-first traversal.
	 * @param maxIndegree Maximum indegree for the vertices in this graph.
	 * @param visited An array with visited vertices.
	 * @param indegree Array with indegree for the vertices in this graph.
	 */
	private boolean dfs(int v_i, int x, int i, int maxIndegree, boolean[] visited, int[] indegree)  {
		if (visited[v_i] || indegree[v_i] > x+i) {
			return false;
		}

		if (indegree[v_i] == maxIndegree) {
			/* v_k found */
			return true;
		}

		/* Mark the current vertex as visited */
		visited[v_i] = true;

		for (int neighbour : this.getNeighbours(v_i)) {
			if (dfs(neighbour, x, i+1, maxIndegree, visited, indegree)) {
				return true;
			}
		}

		/* When leaving a vertex, consider it unvisited */
		visited[v_i] = false;
		return false;
	}

	/**
	 * Returns the maximum element in an array of integers.
	 * @param array The array to search.
	 * @return The maximum value.
	 */
	private int max(int[] array) {
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (array[i] > max) {
				max = array[i];
			}
		}
		return max;
	}

	/**
	 * Returns the minimum element in an array of integers.
	 * @param array The array to search.
	 * @return The minimum value.
	 */
	private int min(int[] array) {
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
			}
		}
		return min;
	}

	/**
	 * Calculate and return the indegree for each vertex in this graph.
	 * @return A copied array which on position i contains the indegree for vertex i.
	 */
	public int[] getIndegree() {
		if (indegree == null) {
			indegree = new int[getVertexCount()];
			for (LinkedList<Integer> neighbourList : adjacencyList) {
				for (int neighbour : neighbourList) {
					indegree[neighbour]++;
				}
			}
		}
		return Arrays.copyOf(indegree, indegree.length);
	}

	/**
	 * Intersect two lists and return the result as a list, i.e return a set
	 * containing the elements which reside in both lists.
	 */
	public LinkedList<Integer> intersect(LinkedList<Integer> a, LinkedList<Integer> b) {
		/* Create lookup set for one of the lists */
		HashSet<Integer> lookup = new HashSet<Integer>(b);
		LinkedList<Integer> intersection = new LinkedList<Integer>();
		for (int i : a) {
			if (b.contains(i)) {
				intersection.add(i);
			}
		}
		return intersection;
	}

	/**
	 * Returns a list of strongly connected components in this graph, sorted
	 * in topological order.
	 * @return A list of strongly connected components sorted in topological
	 * order.
	 */
	public List<Graph> getStrongComponents() {
		/* Find strongly connected components. */
		StrongConnectivityInspector<Integer, DefaultEdge> sci = new StrongConnectivityInspector<Integer, DefaultEdge>(this.graph);
		List<Set<Integer>> partitions = sci.stronglyConnectedSets();
		
		ArrayList<Graph> strongComponents = new ArrayList<Graph>(partitions.size());
		List<Graph> orderedStrongComponents = new LinkedList<Graph>();

		/* Create a DAG defining a topological order for the strongly connected components of this graph */
		ArrayList<TreeSet<Integer>> DAG = new ArrayList<TreeSet<Integer>>(partitions.size());
		for (int i = 0; i < partitions.size(); i++) DAG.add(new TreeSet<Integer>());
		
		int partitionNumber = 0;
		for (Set<Integer> partition : partitions) {
			Integer[] partitionArray = partition.toArray(new Integer[partition.size()]);
			
			/* Create translation table */
			int[] newToOldTranslator = new int[partition.size()];
			int newIndex = 0;
			for (int vertex : partitionArray) {
				newToOldTranslator[newIndex] = vertex;
				newIndex++;
			}
			int[] oldToNewTranslator = new int[this.getVertexCount()];
			Arrays.fill(oldToNewTranslator, -1);
			for (int vertex = 0; vertex < newToOldTranslator.length; vertex++) {
				oldToNewTranslator[newToOldTranslator[vertex]] = vertex;
			}

			/* Create strong component induced by the vertices in the partition */
			ArrayList<LinkedList<Integer>> strongComponent = new ArrayList<LinkedList<Integer>>(partition.size());
			for (int i = 0; i < partition.size(); i++) strongComponent.add(new LinkedList<Integer>());
			for (int vertex : partitionArray) {
				for (int neighbour : getNeighbours(vertex)) {
					if (partition.contains(neighbour)) {
						/* The edge vertex->neighbour belong to the strong component */
						strongComponent.get(oldToNewTranslator[vertex]).add(oldToNewTranslator[neighbour]);
					} else {
						/* This partition supersedes another partition in the topological order */
						DAG.get(partitionNumber).add(getPartitionNumber(neighbour, partitions));
					}
				}
			}
			strongComponents.add(new Graph(strongComponent, newToOldTranslator));
			
			partitionNumber++;
		}
		
		/* Create topological order by depth-first traversal of the DAG */
		Stack<Integer> stack = new Stack<Integer>();
		boolean[] visited = new boolean[partitions.size()];
		for (int i = 0 ; i < partitions.size(); i++) {
			dfs(stack, visited, i, DAG);
		}
		while (!stack.isEmpty()) {
			orderedStrongComponents.add(strongComponents.get(stack.pop()));
		}
		
		return orderedStrongComponents;
	}

	private int getPartitionNumber(int vertex, List<Set<Integer>> partitions) {
		int partitionNumber = 0;
		for (Set<Integer> partition : partitions) {
			if (partition.contains(vertex)) {
				return partitionNumber;
			}
			partitionNumber++;
		}
		
		return -1;
	}
	
	/**
	 * Create topological order by depth first search.
	 * The topological order is obtained by popping the stack 
	 * given as first parameter.
	 */
	private void dfs(Stack<Integer> stack, boolean[] visited, int vertex, ArrayList<TreeSet<Integer>> DAG) {
		if (visited[vertex]) {
			return;
		}
		visited[vertex] = true;
		for (int neighbour : DAG.get(vertex)) {
			dfs(stack, visited, neighbour, DAG);
		}	
		stack.push(vertex);		
	}

	/**
	 * Returns the string representation of this graph.
	 * 
	 * The first line contains the vertices of this graph, 
	 * e.g [1, 2, 3] followed by one or more edges
	 * on the form u->v.
	 * 
	 * The edges are sorted in ascending order, such
	 * that the edge u->v precedes x->y if u < x or
	 * if u == x and v < y.
	 * 
	 * The vertex labels are translated to
	 * indices for the whole graph before they are
	 * printed.
	 * 
	 * Time complexity: O(|V|*|E|*log(|E|))
	 * 
	 * @return A string representation of this graph.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		/* Print vertices */
		sb.append("[");
		for (int i = 0; i < getVertexCount(); i++) {
			sb.append(translate(i));
			if (i != getVertexCount() - 1) {
				sb.append(", ");
			}
		}
		sb.append("]\n");

		/* Print edges */
		for (int i = 0; i < getVertexCount(); i++) {
			LinkedList<Integer> queue = getNeighbours(i);
			if (queue.isEmpty()) continue;
			Collections.sort(queue);
			for (int neighbour : queue) {
				sb.append((translate(i)+1) + "->" + (translate(neighbour)+1) + "\n");
			}
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
	
	/**
	 * Return a string containing the edges of this graph. Each line
	 * is on the format u->v.
	 */
	public String edgeString() {
		StringBuilder sb = new StringBuilder();

		/* Print edges */
		for (int i = 0; i < getVertexCount(); i++) {
			LinkedList<Integer> queue = getNeighbours(i);
			if (queue.isEmpty()) continue;
			Collections.sort(queue);
			for (int neighbour : queue) {
				sb.append((translate(i)+1) + "->" + (translate(neighbour)+1) + "\n");
			}
		}
		
		if (sb.length() == 0) return "no edges";
		
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * Translates the vertex index given as argument to a vertex
	 * index for the graph this graph is a subgraph of. If this
	 * graph is not a subgraph of another graph, no translation
	 * will be done.
	 * @param vertex The vertex to translate.
	 * @return The translated vertex.
	 */
	public int translate(int vertex) {
		if (translator == null) {
			return vertex;
		}
		return translator[vertex];
	}
	
	/**
	 * Returns a list of elementary circuits in this graph.
	 */
	public List<List<Integer>> getCycles() {
		JohnsonSimpleCycles<Integer, DefaultEdge> jsc = new JohnsonSimpleCycles<Integer, DefaultEdge>(this.graph);
		return jsc.findSimpleCycles();
	}
}
