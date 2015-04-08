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
	private Graph g = null;
	
	public Graph getGraph() {
		return g;
	}
	
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
		
		// Verify solution
		Verify v = new Verify(solver.getGraph());
		int len = 0;
		int pursuers = Integer.MIN_VALUE;
		for (LinkedList<int[]> componentSolution : solution) {
			len += componentSolution.size();
			if (componentSolution.get(0).length > pursuers) {
				pursuers = componentSolution.get(0).length;
			}
		}
		int daysTotal = 0;
		int[][] seed = new int[len][pursuers];
		for (LinkedList<int[]> componentSolution : solution) {
			for (int[] placement : componentSolution) {
				int c = 0;
				for (int p : placement) {
					seed[daysTotal][c] = p;
					c++;
				}
				daysTotal++;
			}
		}
			
		if (v.verify(pursuers, len, seed)) {
			System.err.println("VALID!!!!!!!!");
		} else {
			System.err.println("NOOOO!");
		}
	}

	private LinkedList<LinkedList<int[]>> getSolution() {
		Kattio io = null;
		try {
			io = new Kattio(new FileInputStream("/afs/nada.kth.se/home/o/u1h8xqho/Downloads/kex15/tests/3star.txt"), System.out);
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
		this.g = g;

		/* Get strong components */
		Graph[] queue = g.getStrongComponents();

		/* Solve each component separately */
		LinkedList<LinkedList<int[]>> solutions = new LinkedList<LinkedList<int[]>>();
		for (Graph component : queue) {
			if (component.isSingleton()) {
				// Only one vertex to check
				int[] simpleStrategy = new int[] { component.translate(0) };
				LinkedList<int[]> ll = new LinkedList<int[]>();
				ll.add(simpleStrategy);
				solutions.add(ll);
			} else {
				solutions.addLast(solveComponent(component));
			}
		}

		return solutions;
	}

	/**
	 * 
	 * @param component
	 * @return
	 */
	private LinkedList<int[]> solveComponent(Graph component) {
		int[] freeEdges = component.getIndegree();
		/* Determine bounds */
		int lowerBound = component.getLowerBoundNrOfPursuers();
		int upperBound = component.getUpperBoundNrOfPursuers();
		int p = component.getEstimateNrOfPursuers();
		LinkedList<int[]> nextSolution = null; // next solution to test
		LinkedList<int[]> prevSolution = null; // previous working solution
		if (lowerBound == upperBound) upperBound++;
		assert(p>0);
		for (int pursuers = lowerBound; pursuers <= upperBound; pursuers++) {
			nextSolution = getSolution(new VisitedStates(component.getVertexCount()), pursuers, pursuers, component, new boolean[component.getVertexCount()], freeEdges, new int[pursuers], 0);
			if (nextSolution != null) {
				return nextSolution;
			}
		}
		return null;
		/*while (lowerBound < upperBound) {
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
		return nextSolution;*/
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
		assert(staticPursuers >= dynPursuers);
		pBool(state); // DEBUG
		/*-------------- Base cases ------------*/
		// If we have been here before, backtrack
		if (visited.hasVisited(state)) {
			System.err.println("b (visited)");
			return null;
		}
		// If all vertices are decontaminated, backtrack solution
		LinkedList<Integer> contaminated = new LinkedList<Integer>();
		for (int u = 0; u < state.length; u++) {
			if (!state[u]) {
				contaminated.add(u);
				if (contaminated.size() > staticPursuers) {
					break;
				}
			}
		}
		if (contaminated.size() <= staticPursuers && pos==0) {
			System.err.println("Solution reachable from here. Contaminated vertices: " + contaminated.toString());
			for (int vertex : contaminated) {
				placements[pos] = g.translate(vertex);
				pos++;
			}
			LinkedList<int[]> ll = new LinkedList<int[]>();
			ll.add(placements);
			System.err.println("Found solution!");
			System.err.println("b ( " + getArrayString(placements)+ " )");
			return ll;
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
			// c > 0 bugg ?
			if (true) {
				boolean[] newState; 
				int[] newFreeEdges = copy(freeEdges);
				int[] newPlacements = copy(placements);
				for (int v : neighbours) {
					newFreeEdges[v]--;
					assert(newFreeEdges[v]>=0); //FIXME wtf???
				}
				newPlacements[pos] = g.translate(i);
				LinkedList<int[]> result;
				if (dynPursuers > 1) {
					newState = copy(state);
					newState[i] = true;
					result = getSolution(visited, staticPursuers, dynPursuers-1, g, newState, newFreeEdges, newPlacements, pos+1);
					if (result != null) {
						System.err.println("b");
						return result;
					}
				} else {
					// only one pursuer left to place, we need to update params accordingly
					newState = new boolean[g.getVertexCount()];
					
					/* Determine which nodes will be decontaminated the next day */
					for (int x = 0; x < g.getVertexCount(); x++) {
						if (newFreeEdges[x] <= 0) {
//							assert(newFreeEdges[x]!=0);
							newState[x] = true;
						}
					}
					
					/* Determine which edges will be blocked the next day */
					newFreeEdges = g.getIndegree();
					for (int j = 0; j < state.length; j++) {
						if (state[j]) {
//							LinkedList<Integer> neighbours_of_j = g.getAdjacencyList().get(j);
//							for (int n : neighbours_of_j) {
//								newFreeEdges[n]--;
//							}
							for (int neighbour : g.getAdjacencyList().get(j)) {
								newFreeEdges[neighbour]--;
							}
						}
					}
					
					result = getSolution(visited, staticPursuers, staticPursuers, g, newState, newFreeEdges, new int[staticPursuers], 0);
					if (result != null) {
						result.addFirst(newPlacements);
						System.err.println("b ( " + result.toString() + ")");
						return result;
					}
				}
			}
		}
		System.err.println("b (null)");
		return null;
	}
	
	private String getArrayString(int[] arr) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			sb.append(arr[i] + " ");
		}
		return sb.toString();
	}
	
	void pBool(boolean[] data) {
		for (boolean b : data) {
			System.err.print((b == true ? 1 : 0) + " ");
		}
		System.err.println();
	}
	
	public static int[] copy(int[] arr) {
		return arr.clone();
//		int[] copy = new int[arr.length];
//		for (int i = 0; i<arr.length;i++) {
//			copy[i]=arr[i];
//		}
//		return copy;
	}
	
	private boolean[] copy(boolean[] arr) {
		return arr.clone();
//		boolean[] copy = new boolean[arr.length];
//		for (int i = 0; i<arr.length;i++) {
//			copy[i]=arr[i];
//		}
//		return copy;
	}
}
