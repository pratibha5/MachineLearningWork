/*
 * @(#)FreqBased.java    1.2 2002/11/21
 */

package org.probe.stats.structures.cf;

import org.probe.data.DataModel;
import org.probe.stats.structures.learner.attribute.LearnerAttribute;
import org.probe.rule.Rule;

/**
 * A certainty factor fucntion that computes the single-tailed P-value to the
 * right. That is, the LHS of the rule is positively correlated with the RHS.
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 * 
 * Formatted code and comments. Added comments.
 * 
 * @version 1.2 2002/11/21
 * @author Philip Ganchev
 */

public class PValueRight extends PValue {

	public PValueRight() {
		super("P-Value: right tail", "");
	}

	/**
	 * Returns the value of this certainty factor function for <code>r</code>
	 * evaluated as the single-tailed P-value to the right. That is, the LHS of
	 * <code>r</code> is positively correlated with the RHS.
	 * 
	 * @param r
	 *            the rule whose certainty factor value to compute
	 * @return the right-tail P-value of the RHS of <code>r</code>
	 */
	/*
	 * Use this table as a reference for the implementation 
	 * -------------- 
	 * | tp | fp | r1 
	 * -------------- 
	 * | fn | tn | r2 
	 * -------------- 
	 * c1 | c2 | N
	 */
	public double getCf(Rule r, LearnerAttribute trg, DataModel d) {
		final int c1 = r.getPos();
		final int c2 = r.getNeg();
		int tp = r.getTruePos();
		int fp = r.getFalsePos();
		int fn = c1 - tp;
		int tn = c2 - fp;
		final int r2 = tn + fn;
		final int r1 = tp + fp;

		double sum; // The eventual p-value

		// Calculate the p-value cutoff.
		// The divisor & rowFactorial will not change, so do not need to be
		// recalculated in other tables.
		final double N_FACTORIAL = factln(r1 + r2);
		double dividend = factln(c1) + factln(c2) + factln(r1) + factln(r2);
		double divisor = factln(tp) + factln(fp) + factln(tn) + factln(fn)
				+ N_FACTORIAL;
		final double CUTOFF = Math.exp(dividend - divisor);
		sum = CUTOFF;

		// Calculate all the conditional probability for all other tables to
		// the right and add them to the p-value to be reported.
		int up = tp;
		int down = fp;
		int up2 = tn;
		int down2 = fn;

		while (down > 0 && down2 > 0) {
			up++;
			up2++;
			down--;
			down2--;
			divisor = N_FACTORIAL + factln(up) + factln(up2) + factln(down)
					+ factln(down2);
			sum += Math.exp(dividend - divisor);
		}

		// Correct for rounding error. Sum could still be inaccurate because
		// of rounding error, but we aren't that picky.
		if (sum > 1.0)
			sum = 1.0;

		return sum;
	}
}