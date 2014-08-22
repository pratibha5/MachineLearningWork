package org.probe.datastructure;

import java.util.LinkedList;
import java.util.List;

import org.probe.data.DataAttribute;

public class RuleNode {
	private final List<RuleNode> children = new LinkedList<RuleNode>();
	private final DataAttribute attribute;
	
	public RuleNode(DataAttribute attribute){
		this.attribute = attribute;
	}
	
	public void appendChild(RuleNode ruleNode){
		children.add(ruleNode);
	}
	
	public List<RuleNode> getChildren(){
		return children;
	}
	
	public void erase(){
		for(RuleNode child : children){
			child.erase();
		}
		children.clear();
	}

	public DataAttribute getAttribute() {
		return attribute;
	}
}
