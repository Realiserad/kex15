package kex.heuristics;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import kex.Graph;
import kex.Kattio;
import kex.Verify;
import kex.heuristics.selectors.GreedySelector;
import kex.heuristics.selectors.Selector;
import kex.heuristics.selectors.SelectorType;
import kex.heuristics.selectors.SimpleSelector;
import kex.stateinspectors.IndexedStateInspector;
import kex.stateinspectors.ProbabilisticStateInspector;
import kex.stateinspectors.StateInspector;
import kex.stateinspectors.StateInspectorType;

/**
 * Heuristics for The Monk problem.
 * @author Bastian Fredriksson
 * @author Edvin Lundberg
 */
public class Heuristics {
	private static boolean DEBUG = false;
	
	/* 
	 * Read a graph from stdin and print a strategy to stdout.
	 */
	public static void main(String[] args) {
		/* Default values */
		InputStream is = System.in;
		
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
			} else if (args[i++].equals("-d")) {
				//--------------- ENABLE DEBUGGING ----------------//
				DEBUG = true;
			} else {
				//----------------- UNKNOWN FLAG -----------------//
				System.err.println("Unknown flag " + args[i]);
				System.exit(1);
			}
			
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
		Heuristics heuristics = new Heuristics();
		Strategy strategy = heuristics.solve(graph);
		
		/* Print the vertices to be decontaminated at each day */
		io.println(strategy.toString());
		
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
		
