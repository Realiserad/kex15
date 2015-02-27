import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

/**
 * Verify a solution to The Monk problem using an EL-system. The solution should contain a
 * directed graph G consisting of one component, and a set of solution vectors describing the
 * movement of the pursuers.
 * 
 * The solution to be checked is read from stdin and the program answers YES or NO to stdout.
 * 
 * Input format: 
 * Row					Data
 * 0					Should contain the number of rows "r" in the neighbour matrix for G.
 * 1 to r				Should contain the data (separated by space) for the neighbour matrix.
 * r+1					Should contain two integers separated by space; the number of pursuers "p" 
 * 						and the length of a solution vector.
 * r+2 to r+2+p 		Should contain the movement of the pursuers (solution vectors for G).
 * 						The vertices of G are assumed to be labeled from 1 and up (see the
 * 						example below).
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
 * @author Realiserad
 */
public class Verify {
	public class Kattio extends PrintWriter {
		private BufferedReader r;
		private String line;
		private StringTokenizer st;
		private String token;
		
		public Kattio(InputStream i) {
			super(new BufferedOutputStream(System.out));
			r = new BufferedReader(new InputStreamReader(i));
		}

		public boolean hasMoreTokens() {
			return peekToken() != null;
		}

		public int getInt() {
			return Integer.parseInt(nextToken());
		}

		private String peekToken() {
			if (token == null) 
				try {
					while (st == null || !st.hasMoreTokens()) {
						line = r.readLine();
						if (line == null) return null;
						st = new StringTokenizer(line);
					}
					token = st.nextToken();
				} catch (IOException e) { }
			return token;
		}

		private String nextToken() {
			String ans = peekToken();
			token = null;
			return ans;
		}
	}

	public static void main(String[] args) {
		new Verify();
	}

	public Verify() {
		Kattio io = new Kattio(System.in);
		
		/* Read neighbour matrix */
		final int ROWS = io.getInt();
		int[][] graph = new int[ROWS][ROWS];
		int[] w = new int[ROWS];
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
		io.close();
		
		/* Verify solution by iterating the formula s_{n+1}=graph*s_{n}+seed with s_{0} = 0 */
		int[] s = seed[0]; // s_{1}
		for (int i = 1; i < LEN; i++) {
			s = radd(rmul(graph, s, w), seed[i]);
		}
		
		if (onesOnly(s)) {
			System.out.println("YES");
		} else {
			System.out.println("NO");
		}
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
}
