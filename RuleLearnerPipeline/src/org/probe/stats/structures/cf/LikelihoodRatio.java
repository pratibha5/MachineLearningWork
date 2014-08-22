/**
 * 
 */
package org.probe.stats.structures.cf;

import data.dataset.DataModel;
import org.probe.stats.structures.learner.attribute.LearnerAttribute;
import org.probe.rule.Rule;

/**
 * @author Jonathan
 * 
 */
public class LikelihoodRatio extends CertaintyFactor {

	public LikelihoodRatio() {
		super("LikelihoodRatio",
				"The likelihood ratio, "
						+ "computed using priors and"
						+ " the conditional probability");
	}

	/**
	 * @param name
	 * @param description
	 */
	public LikelihoodRatio(String name, String description) {
		super(name, description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see corefiles.structures.cf.CertaintyFactor#getCf(corefiles.structures.learner.rule.Rule,
	 *      corefiles.structures.learner.attribute.LearnerAttribute)
	 */
	@Override
	public double getCf(Rule r, LearnerAttribute trg, DataModel d) {
		return (r.getTruePos())
				/ (r.getPos() - (double) r.getTruePos());
	}
}
