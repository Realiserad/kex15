import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;

/**
 * An immutable representation of a directed graph 
 * with vertices and edges. Self loops allowed but not multiple edges.
 *
 * @author Bastian Fredriksson
 * @author Edvin Lundberg
 * @version 2015-04-08
 */
public class Graph {
	private ArrayList<LinkedList<Integer>> neighbours;
	private int vertexCount, edgeCount;
	private int lowerBoundPursuers;
	private int upperBoundPursuers;
	private int[] indegree;
	private int[] translator; //Translates vertex indices to indices for whole graph. Used for components.

	/**
	 * Create a graph from a neighbour matrix "m".
	 * The matrix given as first argument should contain
	 * a one on position m[i][j] if there is an edge j->i
	 * and zero otherwise.
	 * @param newToOldTranslator 
	 */
	public Graph(int[][] m) {
		this.vertexCount = m.length;
		/* Create neighbour list */
		neighbours = new ArrayList<LinkedList<Integer>>(m.length);
		for (int i=0; i<m.length; i++) {
			neighbours.add(new LinkedList<Integer>());
		}
		for (int row=0; row<m.length; row++) {
			for (int col=0; col<m.length; col++) {
				if (m[row][col] == 1) {
					// Add edge col->row
					neighbours.get(col).add(row);
					edgeCount++;
				}
			}
		}
		this.lowerBoundPursuers = 0; // Initial lower bound
	}
	
	/**
	 * Create a graph from a neighbour matrix "m".
	 * The matrix given as first argument should contain
	 * a one on position m[i][j] if there is an edge j->i
	 * and zero otherwise.
	 * @param newToOldTranslator translates new vertex indices to old (whole graph) indices.
	 */
	public Graph(int[][] m, int[] newToOldTranslator) {
		this(m);
		this.translator = newToOldTranslator;
	}
	
	/**
	 * Create a graph from a adjacency list "neighbours".
	 * neighbours.get(i) should contain all neighbours to vertex i
	 */
	public Graph(ArrayList<LinkedList<Integer>> neighbours) {
		this.neighbours = neighbours;
		this.vertexCount = neighbours.size();
		this.lowerBoundPursuers = 0;
	}
	
	/**
	 * Get the adjacency list.
	 */
	public ArrayList<LinkedList<Integer>> getAdjacencyList() {
		return neighbours;
	}

	/**
	 * Get the number of vertices for this graph.
	 */
	public int getVertexCount() {
		return vertexCount;
	}

	/**
	 * Get the number of edges in this graph.
	 */
	public int getEdgeCount() {
		return edgeCount;
	}
	
	/**
	 * Get all neighbours for a vertex.
	 */
	public LinkedList<Integer> getNeighbours(int vertex) {
		return neighbours.get(vertex);
	}

	/**
	 * Get all cycles in this graph.
	 */
	public ArrayList<LinkedList<Integer>> getCycles() {
		// visited[v] is true if vertex v has been visited during current dfs
		boolean[] visited = new boolean[vertexCount];
		// traversed[vertexCount*u+v] is true if the edge u->v has been traversed during any dfs
		boolean[] traversed = new boolean[vertexCount*vertexCount];
		// path contains the path traveled during current dfs
		LinkedList<Integer> path = new LinkedList<Integer>();
		// list of all cycles found
		ArrayList<LinkedList<Integer>> cycles = new ArrayList<LinkedList<Integer>>();
		for (int vertex=0; vertex<vertexCount; vertex++) {
			for (int neighbour : neighbours.get(vertex)) {
				if (traversed[vertex*vertexCount+neighbour]) continue;
				// Start new dfs with vertex as start node
				Arrays.fill(visited, false);
				path.clear();
				visited[vertex] = true;
				path.add(vertex);
				dfs(vertex, neighbour, visited, traversed, path, cycles);
			}
		}
		return cycles;
	}

	private void dfs(int previous, int current, boolean[] visited, boolean[] traversed, LinkedList<Integer> path, ArrayList<LinkedList<Integer>> cycles) {
		// Mark the edge which lead here as traversed
		traversed[previous*vertexCount+current] = true;
		// Check if we have a cycle
		if (visited[current]) {
			path.addLast(current);
			cycles.add(extractCycle(path));
			return;
		}
		// Continue dfs
		visited[current] = true;
		path.addLast(current);
		for (int neighbour : neighbours.get(current)) {
			dfs(current, neighbour, visited, traversed, path, cycles);
		}
		visited[current] = false;
		path.removeLast();
	}

