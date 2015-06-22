package kex.solvers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import kex.Graph;
import kex.solvers.selectors.Selector;
import kex.solvers.selectors.SelectorType;
import kex.stateinspectors.StateInspector;
import kex.stateinspectors.StateInspectorType;

/**
 * A brute force solver, will find optimal strategy.
 * @author Edvin Lundberg
 *
 */
public class BruteForceSolver extends Solver {

	protected BruteForceSolver() {
		super();
		SELECTORTYPE = SelectorType.SIMPLE;
		STATEINSPECTORTYPE = StateInspectorType.ARRAY;
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
	protected Strategy solve(Graph strongComponent, Selector selector, int staticPursuers, int dynPursuers, int[] currentState, int[] nextState, StateInspector stateInspector, int[] vertices, int depth) {
		//TODO
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
			
//			if (lastPursuer) {
//				if (getContaminatedVertices(newCurrentState, staticPursuers).size() > 
//				(getContaminatedVertices(newNextState, staticPursuers).size()+staticPursuers) ) {
//					assert(false);
//					return null; // Decreasing strategy
//				}
//			}
						
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
}
