import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test cases for kex15.
 * You can run these tests in Eclipse by selecting this class (UnitTest.java)
 * in "Package Explorer" and pressing Shift+Alt+X followed by T.
 * @author Bastian Fredriksson
 */
public class UnitTest {
	@Before
	public void setUp() throws Exception {
		/* Nothing to do. */
	}

	@After
	public void tearDown() throws Exception {
		/* Nothing to do. */
	}

	/****************************************************
	/***************   verify.java  *********************/

	@Test
	public void testVerify1() {
		Graph g = new Graph(new int[][] {
				{ 0, 0, 1, 0, 0, 0, },
				{ 0, 0, 1, 0, 0, 0, },
				{ 1, 1, 1, 0, 1, 0, },
				{ 0, 0, 0, 0, 1, 0, },
				{ 0, 0, 0, 1, 0, 1, },
				{ 0, 0, 0, 1, 0, 1, },
		});
		Verify verifier = new Verify(g);
		assertTrue(!verifier.verify(2, 3, new int[][] {
				{ 1, 4 },
				{ 1, 5 },
				{ 0, 5 },
		}));
	}

	@Test
	public void testVerify2() {
		Graph g = new Graph(new int[][] {
				{ 0, 1, 1, 1, },
				{ 1, 0, 1, 1, },
				{ 1, 1, 0, 1, },
				{ 1, 1, 1, 1, },
		});
		Verify verifier = new Verify(g);
		assertTrue(!verifier.verify(2, 4, new int[][] {
				{ 2, 1 },
				{ 0, 0 },
				{ 1, 1 },
				{ 2, 2 },
		}));
	}

	@Test
	public void testVerify3() {
		Graph g = new Graph(new int[][] {
				{ 0, 1, 0, 0, 0 },
				{ 1, 0, 1, 0, 0 },
				{ 0, 1, 0, 1, 0 },
				{ 0, 0, 1, 0, 1 },
				{ 0, 0, 0, 1, 0 },
		});
		Verify verifier = new Verify(g);
		assertTrue(verifier.verify(1, 6, new int[][] {
				{ 1 },
				{ 2 },
				{ 3 },
				{ 3 },
				{ 2 },
				{ 1 },
		}));
	}

	@Test
	public void testVerify4() {
		Graph g = new Graph(new int[][] {
				{ 0, 0, 1, 0, 0, 0, },
				{ 0, 0, 1, 0, 0, 0, },
				{ 1, 1, 1, 0, 1, 0, },
				{ 0, 0, 0, 0, 1, 0, },
				{ 0, 0, 0, 1, 0, 1, },
				{ 0, 0, 0, 1, 0, 1, },
		});
		Verify verifier = new Verify(g);
		assertTrue(verifier.verify(1, 5, new int[][] {
				{ 4 },
				{ 5 },
				{ 3 },
				{ 2 },
				{ 2 },
		}));
	}

	@Test
	public void testVerify5() {
		Graph g = new Graph(new int[][] {
				{ 0, 0, 1, 0, 0, 0, },
				{ 0, 0, 1, 0, 0, 0, },
				{ 1, 1, 1, 0, 1, 0, },
				{ 0, 0, 0, 0, 1, 0, },
				{ 0, 0, 0, 1, 0, 1, },
				{ 0, 0, 0, 1, 0, 1, },
		});
		Verify verifier = new Verify(g);
		assertTrue(verifier.verify(2, 3, new int[][] {
				{ 2, 4 },
				{ 2, 5 },
				{ 2, 3 },
		}));
	}

	/****************************************************
	/***************   graph.java   *********************/

	@Test
	public void testCycles1() {
		Graph g = new Graph(new int[][] {
				{ 0, 0, 0, 1 },
				{ 1, 0, 0, 0 },
				{ 0, 1, 1, 0 },
				{ 1, 0, 1, 0 },
		});
		ArrayList<LinkedList<Integer>> cycles = g.getCycles();
		for (LinkedList<Integer> cycle : cycles) Collections.sort(cycle);
		Collections.sort(cycles, new Comparator<LinkedList<Integer>>() {
			@Override
			public int compare(LinkedList<Integer> l1, LinkedList<Integer> l2) {
				int compare = l1.size() - l2.size();
				int pos = 0;
				while (compare == 0) {
					compare = l1.get(pos) - l2.get(pos);
					pos++;
				}
				return compare;
			}
		});
		assertTrue(cycles.toString().
				equals("[[2], [0, 3], [0, 1, 2, 3]]"));
	}

	@Test
	public void testCycles2() {
		Graph g = new Graph(new int[][] {
				{ 0, 1, 0, 0, 1 },
				{ 0, 0, 1, 0, 0 },
				{ 1, 0, 0, 1, 0 },
				{ 0, 1, 0, 0, 1 },
				{ 0, 0, 1, 0, 0 },
		});
		ArrayList<LinkedList<Integer>> cycles = g.getCycles();
		for (LinkedList<Integer> cycle : cycles) Collections.sort(cycle);
		Collections.sort(cycles, new Comparator<LinkedList<Integer>>() {
			@Override
			public int compare(LinkedList<Integer> l1, LinkedList<Integer> l2) {
				int compare = l1.size() - l2.size();
				int pos = 0;
				while (compare == 0) {
					compare = l1.get(pos) - l2.get(pos);
					pos++;
				}
				return compare;
			}
		});
		assertTrue(cycles.toString().
				equals("[[0, 1, 2], [0, 2, 4], [1, 2, 3], [2, 3, 4]]"));
	}

