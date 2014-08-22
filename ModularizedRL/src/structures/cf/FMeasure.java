package structures.cf;

import data.dataset.Dataset;
import structures.learner.attribute.LearnerAttribute;
import rule.Rule;

/**
 * @author Jonathan
 */
public class FMeasure extends CertaintyFactor {

	/**
	 * @param name
	 * @param description
	 */
	public FMeasure(String name, String description) {
		super(name, description);
	}

	public FMeasure() {
		super("F-Measure", "2 * precision * recall / (precision + recall)");
	}

	/**
	 * Returns the F-Measure of <code>r</code>.
	 * 
	 * @param r
	 *            the rule whose certainty factor value to compute
	 * @return
	 * 
	 * <pre>
	 * 2 * precision * recall / (precision + recall)
	 * </pre>
	 */
	public double getCf(Rule r, LearnerAttribute targ, Dataset d) {
		int tp = r.getTruePos();
		int fp = r.getFalsePos();
		int fn = r.getPos() - tp;
		int tn = r.getNeg() - fp;
		if (tp == 0 && fp == 0 && fn == 0)
			return 0;
		double prec = (double) tp / ((double) (tp + fp));
		double recall = (double) tp / ((double) (tp + fn));
		return 2.0 * prec * recall / (prec + recall);
	}
}
