package org.probe.util;

import org.probe.algo.rule.id3.TreeNode;
import org.probe.rule.Conjunct;
import org.probe.rule.RuleOG;
import org.probe.rule.RuleModel;

public class TreeToRuleModelConverter {
	/**
	public static RuleModel createRuleModelFromTree(TreeNode rootNode){
		
		
		return null;
	}
	
	public void printInOrder(){
		recurseInOrder(this, new StringBuffer(SEPARATOR));
	}
	
	public void recurseInOrder(TreeNode currentNode){
		currentNode.getPayload().printPayload();
		if(currentNode.isLeaf())
			System.out.println('\t' + currentNode.getSEPARATOR() + "@" + currentNode.getLeafLabel() + "==>" + currentNode.getLeafValue());
		
		for(TreeNode childNode : currentNode.getChildren()){
			recurseInOrder(childNode, new StringBuffer('\t'));
		}
	}
	
	private Rule createRule(){
		Conjunction lhs = new Conjunct(field, relation, value);
		
		Rule rule = new Rule(lhs, rhs);
	}
	*/
}
