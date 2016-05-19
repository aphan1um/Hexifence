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
}
