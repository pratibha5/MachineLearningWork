package org.probe.rule;

import org.probe.util.RuleFormatter;

public class RuleOG {

	public RuleOG(Conjunction lhs, Conjunct rhs){
		this.lhs = lhs;
		this.rhs = rhs;
	}
	
	public static RuleOG parseString(String ruleStr) {
		String formattedString = RuleFormatter.removeAllWhiteSpace(ruleStr);
		
		String[] ruleElements = formattedString.split("->");
		Conjunction lhs = Conjunction.parseString(ruleElements[0]);
		Conjunct rhs = Conjunct.parseString(ruleElements[1]);
		
		RuleOG rule = new RuleOG(lhs,rhs);
		return rule;
	}
	
	public Conjunction getLHS(){
		return lhs;
	}
	
	public Conjunct getRHS(){
		return rhs;
	}

	public boolean containsField(String field) {
		return lhs.containsField(field) || rhs.containsField(field);
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(lhs).append("->").append(rhs);
		return sb.toString();
	}
	
	private Conjunction lhs;
	private Conjunct rhs;
}
