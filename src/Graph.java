import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * An immutable representation of a directed graph 
 * with vertices and edges.
 *
 * @author Realiserad
 * @author Edvin Lundberg
 */
public class Graph {
	private ArrayList<LinkedList<Integer>> neighbours, backNeighbours;
	private int[][] m;
	private int vertexCount, edgeCount;
	private int lowerBoundPursuers;
	
	public static void main(String[] args) {
		// Cycles {0, 1, 2, 3}, {0, 3}, {2}
		int[][] m1 = {
				{ 0, 0, 0, 1 },
				{ 1, 0, 0, 0 },
				{ 0, 1, 1, 0 },
				{ 1, 0, 1, 0 },
		};
		Graph g = new Graph(m1);
		System.out.println("Cycles: " + g.getCycles().toString());
		System.out.println("Conjecture: " + g.getLowerBoundNrOfPursuers());
		// Cycles {0, 2, 4}, {1, 2, 3}, {0, 2, 1} {2, 3, 4}
		int[][] m2 = {
				{ 0, 1, 0, 0, 1 },
				{ 0, 0, 1, 0, 0 },
				{ 1, 0, 0, 1, 0 },
				{ 0, 1, 0, 0, 1 },
				{ 0, 0, 1, 0, 0 },
		};
		g = new Graph(m2);
		System.out.println("Cycles: " + g.getCycles().toString());
		System.out.println("Conjecture: " + g.getLowerBoundNrOfPursuers());
		// Cycles {0, 1, 2, 3}, {0, 1, 2, 4}, {4, 5}, {2}
		int[][] m3 = {
				{ 0, 0, 0, 1, 1, 0 },
				{ 1, 0, 0, 0, 0, 0 },
				{ 0, 1, 1, 0, 0, 0 },
				{ 0, 0, 1, 0, 0, 0 },
				{ 0, 0, 1, 0, 0, 1 },
				{ 0, 0, 0, 0, 1, 0 },
		};
		g = new Graph(m3);
		System.out.println("Cycles: " + g.getCycles().toString());
		System.out.println("Conjecture: " + g.getLowerBoundNrOfPursuers());
		// Cycles {0,1,2,3,4}, {0,1}, {0,1,4}
		int[][] m4 = {
				{ 0, 1, 0, 0, 1 },
				{ 1, 0, 0, 1, 0 },
				{ 1, 0, 0, 0, 0 },
				{ 0, 0, 1, 0, 0 },
				{ 0, 1, 0, 0, 0 },
		};
		g = new Graph(m4);
		System.out.println("Cycles: " + g.getCycles().toString());
		System.out.println("Conjecture: " + g.getLowerBoundNrOfPursuers());
		int[][] m5 = {
				{ 1, 0, 1},
				{ 1, 1, 0},
				{ 0, 1, 1},
		};
		g = new Graph(m5);
		System.out.println("Cycles: " + g.getCycles().toString());
		System.out.println("Conjecture: " + g.getLowerBoundNrOfPursuers());
		System.out.println("-----------");
		int[][] m6 = {
				{0,1,0,0,0},
				{0,0,1,0,0},
				{1,0,0,0,0},
				{0,0,1,0,1},
				{0,0,0,1,0},
		};
		g = new Graph(m6);
		for (List<Integer> ll : g.getStrongComponents()) {
			System.out.println(ll.toString());
		}
		System.out.println("-----------");
		int[][] m7 = {
				{0,1,0,0},
				{1,0,1,0},
				{0,1,1,1},
				{0,0,1,1},
		};
		g = new Graph(m7);
		for (List<Integer> ll : g.getStrongComponents()) {
			System.out.println(ll.toString());
		}
		
		System.out.println("-----------");
		int[][] m8 = {
				{1,0,0,0,0},
				{1,1,0,0,0},
				{0,1,1,1,0},
				{0,0,1,0,0},
				{0,0,1,1,1},
		};
		g = new Graph(m8);
		for (List<Integer> ll : g.getStrongComponents()) {
			System.out.println(ll.toString());
		}
	}
	
	@SuppressWarnings("unused")
	private Graph() {
		/* This constructor is not available */
	}
	
