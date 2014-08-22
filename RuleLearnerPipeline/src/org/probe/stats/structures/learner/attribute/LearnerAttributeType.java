/*
 * LearnerAttributeType.java
 *
 * Created on June 27, 2005, 1:09 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.probe.stats.structures.learner.attribute;

/**
 * Used to store the different attribute Types
 * 
 * @author Jonathan Lustgarten
 */
public enum LearnerAttributeType {
	Continuous("Continuous", "in"), Discrete("Discrete", "="), Set("Set",
			"includes"), Invalid("Invalid", "unknown relation");

	private String rep;
	private String des;

	LearnerAttributeType(String txt, String desc) {
		rep = txt;
		des = desc;
	}

	String getTypeTxt() {
		return rep;
	}

	String getDescripTxt() {
		return des;
	}

	boolean equals(String tRep) {
		return tRep.equalsIgnoreCase(rep);
	}
}
