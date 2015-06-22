package kex.solvers.selectors;
import java.util.List;

import kex.Graph;


/**
 * The selector interface provides a method for determining in what order pursuers should be placed during a day.
 * @author Edvin Lundberg
 * @version 2015-04-12
 */
public interface Selector {

	/**
	 * Produces an ordering of the vertices in the current state. Interpret the returned
	 * ordering like this: First try to place a pursuer on the first vertex in the list, if that does 
	 * not work, move on to the second vertex etc. The method might return an empty list, but never 
	 * null.
	 * 
	 * @param g The graph currently processed. Zero-indexing.
	 * @param currentState The free incoming edges into the current state.
	 * @param nextState The free incoming edges into the next state.
	 * @param pursuers The numbers of pursuers available. 
	 * @return The order
	 */
	public List<Integer> selectOrder(Graph g, int[] currentState, int[] nextState, int pursuers);
	
}
