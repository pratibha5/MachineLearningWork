/*
 * @(#)SetNode.java    1.1 02/01/21
 *
 */

package structures.learner.attribute;

/**
 * Hierarchy node for set values.
 * 
 * @version 1.0 00/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to make use of new code organization
 * 
 * @version 1.1 02/01/21
 * @author Will Bridewell
 */

public class SetNode extends VHierarchyNode implements Cloneable {
	String value;

	public SetNode(String attNm, String v) {
		value = v;
		attName = attNm;
	}

	public String toString() {
		return value;
	}

	/**
	 * A SetNode is equal to an object if a) the object is a string containing
	 * the value in the node b) the object is a set node with an identical value
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof String)
			return ((String) o).indexOf(value) != -1;
		else if (o instanceof SetNode)
			return o.toString().equals(value);
		else {
			System.out.println("Unknown class in SetNode equals");
			return false;
		}
	}

	public Object clone() {
		SetNode sn = new SetNode(this.attName, this.value);
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
