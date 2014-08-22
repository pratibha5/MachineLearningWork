package org.probe.algo.rule;

import org.probe.algo.DataLearner;
import org.probe.rule.RuleModel;

public interface RuleLearner extends DataLearner {
	boolean hasLearntRules();
	RuleModel getRuleModel();
}
