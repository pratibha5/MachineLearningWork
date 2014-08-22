package org.probe.test.algo;

import org.junit.Test;
import org.probe.algo.rule.ID3RuleLearner;
import org.probe.algo.rule.RuleLearner;
import org.probe.data.DataModel;
import org.probe.data.FileDataManager;
import org.probe.data.FileType;
import org.probe.data.discretize.Discretizer;
import org.probe.data.discretize.EqualFrequencyBinGenerator;
import org.probe.rule.RuleModel;

public class TestRuleLearningAlgorithm {
	
	@Test
	public void test() throws Exception{
		FileDataManager dataManager = new FileDataManager();
		dataManager.loadFromFile("Test//data.txt", FileType.TSV.getSeparator());
		
		DataModel dataModel = dataManager.getDataModel();
		
		RuleLearner ruleLearner = new ID3RuleLearner();
		ruleLearner.setDataModel(dataModel);
		ruleLearner.runAlgo();
		
		RuleModel ruleModel = ruleLearner.getRuleModel();
		
		//assert
		//ruleModel.containsField("");
	}
	
	public void test2() throws Exception{
		FileDataManager dataManager = new FileDataManager();
		dataManager.loadFromFile("Test//sampleLearningData.csv", FileType.CSV.getSeparator());
		
		DataModel dataModel = dataManager.getDataModel();
		
		Discretizer discretizer = new Discretizer(new EqualFrequencyBinGenerator());
		DataModel discDataModel = discretizer.discretize(dataModel);
		//discDataModel.print();
		
		RuleLearner ruleLearner = new ID3RuleLearner();
		ruleLearner.setDataModel(discDataModel);
		ruleLearner.runAlgo();
		
		RuleModel ruleModel = ruleLearner.getRuleModel();
		
		//assert
		//ruleModel.containsField("");
	}
}
