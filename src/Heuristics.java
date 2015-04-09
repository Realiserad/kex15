import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Heuristics for The Monk problem.
 * @author Bastian Fredriksson
 * @author Edvin Lundberg
 */
public class Heuristics {
	
	/**
	 * Class for managing states. A state consists of the decontaminated
	 * nodes in a graph.
	 */
	private class StateInspector {
		private BloomFilter<String> visitedStates;
		
		/** 
		 * Create a new inspector without any stored states.
		 * @param expectedCapacity The expected number of invokations to markAsVisited().
		 */
		public StateInspector(int expectedCapacity) {
			visitedStates = new BloomFilter<String>(0.001, expectedCapacity);
		}
		
		/**
		 * Returns true if the state is marked as visited by this
		 * state inspector.
		 * @param indegree An array with the number of free edges for
		 * each vertex.
		 */
		public boolean isVisited(int[] indegree) {
			return visitedStates.contains(getState(indegree));
		}
		
		/**
		 * Mark a state as visited.
		 * @param indegree An array with the number of free edges for
		 * each vertex.
		 */
		public void markAsVisited(int[] indegree) {
			visitedStates.add(getState(indegree));
		}
		
		/**
		 * Convert an array containing indegree for vertices into
		 * a string with indices of decontaiminated vertices.
		 */ 
		private String getState(int[] indegree) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < indegree.length; i++) {
				if (indegree[i] == 0) {
					sb.append(i);
				}
			}
			return sb.toString();
		}
	}
	
	private static class Strategy {
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
			Strategy commonStrategy = new Strategy(maxPursuerCount, null);
			for (Strategy strategy : strategies) {
				LinkedList<int[]> raw = strategy.getStrategy();
				for (int[] vertices : raw) {
					int[] copy = new int[maxPursuerCount];
					for (int i = 0; i < vertices.length; i++) copy[i] = vertices[i];
					commonStrategy.addVertices(copy);
				}
			}
			return commonStrategy;
		}
		
		/**
		 * Returns a strategy for a singleton graph.
		 */
		public static Strategy getSingletonStrategy(Graph singleton) {
			assert(singleton.getVertexCount() == 1);
			
			Strategy strategy = new Strategy(1, null);
			int[] vertex = { singleton.translate(0) };
			strategy.addVertices(vertex);
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
		 * Returns an array of vertices to be decontaminated
		 * at the day given as argument.
		 */
		public int[] getVertices(int day) {
			return strategy.get(day);
		}
		
		/**
		 * Add vertices to this strategy. The vertices added are the vertices
		 * to simultaneously decontaminate at day getLength() + 1. Vertex labels
		 * are translated before they are added according to the translator
		 * given as argument when this strategy was created.
		 * @param vertices An array with as many vertices as this strategy has
		 * pursuers, each vertex labeled with a number zero or higher.
		 */
		public Strategy addVertices(int[] vertices) {
			assert(vertices.length == pursuerCount);
			
			translate(vertices);
			strategy.add(vertices);
			return this;
		}
		
		/**
		 * Add vertices to this strategy. The vertices added are the vertices
		 * to simultaneously decontaminate at day getLength() + 1. Vertex labels
		 * are translated before they are added according to the translator
		 * given as argument when this strategy was created.
		 * @param vertices A list with at most as many vertices as this strategy
		 * has pursuers, each vertex labeled with a number zero or higher.
		 */
		public Strategy addVertices(LinkedList<Integer> vertices) {
			assert(vertices.size() <= pursuerCount);
			
			int[] copy = new int[pursuerCount];
			int index = 0;
			for (int vertex : vertices) {
				copy[index] = vertex;
				index++;
			}
			translate(copy);
			strategy.add(copy);
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
			StringBuilder sb = new StringBuilder();
			for (int[] vertices : strategy) {
				for (int vertex : vertices) {
					sb.append((vertex + 1) + " ");
				}
				sb.append(System.lineSeparator());
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
			int[][] seed = new int[getLength()][getPursuerCount()];
			int day = 0;
			for (int[] vertices : strategy) {
				for (int i = 0; i < getPursuerCount(); i++) {
					seed[day][i] = vertices[i];
				}
				day++;
			}
			return verifier.verify(getPursuerCount(), getLength(), seed);
		}
	}
	
	/* 
	 * Read a graph from stdin and print a strategy to stdout.
	 */
	public static void main(String[] args) {
		/* Default values */
		InputStream is = System.in;
		
		System.out.println(new File(".").getAbsoluteFile());
		/* Parse arguments */
		for (int i = 0; i < args.length; i++) {
			int argCount = 0; // expected number of arguments to follow a flag
			
			if (args[i++].equals("-f")) {
				//----------------- READ FROM FILE -----------------//
				argCount = 1;
				String arg = getArguments(args, argCount, i)[0];
				if (arg == null) {
					System.err.println("The flag -f requires one argument.");
					System.exit(1);
				} else {
					try {
						is = new FileInputStream(arg);
					} catch (FileNotFoundException e) {
						System.err.println("The file " + arg + " does not exist.");
						System.exit(1);
					}
				}
			} else {
				//----------------- UNKNOWN FLAG -----------------//
				System.err.println("Unknown flag " + args[i]);
				System.exit(1);
			}
			
			i += argCount;
		}
		
		/* Read graph from stdin */
		Kattio io = new Kattio(is);
		int vertexCount = io.getInt();
		ArrayList<LinkedList<Integer>> adjacencyList = new ArrayList<LinkedList<Integer>>(vertexCount);
		for (int i = 0; i < vertexCount; i++) adjacencyList.add(new LinkedList<Integer>());
		while (io.hasMoreTokens()) {
			/* Read an edge u->v */
			int u = io.getInt();
			int v = io.getInt();
			adjacencyList.get(u).add(v);
		}
		io.close();
		
		/* Create graph from adjacency list */
		Graph graph = new Graph(adjacencyList);
		
		/* Decontaminate the graph */
		Heuristics heuristics = new Heuristics();
		Strategy strategy = heuristics.solve(graph);
		
		/* Print the vertices to be decontaminated at each day */
		System.out.print(strategy.toString());
		
		/* Verify the solution and print the result as a certificate */
		if (strategy.verify(graph)) {
			System.out.println("The solution is winning.");
		} else {
			System.out.println("The solution is not winning.");
		}
	}
	
	/**
	 * Get count arguments from args starting at i.
	 * @return An array of arguments or null if out of bounds.
	 */
	private static String[] getArguments(String[] args, int i, int count) {
		assert(args.length != 0);
		assert(args != null);
		assert(count > 0);
		
		if (i + count > args.length) {
			return null;
		}
		
		String[] result = new String[count];
		for (int j = 0; j < count; j++) {
			result[j] = args[i + j];
		}
		return result;
	}

	/**
	 * Find a strategy for the graph given as argument.
	 * @param g The graph to decontaminate.
	 * @return A winning strategy for g.
	 */
	public Strategy solve(Graph g) {
		/* The strong components of this graph */
		Graph[] strongComponents = g.getStrongComponents();
		/* A list of strategies for each stable component */
		LinkedList<Strategy> strategies = new LinkedList<Strategy>();
		
		/* Solve each strong component separately */
		for (Graph strongComponent : strongComponents) {
			if (strongComponent.isSingleton()) {
				/* The strong component consists of only one vertex and has a trivial strategy */
				strategies.add(Strategy.getSingletonStrategy(strongComponent));
				continue;
			}
			int lowerBound = strongComponent.getLowerBoundNrOfPursuers();
			int upperBound = strongComponent.getUpperBoundNrOfPursuers();
			Strategy strategy = linearSearch(lowerBound, upperBound, strongComponent);
			strategies.add(strategy);
		}
		
		return Strategy.merge(strategies);
	}
	
	/**
	 * Perform a linear search for the search number of a strong component
	 * given as third argument.
	 * @param lower The lower bound where the linear search starts.
	 * @param upper The upper bound where the linear search stops.
	 * @param g A strongly connected graph to decontaminate.
	 * @return A winning strategy for g or null.
	 */
	private Strategy linearSearch(int lower, int upper, Graph strongComponent) {
		for (int pursuerCount = lower; pursuerCount <= upper; pursuerCount++) {
			Strategy strategy = solve(
					strongComponent, 
					pursuerCount, 
					pursuerCount, 
					strongComponent.getIndegree(),
					strongComponent.getIndegree(),
					new StateInspector((int) Math.pow(2, strongComponent.getVertexCount())),
					new int[pursuerCount]
			);
			if (strategy != null) {
				/* This strategy utilizes the smallest search number */
				return strategy;
			}
		}
		
		return null;
	}
	
	/**
	 * Calculate the next state in a recursive manner until all vertices are decontaminated, whereby
	 * this method returns a strategy by backtracking. The states are kept in an array freeEdges, where
	 * freeEdges[v]=0 means that vertex v is decontaminated.
	 * @param strongComponent A strongly connected graph to decontaminate.
	 * @param staticPursuers The number of pursuers in the strategy.
	 * @param dynPursuers The number of pursuers left to place.
	 * @param indegree An array containing original indegree for each vertex.
	 * @param freeEdges An array containing the number of free edges for each vertex. 
	 * @param stateInspector A state inspector containing visited states.
	 * @param vertices The vertices occupied by pursuers in this state.
	 * @return A winning strategy for the graph given as first argument or null
	 * if no strategy could be found.
	 */
	private Strategy solve(Graph strongComponent, int staticPursuers, int dynPursuers, int[] indegree, int[] freeEdges, StateInspector stateInspector, int[] vertices) {
		assert(staticPursuers >= dynPursuers);
		
		if (staticPursuers == dynPursuers) {
			/* This is a transition between two states */
			recontaminate(strongComponent, indegree, freeEdges);
		}
		
		if (stateInspector.isVisited(indegree)) {
			/* This state has already been reached from another branch. */
			return null;
		}
		
		if (staticPursuers == dynPursuers) {
			LinkedList<Integer> contaminatedVertices = getContaminatedVertices(indegree, staticPursuers);
			if (contaminatedVertices.size() <= staticPursuers) {
				/* It is possible to decontaminate the whole graph at this stage. */
				Strategy strategy = new Strategy(staticPursuers, strongComponent);
				for (int vertex : contaminatedVertices) {
					vertex = strongComponent.translate(vertex);
				}
				return strategy.addVertices(contaminatedVertices);
			}
		}
		
		stateInspector.markAsVisited(indegree);
		
		/* Continue the pursuit by positioning pursuers at the next available positions. */
		for (int vertex = 0; vertex < indegree.length; vertex++) {
			assert(indegree[vertex] >= 0);
			
			if (indegree[vertex] == 0) {
				/* This vertex is decontaminated already, no need to put pursuer here */
				continue;
			}
			
			/* Remember which vertex we put a pursuer on */
			vertices[staticPursuers - dynPursuers] = vertex;
			
			Strategy strategy = solve(
				strongComponent,
				staticPursuers,
				dynPursuers == 1 ? staticPursuers : dynPursuers - 1,
				indegree,
				decontaminate(strongComponent, freeEdges, vertex),
				stateInspector,
				dynPursuers == 1 ? new int[staticPursuers] : vertices
			);
			if (strategy != null) {
				/* Strategy has been found from here, backtrack */
				if (staticPursuers == dynPursuers) {
					return strategy.addVertices(vertices);
				}
			}
		}
		
		return null;
	}
	
	/* 
	 * Put a pursuer at the vertex v given as third argument and return a new
	 * array of free edges where all neighbours to v will have its indegree
	 * reduced by one.
	 */
	private int[] decontaminate(Graph strongComponent, int[] freeEdges, int vertex) {
		assert(freeEdges[vertex] > 0);
		
		int[] newFreeEdges = Arrays.copyOf(freeEdges, freeEdges.length);
		blockEdges(strongComponent, vertex, newFreeEdges);
		return newFreeEdges;
	}
	
	/* 
	 * Block edges u->v in a graph, given a pursuer is positioned at the
	 * vertex u given as second argument. This method will block all edges originating
	 * from u, meaning any neighbor v to u, will have its indegree reduced by one.
	 */
	private void blockEdges(Graph strongComponent, int vertex, int[] freeEdges) {
		for (int neighbour : strongComponent.getNeighbours(vertex)) {
			freeEdges[neighbour]--;
		}
	}
	
	/**
	 * Remeasure the number of free edges for each vertex given a transition between two 
	 * states. Since this is when the monk moves, recontamination can occur. 
	 */
	private int[] recontaminate(Graph strongComponent, int[] indegree, int[] freeEdges) {
		LinkedList<Integer> decontaminated = new LinkedList<Integer>();
		for (int i = 0; i < indegree.length; i++) {
			if (freeEdges[i] == 0) {
				decontaminated.add(i);
			}
			freeEdges[i] = indegree[i];
		}
		for (int vertex : decontaminated) {
			blockEdges(strongComponent, vertex, freeEdges);
		}
		return freeEdges;
	}

	/**
	 * Returns a list of contaminated vertices, with at most staticPursuers + 1 elements.
	 */
	private LinkedList<Integer> getContaminatedVertices(int[] indegree, int staticPursuers) {
		LinkedList<Integer> contaminatedVertices = new LinkedList<Integer>();
		for (int i = 0; i < indegree.length; i++) {
			if (indegree[i] > 0) {
				/* This vertex is contaminated because it still has an incoming edge free */
				contaminatedVertices.add(i);
				if (contaminatedVertices.size() > staticPursuers) {
					break;
				}
			}
		}
		
		return contaminatedVertices;
	}
}
