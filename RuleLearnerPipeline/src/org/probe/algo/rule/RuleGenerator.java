package org.probe.algo.rule;

import org.probe.algo.rule.LearnerParameters;
import org.probe.data.DefaultDataModel;
import org.probe.util.RuleList;

public interface RuleGenerator {
	public RuleList generateRules();

	public void setDataModel(DefaultDataModel trnData);

	public void setParameters(LearnerParameters p);

	public int ruleCount();
}
