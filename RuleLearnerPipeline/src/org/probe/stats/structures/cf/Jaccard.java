/*
 * Jaccard.java
 *
 * Created on May 30, 2006, 5:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.probe.stats.structures.cf;

import java.io.*;
import java.lang.*;

import org.probe.stats.structures.cf.*;
import data.dataset.DataModel;
import org.probe.stats.structures.learner.attribute.LearnerAttribute;
import org.probe.rule.*;

/**
 * The Jaccard Measure
 * 
 * @author Jonathan Lustgarten
 */
public class Jaccard extends CertaintyFactor {
	/** Creates a new instance of Jaccard */
	public Jaccard() {
		super("Jaccard", "TP / (Pos + FN)");
	}

	/**
	 * @param r
	 *            the rule whose certainty factor value to compute
	 * @return
	 * 
	 * <pre>
	 * TP / (FP + TP + FN)
	 * </pre>
	 */
	public double getCf(Rule r, LearnerAttribute trg, DataModel d) {
		int tp = r.getTruePos();
		int fp = r.getFalsePos();
		int fn = r.getPos() - tp;
		int tn = r.getNeg() - fp;

		return ((tp + fp + fn) == 0) ? 0 : ((double) tp) / (tp + fp + fn);
	}
}
