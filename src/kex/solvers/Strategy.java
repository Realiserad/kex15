package kex.solvers;

import java.util.LinkedList;

import kex.Graph;
import kex.Verify;

/**
 * A strategy describes where to place pursuers at each timestep.
 * @author Bastian Fredriksson
 */
public class Strategy {
	private LinkedList<int[]> strategy;
	private int pursuerCount;
	private Graph graph;
	
	/**
	 * Create a new strategy for a graph with the number of pursuers
	 * given as first argument.
	 */
	public Strategy(int pursuerCount, Graph graph) {
		assert(pursuerCount > 0);
		
		this.strategy = new LinkedList<int[]>();
		this.pursuerCount = pursuerCount;
		this.graph = graph;
	}
	
	/**
	 * Create a new strategy from a list of arrays, where each array contains the
	 * vertices to be decontaminated at a specific day. All arrays in the list
	 * should have equal length.
	 * @param strategy An array with vertices to decontaminate at each day.
	 */
	private Strategy(LinkedList<int[]> strategy) {
		this.strategy = strategy;
		this.pursuerCount = strategy.get(0).length;
	}

	/**
	 * Merge a list of strategies into one strategy.
	 * The strategies will be merged in the same order
	 * as they appear in the list.
	 * @param strategies The strategies to merge.
	 * @return A common strategy.
	 */
	public static Strategy merge(LinkedList<Strategy> strategies) {
		int maxPursuerCount = 0;
		for (Strategy strategy : strategies) {
			if (strategy.getPursuerCount() > maxPursuerCount) {
				maxPursuerCount = strategy.getPursuerCount();
			}
		}
		LinkedList<int[]> commonStrategy = new LinkedList<int[]>();
		for (Strategy strategy : strategies) {
			LinkedList<int[]> raw = strategy.getStrategy();
			for (int[] vertices : raw) {
				int[] copy = new int[maxPursuerCount];
				for (int i = 0; i < vertices.length; i++) copy[i] = vertices[i];
				commonStrategy.addLast(copy);
			}
		}
		return new Strategy(commonStrategy);
	}
	
	/**
	 * Returns a strategy for a singleton graph.
	 */
	public static Strategy getSingletonStrategy(Graph singleton) {
		assert(singleton.getVertexCount() == 1);
		
		Strategy strategy = new Strategy(1, null);
		int[] vertex = { singleton.translate(0) };
		strategy.addFirst(vertex);
		return strategy;
	}
	
	/**
	 * Returns the strategy in raw format.
	 */
	private LinkedList<int[]> getStrategy() {
		return strategy;
	}
	
	/**
	 * Returns the length of this strategy.
	 */
	public int getLength() {
		return strategy.size();
	}
	
	/**
	 * Returns the number of pursuers used in this strategy.
	 */
	public int getPursuerCount() {
		return pursuerCount;
	}
	
	/**
	 * Add vertices to this strategy. The vertices added are the vertices
	 * to simultaneously decontaminate at day getLength() + 1. Vertex labels
	 * are translated before they are added according to the translator
	 * given as argument when this strategy was created.
	 * @param vertices An array with as many vertices as this strategy has
	 * pursuers, each vertex labeled with a number zero or higher.
	 */
	public Strategy addLast(int[] vertices) {
		assert(vertices.length == pursuerCount);
		
		translate(vertices);
		strategy.addLast(vertices);
		return this;
	}
	
	/**
	 * Same as addLast() but appends in reverse order, i.e add the vertices
	 * to simultanously decontaminate at day 1.
	 * @param vertices An array with as many vertices as this strategy has
	 * pursuers, each vertex labeled with a number zero or higher.
	 */
	public Strategy addFirst(int[] vertices) {
		assert(vertices.length == pursuerCount);
		
		translate(vertices);
		strategy.addFirst(vertices);
		return this;
	}
	
	/**
	 * Add vertices to this strategy. The vertices added are the vertices
	 * to simultaneously decontaminate at day 1. Vertex labels
	 * are translated before they are added according to the translator
	 * given as argument when this strategy was created.
	 * @param vertices A list with at most as many vertices as this strategy
	 * has pursuers, each vertex labeled with a number zero or higher.
	 */
	public Strategy addFirst(LinkedList<Integer> vertices) {
		assert(vertices.size() <= pursuerCount);
		
		int[] copy = new int[pursuerCount];
		int index = 0;
		for (int vertex : vertices) {
			copy[index] = vertex;
			index++;
		}
		translate(copy);
		strategy.addFirst(copy);
		return this;
	}
	
	/**
	 * Replace vertex indices in the array given as first argument with indices
	 * for the whole graph according to a translator. No translation takes place
	 * if graph is null, or if no translator is available.
	 * @param vertices The vertex indices to translate
	 */
	private void translate(int[] vertices) {
		if (graph == null) return;
		
		for (int i = 0; i < vertices.length; i++) {
			vertices[i] = graph.translate(vertices[i]);
		}
	}
	
	/**
	 * Returns the string representation of this object.
	 * Each line in the string returned consists of a space
	 * separated list of vertices to be decontaminated at a
	 * specific day. The vertices are labeled from one and up.
	 */
	@Override
	public String toString() {
		if (strategy.isEmpty()) {
			return "empty";
		}
		
		StringBuilder sb = new StringBuilder();
		for (int[] vertices : strategy) {
			for (int vertex : vertices) {
				sb.append((vertex) + " ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * Returns a simple string representation of this object, suitable
	 * for printing to stdout or logfiles.
	 */
	public String getSimpleRepresentation() {
		if (strategy.isEmpty()) {
			return "empty";
		}
		
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (int[] vertices : strategy) {
			if (count > 0) {
				sb.append(" ");
			}
			sb.append("(");
			for (int i = 0; i < vertices.length; i++) {
				sb.append(vertices[i]);
				if (i != vertices.length - 1) {
					sb.append(" ");
				}
			}
			sb.append(")");
			count++;
		}
		return sb.toString();
	}
	
	/**
	 * Verify this strategy against the graph given as argument.
	 * @param graph A graph to be decontaminated.
	 * @return True if the strategy is winning, false otherwise.
	 */
	public boolean verify(Graph graph) {
		Verify verifier = new Verify(graph);
		/* The verifier uses indexing from one and up */
		int[][] seed = getSeed();
		return verifier.verify(getPursuerCount(), getLength(), seed);
	}
	
	/**
	 * Returns the strategy in the form of a seed:
	 * A int matrix where seed[d] return the placements of
	 * pursuer on day d.
	 */
	public int[][] getSeed() {
		/* The verifier uses indexing from one and up */
		int[][] seed = new int[getLength()][getPursuerCount()];
		int day = 0;
		for (int[] vertices : strategy) {
			for (int i = 0; i < getPursuerCount(); i++) {
				seed[day][i] = vertices[i];
			}
			day++;
		}
		return seed;
	}
}
