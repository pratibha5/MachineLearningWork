package org.probe.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.probe.data.fold.FoldGenerator;
import org.probe.stats.structures.data.converters.input.TabCsvDataLoader;
import org.probe.stats.structures.learner.attribute.AttributeList;
import org.probe.test.rule.TestProperties;
import org.probe.algo.SAL;
import org.probe.algo.rule.LearnerParameters;
import org.probe.algo.rule.RuleGenerator;
import org.probe.util.Arrays;
import org.probe.util.RuleList;

import org.probe.rule.RuleModel;
import data.dataset.Attribute;

public class FileDataManager implements DataManager {

	@Override
	public DataModel getDataModel() {
		return dataModel;
	}

	@Override
	public List<DataModel> createFolds(FoldGenerator foldGenerator) {
		if(dataModel == null)
			return null;
		
		List<DataModel> foldDataModels = foldGenerator.generateFolds(dataModel);
		
		return foldDataModels;
	}

	public void loadFromFile(String fileName, String itemSeparator) throws Exception {
		TabCsvDataLoader tcsvl = new TabCsvDataLoader(fileName,null);
		ArrayList<Attribute> data;
		String args[] = null;
		args = TestProperties.loadProperties("/RLParameters.properties");
		int indexLP = Arrays.indexOf(args, "-lp");
		int indexDP = Arrays.indexOf(args, "-dp");
		data =tcsvl.loadData(itemSeparator);
			 AttributeList atl = new AttributeList();
			RuleGenerator rg;
				atl.defineAttributes(data);
				LearnerParameters learnParams;
				learnParams = new LearnerParameters(Arrays.subArray(args,
						indexLP + 1, indexDP));
				rg = new SAL(atl, learnParams);
				RuleList rules = rg.generateRules();
				RuleModel rm = new RuleModel(learnParams, rules);
		/*BufferedReader br = new BufferedReader(new FileReader(fileName));

		dataModel = new DefaultDataModel();
		dataModel.parseAndAddHeader(br.readLine(), itemSeparator);

		String line = "";
		while ((line = br.readLine()) != null) {
			dataModel.parseAndAddRow(line, itemSeparator);
		}

		br.close();*/
	}
	
	private DataModel dataModel = null;
}
