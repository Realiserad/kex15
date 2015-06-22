package kex.solvers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import kex.Graph;
import kex.Kattio;
import kex.Verify;
import kex.solvers.selectors.GreedySelector;
import kex.solvers.selectors.RandomSelector;
import kex.solvers.selectors.Selector;
import kex.solvers.selectors.SelectorType;
import kex.solvers.selectors.SimpleSelector;
import kex.stateinspectors.IndexedStateInspector;
import kex.stateinspectors.ProbabilisticStateInspector;
import kex.stateinspectors.StateInspector;
import kex.stateinspectors.StateInspectorType;

/**
 * Solver for The Monk problem.
 * @author Bastian Fredriksson
 * @author Edvin Lundberg
 */
public abstract class Solver {
	
	protected static boolean DEBUG = false;
	protected static boolean BRUTEFORCE = false;
	
	/* Implementing classes should set these in constructor */ 
	protected static SelectorType SELECTORTYPE;
	protected static StateInspectorType STATEINSPECTORTYPE;
	
	/* 
	 * Read a graph from stdin and print a strategy to stdout.
	 */
	public static void main(String[] args) {
		/* Default values */
		InputStream is = System.in;
		
		/* Parse arguments */
		for (int i = 0; i < args.length; i++) {
			int argCount = 0; // expected number of arguments to follow a flag
			
			if (args[i].equals("-f")) {
				//----------------- READ FROM FILE -----------------//
				argCount = 1;
				String arg = getArguments(args, argCount, i+1)[0];
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
			} else if (args[i].equals("-d")) {
				//--------------- ENABLE DEBUGGING ----------------//
				DEBUG = true;
			} else if (args[i].equals("-bf")) {
				//--------------- ENABLE BRUTE FORCE MODE ----------------//
				BRUTEFORCE = true;
			} else {
				//----------------- UNKNOWN FLAG -----------------//
				System.err.println("Unknown flag " + args[i]);
				System.exit(1);
			}
			
			i++;
			i += argCount;
		}
		
		/* Read graph from stdin */
		Kattio io = new Kattio(is, System.out);
		int vertexCount = io.getInt();
		ArrayList<LinkedList<Integer>> adjacencyList = new ArrayList<LinkedList<Integer>>(vertexCount);
		for (int i = 0; i < vertexCount; i++) adjacencyList.add(new LinkedList<Integer>());
		while (io.hasMoreTokens()) {
			/* Read an edge u->v */
			int u = io.getInt();
			int v = io.getInt();
			adjacencyList.get(u).add(v);
		}
		
		
		/* Create graph from adjacency list */
		Graph graph = new Graph(adjacencyList);
		
		
		/* Decontaminate the graph */
		Solver solver = (BRUTEFORCE) ? new BruteForceSolver() : new Heuristics();
		long time = System.currentTimeMillis();
		Strategy strategy = solver.solve(graph);
		time = System.currentTimeMillis()-time;		
		
		/* Print the vertices to be decontaminated at each day */
		io.println(strategy.toString());
		
		/* Print number of pursuers */
		io.println("Number of pursuers: "+strategy.getPursuerCount());
		
		/* Print length of solution */
		io.println("Solution length: "+strategy.getLength());
		
		/* Verify the solution and print the result as a certificate */
		Verify verifier = new Verify(graph);
		if (verifier.verify(strategy)) {
			io.println("The solution is winning.");
		} else {
			io.println("The solution is not winning.");
			io.print(verifier.getStatesString());
		}
		
		
		io.println("Solution found in: "+time+" ms");
		io.close();
	}
	
	/**
	 * Calculate the next state in a recursive manner until all vertices are decontaminated, whereby
	 * this method returns a strategy by backtracking.
	 * @param strongComponent A strongly connected graph to decontaminate.
	 * @param staticPursuers The number of pursuers in the strategy.
	 * @param dynPursuers The number of pursuers left to place.
	 * @param currentState An array containing the number of free edges for each vertex. 
	 * @param stateInspector A state inspector containing visited states.
	 * @param vertices The vertices occupied by pursuers in this state.
	 * @param depth The number of steps in the current strategy.
	 * @return A winning strategy for the graph given as first argument or null
	 * if no strategy could be found.
	 */
	protected abstract Strategy solve(Graph strongComponent, Selector selector, int staticPursuers, int dynPursuers, int[] currentState, int[] nextState, StateInspector stateInspector, int[] vertices, int depth);
	