	@Test
	public void testCycles3() {
		Graph g = new Graph(new int[][] {
				{ 0, 0, 0, 1, 1, 0 },
				{ 1, 0, 0, 0, 0, 0 },
				{ 0, 1, 1, 0, 0, 0 },
				{ 0, 0, 1, 0, 0, 0 },
				{ 0, 0, 1, 0, 0, 1 },
				{ 0, 0, 0, 0, 1, 0 },
		});
		ArrayList<LinkedList<Integer>> cycles = g.getCycles();
		for (LinkedList<Integer> cycle : cycles) Collections.sort(cycle);
		Collections.sort(cycles, new Comparator<LinkedList<Integer>>() {
			@Override
			public int compare(LinkedList<Integer> l1, LinkedList<Integer> l2) {
				int compare = l1.size() - l2.size();
				int pos = 0;
				while (compare == 0) {
					compare = l1.get(pos) - l2.get(pos);
					pos++;
				}
				return compare;
			}
		});
		assertTrue(cycles.toString().
				equals("[[2], [4, 5], [0, 1, 2, 3], [0, 1, 2, 4]]"));
	}

	@Test
	public void testCycles4() {
		Graph g = new Graph(new int[][] {
				{ 0, 1, 0, 0, 1 },
				{ 1, 0, 0, 1, 0 },
				{ 1, 0, 0, 0, 0 },
				{ 0, 0, 1, 0, 0 },
				{ 0, 1, 0, 0, 0 },
		});
		ArrayList<LinkedList<Integer>> cycles = g.getCycles();
		for (LinkedList<Integer> cycle : cycles) Collections.sort(cycle);
		Collections.sort(cycles, new Comparator<LinkedList<Integer>>() {
			@Override
			public int compare(LinkedList<Integer> l1, LinkedList<Integer> l2) {
				int compare = l1.size() - l2.size();
				int pos = 0;
				while (compare == 0) {
					compare = l1.get(pos) - l2.get(pos);
					pos++;
				}
				return compare;
			}
		});
		assertTrue(cycles.toString().
				equals("[[0, 1], [0, 1, 4], [0, 1, 2, 3], [0, 1, 2, 3, 4]]"));
	}

	@Test
	public void testStrongComponents1() {
		Graph g = new Graph(new int[][] {
				{ 0, 1, 0, 0, 0 },
				{ 0, 0, 1, 0, 0 },
				{ 1, 0, 0, 0, 0 },
				{ 0, 0, 1, 0, 1 },
				{ 0, 0, 0, 1, 0 },
		});
		List<Graph> strongComponents = g.getStrongComponents();
		List<String> componentStrings = Arrays.asList(new String[] {
				"1 --> 3\n2 --> 1\n3 --> 2\n",
				"4 --> 5\n5 --> 4\n",
		});
		assertTrue(strongComponents.size()==componentStrings.size());
		for (Graph strongComponent : strongComponents) {
			assertTrue(componentStrings.contains(strongComponent.toString()));
		}
	}

	@Test
	public void testStrongComponents2() {
		Graph g = new Graph(new int[][] {
				{0,1,0,0},
				{1,0,1,0},
				{0,1,1,1},
				{0,0,1,1},
		});
		List<Graph> strongComponents = g.getStrongComponents();
		List<String> componentStrings = Arrays.asList(new String[] {
				"1 --> 2\n2 --> 1\n2 --> 3\n3 --> 2\n3 --> 3\n3 --> 4\n4 --> 3\n4 --> 4\n",
		});
		assertTrue(strongComponents.size()==componentStrings.size());
		for (Graph strongComponent : strongComponents) {
			assertTrue(componentStrings.contains(strongComponent.toString()));
		}
	}
	
	@Test
	public void testStrongComponents3() {
		Graph g = new Graph(new int[][] {
				{1,0,0,0,0},
				{1,1,0,0,0},
				{0,1,1,1,0},
				{0,0,1,0,0},
				{0,0,1,1,1},
		});
		List<Graph> strongComponents = g.getStrongComponents();
		List<String> componentStrings = Arrays.asList(new String[] {
				"1 --> 1\n",
				"2 --> 2\n",
				"3 --> 3\n3 --> 4\n4 --> 3\n",
				"5 --> 5\n",
		});
		assertTrue(strongComponents.size()==componentStrings.size());
		for (Graph strongComponent : strongComponents) {
			assertTrue(componentStrings.contains(strongComponent.toString()));
		}
	}
	
	@Test
	public void testStrongComponents4() {
		Graph g = new Graph(new int[][] {
				{0,0,0},
				{0,0,0},
				{1,1,1},
		});
		List<Graph> strongComponents = g.getStrongComponents();
		List<String> componentStrings = Arrays.asList(new String[] {
				"no edges\n",
				"no edges\n",
				"3 --> 3\n",
		});
		assertTrue(strongComponents.size()==componentStrings.size());
		for (Graph strongComponent : strongComponents) {
			assertTrue(componentStrings.contains(strongComponent.toString()));
		}
	}
}
