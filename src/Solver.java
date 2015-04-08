import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Heuristics for The Monk problem.
 * @author Bastian Fredriksson
 * @author Edvin Lundberg
 * @version 2015-04-07
 */
public class Solver {
	
	private class VisitedStates {
		/*
		 * Plan E: Use trie
		 * Plan B: Use bloomfilter, may cause solver to not find solution for given p sometimes, but only false negatives.
		 */
		
		boolean[] visitedStates;
		
		public VisitedStates(int vertexCount) {
			visitedStates = new boolean[(int) Math.pow(2, vertexCount)];
		}
		
		/**
		 * Check if given state has been visited, if not it will be marked as visited
		 * for future reference.
		 * @return True if state has been visited, false if not.
		 */
		public boolean hasVisited(boolean[] state) {
			int index = 0;
			for (int i = 0; i < state.length; i++) {
				if (state[state.length-1-i]) {
					index += Math.pow(2, i); //Perhaps bitshift in future
				}
			}
			
			boolean visited = visitedStates[index];
			if (!visited) {
				visitedStates[index]=true;
			}
			return visited;
		}
	}

	public static void main(String[] args) {
		Solver solver = new Solver();
		LinkedList<LinkedList<int[]>> solution = solver.getSolution();

		for (LinkedList<int[]> s : solution) {
			System.out.println("Strong component:");
			for (int[] i : s) {
				for (int j : i) {
					System.out.print(j + " ");
				}
				System.out.println();
			}
		}
	}

	private LinkedList<LinkedList<int[]>> getSolution() {
		Kattio io = null;
		try {
			io = new Kattio(new FileInputStream("/afs/nada.kth.se/home/o/u1h8xqho/Downloads/kex15/bin/test2.txt"), System.out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		/* Read number of vertices */
		int vertexCount = io.getInt();

		/* Read edges from stdin */
		ArrayList<LinkedList<Integer>> neighbours = new ArrayList<LinkedList<Integer>>(vertexCount);
		for (int i = 0; i < vertexCount; i++) neighbours.add(new LinkedList<Integer>());
		while (io.hasMoreTokens()) {
			// Add edge u -> v
			int u = io.getInt();
			int v = io.getInt();
			neighbours.get(u).add(v);
		}

		/* Create a graph */
		Graph g = new Graph(neighbours);

		/* Get strong components */
		Graph[] queue = g.getStrongComponents();

		/* Solve each component separately */
		LinkedList<LinkedList<int[]>> solutions = new LinkedList<LinkedList<int[]>>();
		for (Graph component : queue) {
			assert(solveComponent(component)!=null);
			solutions.addLast(solveComponent(component));
		}

		return solutions;
	}

	/**
	 * 
	 * @param component
	 * @return
	 */
	private LinkedList<int[]> solveComponent(Graph component) {
		int[] freeEdges = new int[component.getVertexCount()];
		/* Determine bounds */
		int lowerBound = component.getLowerBoundNrOfPursuers();
		int upperBound = component.getUpperBoundNrOfPursuers();
		int p = component.getEstimateNrOfPursuers();
		LinkedList<int[]> nextSolution = null;
		if (lowerBound == upperBound) upperBound++;
		assert(p>0);
		while (lowerBound < upperBound) {
			System.err.println(p);
			assert(p > 0);
			nextSolution = getSolution(new VisitedStates(component.getVertexCount()), p, p, component, new boolean[component.getVertexCount()], freeEdges, new int[p], 0);
			if (nextSolution == null) {
				lowerBound = p + 1;
				p = (p + upperBound) / 2;
			} else {
				upperBound = p;
				p = (p + lowerBound) / 2;
			}
		}
		return nextSolution;
	}

	/**
	 * 
	 * @param staticPursuers
	 * @param dynPursuers
	 * @param g
	 * @param state state[i] is true iff node nr i is decontaminated.
	 * @param freeEdges
	 * @return
	 */
	private LinkedList<int[]> getSolution(VisitedStates visited, int staticPursuers, int dynPursuers, Graph g, boolean[] state, int[] freeEdges, int[] placements, int pos)  {
		/*-------------- Base cases ------------*/
		// If we have been here before, backtrack
		if (visited.hasVisited(state)) {
			return null;
		}
		// If we are done, start returning solution
		int u = 0;
		for (; u < state.length; u++) {
			if (!state[u]) {
				break;
			}
		}
		if (u == state.length) {
			return new LinkedList<int[]>();
		}
				
		// Place a pursuer on a contaminated vertex.
		for (int i = 0; i < g.getVertexCount(); i++) {
			if (state[i]) {
				// No need to place pursuer here since decontaminated
				continue;
			}

			// c: if I pick this vertex how many vertices can get blocked? 
			int c = 0;
			List<Integer> neighbours = g.getAdjacencyList().get(i);
			for (int v : neighbours) {
				if (freeEdges[v] <= dynPursuers) {
					c++;
				}
			}

			/*
			 * If c > 0 we have a candidate vertex, we try placing a pursuer there and recursively call this method,
			 * also updated the parameters to recursive call.
			 */
			if (c > 0) {
				boolean[] newState; 
				int[] newFreeEdges = freeEdges.clone();
				for (int v : neighbours) {
					newFreeEdges[v]--;
				}
				placements[pos] = g.translate(i);
				LinkedList<int[]> result;
				if (dynPursuers > 1) {
					newState = state.clone();
					newState[i] = true;
					 result = getSolution(visited, staticPursuers, dynPursuers-1, g, newState, newFreeEdges, placements, pos+1);
					if (result != null) {
						return result;
					}
				} else {
					// only one pursuer left to place, we need to update params accordingly
					newState = new boolean[g.getVertexCount()];
					for (int x = 0; x < g.getVertexCount(); x++) {
						if (newFreeEdges[x] == 0) {
							newState[x] = true;
						}
					}
					newFreeEdges = new int[g.getVertexCount()];
					for (int x = 0; x < g.getVertexCount(); x++) {
						if (!newState[x]) {
							for (int n : g.getAdjacencyList().get(x)) {
								newFreeEdges[n]++;
							}
						}
					}
					
					result = getSolution(visited, staticPursuers, staticPursuers, g, newState, newFreeEdges, new int[staticPursuers], 0);
					if (result != null) {
						result.addFirst(placements);
						return result;
					}
				}
			}
		}
		return null;
	}
}
