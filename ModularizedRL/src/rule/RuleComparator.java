package rule;

import rule.Rule;

/**
 * RuleComparator orders rules in by decreasing worth, then by decreasing true
 * positive count on the test set.
 * 
 * <p>
 * Title: JavaRL
 * </p>
 * <p>
 * Description: rule induction for knowledge discovery
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: Intelligent Systems Laboratory at University of Pittsburgh
 * </p>
 * 
 * @author Jeremy Ludwig, Will Bridewell, Eric Williams
 * @version 0.8 (new version numbering system)
 */
public class RuleComparator implements java.util.Comparator {
	public final int compare(Object a, Object b) {

		if (!((a instanceof Rule) && (b instanceof Rule)))
			return 0;

		double worthA = ((Rule) a).getWorth();
		double worthB = ((Rule) b).getWorth();

		if (worthA < worthB)
			return 1;
		else if (worthA > worthB)
			return -1;
		else {
			double tpA = ((Rule) a).getTruePos();
			double tpB = ((Rule) b).getTruePos();
			return (tpA < tpB) ? 1 : (tpA > tpB) ? -1 : 0;
		}
	}
}
