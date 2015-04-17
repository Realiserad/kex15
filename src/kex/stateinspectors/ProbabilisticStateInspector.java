package kex.stateinspectors;

import kex.BloomFilter;

/**
 * A probabilistic state inspector with small fault rate, O(1) lookup, and low memory usage.
 * @author Bastian Fredriksson
 */
public class ProbabilisticStateInspector implements StateInspector {
	private BloomFilter<String> visitedStates;
	
	/** 
	 * Create a new probabilistic state inspector without any stored states.
	 * @param vertexCount The number of vertices in the graph.
	 */
	public ProbabilisticStateInspector(int vertexCount) {
		visitedStates = new BloomFilter<String>(0.001, 2*vertexCount);
	}
	
	@Override
	public boolean isVisited(int[] state) {
		return visitedStates.contains(getState(state));
	}
	
	@Override
	public void markAsVisited(int[] state) {
		visitedStates.add(getState(state));
	}
	
	/**
	 * Convert an array containing indegree for vertices into
	 * a string with indices of decontaiminated vertices.
	 */ 
	private String getState(int[] state) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < state.length; i++) {
			if (state[i] == 0) {
				sb.append(i);
			}
		}
		return sb.toString();
	}
}
