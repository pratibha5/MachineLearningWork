/*
 * @(#)HNode.java    1.1 02/01/21
 */

package structures.learner.attribute;

/**
 * This is a non-leaf hierarchy node. The hierarchy consists of object (String,
 * Interval) and this is simply a parent object.
 * 
 * @version 1.0 00/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to make use of new code organization
 * 
 * @version 1.1 02/01/21
 * @author Will Bridewell
 */
public class HNode extends VHierarchyNode implements Cloneable {
	public HNode(String attNm, String s) {
		super(attNm, s);
	}

	/**
	 * No comparison allowed.
	 */
	public int compareTo(Object o) {
		return VHierarchyNode.NO_COMPARE;
	}

	public Object clone() {
		HNode sn = new HNode(this.attName, this.name);
		sn.nodeCount = this.nodeCount;
		sn.nodeList = new VHierarchyNode[this.nodeList.length];
		for (int i = 0; i < this.nodeList.length; i++) {
			VHierarchyNode clnLstNd = (VHierarchyNode) this.nodeList[i].clone();
			sn.insert(clnLstNd, sn.getChildCount());
			sn.nodeList[i] = clnLstNd;
		}
		return sn;
	}
}
