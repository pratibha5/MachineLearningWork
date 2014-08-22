package org.probe.algo.rule;

import java.util.ArrayList;

import org.probe.algo.SAL;
import org.probe.data.dataset.DefaultDataModel;
import org.probe.data.FileType;
import org.probe.rule.RuleModel;
import org.probe.stats.structures.data.converters.input.TabCsvDataLoader;
import org.probe.stats.structures.learner.attribute.AttributeList;
import org.probe.test.rule.TestProperties;
import org.probe.util.Arrays;
import org.probe.util.RuleList;
import org.probe.data.dataset.Attribute;
import org.probe.data.dataset.DataModel;

public class SALRuleLearner implements RuleLearner{
	
	@Override
	public void setDataModel(DataModel dataModel) {
		// TODO Auto-generated method stub
		this.dataModel = dataModel;
	}

	@Override
	public void runAlgo() throws Exception {
		// TODO Auto-generated method stub
		DataModel data = new DefaultDataModel();
		TabCsvDataLoader tcsvl = new TabCsvDataLoader("Test//data.txt",FileType.TSV.getSeparator());
		String args[] = null;
		args = TestProperties.loadProperties("/RLParameters.properties");
		int indexLP = Arrays.indexOf(args, "-lp");
		int indexDP = Arrays.indexOf(args, "-dp");
		data =tcsvl.loadData(FileType.TSV.getSeparator());
			 AttributeList atl = new AttributeList();
			RuleGenerator rg;
				atl.defineAttributes(data);
				LearnerParameters learnParams;
				learnParams = new LearnerParameters(Arrays.subArray(args,
						indexLP + 1, indexDP));
				rg = new SAL(atl, learnParams);
				RuleList rules = rg.generateRules();
				RuleModel rm = new RuleModel(learnParams, rules);
		
	}

	@Override
	public boolean hasLearntRules() {
		// TODO Auto-generated method stub
		return ruleModel != null;
	}

	@Override
	public RuleModel getRuleModel() {
		// TODO Auto-generated method stub
		return ruleModel;
	}
	private DataModel dataModel = null;
	private RuleModel ruleModel = null;
}
