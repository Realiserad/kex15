
/**
 * A solver should take a graph and produce the minimum number of pursuers needed
 * as well as a (minimum) search strategy for these pursuers.
 * 
 * @author Edvin Lundberg
 * @version 2015-03-05
 */
public interface Solver {
	
	/**
	 * Produce the minimum number of pursuers needed
	 * as well as a (minimum) search strategy for these pursuers.
	 * 
	 * @param g The graph where the optimal solution is sought
	 * @return Solution vectors int[LengthOfStrategy][NrOfPursuers]
	 */
	public int[][] solve(Graph g);

}
