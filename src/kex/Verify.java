package kex;
import java.util.LinkedList;
import java.util.List;

import kex.solvers.Strategy;

/**
 * Verify a solution to The Monk problem using an EL-system. The solution should contain a
 * directed graph G consisting of one component, and a set of solution vectors describing the
 * movement of the pursuers.
 * 
 * The program can either be used as a standalone component by piping in a solution via stdin
 * (see sample below) or as a helper class by using the constructor which takes a graph as
 * parameter. 
 * 
 * The solution to be checked is read from stdin (if used as a standalone component) and the 
 * program answers YES or NO to stdout followed by the internal state for each iteration.
 * Each state consists of a bitvector with as many bits as there are vertices. The bitvector
 * number i has a one on position p if vertex p is decontaminated at day i.
 * 
 * -------------------------------------------------------------------------------------------------|
 * Input format:                                                                                    |
 * Row				|	Data                                                                        |
 * -----------------|-------------------------------------------------------------------------------|
 * 0				|	Should contain the number of rows "r" in the neighbour matrix for G.        |
 * 1 to r			|	Should contain the data (separated by space) for the neighbour matrix.      |
 * r+1				|	Should contain two integers separated by space; the number of pursuers "p"  |
 * 					|	and the length of a solution vector.                                        |
 * r+2 to r+2+p 	|	Should contain the movement of the pursuers (solution vectors for G).       |
 * 					|	The vertices of G are assumed to be labeled from 1 and up (see the          |
 * 					|	example below).                                                             |
 * -------------------------------------------------------------------------------------------------|
 * 
 * Example:
 * 5
 * 0 1 0 0 0
 * 1 0 1 0 0
 * 0 1 0 1 0
 * 0 0 1 0 1
 * 0 0 0 1 0
 * 1 6
 * 2 3 4 4 3 2
 * 
 * The example describes a solution to the original version of The Monk Problem
 * stated by Dilian Gurov, in which a monk is to be found by one pursuer, in a 
 * "chained graph" with 5 vertices as shown below.
 * (1) <--> (2) <--> (3) <--> (4) <--> (5)
 * 
 * Run the verifier with javac Verify.java && cat solution.txt | java Verify
 * 
 * @author Bastian Fredriksson
 */
public class Verify {
	private int vertexCount;
	private int[][] graph;
	private int[] w;
	List<int[]> states;
	
	public static void main(String[] args) {
		new Verify();
	}
	
