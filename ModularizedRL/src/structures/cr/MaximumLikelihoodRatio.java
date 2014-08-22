/**
 * 
 */
package structures.cr;

import data.dataset.Dataset;
import rule.Rule;
import rule.RuleList;
import util.Arrays;
import util.MathUtil;

/**
 * @author Jonathan
 * 
 */
public class MaximumLikelihoodRatio extends ConflictResolver {

	public MaximumLikelihoodRatio() {
		super("Maximum LikelihoodRatio",
				"Uses the likelihood of the rules to calculate the most likely class.");
	}

	/**
	 * @param name
	 * @param description
	 */
	public MaximumLikelihoodRatio(String name, String description) {
		super(name, description);
	}

	@Override
	public int predict(RuleList rules, Dataset d) {
		double[] classVals = Arrays.init(new double[d
				.classAttribute().numValues()], 0);
		// For each class, sum the worths of matching rules predicting that class 
		for (int i = 0; i < rules.size(); i++) {
			Rule r = (Rule) rules.get(i);
			int ind = r.getPredictedValueIndex();
			if (ind > -1)
				classVals[ind] += r.getWorth();
		}
		for (int i = 0; i < classVals.length; i++) {
			double p = Math.pow(10, classVals[i]);
			classVals[i] = p / (1 + p);
		}
		double sum = MathUtil.sumArray(classVals);
		for (int i = 0; i < classVals.length; i++)
			classVals[i] = classVals[i] / sum;

		int mxInd = 0;
		for (int i = 1; i < classVals.length; i++) {
			if (classVals[i] > classVals[mxInd])
				mxInd = i;
		}
		certainty = classVals[mxInd] / (1 - classVals[mxInd]);
		return mxInd;
	}
}
