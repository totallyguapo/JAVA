package trees;

import java.util.ArrayList;
import java.util.Iterator;

// Name: James Stinchcombe
// ID: 260 787 400

public class KDTree implements Iterable<Datum> {

	KDNode rootNode;
	int k;
	int numLeaves;

	// constructor

	public KDTree(ArrayList<Datum> datalist) throws Exception {

		Datum[] dataListArray = new Datum[datalist.size()];

		if (datalist.size() == 0) {
			throw new Exception("Trying to create a KD tree with no data");
		} else
			this.k = datalist.get(0).x.length;

		int ct = 0;
		for (Datum d : datalist) {
			dataListArray[ct] = datalist.get(ct);
			ct++;
		}

		// Construct a KDNode that is the root node of the KDTree.

		rootNode = new KDNode(dataListArray);
	}

	// KDTree methods

	public Datum nearestPoint(Datum queryPoint) {
		return rootNode.nearestPointInNode(queryPoint);
	}

	public int height() {
		return this.rootNode.height();
	}

	public int countNodes() {
		return this.rootNode.countNodes();
	}

	public int size() {
		return this.numLeaves;
	}

	// ------------------- helper methods for KDTree ------------------------------

	public static long distSquared(Datum d1, Datum d2) {

		long result = 0;
		for (int dim = 0; dim < d1.x.length; dim++) {
			result += (d1.x[dim] - d2.x[dim]) * ((long) (d1.x[dim] - d2.x[dim]));
		}
		// if the Datum coordinate values are large then we can easily exceed the limit
		// of 'int'.
		return result;
	}

	public double meanDepth() {
		int[] sumdepths_numLeaves = this.rootNode.sumDepths_numLeaves();
		return 1.0 * sumdepths_numLeaves[0] / sumdepths_numLeaves[1];
	}

	class KDNode {

		boolean leaf;
		Datum leafDatum; // only stores Datum if this is a leaf

		// the next two variables are only defined if node is not a leaf

		int splitDim; // the dimension we will split on
		int splitValue; // datum is in low if value in splitDim <= splitValue, and high if value in
						// splitDim > splitValue

		KDNode lowChild, highChild; // the low and high child of a particular node (null if leaf)
		// You may think of them as "left" and "right" instead of "low" and "high",
		// respectively

		KDNode(Datum[] datalist) throws Exception {

			/*
			 * This method takes in an array of Datum and returns the calling KDNode object
			 * as the root of a sub-tree containing the above fields.
			 */

			// ADD YOUR CODE BELOW HERE

			// checks datalist is non-empty
			if (datalist.length == 0) {
				throw new Exception("The list of inputs is empty (idiot)");
			}

			// sets root node
			if (rootNode == null) {
				rootNode = this;
				k = datalist[0].x.length;
			}

			// base case #1, there is only 1 datum
			if (datalist.length == 1) {
				leaf = true;
				leafDatum = datalist[0];
				numLeaves++;
				return;
			}

			this.leaf = false;
			int lrgstRng = 0;
			int dims = datalist[0].x.length;
			int points = 1;
			int listSize = datalist.length;

			// determines the dimension with largest range
			for (int i = 0; i < dims; i++) {
				int max = datalist[0].x[i];
				int min = max;
				for (int j = 1; j < listSize; j++) {

					if (datalist[j] == null) {
						points = j;
						break;
					}

					if (datalist[j].x[i] > max) {
						max = datalist[j].x[i];
					}
					if (datalist[j].x[i] < min) {
						min = datalist[j].x[i];
					}
					points++;
				}
				int rng = max - min;
				if (rng > lrgstRng) {
					lrgstRng = rng;
					splitDim = i;
					splitValue = min + (rng / 2);
				}

			}
			Datum[] highChild = new Datum[points];
			Datum[] lowChild = new Datum[points];
			int nextHigh = 0;
			int nextLow = nextHigh;

			// adds points to nodes, separating high and low children (not like how trump
			// separates children)
			for (int i = 0; i < listSize; i++) {
				if (datalist[i] == null) {
					break;
				}
				if (datalist[i].x[splitDim] > splitValue) {
					highChild[nextHigh] = datalist[i];
					nextHigh++;
				} else {
					lowChild[nextLow] = datalist[i];
					nextLow++;
				}
			}
			// all points are duplicates
			if (lowChild[0] == null || highChild[0] == null) {
				leaf = true;
				leafDatum = datalist[0];
				numLeaves++;
				return;
			}

			this.highChild = new KDNode(highChild);
			this.lowChild = new KDNode(lowChild);

			// ADD YOUR CODE ABOVE HERE

		}

