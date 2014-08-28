package org.probe.algo.rule;

import org.probe.algo.SAL;
import org.probe.rule.RuleModel;
import org.probe.stats.structures.learner.attribute.AttributeList;
import org.probe.util.RuleList;
import org.probe.data.DefaultDataModel;

public class SALRuleLearner implements RuleLearner{
	
	@Override
	public void setDataModel(DefaultDataModel dataModel) {
		this.dataModel = dataModel;
	}
	public void setLearnerparams(LearnerParameters learnerParams) {
		this.learnParams = learnerParams;
	}
	public void setRuleModel(RuleModel ruleModel) {
		this.ruleModel = ruleModel;
	}
	@Override
	public void runAlgo() throws Exception {
			AttributeList atl = new AttributeList();
			RuleGenerator rg;
			atl.defineAttributes(dataModel);
			rg = new SAL(atl, learnParams);
			rg.setDataModel(dataModel);
			RuleList rules = rg.generateRules();
			ruleModel = new RuleModel(learnParams, rules);
			this.setRuleModel(ruleModel);
	}

	@Override
	public boolean hasLearntRules() {
		return ruleModel != null;
	}

	@Override
	public RuleModel getRuleModel() {
		return ruleModel;
	}
	private DefaultDataModel dataModel = null;
	private RuleModel ruleModel = null;
	private LearnerParameters learnParams = null;
}
