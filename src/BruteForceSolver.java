/**
 * The brute-force solver simply enumerates possible solution 
 * and uses Verify.java to find the optimal one.
 * 
 * @author Edvin Lundberg
 * @version 2015-03-05
 */
public class BruteForceSolver implements Solver {

	private Graph graph;
	private int conjectureMinPursuers;
	private Verify verifier;
	
	/**
	 * TODO
	 * @param g The graph where an optimal solution is sought
	 */
	public BruteForceSolver(Graph g) {
		this.graph = g;
		this.conjectureMinPursuers = g.getLowerBoundNrOfPursuers();
		verifier = new Verify(g);
		
		int[][] solution = solve(g);
	}

	/**
	 * Produce the minimum number of pursuers needed
	 * as well as a (minimum) search strategy for these pursuers.
	 * 
	 * @param g The graph where the optimal solution is sought
	 * @return Solution vectors int[LengthOfStrategy][NrOfPursuers]
	 */
	@Override
	public int[][] solve(Graph g) {
		int n = graph.getVertexCount();
		int[][] solution;
		
		/* Open problem: How long should the solution be allowed to become before
		 * giving up on it and adding a pursuer? Arbitrarily 2 times n now. */
		for (int p = 1; p <= n; p++) { //Pursuers
			for (int len = 1; len <= 2*n; len++) { //Length of solution
				solution = new int[len][p];
				if (solveR(1,0,p,len,solution)) return solution;
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param day
	 * @param pursuer
	 * @param p
	 * @param len
	 * @return
	 */
	private boolean solveR(int day, int pursuer, int p, int len, int[][] sol) {
		if (day == len-1 && pursuer == p) {
			int n = graph.getVertexCount();
			for (int i = pursuer; i<n; i++) {
				sol[day][pursuer] = i;
				if (verifier.verify(p, len, sol)) {
					return true;
				}
			}
			return false;
		}
		
		//TODO
		return false;		
	}
	
}