	/**
	 * Return the cycle in a path.
	 */
	private LinkedList<Integer> extractCycle(LinkedList<Integer> path) {
		int tail = path.removeLast(); // The cycle should end in this vertex
		LinkedList<Integer> cycle = new LinkedList<Integer>();
		Iterator<Integer> it = path.descendingIterator(); // Iterator in reverse order
		while (it.hasNext()) {
			int vertex = it.next();
			cycle.add(vertex);
			if (vertex == tail) {
				return cycle;
			}
		}

		return null;
	}
	
	/**
	 * Get a lower bound, that is, a positive number where the graph can not be solved with less pursuers than returned number.
	 * @return A lower bound for the number of pursuers needed, it can't be done with less than returned value.
	 */
	public int getLowerBoundNrOfPursuers() {
		getIndegree();
		//The minimum indegree is a lower bound because we must have that many pursuers to make any progress at all.
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < indegree.length; i++) {
			if (indegree[i] < min) min = indegree[i];
		}
		
		return Math.max(1, min);
	}
	
	public int[] getIndegree() {
		if (this.indegree == null) {
			indegree = new int[this.getVertexCount()];
			for (LinkedList<Integer> neighbourSet : neighbours) {
				for (int neighbour : neighbourSet) {
					this.indegree[neighbour]++;
				}
			}
		}
		return Solver.copy(indegree);
	}

	/**
	 * Use the "Edvin-estimate" to calculate a lower bound for the minimum number of pursuers needed. 
	 * @return A lower bound for the number of pursuers needed.
	 */
	public int getEstimateNrOfPursuers() {
		// If already calculated lower bound, return it 
		if (this.lowerBoundPursuers != 0) {
			return this.lowerBoundPursuers;
		}

		/* Recall that:
		 * The matrix given as first argument should contain
		 * a one on position m[i][j] if there is an edge j->i
		 * and zero otherwise.
		 */
		getIndegree();
		
		// Array of vertices which we will sort based on in-degree 
		Integer[] vertices = new Integer[vertexCount];
		for (int i = 0; i<vertices.length; i++) {
			vertices[i] = i;
		}

		// Sort vertices in ascending order of in-degree
		Arrays.sort(vertices, new Comparator<Integer>() {
			/**
			 * Compares its two arguments for order. 
			 * Returns a negative integer, zero, or a positive
			 * integer as the first argument is less than, equal 
			 * to, or greater than the second.
			 */
			@Override
			public int compare(Integer a, Integer b) {
				return indegree[a]-indegree[b];
			}
		});

		/*
		 * Now the vertices array should be sorted on indegree and we traverse it from 
		 * left to right. We process the vertices until one satisfies this condition:
		 * 	Let v_0 be the vertex we are processing
		 * 	Let x be v_0's indegree
		 * 	Find a path leading out of v_0 [v_0,v_1,v_2,v_3,...,v_k] where 
		 * 		indegree[v_i] <= x+i, 0<=i<=k
		 * 		and 
		 * 		indegree(v_k) == max(indegree)
		 * 	
		 */
		int maxInDegree = indegree[indegree.length-1]; //Last element should have highest indegree
		//This loop guarantees that lower bound will be found, if we are at the last vertex with
		// highest indegree, a valid path is trivially found.
		for (int i = 0; i<vertices.length; i++) {
			int v_0 = vertices[i];
			int x = indegree[v_0];
			if (validEstimatePathExists(v_0, x, maxInDegree, indegree)) {
				this.lowerBoundPursuers = x; //We need x pursuers to start by blocking v_0
				break;
			}
		}

		return Math.max(1, this.lowerBoundPursuers);
	}
	
	/** 
	 * Return an upper bound for the number of pursuers needed 
	 */
	public int getUpperBoundNrOfPursuers() {
		if (upperBoundPursuers != 0) {
			return upperBoundPursuers;
		}
		int[] indegree = getIndegree();
		int max = Integer.MIN_VALUE;
		for (int i : indegree) {
			if (i > max) {
				max = i;
			}
		}
		upperBoundPursuers = max;
		//return Math.max(upperBoundPursuers, 1);
		return this.getVertexCount();
	}

	/**
	 * Check if a valid estimate path (of distinct vertices) exists which starts from v_0.
	 * 
	 * @param v_0 The vertex which the search starts from
	 * @param x The indegree of v_0
	 * @param maxInDegree The maximum indegree in the graph
	 * @param indegree The array of indegrees of the vertices in the graph
	 * @return True iff there exists a path leading out of v_0 [v_0,v_1,v_2,v_3,...,v_k] where 
	 * 		indegree(v_i) <= x+i, 0<=i<=k
	 * 		and 
	 * 		indegree(v_k) == max(indegree).
	 */
	private boolean validEstimatePathExists(int v_0, int x, int maxInDegree, final int[] indegree) {		
		// Variant of dfs

		// visited[v] is true if vertex v has been visited during current bfs
		boolean[] visited = new boolean[vertexCount];
		Arrays.fill(visited, false);
		return dfsEstimate(v_0,x,0,maxInDegree, visited, indegree);
	}

	/**
	 * If a valid path could be found from here, pathFound will be true.
	 * 
	 * @param v_i Current vertex 
	 * @param x The indegree of v_0
	 * @param i index in path
	 * @param pathFound "Return"-value, check this after call.
	 * @param maxDegree maxDegree in graph
	 * @param visited Keep track of which vertices are visited, cycles are not allowed for this path
	 * @param indegree array of indegrees of vertices
	 */
	private boolean dfsEstimate(int v_i, int x, int i, int maxInDegree, boolean[] visited, final int[] indegree)  {
		// Not valid path
		if (!(indegree[v_i] <= x+i)) return false;

		// Found valid path
		if (indegree[v_i] == maxInDegree) {
			return true;
		}

		// Continue search for valid path
		visited[v_i] = true;

		for (int neighbor : neighbours.get(v_i)) {
			if (!visited[neighbor]) {
				boolean hasValidPath = dfsEstimate(neighbor, x, i+1, maxInDegree, visited, indegree);
				if (hasValidPath) return true;
			}
		}

		/* When leaving a vertex, consider it unvisited */
		visited[v_i] = false;
		return false;
	}
	
	public int[][] getAdjacencyMatrix() {
		int[][] m = new int[this.getVertexCount()][this.getVertexCount()];
		for (int i = 0; i < this.getVertexCount(); i++) {
			for (int neighbour : neighbours.get(i)) {
				// i --> neighbour
				m[neighbour][i]=1;
			}
		}
		return m;
	}

	/**
	 * Returns a list of maximal strong components for this graph. The list is sorted in the
	 * order in which the components needs to be decontaminated.
	 * @return A vector of strongly connected components
	 */
	public List<Graph> getStrongComponents() {
		/* comp[v] describes the component that v belongs to, and zero if v belongs to no component so far */
		int[] comp = new int[getVertexCount()]; 

		// Number of strongly connected components in graph
		int numComponents = 0;

		for (int v = 0; v < comp.length; v++) {
			// If already in component, skip it
			if (comp[v]!=0) continue;

			numComponents++;
			int compNumber = numComponents; //+1 to avoid confusion with "zero value= no component"
			// Our component consists of v so far, try to expand 
			comp[v] = compNumber;
			buildStrongComponent(v, new boolean[comp.length], new LinkedList<Integer>(), comp, compNumber);
		}

		/* Topological ordering */
		// One set for each component
		@SuppressWarnings("unchecked")
		List<Integer>[] comps = new LinkedList[numComponents];
		for (int i = 0; i < comps.length; i++) {
			comps[i] = new LinkedList<Integer>();
		}
		for (int v = 0; v < comp.length; v++) {
			// Add v to the component it belongs to
			comps[comp[v]-1].add(v);
		}

		/* Create subgraphs from the components above */
		return getSubgraphs(comps);
	}
	
	/** 
	 * Returns true if this graph is a a singleton.
	 * A singleton consists of one vertex.
	 * @return True if the this graph consists of only one vertex
	 */
	public boolean isSingleton() {
		return this.vertexCount == 1 ? true : false;
	}

	/**
	 * Returns an array of subgraphs sorted in topological order.
	 * Each subgraph is created from a partition of vertices from this
	 * graph. More formally, if V(X) is the vertex set given from one of the
	 * partitions X given as argument, then the subgraph (V, E) is a graph such 
	 * that V=V(X) and E=(u,v) where u,v ∈ V(X).
	 * @param A partioning of this graph
	 * @return A list of subgraphs sorted in topological order
	 */
	private List<Graph> getSubgraphs(List<Integer>[] partitions) {
		// The number of partitions, equal to the number of subgraphs
		int partitionCount = partitions.length;
		// A queue of subgraphs, will be sorted later
		ArrayList<Graph> subgraphs = new ArrayList<Graph>(partitionCount);
		// Create a set representation of each partition for O(1) lookup and removal
		ArrayList<HashSet<Integer>> partitionSets = new ArrayList<HashSet<Integer>>();
		for (int i = 0; i < partitionCount; i++) partitionSets.add(new HashSet<Integer>(partitions[i]));

		/* Neighbour list for a DAG representing the topological order for the subgraphs.
		 * Each vertex in this graph is a partition and an edge means that the partitions
		 * have a common element.
		 * topologicalOrder.get(u).contains(v) is true if exists an edge v->u, i.e if
		 * the partition v supersedes the partition u in the topological ordering.
		 */
		ArrayList<HashSet<Integer>> topologicalOrder = new ArrayList<HashSet<Integer>>();
		for (int i = 0; i < partitionCount; i++) topologicalOrder.add(new HashSet<Integer>());

		for (int i = 0; i < partitionCount; i++) {
			HashSet<Integer> currentSet = partitionSets.get(i);
			List<Integer> currentPartition = partitions[i];
			
			/* Use a translator to translate new vertexname into original vertexname */
			int[] newToOldTranslator = new int[currentSet.size()];
			int newIndex = 0;
			for (int v : currentPartition) {
				newToOldTranslator[newIndex]=v;
				newIndex++;
			}
			int[] oldToNewTranslator = new int[this.getVertexCount()];
			Arrays.fill(oldToNewTranslator, -1);
			for (int v = 0; v < newToOldTranslator.length; v++) {
				oldToNewTranslator[newToOldTranslator[v]] = v;
			}

			// matrix[u][v]=1 if there is an edge v->u
			int[][] matrix = new int[currentPartition.size()][currentPartition.size()];

			for (int vertex : currentPartition) {
				for (int neighbour : neighbours.get(vertex)) {
					if (currentSet.contains(neighbour)) {
						// The subgraph we're building should contain an edge vertex->neighbour
						matrix[oldToNewTranslator[neighbour]][oldToNewTranslator[vertex]]=1;
					} else {
						// The partition i has a common element with the partition in which
						// the neighbour resides
						topologicalOrder.get(getComponent(neighbour, partitionSets)).add(i);
					}
				}
			}
			subgraphs.add(new Graph(matrix, newToOldTranslator));
		}
		
		/* component[i] contains the component number for vertex i */ 
		int[] component = new int[this.getVertexCount()];
		for (int compNumber = 0; compNumber < partitionCount; compNumber++) {
			for (int vertex : partitions[compNumber]) {
				component[vertex] = compNumber;
			}
		}
		
		/* create DAG where each vertex is a component from the original graph */
		// Adjacency list for DAG, +1 for supernode
		ArrayList<TreeSet<Integer>> dag = new ArrayList<TreeSet<Integer>>(partitionCount);
		for (int i = 0; i < partitionCount; i++) dag.add(new TreeSet<Integer>());
		for (List<Integer> partition : partitions) {
			for (int vertex : partition) {
				for (int neighbour : this.getNeighbours(vertex)) {
					if (component[neighbour] != component[vertex]) {
						dag.get(component[vertex]).add(component[neighbour]);
					}
				}
			}
		}
		
		/* Use DFS search to create topological order. */
		Stack<Integer> stack = new Stack<Integer>();
		boolean[] visited = new boolean[partitionCount];
		for (int c = 0 ; c < partitionCount; c++) {
			dfsTopological(stack, visited, c, dag);
		}
		LinkedList<Graph> sorted = new LinkedList<Graph>();
		Integer compNumber = null;
		while (!stack.isEmpty()) {
			compNumber=stack.pop();
			sorted.addLast(subgraphs.get(compNumber));
		}
		
		return sorted;
	}

	/**
	 * Create topological order by depth first search.
	 * The topological order is obtained by popping the returned stack.
	 */
	private Stack<Integer> dfsTopological(Stack<Integer> stack, boolean[] visited, int vertex,ArrayList<TreeSet<Integer>> dag) {
		if (visited[vertex]) {
			return stack;
		}
		visited[vertex] = true;
		
		for (int neighbour : dag.get(vertex)) {
			dfsTopological(stack, visited, neighbour, dag);
		}
				
		stack.push(vertex);
		return stack;		
	}

	/**
	 * Returns the the index of the strong component which contains the 
	 * vertex given as first argument.
	 * @return The index of the strong component or -1 if the vertex does not exist
	 */
	private Integer getComponent(int vertex, ArrayList<HashSet<Integer>> sets) {
		for (int i = 0; i < sets.size(); i++) {
			if (sets.get(i).contains(vertex)) return i;
		}
		return -1;
	}

	/**
	 * Build the strong component which "vertex" belongs to. In a strong component, there is a path connecting all pairs 
	 * of vertices in the component. 
	 * 
	 * @param visited The visited vertices.
	 * @param path The path travelled so far.
	 * @param comp comp[v]=the compNumber that v belongs to, 0 if not belonging to any component. Will be mutated.
	 * @param compNumber The component number of this component.
	 */
	private void buildStrongComponent(int vertex, boolean[] visited, LinkedList<Integer> path, int[] comp, int compNumber) {
		if (visited[vertex]) {
			// I was already here yo
			return;
		}

		visited[vertex] = true;
		path.addLast(vertex);

		// If returning to an "accepted" vertex, mark this path as "accepted", i.e. belonging to this component  
		for (int neighbour : neighbours.get(vertex)) {
			if (comp[neighbour] == compNumber) {
				markPath(comp, compNumber, path);
				break;
			}
		}

		// Expand component further
		for (int neighbour : neighbours.get(vertex)) {
			if (comp[neighbour] != 0) {
				// No need to search another component (we will never return to this component if we do)
				// No need to check vertices already in this component
				continue;
			}
			// Expand
			buildStrongComponent(neighbour, visited, path, comp, compNumber);
		}

		path.removeLast();
	}

	/**
	 * Mark the vertices in given path as belonging to the component with the compNumber 
	 * given as the second parameter. 
	 * 
	 * @param comp This parameter will be mutated, vertices on their index will have value compNumber.
	 * @param compNumber The component number of this component
	 * @param path Path of vertices which has been proven to belong to this component.
	 */
	private void markPath(int[] comp, int compNumber, LinkedList<Integer> path) {
		Iterator<Integer> it = path.descendingIterator();
		while (it.hasNext()) {
			int v = it.next();
			if (comp[v] == compNumber) {
				return;
			}
			comp[v] = compNumber;
		}
	}
	
	/**
	 * Returns the string representation of this graph
	 * terminated by newline.
	 * 
	 * The first line contains the vertices of this graph, 
	 * e.g [1, 2, 3] followed by one or more edges
	 * on the form u->v.
	 * 
	 * The edges are sorted in ascending order, such
	 * that the edge u->v precedes x->y if u < x or
	 * if u == x and v < y.
	 * 
	 * The vertex labels are translated to
	 * indices for the whole graph before they are
	 * printed.
	 * 
	 * Time complexity: O(|V|*|E|*log(|E|))
	 * 
	 * @return A string representation of this graph.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		/* Print vertices */
		sb.append("[");
		for (int i = 0; i < neighbours.size(); i++) {
			sb.append(translate(i));
			if (i != neighbours.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append("]\n");
		
		/* Print edges */
		if (this.getEdgeCount() == 0) {
			sb.append("no edges\n");
		}
		for (int i = 0; i < neighbours.size(); i++) {
			LinkedList<Integer> queue = neighbours.get(i);
			if (queue.isEmpty()) continue;
			Collections.sort(queue);
			for (int neighbour : queue) {
				sb.append((translate(i)+1) + "->" + (translate(neighbour)+1) + "\n");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Translates vertex index to index for the whole graph. This is because, for a subgraph,
	 * the vertices will have different vertex indices than for the whole graph.
	 * @param vertex
	 * @return the vertex index in the whole graph
	 */
	public int translate(int vertex) {
		if (translator == null) {
			return vertex;
		}
		return translator[vertex];
	}
}
