package kex.stateinspectors;

/**
 * An indexed state inspector with O(1) lookup and exponential memory usage.
 * @author Edvin Lundberg
 * @author Bastian Fredriksson
 * @version 2014-04-12
 */
public class IndexedStateInspector implements StateInspector {
	boolean[] visitedStates;
	
	public IndexedStateInspector(int vertexCount) {
		visitedStates = new boolean[(int) Math.pow(2, vertexCount)];
	}

	@Override
	public boolean isVisited(int[] state) {
		return visitedStates[getIndex(state)];
	}

	@Override
	public void markAsVisited(int[] state) {
		visitedStates[getIndex(state)] = true;
	}
	
	private int getIndex(int[] state) {
		int index = 0;
		for (int i = 0; i < state.length; i++) {
			if (state[state.length-1-i] == 0) {
				index += Math.pow(2, i);
			}
		}
		return index;
	}
}
