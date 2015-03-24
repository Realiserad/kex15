import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Heuristics for The Monk problem.
 * @author Bastian Fredriksson
 * @author Edvin Lundberg
 */
public class Solver {
	
	public static void main(String[] args) {
		Solver solver = new Solver();
		List<int[][]> solution = solver.getSolution();
		
		// TODO Print solution
	}
	
	private LinkedList<int[][]> getSolution() {
		Kattio io = new Kattio(System.in, System.out);
		
		/* Read number of vertices */
		int vertexCount = io.getInt();
		
		/* Read edges from stdin */
		ArrayList<LinkedList<Integer>> neighbours = new ArrayList<LinkedList<Integer>>(vertexCount);
		for (int i = 0; i < vertexCount; i++) neighbours.add(new LinkedList<Integer>());
		while (io.hasMoreTokens()) {
			// Add edge u -> v
			int u = io.getInt();
			int v = io.getInt();
			neighbours.get(u).add(v);
		}
		
		/* Create a graph */
		Graph g = new Graph(neighbours);
		
		/* Determine bounds */
		int lowerBound = g.getLowerBoundNrOfPursuers();
		int upperBound = g.getUpperBoundNrOfPursuers();
		
		/* Get strong components */
		Graph[] queue = g.getStrongComponents();
		
		/* Solve each component separately */
		LinkedList<int[][]> solutions = new LinkedList<int[][]>();
		for (int i = 0; i < queue.length; i++) {
			// TODO Solve
		}
		
		return solutions;
	}
}
