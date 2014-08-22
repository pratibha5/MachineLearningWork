package org.probe.rule;

public enum ConjunctRelation {
	GREATER_THAN(">"),
	LESSER_THAN("<"),
	EQUAL("="), 
	UNKNOWN("UNKNOWN");

	ConjunctRelation(String str){
		this.str = str;
	}
	
	@Override
	public String toString(){
		return str;
	}
	
	private String str;
}
