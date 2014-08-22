/*
 * @(#)Conjunct.java    1.1 2002/01/21
 */

package rule;

import java.util.Scanner;
import java.io.*;
import data.dataset.*;
import structures.learner.attribute.*;
import rule.*;

/**
 * An attribute/value pair
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to make use of new code organization
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 */

public class Conjunct {
	private VHierarchyNode value;
	private String attributeName;
	private int index = -1;

	public Conjunct() {
	}

	public Conjunct(LearnerAttribute a, VHierarchyNode v, int ix) {
		value = v;
		attributeName = v.getAttributeName();
		index = ix;
	}

	public Conjunct(Scanner scanner) throws IOException {
		scanner.useDelimiter("\\s*\\(\\s*|\\s*\\)\\s*");
		String attValPair = scanner.next();
		System.out.println(attValPair);
		if (scanner.ioException() != null) {
			throw new IOException(scanner.ioException());
		}
	}
	
	public void setAttribute(LearnerAttribute a) {
		attributeName = a.getName();
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setValue(VHierarchyNode v, int mkI) {
		value = v;
		index = mkI;
		attributeName = v.getAttributeName();
	}

	public VHierarchyNode getValue() {
		return value;
	}

	public boolean matchesValue(Object v) {
		return value.equals(v) || value.inSubtree(v);
	}

	public boolean matchesDatum(Dataset d, int instIx) {
		try {
			return matchesValue(d.getMatchingValue(attributeName, instIx));
		} catch (AttributeDoesNotExistException e) {
			return false;
		} catch (InstanceNotFoundException e) {
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
			System.exit(1);
		} catch (ValueNotFoundException e) {
			return false;
		}
		return false;
	}

	public boolean equals(Object c) {
		return c instanceof Conjunct && value.equals(((Conjunct) c).getValue())
				&& attributeName.equals(((Conjunct) c).getAttributeName());
	}

	public String toString() {
		return "(" + attributeName + " = " + value.toString() + ")";
	}

	public boolean matches(LearnerAttribute a) {
		return attributeName.equalsIgnoreCase(a.getName());
	}
	
	public boolean matches(String attName) {
		return attributeName.equalsIgnoreCase(attName);
	}

	public int getIndex() {
		return index;
	}

	public int getValueIndex(VHierarchyNode v) {
		return value.getValueIndex(v);
	}
}