	/**
	 * Create a graph from a neighbour matrix "m".
	 * The matrix given as first argument should contain
	 * a one on position m[i][j] if there is an edge j->i
	 * and zero otherwise.
	 */
	public Graph(int[][] m) {
		this.m = m;
		this.vertexCount = m.length;
		this.lowerBoundPursuers = 0; // Initial lower bound
		/* Create neighbour list */
		neighbours = new ArrayList<LinkedList<Integer>>(vertexCount);
		backNeighbours = new ArrayList<LinkedList<Integer>>(vertexCount);
		for (int i=0; i<vertexCount; i++) {
			neighbours.add(new LinkedList<Integer>());
			backNeighbours.add(new LinkedList<Integer>());
		}
		for (int row=0; row<m.length; row++) {
			for (int col=0; col<m.length; col++) {
				if (m[row][col] == 1) {
					// Add edge col->row
					neighbours.get(col).add(row);
					edgeCount++;
					// Add edge row->col
					backNeighbours.get(row).add(col);
				}
			}
		}
	}
	
	/**
	 * Get the adjacency matrix. The matrix will contain
	 * a one on position m[i][j] if there is an edge j->i
	 * and zero otherwise.
	 * @return m Adjacency matrix
	 */
	public int[][] getAdjacencyMatrix() {
		return m;
	}
	
	/**
	 * Get the number of vertices for this graph.
	 */
	public int getVertexCount() {
		return vertexCount;
	}
	
	/**
	 * Get the number of edges in this graph.
	 */
	public int getEdgeCount() {
		return edgeCount;
	}
	
	/**
	 * Get all cycles in this graph.
	 */
	public ArrayList<LinkedList<Integer>> getCycles() {
		// visited[v] is true if vertex v has been visited during current dfs
		boolean[] visited = new boolean[vertexCount];
		// traversed[vertexCount*u+v] is true if the edge u->v has been traversed during any dfs
		boolean[] traversed = new boolean[vertexCount*vertexCount];
		// path contains the path traveled during current dfs
		LinkedList<Integer> path = new LinkedList<Integer>();
		// list of all cycles found
		ArrayList<LinkedList<Integer>> cycles = new ArrayList<LinkedList<Integer>>();
		for (int vertex=0; vertex<vertexCount; vertex++) {
			for (int neighbour : neighbours.get(vertex)) {
				if (traversed[vertex*vertexCount+neighbour]) continue;
				// Start new dfs with vertex as start node
				Arrays.fill(visited, false);
				path.clear();
				visited[vertex] = true;
				path.add(vertex);
				dfs(vertex, neighbour, visited, traversed, path, cycles);
			}
		}
		return cycles;
	}
	
	private void dfs(int previous, int current, boolean[] visited, boolean[] traversed, LinkedList<Integer> path, ArrayList<LinkedList<Integer>> cycles) {
		// Mark the edge which lead here as traversed
		traversed[previous*vertexCount+current] = true;
		// Check if we have a cycle
		if (visited[current]) {
			path.addLast(current);
			cycles.add(extractCycle(path));
			return;
		}
		// Continue dfs
		visited[current] = true;
		path.addLast(current);
		for (int neighbour : neighbours.get(current)) {
			dfs(current, neighbour, visited, traversed, path, cycles);
		}
		visited[current] = false;
		path.removeLast();
	}
	
	/**
	 * Return the cycle in a path.
	 */
	private LinkedList<Integer> extractCycle(LinkedList<Integer> path) {
		int tail = path.removeLast(); // The cycle should end in this vertex
		LinkedList<Integer> cycle = new LinkedList<Integer>();
		Iterator<Integer> it = path.descendingIterator(); // Iterator in reverse order
		while (it.hasNext()) {
			int vertex = it.next();
			cycle.add(vertex);
			if (vertex == tail) {
				return cycle;
			}
		}

		return null;
	}
	
	/**
	 * Intersect two lists and return the result as a set, i.e return a set
	 * containing the element which resides in both lists.
	 */
	public HashSet<Integer> intersect(LinkedList<Integer> a, LinkedList<Integer> b) {
		HashSet<Integer> s1 = new HashSet<Integer>(a);
		HashSet<Integer> s2 = new HashSet<Integer>(b);
		s1.retainAll(s2);
		return s1;
	}
	
