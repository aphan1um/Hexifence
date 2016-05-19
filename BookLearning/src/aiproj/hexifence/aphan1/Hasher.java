/* 
 * COMP30024 - Project B
 * Andy Phan (aphan1) and Martin Cheong (cheongm)
 * 
 */

package aiproj.hexifence.aphan1;

/** Hashing interface for TranspositionTable.java to use.
 */
public interface Hasher {
	public long generateHashKey(int[][] edges);
}