		io.close();
	}
	
	private Selector getSelector(SelectorType type) {
		if (type == SelectorType.SIMPLE) return new SimpleSelector();
		if (type == SelectorType.GREEDY) return new GreedySelector();
		
		return null;
	}
	
	private StateInspector getStateInspector(StateInspectorType type, int vertexCount) {
		if (type == StateInspectorType.BLOOM_FILTER) return new ProbabilisticStateInspector(vertexCount);
		if (type == StateInspectorType.ARRAY) return new IndexedStateInspector(vertexCount);
		
		return null;
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
		List<Graph> strongComponents = g.getStrongComponents();
		d("Number of strong components: " + strongComponents.size());
		e("Number of strong components: " + strongComponents.size());
		/* A list of strategies for each stable component */
		LinkedList<Strategy> strategies = new LinkedList<Strategy>();
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
			Strategy strategy = binarySearch(lowerBound, upperBound, strongComponent);
			strategies.add(strategy);
			//strategies.add(testStrategy(strongComponent));
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
					getSelector(SelectorType.SIMPLE),
					pursuerCount, 
					pursuerCount,
					strongComponent.getIndegree(),
					strongComponent.getIndegree(),
					getStateInspector(StateInspectorType.ARRAY, strongComponent.getVertexCount()),
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
	private Strategy binarySearch(int lower, int upper, Graph strongComponent) {
		
		Strategy bestStrategy = null;
		Strategy nextStrategy = null;
		/* Start at estimate value */
		int p = strongComponent.getEstimate();
		int prevP = p+1; // Previous value of p, when not changing we have a solution
		assert(p<=upper && p >= lower);
		
		/* Perform binary search */
		while (p != prevP) {
			e("Trying with "+p+" pursuers.");
			nextStrategy = solve(
					strongComponent, 
					getSelector(SelectorType.GREEDY),
					p, 
					p,
					strongComponent.getIndegree(),
					strongComponent.getIndegree(),
					getStateInspector(StateInspectorType.BLOOM_FILTER, strongComponent.getVertexCount()),
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
			prevP = p;
			p = (lower+upper)/2;
		}
		assert(bestStrategy != null);
		return bestStrategy;
	}
	
	private void e(String string) {
		System.out.println(string);
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
	private Strategy solve(Graph strongComponent, Selector selector, int staticPursuers, int dynPursuers, int[] currentState, int[] nextState, StateInspector stateInspector, int[] vertices, int depth) {
		assert(staticPursuers >= dynPursuers);
		boolean newDay = (staticPursuers == dynPursuers);
		boolean lastPursuer = (dynPursuers == 1);
		
		d("State: " + arrayString(currentState) + " (" + staticPursuers + " " + dynPursuers + ")", depth);
		
		/* Backtrack if solution is too long */
		if (depth > 2*strongComponent.getVertexCount()) {
			d("Abort. Strategy too long.");
			return null;
		}
		
		if (newDay) {
			/* This is a transition between two states */
			if (stateInspector.isVisited(currentState)) {
				d("Abort. State is visisted.", depth);
				return null;
			}
			
			stateInspector.markAsVisited(currentState);
			
			LinkedList<Integer> contaminatedVertices = getContaminatedVertices(currentState, staticPursuers);
			if (contaminatedVertices.size() <= staticPursuers) {
				/* It is possible to decontaminate the whole graph at this stage. */
				Strategy strategy = new Strategy(staticPursuers, strongComponent);
				d("Strategy found at depth " + depth + "!", depth);
				// Debug
				d(depth + "\t" + arrayString(currentState) + "\t (" + contaminatedVertices.toString()+ ")");
				return strategy.addFirst(contaminatedVertices);
			}
		}
		
		List<Integer> pursueOrder = selector.selectOrder(strongComponent, currentState, nextState, dynPursuers);
		/* Continue the pursuit by positioning pursuers at the next available positions. */
		for (int vertex : pursueOrder) {
			assert(currentState[vertex] >= 0);
						
			d("Testing vertex " + vertex, depth);
			
			/* Remember which vertex we put a pursuer on */
			int[] newVertices = Arrays.copyOf(vertices, vertices.length);
			newVertices[staticPursuers - dynPursuers] = vertex;
			
			/* Block edges originating from the current vertex */
			int[] newCurrentState = Arrays.copyOf(currentState, currentState.length);
			int[] newNextState = Arrays.copyOf(nextState, nextState.length);
			decontaminate(newCurrentState, newNextState, vertex, strongComponent);
						
			Strategy strategy = solve(
				strongComponent,
				selector,
				staticPursuers,
				lastPursuer ? staticPursuers : dynPursuers - 1,
				lastPursuer ? newNextState : newCurrentState,
				lastPursuer ? transition(newNextState, strongComponent) : newNextState,
				stateInspector,
				lastPursuer ? new int[staticPursuers] : newVertices,
				lastPursuer ? depth + 1 : depth
			);
			if (strategy != null) {
				/* Strategy has been found from here, backtrack */
				if (lastPursuer) {
					strategy.addFirst(newVertices);
					d(depth + "\t" + arrayString(newCurrentState) + "\t (" + arrayString(newVertices)+ ")");
				}
				//d("Strategy: " + strategy.getSimpleRepresentation(), depth);
				return strategy;
			}
		}
		
		d("Abort. No solution.", depth);
		return null;
	}
	
	/**
	 * Calculates the next state based on the previous state. The next state
	 * will be equal to the first state with indegree reduced for vertices which
	 * are neighbours to decontaminated vertices in previous state.
	 * @param state The previous state.
	 * @param strongComponent A strongly connected graph.
	 * @return The next state given after a transition.
	 */
	private int[] transition(int[] state, Graph strongComponent) {
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
	private void decontaminate(int[] currentState, int[] nextState, int vertex, Graph strongComponent) {
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
	private int[] blockEdges(Graph strongComponent, int vertex, int[] state) {
		for (int neighbour : strongComponent.getNeighbours(vertex)) {
			assert(state[neighbour] > 0);
			state[neighbour]--;
		}
		return state;
	}
	
	/**
	 * Returns a list of contaminated vertices, with at most staticPursuers + 1 elements.
	 */
	private LinkedList<Integer> getContaminatedVertices(int[] state, int staticPursuers) {
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
	private String arrayString(int[] array) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i] + " ");
		}
		return sb.toString();
	}
	
	/**
	 * Print a message to stderr.
	 */
	private void d(String msg, int depth) {
		if (!DEBUG) return;
		String padding = "";
		for (int i = 0; i < depth; i++) {
			padding += ">";
		}
		System.err.println(padding + msg);
	}
	
	private void d(String msg) {
		d(msg, 0);
	}
	
	/**
	 * Used for debug purposes. Hard code a strategy here. 
	 */
	@SuppressWarnings("unused")
	private Strategy testStrategy(Graph g) {
		Strategy strat = new Strategy(3, null);
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
			strat.addLast(dayStrat);
		}
		
		
		return strat;
	}
}
