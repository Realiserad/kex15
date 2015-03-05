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
				//TODO work in progress, how to enumerate all possibilities?
				// The stupid solution is to create all possible matrices
				// M[len][p] (each element in the matrix is a value 1 to n and
				// there are len*p values. The matrices are processed up to 2*n times which
				// means we have a total of (at most) 2n^(len*p+1) matrices to check
//				for (int pursuer = 0; pursuer < p; pursuer++) {
//					
//				}
			}
		}
		
		return null;
	}
}
