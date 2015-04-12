package kex.stateinspectors;

/**
 * StateInspectors can keep track of states that have been visited. 
 * A state is defined as an array which hold the number of free incoming edges
 * for each vertex at a given time/day. The StateInspector will usually only focus
 * on which indices has a zero value and which has positive values, and does not 
 * differentiate between two states where the zero values are at exactly the same indices.
 * Note: A zero value reflects a decontaminated vertex.
 * @author Edvin Lundberg
 * @author Bastian Fredriksson
 * @version 2015-04-12
 */
public interface StateInspector {
	
	/**
	 * Returns true if the state is marked as visited by this
	 * state inspector.
	 * @param state An array with the number of free edges for
	 * each vertex.
	 * @return True if the state has been visited, false otherwise.
	 */
	public boolean isVisited(int[] state);
	/**
	 * Mark a state as visited.
	 * @param state An array with the number of free edges for
	 * each vertex.
	 */
	public void markAsVisited(int[] state);
}