	/**
	 * Use the "Edvin-conjecture" to calculate a lower bound for the minimum number of pursuers needed. 
	 * @return A lower bound for the number of pursuers needed.
	 */
	public int getLowerBoundNrOfPursuers() {
		// If already calculated lower bound, return it 
		if (this.lowerBoundPursuers != 0) {
			return this.lowerBoundPursuers;
		}
		
		/* Recall that:
		 * The matrix given as first argument should contain
		 * a one on position m[i][j] if there is an edge j->i
		 * and zero otherwise.
		 */
		// Keep track of the in-degree of each vertex
		final int[] indegree = new int[vertexCount];
		for (int r = 0; r < m.length; r++) {
			for (int c = 0; c < m.length; c++) {
				indegree[r] += m[r][c];
			}
		}
		
		// Array of vertices which we will sort based on in-degree 
		Integer[] vertices = new Integer[vertexCount];
		for (int i = 0; i<vertices.length; i++) {
			vertices[i] = i;
		}
		
		// Sort vertices in ascending order of in-degree
		Arrays.sort(vertices, new Comparator<Integer>() {
			/**
			 * Compares its two arguments for order. 
			 * Returns a negative integer, zero, or a positive
			 * integer as the first argument is less than, equal 
			 * to, or greater than the second.
			 */
			@Override
			public int compare(Integer a, Integer b) {
				return indegree[a]-indegree[b];
			}
		});
		
		/*
		 * Now the vertices array should be sorted on indegree and we traverse it from 
		 * left to right. We process the vertices until one satisfies this condition:
		 * 	Let v_0 be the vertex we are processing
		 * 	Let x be v_0's indegree
		 * 	Find a path leading out of v_0 [v_0,v_1,v_2,v_3,...,v_k] where 
		 * 		indegree[v_i] <= x+i, 0<=i<=k
		 * 		and 
		 * 		indegree(v_k) == max(indegree)
		 * 	
		 */
		int maxInDegree = indegree[indegree.length-1]; //Last element should have highest indegree
		//This loop guarantees that lower bound will be found, if we are at the last vertex with
		// highest indegree, a valid path is trivially found.
		for (int i = 0; i<vertices.length; i++) {
			int v_0 = vertices[i];
			int x = indegree[v_0];
			if (validConjecturePathExists(v_0, x, maxInDegree, indegree)) {
				this.lowerBoundPursuers = x; //We need x pursuers to start by blocking v_0
				break;
			}
		}
		
		return this.lowerBoundPursuers;
	}

	/**
	 * Check if a valid conjecture path (of distinct vertices) exists which starts from v_0.
	 * 
	 * @param v_0 The vertex which the search starts from
	 * @param x The indegree of v_0
	 * @param maxInDegree The maximum indegree in the graph
	 * @param indegree The array of indegrees of the vertices in the graph
	 * @return True iff there exists a path leading out of v_0 [v_0,v_1,v_2,v_3,...,v_k] where 
	 * 		indegree(v_i) <= x+i, 0<=i<=k
	 * 		and 
	 * 		indegree(v_k) == max(indegree).
	 */
	private boolean validConjecturePathExists(int v_0, int x, int maxInDegree, final int[] indegree) {		
		// Variant of dfs

		// visited[v] is true if vertex v has been visited during current bfs
		boolean[] visited = new boolean[vertexCount];
		Arrays.fill(visited, false);
		return dfsConjecture(v_0,x,0,maxInDegree, visited, indegree);
	}
	
	/**
	 * If a valid path could be found from here, pathFound will be true. 
	 * 
	 * 
	 * @param v_i Current vertex 
	 * @param x The indegree of v_0
	 * @param i index in path
	 * @param pathFound "Return"-value, check this after call.
	 * @param maxDegree maxDegree in graph
	 * @param visited Keep track of which vertices are visited, cycles are not allowed for this path
	 * @param indegree array of indegrees of vertices
	 */
	private boolean dfsConjecture(int v_i, int x, int i, int maxInDegree, boolean[] visited, final int[] indegree)  {
		// Not valid path
		if (!(indegree[v_i] <= x+i)) return false;
		
		// Found valid path
		if (indegree[v_i] == maxInDegree) {
			return true;
		}
		
		// Continue search for valid path
		visited[v_i] = true;
		
		for (int neighbor : neighbours.get(v_i)) {
			if (!visited[neighbor]) {
				boolean hasValidPath = dfsConjecture(neighbor, x, i+1, maxInDegree, visited, indegree);
				if (hasValidPath) return true;
			}
		}
		
		/* When leaving a vertex, consider it unvisited */
		visited[v_i] = false;
		return false;
	}
	
