package org.probe.test.rule;

import org.junit.Test;
import org.probe.rule.Conjunct;
import org.probe.rule.Conjunction;
import org.probe.rule.RuleOG;

import static org.junit.Assert.assertTrue;

public class TestRule {
	
	@Test
	public void testConstruction(){
		Conjunction lhs = Conjunction.parseString("((a>500)(b<300))");
		Conjunct rhs = Conjunct.parseString("class=1");
		
		RuleOG rule = new RuleOG(lhs,rhs);

		assertTrue(rule.containsField("a"));
		assertTrue(rule.containsField("b"));
		assertTrue(rule.containsField("class"));
	}
	
	@Test
	public void testParse(){
		String ruleStr = "((a>500)(b<300))->(class=1)";
		
		RuleOG rule = RuleOG.parseString(ruleStr);
		
		assertTrue(rule.containsField("a"));
		assertTrue(rule.containsField("b"));
		assertTrue(rule.containsField("class"));
	}
}
