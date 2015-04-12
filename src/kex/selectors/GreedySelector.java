package kex.selectors;
import java.util.LinkedList;
import java.util.List;

import kex.Graph;
import kex.maxheap.MaxHeap;


/**
 * The greedy selector will order the vertices based on the number of vertices which can get decontaminated the next day.
 * @author Edvin Lundberg
 * @version 2015-04-12
 */
public class GreedySelector implements Selector {
	
	/**
	 * Wrapped vertex which can be put into a Max Heap.
	 * @author Edvin Lundberg
	 */
	private class Vertex implements Comparable<Vertex> {

		final int vertexNr;
		final int key;
		
		public Vertex(int vertexNr, int key) {
			this.vertexNr = vertexNr;
			this.key = key;
		}
		
		@Override
		public int compareTo(Vertex otherVertex) {
			return this.key-otherVertex.key;
		}
		
	}
 
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
	@Override
	public List<Integer> selectOrder(Graph g, int[] currentState, int[] nextState, int pursuers) {
		MaxHeap<Vertex> maxHeap = new MaxHeap<Vertex>(new Vertex[currentState.length], 0, currentState.length);
		for (int v = 0; v < currentState.length; v++) {
			if (currentState[v]==0) {
				// v already decontaminated
				continue;
			}
			
			/* Let c represent the number of vertices which can get decontaminated the next day as a result of
			 * decontamimating v this day */
			int c = 0;
			
			for (int neighbour : g.getAdjacencyList().get(v)) {
				if (nextState[neighbour] <= pursuers) {
					// By decontaminating v, we can potentially decontaminate neighbour also
					c++;
				}
			}
			
			if (c > 0) {
				// We have a candidate vertex
				maxHeap.insert(new Vertex(v,c));
			}
		}
		
		return maxHeapToList(maxHeap);
	}

	/**
	 * Convert maxheap of vertices into List of vertices.
	 */
	private List<Integer> maxHeapToList(MaxHeap<Vertex> maxHeap) {
		LinkedList<Integer> order = new LinkedList<Integer>();
		int n = maxHeap.heapsize();
		for (int i = 0; i<n; i++) {
			order.addLast(maxHeap.removemax().vertexNr);
		}
		return order;
	}

}
