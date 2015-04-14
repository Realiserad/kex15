package kex.generators;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class StarGraph implements GraphGenerator {
	private Random r = new Random();
	private ArrayList<LinkedList<Integer>> adjacencyList;
	
	@Override
	public String generate(int vertexCount) {
		assert(vertexCount > 10);
		
		adjacencyList = new ArrayList<LinkedList<Integer>>(vertexCount);
		for (int i = 0; i < vertexCount; i++) adjacencyList.add(new LinkedList<Integer>());
		
		/* Generate a double-linked chain */
		for (int i = 0; i < vertexCount / 10; i++) {
			adjacencyList.get();
		}
	}
}
