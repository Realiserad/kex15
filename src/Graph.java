import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * An immutable representation of a directed graph 
 * with vertices and edges.
 *
 * @author Realiserad
 */
public class Graph {
	private ArrayList<LinkedList<Integer>> list;
	private int[][] m;
	private int vertexCount, edgeCount;
	
	public static void main(String[] args) {
		System.out.println("Not implemented.");
	}
	
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
		list = new ArrayList<LinkedList<Integer>>(vertexCount);
		for (int i=0; i<vertexCount; i++) list.add(new LinkedList<Integer>());
		for (int col=0; col<m.length; col++) {
			for (int row=0; row<m.length; row++) {
				if (m[row][col] == 1) {
					// Add edge col->row
					list.get(col-1).add(row);
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
		HashSet<Integer> seen = new HashSet<Integer>((int)(vertexCount / 0.75 + 1));
		LinkedList<Integer> path = new LinkedList<Integer>();
		ArrayList<LinkedList<Integer>> cycles = new ArrayList<LinkedList<Integer>>();
		// Use depth first traversal starting at vertex 1
		seen.add(1);
		path.addLast(1);
		for (int neighbour : list.get(0)) {
			dfs(neighbour, seen, path, cycles);
		}
		return cycles;
	}
	
	private void dfs(int current, HashSet<Integer> seen, LinkedList<Integer> path, ArrayList<LinkedList<Integer>> cycles) {
		// Check if we have a cycle
		if (seen.contains(current)) {
			cycles.add(clone(path));
			return;
		}
		// Traverse remaining neighbours
		seen.add(current);
		path.addLast(current);
		for (int neighbour : list.get(current-1)) {
			dfs(neighbour, seen, path, cycles);
		}
		seen.remove(current);
		path.removeLast();
	}
	
	/**
	 * Return a deep copy of the LinkedList given as argument.
	 */
	private LinkedList<Integer> clone(LinkedList<Integer> list) {
		LinkedList<Integer> clone = new LinkedList<Integer>();
		for (int i : list) {
			clone.add(i);
		}
		return clone;
	}
}
