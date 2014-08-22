/*
 * @(#)LaplaceDepth.java    1.2 2002/11/21
 */

package org.probe.stats.structures.cf;

import data.dataset.DataModel;
import org.probe.stats.structures.learner.attribute.LearnerAttribute;
import org.probe.rule.Rule;

/**
 * A certainty factor based on an extension to laplace depth with bias against
 * longer rules. Laplace depth is calculated as
 * 
 * <pre>
 * (TP + K * MU) / (TP + FP + K)
 * </pre>, where
 * 
 * <pre>
 *          MU = TP/(TP+TN)
 *          K = 1 + # conjuncts in the rule
 * </pre>.
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to account for new code organization Changed to use TP and FP (and
 * TotalNeg) in rules
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 * 
 * Added comments
 * 
 * @version 1.2 2002/11/21
 * @author Philip Ganchev
 */
public class LaplaceDepth extends CertaintyFactor {
	public LaplaceDepth() {
		super(
				"Laplace + bias to short rules",
				"(TP + k * m) / (TP + FP + k), where k = 1 + #Conjuncts, m = Pos / (Pos + Neg)");
	}

	/**
	 * @param r
	 *            the rule whose certainty factor value to compute
	 * @return
	 * <pre>
	 * (TP + k * m) / (TP + FP + k)
	 * </pre>, where
	 * <pre>
	 *          m = TP / (TP + TN)
	 *          k = 1 + # conjuncts in the rule
	 * </pre>
	 */
	public double getCf(Rule r, LearnerAttribute trg, DataModel d) {
		/* Variance parameter on prior distribution of rule accuracies */
		double k;
		int tp = r.getTruePos();
		int fp = r.getFalsePos();
		int totP = r.getPos();
		int totN = r.getNeg();

		/* Positive class prior */
		double m = (totP + totN) > 0 ? ((double) totP) / (totP + totN) : 0;

		k = r.getConjunctCount() + 1;
		return (tp + k * m) / (tp + fp + k);
	}
}