	/**
	 * Read a neighbour matrix from stdin and answer YES or NO to stdout
	 * followed by the internal state for each iteration.
	 */
	private Verify() {
		Kattio io = new Kattio(System.in);
		
		/* Read neighbour matrix */
		final int ROWS = io.getInt();
		graph = new int[ROWS][ROWS];
		w = new int[ROWS];
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < ROWS; col++) {
				graph[row][col] = io.getInt() == 0 ? 0 : 1; // 1 means "has edge (col -> row)"
				if (graph[row][col] == 1) w[row]++; // w[i] should contain Hamming weight for row i
			}
		}
		
		/* Read solution vectors */
		final int P = io.getInt();
		final int LEN = io.getInt();
		int[][] seed = new int[LEN][P];
		for (int col = 0; col < P; col++) {
			for (int row = 0; row < LEN; row++) {
				seed[row][col] = io.getInt()-1; // use 0-indexing internally
			}
		}
		
		/* Save states for debugging. A state consists of a bitvector with a one on position v
		 * if vertex v is decontaminated. */
		states = new LinkedList<int[]>();
		
		/* Verify solution by iterating the formula s_{n+1}=graph*s_{n}+seed with s_{0} = 0 */
		int[] s = expand(seed[0], ROWS); // s_{1}
		states.add(s);
		for (int i = 1; i < LEN; i++) {
			s = radd(rmul(graph, s, w), expand(seed[i], ROWS));
			states.add(s);
		}
		
		/* Print answer */
		if (onesOnly(s)) {
			io.println("YES");
		} else {
			io.println("NO");
		}
		
		io.print(getStatesString());
		
		io.close();
	}
	
	/**
	 * Returns String representing the states produced by last given solution.
	 * A state of a day will have a 1 on decontaminated vertices and 0 on contaminated ones.
	 * @return String representing the states produced by last given solution.
	 */
	public String getStatesString() {
		/* Print states */
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < states.size(); i++) {
			sb.append((i+1) + padding(i+1));
			int[] state = states.get(i);
			for (int j = 0; j < state.length; j++) {
				sb.append(readable(state[j]) + " ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Returns X if v is decontaminated, and _ if it is contaminated.
	 * @param v The vertex value to check.
	 */
	private String readable(int v) {
		return (v == 1) ? "X" : "_"; 
	}

	/**
	 * Returns padding for an integer to smoothly align two columns
	 * in a printout.
	 * With (dynamic) padding:      Without padding
	 * 1     text          			1     text
	 * 11    text                   11     text
	 * 242   text                   242      text
	 * @return Padding string with spaces
	 */
	private String padding(int i) {
		String tmp = String.valueOf(i);
		StringBuilder padding = new StringBuilder();
		for (int n = 0; n < 7-tmp.length(); n++) padding.append(' ');
		return padding.toString();
	}
	
	/**
	 * Create a verifier for the graph given as argument.
	 * Invoke verify() to check if a solution is valid for this graph.
	 */
	public Verify(Graph g) {
		this.vertexCount = g.getVertexCount();
		this.graph = g.getAdjacencyMatrix();
		this.w = new int[vertexCount];
		for (int row = 0; row < vertexCount; row++) {
			for (int col = 0; col < vertexCount; col++) {
				if (graph[row][col] == 1) w[row]++; // w[i] should contain Hamming weight for row i
			}
		}
	}
	
	/**
	 * Verify a solution to the Monk problem.
	 * Movement of the pursuers are given as a len*p matrix
	 * where row 0<=i<=len-1 contains the vertices which should 
	 * be swept by the pursuers at day i. Vertices are numbered 
	 * from 0 to n-1.
	 * Example input:
	 * p = 2;
	 * len = 3;
	 * seed = {
	 *   { 1, 3 },
	 *   { 0, 4 },
	 *   { 3, 4 },
	 * };
	 * @param p The number of pursuers used to sweep this graph
	 * @param len The number of days used to sweep this graph
	 * @param seed The movements of the pursuers for each day
	 * @return True if the solution is valid, false otherwise
	 */
	public boolean verify(int p, int len, int[][] seed) {
		/* Save states for debugging. A state consists of a bitvector with a one on position v
		 * if vertex v is decontaminated. */
		states = new LinkedList<int[]>();
		
		/* Verify solution by iterating the formula s_{n+1}=graph*s_{n}+seed with s_{0} = 0 */
		int[] s = expand(seed[0], vertexCount); // s_{1}
		states.add(s);
		for (int i = 1; i < len; i++) {
			s = radd(rmul(graph, s, w), expand(seed[i], vertexCount));
			states.add(s);
		}
		
		return (onesOnly(s));
	}
	
	/**
	 * Create a binary array with 1 on position p iff seed contains p. 
	 */
	private int[] expand(int[] seed, int len) {
		int[] a = new int[len];
		for (int i = 0; i < seed.length; i++) a[seed[i]] = 1;
		return a;
	}
	
	/**
	 * Reduced addition of two column vectors "a" and "b".
	 */
	private int[] radd(int[] a, int[] b) {
		int[] c = new int[a.length];
		for (int i = 0; i < a.length; i++) {
			c[i] = a[i]+b[i];
			if (c[i] > 1) c[i] = 1;
		}
		return c;
	}
	
	/**
	 * Reduced multiplication of an N x N-matrix "a" with a column vector "b".
	 */
	private int[] rmul(int[][] a, int[] b, int[] w) {
		int[] c = new int[b.length];
		for (int row = 0; row < b.length; row++) {
			for (int i = 0; i < b.length; i++) {
				c[row] += a[row][i]*b[i];
			}
			if (c[row] > 0) {
				c[row] = c[row] == w[row] ? 1 : 0;
			}
		}
		return c;
	}
	
	/**
	 * Returns true if all elements in the array are ones.
	 */
	private boolean onesOnly(int[] a) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] != 1) return false;
		}
		return true;
	}

	/**
	 * Verify a strategy.
	 * @param strategy
	 */
	public boolean verify(Strategy strategy) {
		return verify(strategy.getPursuerCount(), strategy.getLength(), strategy.getSeed());
	}
}
