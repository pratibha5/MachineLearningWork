/*
 * @(#)VHierarchyNode.java    1.1 2002/01/21
 */

package org.probe.stats.structures.learner.attribute;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * Parent node class for value hierarchy objects.
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * 
 * Renamed from less the descriptive "NodeObject".  
 * Made to extend DefaultMutableTreeNode to simplify the value hierarchy. 
 * Edited to make use of new code organization
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 */
public class VHierarchyNode extends DefaultMutableTreeNode implements
		Comparable, Cloneable {
	protected String name;
	protected String attName;
	protected VHierarchyNode top;

	/** Number of nodes in this value hierarchy */
	protected int nodeCount;
	protected final static int NO_COMPARE = -9;

	/** Nodes in this hierarchy */
	protected VHierarchyNode[] nodeList;

	public VHierarchyNode() {
		super();
		name = "";
		attName = "";
		top = this;
		nodeCount = 0;
		nodeList = new VHierarchyNode[0];
	}

	public VHierarchyNode(String attName,
 String name) {
		super();
		this.name = name;
		this.attName = attName;
		top = this;
		nodeCount = 0;
		nodeList = new VHierarchyNode[0];
	}

	/**
	 * Called only when creating attributes. Once a user can alter them, this
	 * function is no longer valid.
	 */

	public void addValue(VHierarchyNode s) {
		if (top.getValueIndex(s) == -1) {
			insertNode(this, s);
		}
	}

	/**
	 * Returns the index of the value represented by <code>name</code>.
	 */
	public int getValueIndex(String name) {
		for (int x = 0; x < nodeList.length; x++) {
			if (nodeList[x].getName().compareTo(name) == 0) {
				return x;
			}
		}
		return -1;
	}

	public VHierarchyNode getValue(Object o) {
		for (int x = 0; x < nodeList.length; x++) {
			if (nodeList[x].equals(o)) {
				return nodeList[x];
			}
		}
		return null;
	}

	/**
	 * @return <code>true</code> if <code>o</code> is in
	 *         <code>subtree</code>.
	 */
	public boolean inSubtree(Object o) {
		// Found the subtree. Now see if o is an element of this
		for (Enumeration e2 = this.preorderEnumeration(); e2.hasMoreElements();) {
			if (e2.nextElement().equals(o)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return <code>true</code> if all objects in <code>subtree</code> are
	 *         either smaller than <code>o</code> or of a different type.
	 */
	private boolean allSubtreeLT(Object o) {
		VHierarchyNode tempObject;
		int nCompare;
		// Found the subtree, now see if o is an element of this
		for (Enumeration e2 = this.preorderEnumeration(); e2.hasMoreElements();) {
			tempObject = (VHierarchyNode) e2.nextElement();

			nCompare = tempObject.compareTo(o);
			if (nCompare == 0 || nCompare == 1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return <code>true</code> if all abjects in <code>subtree</code> are
	 *         either greater than <code>o</code> or of a different type.
	 */
	private boolean allSubtreeGT(Object o) {
		VHierarchyNode tempObject;
		int nCompare;

		// Found the subtree, now see if o is an element of this
		for (Enumeration e2 = this.preorderEnumeration(); e2.hasMoreElements();) {
			tempObject = (VHierarchyNode) e2.nextElement();
			nCompare = tempObject.compareTo(o);
			if (nCompare == 0 || nCompare == -1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compares <code>o</code> to the root of <code>tree</code> and its
	 * children.
	 * 
	 * @return <code>0</code> if <code>tree</code> equals <code>o</code>
	 *     <code>1</code>
	 *         if all values in <code>tree</code> are greater than
	 *         <code>o</code>
	 *     <code>-1</code> if all values are less than
	 *         <code>o</code>
	 *     <code>NO_COMPARE</code> otherwise
	 * @see #NO_COMPARE
	 */
	public int compareToDataValue(Object o) {
		return allSubtreeGT(o) ? 1 : allSubtreeLT(o) ? -1 : inSubtree(o) ? 0
				: NO_COMPARE;
	}

	/**
	 * Returns a generic string representation of the value hierarchy. Values
	 * are separated by spaces and the hierarchy is lost.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (isLeaf())
			return name;
		if (!name.equalsIgnoreCase("ROOT"))
			buf.append(name);
		for (int x = 0; x < nodeList.length; x++) {
			buf.append(" ");
			buf.append(nodeList[x].toString());
		}
		return buf.toString();
	}

	public boolean setName(String name) {
		this.name = name;
		return true;
	}

	private void insertNode(VHierarchyNode parent, VHierarchyNode vn) {
		parent.insert(vn, parent.getChildCount());
		vn.attName = parent.attName;
		if (this.name.equalsIgnoreCase("root")) {
			VHierarchyNode[] tmpList = new VHierarchyNode[nodeList.length + 1];
			System.arraycopy(nodeList, 0, tmpList, 0, nodeList.length);
			tmpList[nodeCount] = vn;
			nodeList = tmpList;
			nodeCount++;
			vn.top = this;
		} else {
			VHierarchyNode[] tmpList = new VHierarchyNode[top.nodeList.length + 1];
			System.arraycopy(top.nodeList, 0, tmpList, 0, top.nodeList.length);
			tmpList[top.nodeCount] = vn;
			top.nodeList = tmpList;
			top.nodeCount++;
			vn.top = parent.top;
		}
		// top.organizeNodes();
	}

	public void addValue(VHierarchyNode parent, VHierarchyNode child) {
		if (parent != null) {
			parent.insertNode(parent, child);
		}
	}

	/**
	 * For sets, parses the string and adds each word (if necessary
	 * individually). This then becomes the dictionary.
	 */
	public void addSetValue(String s) {
		StringTokenizer tok = new StringTokenizer(s);

		while (tok.hasMoreTokens()) {
			String add = tok.nextToken();
			//addValue(new SetNode(this.attName, add));
		}
	}

	public int numValues() {
		return nodeCount;
	}

	public VHierarchyNode top() {
		return top;
	}

	public VHierarchyNode getValue(int index) {
		return (index < nodeList.length) ? nodeList[index] : null;
	}

	public VHierarchyNode[] getValueArray() {
		return nodeList;
	}

	public int getValueIndex(Object o) {
		if (this.name.equalsIgnoreCase("ROOT")) {
			for (int x = 0; x < nodeList.length; x++) {
				if (nodeList[x].equals(o)) {
					return x;
				}
			}
			return -1;
		} else {
			return this.top.getValueIndex(o);
		}
	}

	public String getName() {
		return name;
	}

	public String getAttributeName() {
		return attName;
	}

	/**
	 * String equality.
	 */
	public boolean equals(Object o) {
		return name.equals(o.toString());
	}

	/**
	 * String comparison.
	 */
	public int compareTo(Object o) {
		return name.compareTo(o.toString());
	}

	/**
	 * Returns a copy of this node that is not positioned within any tree
	 */
	public Object clone() {
		VHierarchyNode nvh = new VHierarchyNode(this.attName, this.name);
		nvh.nodeCount = this.nodeCount;
		nvh.nodeList = new VHierarchyNode[this.nodeList.length];
		for (int i = 0; i < this.nodeList.length; i++) {
			VHierarchyNode clnLstNd = (VHierarchyNode) this.nodeList[i].clone();
			nvh.insert(clnLstNd, nvh.getChildCount());
			nvh.nodeList[i] = clnLstNd;
		}
		return nvh;
	}

	public VHierarchyNode[] getPathToRoot(VHierarchyNode sN) {
		int ind = top.getIndex(sN);
		TreeNode[] pFr = sN.getPath();
		VHierarchyNode[] pTr = new VHierarchyNode[pFr.length - 1];
		for (int i = pFr.length - 1; i > 0; i--) {
			pTr[pFr.length - 1 - i] = getValue(getIndex(pFr[i]));
		}
		return pTr;
	}

	private ArrayList<VHierarchyNode> traverseNodes() {
		if (this.isLeaf()) {
			ArrayList<VHierarchyNode> tmpL = new ArrayList<VHierarchyNode>(1);
			tmpL.add(this);
			return tmpL;
		} else {
			ArrayList<VHierarchyNode> nods = new ArrayList<VHierarchyNode>();
			VHierarchyNode[] ndsToTraverse = this.getValueArray();
			nods.add(this);
			for (int i = 0; i < ndsToTraverse.length; i++) {
				ArrayList<VHierarchyNode> subNodes 
						= ndsToTraverse[i].traverseNodes();
				nods.addAll(subNodes);
			}
			return nods;
		}
	}

	public void organizeNodes() {
		ArrayList<VHierarchyNode> nds = top.traverseNodes();
		nds.remove(0);
		top.nodeList = nds.toArray(new VHierarchyNode[0]);
	}

	public VHierarchyNode getTop() {
		return top;
	}
}
