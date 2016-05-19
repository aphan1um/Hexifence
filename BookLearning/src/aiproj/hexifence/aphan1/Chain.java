/* 
 * COMP30024 - Project B
 * Andy Phan (aphan1) and Martin Cheong (cheongm)
 * 
 */

package aiproj.hexifence.aphan1;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Chain {
	/** Represents list of cells in a chain */
	public List<Point> cells = new ArrayList<Point>();
	/** If this is false, then one end of the chain has an open
	 * edge (we call this 'half-open'). Otherwise it is closed.
	 */
	public Boolean isClosed = true;
	/** If this chain is a 'potential chain'. That is, a set of
	 * cells can become a potential chain, if one of its cells
	 * with two edges left becomes one. 
	 */
	public Boolean isPotential = false;
}
