package kex.generators;

public interface GraphGenerator {
	/**
	 * Generates a random graph.
	 * @param The number of vertices in the graph.
	 */
	public String generate(int vertexCount);
}
