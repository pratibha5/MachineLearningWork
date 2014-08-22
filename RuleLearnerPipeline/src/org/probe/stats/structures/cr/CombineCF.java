/**
 * 
 */
package org.probe.stats.structures.cr;

import java.util.ArrayList;

import data.dataset.DataModel;
import org.probe.rule.Rule;
import org.probe.util.RuleList;
import org.probe.util.Arrays;


/**
 * @author Jonathan
 * 
 */
public class CombineCF extends ConflictResolver {

	/**
	 * @param name
	 * @param description
	 */
	public CombineCF(String name, String description) {
		super(name, description);
		// TODO Auto-generated constructor stub
	}

	public CombineCF() {
		super("Combine CF", "Combines CF using method within meta-dendral");
	}

	public int predict(RuleList rules, DataModel d) {
		ArrayList[] al = new ArrayList[d.classAttribute().numValues()];
		for (int i = 0; i < al.length; i++)
			al[i] = new ArrayList<Rule>();

		for (int r = 0; r < rules.size(); r++) {
			Rule nr = ((Rule) rules.get(r));
			al[nr.getPredictedValueIndex()].add(nr);
		}
		int predInd = -1;
		double cf = 0;
		for (int i = 0; i < al.length; i++) {
			double currCF = 0;
			if (al[i].size() > 0) {
				currCF = ((Rule) al[i].get(0)).getCf();
				for (int r = 1; r < al[i].size(); r++)
					currCF = currCF + ((Rule) al[i].get(r)).getCf()
							* (1 - currCF);
			}
			if (currCF > cf) {
				cf = currCF;
				predInd = i;
			}
		}
		certainty = cf;
		usedRules = rules;
		return predInd;
	}
}
