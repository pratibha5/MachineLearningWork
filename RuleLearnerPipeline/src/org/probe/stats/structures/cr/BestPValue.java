/*
 * @(#)BestPValue.java 0.9 2002/10/28
 *
 * <h1>JavaRL</h1>
 * A rule induction program for knowledge discovery<br>
 * Copyright (C) 2002
 * Intelligent Systems Laboratory, University of Pittsburgh
 */

package org.probe.stats.structures.cr;

import org.probe.stats.structures.cf.*;
import data.dataset.DataModel;
import org.probe.rule.*;

/**
 * An evidence gathering method that predicts data as the value predicted by the
 * rule with the lowest p-value.
 * 
 * @version 0.9
 * @author Jeremy Ludwig, Will Bridewell, Eric Williams, Philip Ganchev
 *         (philip@cs.pitt.edu)
 */
public class BestPValue extends ConflictResolver {

	public BestPValue() {
		super("Lowest p-value rule",
				"Class predicted by the rule with the lowest p-value");
	}

	public int predict(RuleList rules, DataModel d) {
		int nReturn = -1;

		Rule r, singleBest;
		singleBest = (Rule) rules.get(0);
		for (int y = 1; y < rules.size(); y++) {
			r = (Rule) rules.get(y);
			if (r.getPValue() < singleBest.getPValue()) {
				if (r.getWorth() > singleBest.getWorth()) {
					singleBest = r;
				}
			}
		}
		certainty = singleBest.getCf();
		nReturn = singleBest.getPredictedValueIndex();
		(usedRules = new RuleList()).add(singleBest);
		return nReturn;
	}
}