	/**
	 * @return True iff the vertex has a self-loop
	 */
	private boolean selfLoop(int vertex) {
		return m[vertex][vertex] == 1;
	}
	
	/**
	 * Returns a list of maximal strong components for this graph. The list is sorted in the
	 * order in which the components needs to be decontaminated.
	 * @return
	 * TODO returnvalue should be List<Graph>
	 */
	public List<Integer>[] getStrongComponents() {
		List<Graph> subgraphs = new LinkedList<Graph>();
		/* comp[v] describes the component that v belongs to, and zero if v belongs to no component so far */
		int[] comp = new int[getVertexCount()]; 

		// Number of strongly connected components in graph
		int numComponents = 0;
		
		for (int v = 0; v < comp.length; v++) {
			// If already in component, skip it
			if (comp[v]!=0) continue;
			
			numComponents++;
			int compNumber = numComponents; //+1 to avoid confusion with "zero value= no component"
			// Our component consists of v so far, try to expand 
			comp[v] = compNumber;
			buildStrongComponent(v, new boolean[comp.length], new LinkedList<Integer>(), comp, compNumber);
		}
		
		/* Topological ordering */
		// One set for each component
		@SuppressWarnings("unchecked")
		List<Integer>[] comps = new LinkedList[numComponents];
		for (int i = 0; i < comps.length; i++) {
			comps[i] = new LinkedList<Integer>();
		}
		for (int v = 0; v < comp.length; v++) {
			// Add v to the component it belongs to
			comps[comp[v]-1].add(v);
		}
		
		
		//TODO bastian fixar när han käkat, skapa subgraphs
		
		return comps;
	}
	
	/**
	 * Build the strong component which "vertex" belongs to. In a strong component, there is a path connecting all pairs 
	 * of vertices in the component. 
	 * 
	 * @param visited The visited vertices.
	 * @param path The path travelled so far.
	 * @param comp comp[v]=the compNumber that v belongs to, 0 if not belonging to any component. Will be mutated.
	 * @param compNumber The component number of this component.
	 */
	private void buildStrongComponent(int vertex, boolean[] visited, LinkedList<Integer> path, int[] comp, int compNumber) {
		//Variant of dfs
		
		if (visited[vertex]) {
			// I was already here yo
			return;
		}
		
		visited[vertex] = true;
		path.addLast(vertex);
		
		// If returning to an "accepted" vertex, mark this path as "accepted", i.e. belonging to this component  
		for (int neighbour : neighbours.get(vertex)) {
			if (comp[neighbour] == compNumber) {
				markPath(comp, compNumber, path);
				break;
			}
		}
		
		// Expand component further
		for (int neighbour : neighbours.get(vertex)) {
			if (comp[neighbour] != 0) {
				// No need to search another component (we will never return to this component if we do)
				// No need to check vertices already in this component
				continue;
			}
			// Expand
			buildStrongComponent(neighbour, visited, path, comp, compNumber);
		}
		
		path.removeLast();
	}

	/**
	 * Mark the vertices in given path as belonging to the component with the compNumber 
	 * given as the second parameter. 
	 * 
	 * @param comp This parameter will be mutated, vertices on their index will have value compNumber.
	 * @param compNumber The component number of this component
	 * @param path Path of vertices which has been proven to belong to this component.
	 */
	private void markPath(int[] comp, int compNumber, LinkedList<Integer> path) {
		Iterator<Integer> it = path.descendingIterator();
		while (it.hasNext()) {
			int v = it.next();
			if (comp[v] == compNumber) {
				return;
			}
			comp[v] = compNumber;
		}
	}
}
