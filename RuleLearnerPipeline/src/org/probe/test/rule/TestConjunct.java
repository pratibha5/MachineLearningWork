package org.probe.test.rule;

import org.junit.Test;
import org.probe.rule.Conjunct;
import org.probe.rule.ConjunctRelation;

import static org.junit.Assert.assertEquals;

public class TestConjunct {

	@Test
	public void testParseStringNoSpaces(){
		String conjunctStr = "(a>500)";
		
		Conjunct conjunct = Conjunct.parseString(conjunctStr);
		
		assertEquals("a",conjunct.getField());
		assertEquals(ConjunctRelation.GREATER_THAN,conjunct.getRelation());
		assertEquals("500",conjunct.getValue());
	}
	
	@Test
	public void testParseStringSpaces(){
		String conjunctStr = "( a   < 500   )";
		
		Conjunct conjunct = Conjunct.parseString(conjunctStr);
		
		assertEquals("a",conjunct.getField());
		assertEquals(ConjunctRelation.LESSER_THAN,conjunct.getRelation());
		assertEquals("500",conjunct.getValue());
	}
}
