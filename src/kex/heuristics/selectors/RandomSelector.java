package kex.heuristics.selectors;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import kex.Graph;

public class RandomSelector implements Selector {
	private Random rand = new Random();

	@Override
	public List<Integer> selectOrder(Graph g, int[] currentState, int[] nextState, int pursuers) {
		LinkedList<Integer> ll = new LinkedList<Integer>();
		ll.add(rand.nextInt(currentState.length));
		return ll;
	}

}