		public Datum nearestPointInNode(Datum queryPoint) {
			Datum nearestPoint, nearestPoint_otherSide;

			// ADD YOUR CODE BELOW HERE

			// determines if point is on low child side
			boolean low = false;

			// called until leaf is found
			if (leaf == true) {
				return leafDatum;
			}

			// if query point is low, nearest point is on low child side
			if (queryPoint.x[splitDim] <= splitValue) {
				low = true;
				nearestPoint = lowChild.nearestPointInNode(queryPoint);
			} else {
				// high child could be a candidate too
				low = false;
				nearestPoint = highChild.nearestPointInNode(queryPoint);
			}
			long split = splitValue - queryPoint.x[splitDim];
			long splitSquared = split * split;

			long dist = distSquared(nearestPoint, queryPoint);
			if (dist >= splitSquared) {
				if (low == true) {
					nearestPoint_otherSide = highChild.nearestPointInNode(queryPoint);
				} else {
					nearestPoint_otherSide = lowChild.nearestPointInNode(queryPoint);
				}
				if (dist > distSquared(nearestPoint_otherSide, queryPoint)) {
					nearestPoint = nearestPoint_otherSide;

				}
			}
			return nearestPoint;

			// ADD YOUR CODE ABOVE HERE

		}

		// ----------------- KDNode helper methods (might be useful for debugging)
		// -------------------

		public int height() {
			if (this.leaf)
				return 0;
			else {
				return 1 + Math.max(this.lowChild.height(), this.highChild.height());
			}
		}

		public int countNodes() {
			if (this.leaf)
				return 1;
			else
				return 1 + this.lowChild.countNodes() + this.highChild.countNodes();
		}

		/*
		 * Returns a 2D array of ints. The first element is the sum of the depths of
		 * leaves of the subtree rooted at this KDNode. The second element is the number
		 * of leaves this subtree. Hence, I call the variables sumDepth_size_* where
		 * sumDepth refers to element 0 and size refers to element 1.
		 */

		public int[] sumDepths_numLeaves() {
			int[] sumDepths_numLeaves_low, sumDepths_numLeaves_high;
			int[] return_sumDepths_numLeaves = new int[2];

			/*
			 * The sum of the depths of the leaves is the sum of the depth of the leaves of
			 * the subtrees, plus the number of leaves (size) since each leaf defines a path
			 * and the depth of each leaf is one greater than the depth of each leaf in the
			 * subtree.
			 */

			if (this.leaf) { // base case
				return_sumDepths_numLeaves[0] = 0;
				return_sumDepths_numLeaves[1] = 1;
			} else {
				sumDepths_numLeaves_low = this.lowChild.sumDepths_numLeaves();
				sumDepths_numLeaves_high = this.highChild.sumDepths_numLeaves();
				return_sumDepths_numLeaves[0] = sumDepths_numLeaves_low[0] + sumDepths_numLeaves_high[0]
						+ sumDepths_numLeaves_low[1] + sumDepths_numLeaves_high[1];
				return_sumDepths_numLeaves[1] = sumDepths_numLeaves_low[1] + sumDepths_numLeaves_high[1];
			}
			return return_sumDepths_numLeaves;
		}

	}

	public Iterator<Datum> iterator() {
		return new KDTreeIterator();
	}

	private class KDTreeIterator implements Iterator<Datum> {

		// ADD YOUR CODE BELOW HERE
		ArrayList<Datum> datumList;
		int i = 0;
		int size;

		public KDTreeIterator() {
			datumList = new ArrayList<>();
			inOrder(rootNode);
			size = datumList.size();
		}

		public void inOrder(KDNode node) {
			if (node.lowChild != null) {
				inOrder(node.lowChild);
			}
			if (node.leaf) {
				datumList.add(node.leafDatum);
			}
			if (node.highChild != null) {
				inOrder(node.highChild);
			}
		}

		public boolean hasNext() {
			return size > i;

		}

		public Datum next() {
			return datumList.get(i++);
		}
		// ADD YOUR CODE ABOVE HERE

	}

}
