package org.probe.algo.rule.id3;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class TreeNode {
	private final TreeNodePayload payload;
	
	private String leafLabel = null;
	private String leafValue = null;
	private boolean isLeaf = false;
	
	private List<TreeNode> children = new LinkedList<TreeNode>(); 
	
	
	
	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public List<TreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<TreeNode> children) {
		this.children = children;
	}

	public static String getSEPARATOR() {
		return SEPARATOR;
	}

	public static void setSEPARATOR(String sEPARATOR) {
		SEPARATOR = sEPARATOR;
	}

	public void setLeafLabel(String leafLabel) {
		this.leafLabel = leafLabel;
	}

	public void setLeafValue(String leafValue) {
		this.leafValue = leafValue;
	}


	
	public TreeNode(TreeNodePayload payload){
		this.payload = payload;
	}
	
	public void addChild(TreeNode node){
		children.add(node);
	}
	
	public TreeNode getChildAt(int index){
		if(index < 0 || index >= children.size())
			return null;
		else return children.get(index);
	}

	public TreeNodePayload getPayload() {
		return payload;
	}
	
	public void print(){
		Stack<TreeNode> treeNodeStack = new Stack<TreeNode>();
		treeNodeStack.add(this);
		
		while(treeNodeStack.size() > 0){
			TreeNode currentNode = treeNodeStack.pop();
			currentNode.getPayload().printPayload();
			
			for(TreeNode childNode : currentNode.children){
				treeNodeStack.add(childNode);
			}
		}
	}
	
	public void printInOrder(){
		recurseInOrder(this, new StringBuffer(SEPARATOR));
	}
	
	public void recurseInOrder(TreeNode currentNode, StringBuffer tab){
		System.out.print(tab.toString());
		currentNode.getPayload().printPayload();
		if(currentNode.isLeaf)
			System.out.println(tab + SEPARATOR + "@" + currentNode.leafLabel + "==>" + currentNode.leafValue);
		
		tab.append(SEPARATOR); 
		for(TreeNode childNode : currentNode.children){
			recurseInOrder(childNode, new StringBuffer(tab));
		}
	}

	public String getLeafLabel() {
		return leafLabel;
	}
	
	public String getLeafValue() {
		return leafValue;
	}

	public void setLeaf(String leafLabel, String leafValue) {
		this.leafLabel = leafLabel;
		this.leafValue = leafValue;
		this.isLeaf = true;
	}

	private static String SEPARATOR = "    ";
}
