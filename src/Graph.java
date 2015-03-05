import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * An immutable representation of a directed graph 
 * with vertices and edges.
 *
 * @author Realiserad
 */
public class Graph {
	private ArrayList<LinkedList<Integer>> neighbours;
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
		System.out.println(g.getCycles().toString());
		
		// Cycles {0, 2, 4}, {1, 2, 3}, {0, 2, 1} {2, 3, 4}
		int[][] m2 = {
				{ 0, 1, 0, 0, 1 },
				{ 0, 0, 1, 0, 0 },
				{ 1, 0, 0, 1, 0 },
				{ 0, 1, 0, 0, 1 },
				{ 0, 0, 1, 0, 0 },
		};
		g = new Graph(m2);
		System.out.println(g.getCycles().toString());
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
		System.out.println(g.getCycles().toString());
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
		for (int i=0; i<vertexCount; i++) neighbours.add(new LinkedList<Integer>());
		for (int col=0; col<m.length; col++) {
			for (int row=0; row<m.length; row++) {
				if (m[row][col] == 1) {
					// Add edge col->row
					neighbours.get(col).add(row);
					edgeCount++;
				}
			}
		}
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
		/*
		 * I propose an alternative implementation:
		 * 	Go backwards in the path and add vertices to cycle until reaching the tail/head.
		 * 	This way no unnecessary vertices are processed.  
		 */
		int tail = path.removeLast(); // The cycle should end in this vertex
		LinkedList<Integer> cycle = new LinkedList<Integer>();
		Iterator<Integer> it = path.descendingIterator(); //iterator in reverse order
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
				return indegree[b]-indegree[a];
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
	 * Check if a valid conjecture path exists which starts from v_0.
	 * 
	 * @param v_0 The vertex which the search starts from
	 * @param x The indegree of v_0
	 * @param maxInDegree The maximum indegree in the graph
	 * @param indegree The array of indegrees of the vertices in the graph
	 * @return True iff there exists a path leading out of v_0 [v_0,v_1,v_2,v_3,...,v_k] where 
	 * 		indegree[v_i] <= x+i, 0<=i<=k
	 * 		and 
	 * 		indegree(v_k) == max(indegree).
	 */
	private boolean validConjecturePathExists(int v_0, int x, int maxInDegree, final int[] indegree) {
		// Trivial case
		if (indegree[v_0] == maxInDegree) {
			return true;
		}
		
		// Variant of bfs
		Queue<Integer> q = new LinkedList<Integer>();
		q.offer(v_0);
		
		// todo explain my sleep-deprived thoughts 
		int layer0Count = 1; //Current layer, used to update i when needed
		int layer1Count = 0; //Next layer
		int i=0;
		while (!q.isEmpty()) {
			int v_i = q.poll();
			if (!(indegree[v_i] <= x+i)) {
				continue; //This path is not valid
			}
			if (indegree[v_i] == maxInDegree) {
				return true; //Reached maximum indegree
			}
			
			//Update queue and count for layer 1
			layer1Count+=neighbours.get(v_i).size(); //size-1 if has self-loop
			if (!selfLoop(v_i)) {
				q.addAll(neighbours.get(v_i));
			} else {
				layer1Count--;
				for (int neighbour : neighbours.get(v_i)) { //Optimize
					if (neighbour != v_i) {
						q.add(neighbour);
					}
				}
			}
			
			//Done with this vertex in this layer
			layer0Count--;
			if (layer0Count==0) {
				layer0Count = layer1Count; //Update current layer
				i++; // We move on to the next layer, i increases
				layer1Count = 0; // Next layer has no vertices atm
			}
		}
		
		// If we get here, no valid path could be found from v_0
		return false;
	}
	
	/**
	 * @return True iff the vertex has a self-loop
	 */
	private boolean selfLoop(int vertex) {
		return m[vertex][vertex] == 1;
	}
}
