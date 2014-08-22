package org.probe;

import org.probe.algo.rule.RuleLearner;
import org.probe.data.DataManager;
import org.probe.data.DataModel;
import org.probe.report.ReportManager;
import org.probe.rule.RuleModel;

public class AlgorithmManager {

	public AlgorithmManager(DataManager dataManager, RuleLearner ruleLearner,
			ReportManager reportManager) {
		this.dataManager = dataManager;
		this.ruleLearner = ruleLearner;
		this.reportManager = reportManager;
	}
	
	public void runAlgoOnData() throws Exception {
		DataModel dataModel = dataManager.getDataModel();
		
		ruleLearner.setDataModel(dataModel);
		ruleLearner.runAlgo();
	}
	
	public RuleModel getRuleModel() {
		if(ruleLearner.hasLearntRules()){
			return ruleLearner.getRuleModel();
		}
		else return EMPTY_RULE_MODEL;
	}
	
	private final DataManager dataManager;
	private final RuleLearner ruleLearner;
	private final ReportManager reportManager;
	
	public static final RuleModel EMPTY_RULE_MODEL = RuleModel.createEmptyRuleModel();
}
