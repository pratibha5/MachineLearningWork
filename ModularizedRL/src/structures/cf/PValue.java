/*
 * @(#)FreqBased.java    1.1 2002/01/21
 */

package structures.cf;

import data.dataset.Dataset;
import structures.learner.attribute.LearnerAttribute;
import rule.Rule;

/**
 * A certainty factor based on P-Value.
 * 
 * @version 1.1 02/01/21
 * @author Will Bridewell
 */

abstract class PValue extends CertaintyFactor {
	public PValue() {
		super("P-Value", "Abstract - do NOT add to the list!");
	}

	public PValue(String name, String description) {
		super(name, description);
	}

	public abstract double getCf(Rule r, LearnerAttribute trg, Dataset d);

	/* These methods were originally pulled from Result.java to be used here. */

	protected static double factln(double n) {
		return gammln(n + 1.0);
	}

	protected static double gammln(double d) {
		double stp = 2.50662827465;
		double x = d - 1.0;
		double tmp = x + 5.5;
		double tmp1 = (x + .5) * Math.log(tmp) - tmp;
		double ser = 1 + (76.18009173 / (x + 1.0)) + (-86.50532033 / (x + 2.0))
				+ (24.01409822 / (x + 3.0)) + (-1.231739516 / (x + 4.0))
				+ (.0012085803 / (x + 5.0)) + (-0.00000536382 / (x + 6.0));

		return tmp1 + Math.log(stp * ser);
	}
}
