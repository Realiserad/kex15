import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * An immutable representation of a directed graph 
 * with vertices and edges.
 *
 * @author Realiserad
 * @author Edvin Lundberg
 */
public class Graph {
	private ArrayList<LinkedList<Integer>> neighbours;
	private int[][] m;
	private int vertexCount, edgeCount;
	
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
		/* Create neighbour list */
		neighbours = new ArrayList<LinkedList<Integer>>(vertexCount);
		for (int i=0; i<vertexCount; i++) neighbours.add(new LinkedList<Integer>());
		for (int row=0; row<m.length; row++) {
			for (int col=0; col<m.length; col++) {
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
}
