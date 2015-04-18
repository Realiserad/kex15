package kex.generators;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Generate graphs using Watts-Strogatz graph generation algorithm.
 * 
 * -------------------------------------------------------------------------|
 * Usage: java Generate [options]                                           |
 * -------------------------------------------------------------------------|
 * option                                          |    default             |
 * --batch N : Create N random graphs              |    no batch (N=1)      |
 * --nodes X Y : Graphs created will have at       |    X=4 and Y=1000      |
 *   least X nodes and at most Y nodes.            |                        |
 * --rand P : A number between 1 and 100.          |    P=100               |
 *   1 means cyclic and 100 means completely       |                        |
 *   random.                                       |                        |
 * --file Output test files to disk.               |    stdout              |
 *   The test files will be named                  |                        |
 *   test01, test02... testN and stored in         |                        |
 *   the current directory.                        |                        |
 * --edge Output graph as an edge representation   |    off                 |
 *   instead of a matrix representation. Vertices  |                        |
 *   will be numbered from 0 to N-1.               |                        |
 * --vcount Output number of vertices on first     |    off                 |
 *   row. Will be disabled unless used together    |                        |
 *   with --edge flag.                             |                        |
 * --density X Add ~X extra edges per node in the  |    no extra edges      |
 *   graph                                         |                        |
 * -------------------------------------------------------------------------|
 *
 * @author Bastian Fredriksson
 */
public class WattsStrogatz {
	private class Edge {
		public int a, b;
		public Edge(int a, int b) {
			this.a = a;
			this.b = b;
		}
	}

	private static int batch = 1;
	private static int nodeLower = 4;
	private static int nodeUpper = 1000;
	private static int rand = 100;
	private static int density = 0;
	private static boolean file = false;
	private static boolean asEdges = false;
	private static boolean vertexCount = false;
	private ArrayList<Edge> edges;

	public static void main(String[] args) {
		/* Parse options */
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--batch")) {
				batch = Integer.valueOf(args[i+1]);
				i++;
			} else if (args[i].equals("--nodes")) {
				nodeLower = Integer.valueOf(args[i+1]);
				nodeUpper = Integer.valueOf(args[i+2]);
				i+=2;
			} else if (args[i].equals("--rand")) {
				rand = Integer.valueOf(args[i+1]);
				i++;
			} else if (args[i].equals("--file")) {
				file = true;
			} else if (args[i].equals("--edge")) {
				asEdges = true;
			} else if (args[i].equals("--vcount")) {
				vertexCount = true;
			} else if (args[i].equals("--density")) {
				density = Integer.valueOf(args[i+1]);
				i++;
			} else {
				System.err.println("Unknown flag " + args[i]);
				System.exit(0);
			}
		}
		if (nodeLower < 4) {
			System.out.println("Number of nodes must be at least 4.");
			System.exit(0); 
		}
		if (rand < 1) {
			System.err.println("Randomness must be at least 1");
			System.exit(0);
		}
		/* Generate */
		new WattsStrogatz();
	}

	public WattsStrogatz() {                
		Random r = new Random();
		for (int i = 0; i < batch; i++) {
			int nodes = r.nextInt(nodeUpper-nodeLower) + nodeLower;
			edges = new ArrayList<Edge>(nodes);
			int[][] m = new int[nodes][nodes];
			createCycle(m);
			Collections.shuffle(edges);
			int n = (int)(nodes*(1-(1/(double)rand))); // number of edges to redirect
			for (int j = 0; j < n; j++) {
				Edge e = edges.get(j);
				m[e.a][e.b] = 0;
				m[e.b][e.a] = 0;
				m[e.a][r.nextInt(nodes)] = 1;
				m[e.b][r.nextInt(nodes)] = 1;
			}
			// Make sure there are no dead ends
			for (int column = 0; column < nodes; column++) {
				boolean hasExit = false;
				for (int row = 0; row < nodes; row++) {
					if (row != column && m[row][column] == 1) {
						hasExit = true;
						break;
					}
				}
				if (!hasExit) {
					// Add random edge which is not a loop
					int ep = r.nextInt(nodes);
					if (ep == column) ep = column+1%nodes;
					m[ep][column]=1;
				}
			}
			// Add extra edges
			if (density > 0) {
				for (int j = 0; j < nodes; j++) {
					for (int k = 0; k < density; k++) {
						int from = r.nextInt(nodes);
						int to = r.nextInt(nodes);
						m[to][from] = 1;
					}
				}
			}
			// Print result
			if (!file) {
				printGraph(m);
			} else {
				storeGraph(m, "test" + i+1);
			}
		}
	}

	/**
	 * Store the matrix m in a file.
	 */
	private void storeGraph(int[][] m, String file) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(file, "UTF-8");
			if (asEdges) writer.print(edgeString(m));
			else writer.print(matrixString(m));
			writer.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private String edgeString(int[][] m) {
		StringBuilder sb = new StringBuilder();
		if (vertexCount) sb.append(m.length + "\n");
		for (int col = 0; col < m.length; col++) {
			for (int row = 0; row < m.length; row++) {
				if (m[row][col]==1) {
					// Edge col->row
					sb.append(col + " " + row + "\n");
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Print the matrix m to stdout.
	 */
	private void printGraph(int[][] m) {
		if (asEdges) {
			System.out.print(edgeString(m));
		} else {
			System.out.print(matrixString(m));
		}
	}

	/**
	 * Return the string representation of a square matrix "m".
	 */
	private String matrixString(int[][] m) {
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < m.length; row++) {
			for (int col = 0; col < m.length; col++) {
				sb.append(m[row][col] + " ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/** 
	 * Connect the graph m by adding edges e_{1}, e_{2}.. e_{i} such 
	 * that the edges form exactly one cycle with length i. 
	 */
	private void createCycle(int[][] m) {
		for (int i = 0; i < m.length-1; i++) {
			m[i+1][i] = 1;
			m[i][i+1] = 1;
			edges.add(new Edge(i, i+1));
		}
		m[0][m.length-1] = 1; // close cycle
		m[m.length-1][0] = 1;
		edges.add(new Edge(0, m.length-1));
	}
}
