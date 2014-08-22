package org.probe.algo.rule;

import org.probe.algo.rule.LearnerParameters;
import data.dataset.DataModel;
import org.probe.util.RuleList;

public interface RuleGenerator {
	public RuleList generateRules();

	public void setDataModel(DataModel trnData);

	public void setParameters(LearnerParameters p);

	public int ruleCount();
}
