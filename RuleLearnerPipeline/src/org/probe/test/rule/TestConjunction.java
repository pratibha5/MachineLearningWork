package org.probe.test.rule;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.probe.rule.Conjunct;
import org.probe.rule.ConjunctRelation;
import org.probe.rule.Conjunction;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;;

public class TestConjunction {
	@Test
	public void testConstruction(){
		Conjunct conjunct1 = Conjunct.parseString("a>500");
		Conjunct conjunct2 = Conjunct.parseString("b<300");
		
		Conjunction conjunction = new Conjunction();
		
		conjunction.addConjunct(conjunct1);
		conjunction.addConjunct(conjunct2);
		
		assertTrue(conjunction.containsField("a"));
		assertTrue(conjunction.containsField("b"));
		assertFalse(conjunction.containsField("x"));
		
		Conjunct conjunct = conjunction.getConjunctByField("a");
		assertEquals("a",conjunct.getField());
		assertEquals(ConjunctRelation.GREATER_THAN,conjunct.getRelation());
		assertEquals("500",conjunct.getValue());
		
		conjunct = conjunction.getConjunctByField("b");
		assertEquals("b",conjunct.getField());
		assertEquals(ConjunctRelation.LESSER_THAN,conjunct.getRelation());
		assertEquals("300",conjunct.getValue());
		
		conjunct = conjunction.getConjunctByField("x");
		assertNull(conjunct);
	}
	
	@Test
	public void testParse(){
		String conjunctionStr = "((a>500)(b<300))";
		
		Conjunction conjunction = Conjunction.parseString(conjunctionStr);
		
		Conjunct conjunct = conjunction.getConjunctByField("a");
		assertEquals("a",conjunct.getField());
		assertEquals(ConjunctRelation.GREATER_THAN,conjunct.getRelation());
		assertEquals("500",conjunct.getValue());
		
		conjunct = conjunction.getConjunctByField("b");
		assertEquals("b",conjunct.getField());
		assertEquals(ConjunctRelation.LESSER_THAN,conjunct.getRelation());
		assertEquals("300",conjunct.getValue());
	}
	
	@Test
	public void testParseWithSpaces(){
		String conjunctionStr = "(  (a > 500   )  ( b < 300   )  )";
		
		Conjunction conjunction = Conjunction.parseString(conjunctionStr);
		
		Conjunct conjunct = conjunction.getConjunctByField("a");
		assertEquals("a",conjunct.getField());
		assertEquals(ConjunctRelation.GREATER_THAN,conjunct.getRelation());
		assertEquals("500",conjunct.getValue());
		
		conjunct = conjunction.getConjunctByField("b");
		assertEquals("b",conjunct.getField());
		assertEquals(ConjunctRelation.LESSER_THAN,conjunct.getRelation());
		assertEquals("300",conjunct.getValue());
	}
}
