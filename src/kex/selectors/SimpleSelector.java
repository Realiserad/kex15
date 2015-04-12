package kex.selectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kex.Graph;

/**
 * The simple selector will return all vertices in order 0, 1, 2 ... n where n =
 * |G|-1.
 * 
 * @author Edvin Lundberg
 * @version 2015-04-12
 */
public class SimpleSelector implements Selector {

	List<Integer> order = null;

	@Override
	public List<Integer> selectOrder(Graph g, int[] currentState, int[] nextState, int pursuers) {
		if (order == null) {
			order = new ArrayList<Integer>(currentState.length);
			for (int i = 0; i < currentState.length; i++) {
				order.add(i);
			}
		}
		return order;
	}

}