	protected Selector getSelector(SelectorType type) {
		if (type == SelectorType.SIMPLE) return new SimpleSelector();
		if (type == SelectorType.GREEDY) return new GreedySelector();
		if (type == SelectorType.RANDOM) return new RandomSelector();
		return null;
	}
	
	protected StateInspector getStateInspector(StateInspectorType type, int vertexCount) {
		if (type == StateInspectorType.BLOOM_FILTER) return new ProbabilisticStateInspector(vertexCount);
		if (type == StateInspectorType.ARRAY) return new IndexedStateInspector(vertexCount);
		
		return null;
	}
	
	/**
	 * Get count arguments from args starting at i.
	 * @return An array of arguments or null if out of bounds.
	 */
	protected static String[] getArguments(String[] args, int i, int count) {
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
		List<Graph> strongComponents = g.getStrongComponents();
		d("Number of strong components: " + strongComponents.size());
		e("Number of strong components: " + strongComponents.size());
		/* A list of strategies for each stable component */
		LinkedList<Strategy> strategies = new LinkedList<Strategy>();
		// The maximum pursuers needed for solved components.
		int search_num_approx = 0;
		/* Solve each strong component separately */
		for (Graph strongComponent : strongComponents) {
			d("Number of nodes: " + strongComponent.getVertexCount());
			d("Graph: " + strongComponent.toString());
			if (strongComponent.isSingleton()) {
				/* The strong component consists of only one vertex and has a trivial strategy */
				strategies.add(Strategy.getSingletonStrategy(strongComponent));
				continue;
			}
			int lowerBound = strongComponent.getLowerBound();
			int upperBound = strongComponent.getUpperBound();
			Strategy strategy = binarySearch(
					Math.min(Math.max(search_num_approx,lowerBound), strongComponent.getVertexCount()),
					upperBound, 
					strongComponent);
			strategies.add(strategy);
			search_num_approx = Math.max(search_num_approx, strategy.getPursuerCount());
//			strategies.add(testStrategy(strongComponent));
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
	protected Strategy linearSearch(int lower, int upper, Graph strongComponent) {
		
		for (int pursuerCount = lower; pursuerCount <= upper; pursuerCount++) {
			Strategy strategy = solve(
					strongComponent, 
					getSelector(SELECTORTYPE),
					pursuerCount, 
					pursuerCount,
					strongComponent.getIndegree(),
					strongComponent.getIndegree(),
					getStateInspector(STATEINSPECTORTYPE, strongComponent.getVertexCount()),
					new int[pursuerCount],
					0
			);
			if (strategy != null) {
				/* This strategy utilizes the smallest search number */
				return strategy;
			}
		}
		
		return null;
	}
	
	/**
	 * Perform a binary search for the search number of a strong component
	 * given as third argument.
	 * @param lower The lower bound where the linear search starts.
	 * @param upper The upper bound where the linear search stops.
	 * @param estimate Edvin's estimate between lower and upper
	 * @param g A strongly connected graph to decontaminate.
	 * @return A winning strategy for g or null.
	 */
	protected Strategy binarySearch(int lower, int upper, Graph strongComponent) {
		e("Lower: "+lower+" Upper: "+upper);
		Strategy bestStrategy = null;
		Strategy nextStrategy = null;
		/* Start at estimate value */
		int p = strongComponent.getEstimate();
				
		final int limit = 100;
		/* Perform binary search */
		/* If the vertices are greater than 'limit' we exclude the binary search's last steps */
		while (upper - lower > strongComponent.getVertexCount()/limit || bestStrategy == null) {
			e("Trying with "+p+" pursuers.");
			nextStrategy = solve(
					strongComponent, 
					getSelector(SELECTORTYPE),
					p, 
					p,
					strongComponent.getIndegree(),
					strongComponent.getIndegree(),
					getStateInspector(STATEINSPECTORTYPE, strongComponent.getVertexCount()),
					new int[p],
					0
			);
			
			if (nextStrategy != null) {
				e("Successful.");
				bestStrategy = nextStrategy;
				upper = p;
			} else {
				e("Failed.");
				lower = p+1;
			}
			p = (lower+upper)/2;
		}
		assert(bestStrategy != null);
		return bestStrategy;
	}
	
	protected void e(String string) {
		System.out.println(string);
	}
	
	/**
	 * Calculates the next state based on the previous state. The next state
	 * will be equal to the first state with indegree reduced for vertices which
	 * are neighbours to decontaminated vertices in previous state.
	 * @param state The previous state.
	 * @param strongComponent A strongly connected graph.
	 * @return The next state given after a transition.
	 */
	protected int[] transition(int[] state, Graph strongComponent) {
		int[] nextState = strongComponent.getIndegree();
		for (int vertex = 0; vertex < state.length; vertex++) {
			if (state[vertex] == 0) {
				blockEdges(strongComponent, vertex, nextState);
			}
		}
		return nextState;
	}
	
	/**
	 * Decontaminate a vertex by setting its indegree to zero and reduce indegree 
	 * for all its neighbours in the next state by one.
	 * @param currentState The state where decontamination should occur.
	 * @param nextState The state in which incoming edges should be blocked.
	 * @param vertex The vertex to decontaminate.
	 * @param strongComponent A strongly connected graph.
	 */
	protected void decontaminate(int[] currentState, int[] nextState, int vertex, Graph strongComponent) {
		if (currentState[vertex] != 0) { 
			currentState[vertex] = 0;
			blockEdges(strongComponent, vertex, nextState);
		}
	}

	/** 
	 * Block edges u->v in a graph, given a pursuer is positioned at the
	 * vertex u given as second argument. This method will block all edges originating
	 * from u, meaning any neighbor v to u, will have its indegree reduced by one.
	 * This method does not work with a copy of the state.
	 */
	protected int[] blockEdges(Graph strongComponent, int vertex, int[] state) {
		for (int neighbour : strongComponent.getNeighbours(vertex)) {
			assert(state[neighbour] > 0);
			state[neighbour]--;
		}
		return state;
	}
	
	/**
	 * Returns a list of contaminated vertices, with at most staticPursuers + 1 elements.
	 */
	protected LinkedList<Integer> getContaminatedVertices(int[] state, int staticPursuers) {
		LinkedList<Integer> contaminatedVertices = new LinkedList<Integer>();
		for (int i = 0; i < state.length; i++) {
			if (state[i] > 0) {
				/* This vertex is contaminated because it still has an incoming edge free */
				contaminatedVertices.add(i);
				if (contaminatedVertices.size() > staticPursuers) {
					break;
				}
			}
		}
		
		return contaminatedVertices;
	}
	
	/************************************************************************************/
	/*****************************        DEBUG        **********************************/
	
	/**
	 * Returns a string of integers from an array.
	 */
	protected String arrayString(int[] array) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i] + " ");
		}
		return sb.toString();
	}
	
	/**
	 * Print a message to stderr.
	 */
	protected void d(String msg, int depth) {
		if (!DEBUG) return;
		String padding = "";
		for (int i = 0; i < depth; i++) {
			padding += ">";
		}
		System.err.println(padding + msg);
	}
	
	protected void d(String msg) {
		d(msg, 0);
	}
	
	/**
	 * Used for debug purposes. Hard code a strategy here. 
	 */
	@SuppressWarnings("unused")
	protected Strategy testStrategy(Graph g) {
		Strategy strat = new Strategy(4, null);
		int[][] stratMtrx = new int[][]{				
				{0,1,8},		// Day 1
				{3,11,17},	    // Day 2 etc
				{8,12,20},
				{2,3,20},
				{2,3,20},
				{0,2,20},
				{0,2,17},
				{0,2,17},
				{0,2,8},
				{0,8,17},
				{0,17,22},
				{0,2,17},
				{8,22,27},
				{17,21,22},
		};
		
		for (int[] dayStrat : stratMtrx) {
			strat.addFirst(dayStrat);
		}
		
		return strat;
	}
}
