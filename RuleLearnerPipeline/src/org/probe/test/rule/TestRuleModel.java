package org.probe.test.rule;

import org.junit.Test;
import org.probe.rule.RuleOG;
import org.probe.rule.RuleModel;

import static org.junit.Assert.assertTrue;

public class TestRuleModel {
	
	@Test
	public void test(){
		RuleModel ruleModel = new RuleModel();
		
		ruleModel.addRule(RuleOG.parseString("((a>500)(b<300))->(class=1)"));
		ruleModel.addRule(RuleOG.parseString("((a<500)(b<300))->(class=2)"));
		ruleModel.addRule(RuleOG.parseString("((a<500)(b>300))->(class=1)"));
		
		assertTrue(ruleModel.containsField("a"));
		assertTrue(ruleModel.containsField("b"));
		assertTrue(ruleModel.containsField("class"));
		
		System.out.println(ruleModel);
	}
}
