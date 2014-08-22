package structures.learner;

/**
 * A class used to sort rules in REVERSE natural order
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

public class RuleDataComparator implements java.util.Comparator {
	public int compare(Object a, Object b) {

		if (!(a instanceof RuleData) || !(b instanceof RuleData))
			return 0;

		double worthA = ((RuleData) a).rule.getWorth();
		double worthB = ((RuleData) b).rule.getWorth();

		if (worthA < worthB)
			return 1;

		if (worthA > worthB)
			return -1;

		double tpA = ((RuleData) a).rule.getTruePos();
		double tpB = ((RuleData) b).rule.getTruePos();
		return (tpA < tpB) ? 1 : (tpA > tpB) ? -1 : 0;
	}
}
