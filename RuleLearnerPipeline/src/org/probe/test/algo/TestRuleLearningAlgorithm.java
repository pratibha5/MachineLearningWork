package org.probe.test.algo;

import org.junit.Test;
import org.probe.algo.rule.LearnerParameters;
import org.probe.algo.rule.RuleLearner;
import org.probe.algo.rule.SALRuleLearner;
import org.probe.data.DefaultDataModel;
import org.probe.data.FileDataManager;
import org.probe.data.FileType;
import org.probe.rule.RuleModel;

public class TestRuleLearningAlgorithm {
	
	@Test
	public void test() throws Exception{
		FileDataManager dataManager = new FileDataManager();
		dataManager.loadFromFile("Test//data.txt", FileType.TSV.getSeparator());
		
		DefaultDataModel dataModel = dataManager.getDataModel();
		LearnerParameters learnerParams= dataManager.getLearnerparams();
		SALRuleLearner ruleLearner = new SALRuleLearner();
		ruleLearner.setDataModel(dataModel);
		ruleLearner.setLearnerparams(learnerParams);
		ruleLearner.runAlgo();
		if (ruleLearner.hasLearntRules()){
			RuleModel ruleModel = ruleLearner.getRuleModel();
			System.out.println("Learned " + ruleModel.getRules().size() + " rules\n");
			StringBuffer buf = new StringBuffer();
			buf.append(ruleModel.toString() + "\n");
			System.out.println(buf);
		}
		
	}
	
	public void test2() throws Exception{
		FileDataManager dataManager = new FileDataManager();
		dataManager.loadFromFile("Test//sampleLearningData.csv", FileType.CSV.getSeparator());
		
		DefaultDataModel dataModel = dataManager.getDataModel();
		RuleLearner ruleLearner = new SALRuleLearner();
		ruleLearner.setDataModel(dataModel);
		ruleLearner.runAlgo();
		
		RuleModel ruleModel = ruleLearner.getRuleModel();
	}
}
