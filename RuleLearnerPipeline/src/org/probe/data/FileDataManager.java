package org.probe.data;


import org.probe.stats.structures.data.converters.input.TabCsvDataLoader;
import org.probe.test.rule.TestProperties;
import org.probe.data.DefaultDataModel;
import org.probe.algo.rule.LearnerParameters;
import org.probe.util.Arrays;

import org.probe.util.Util;



public class FileDataManager implements DataManager {

	@Override
	public DefaultDataModel getDataModel() {
		return dataModel;
	}
	public LearnerParameters getLearnerparams() {
		return learnParams;
	}

	public void loadFromFile(String fileName, String itemSeparator) throws Exception {
		TabCsvDataLoader tcsvl = new TabCsvDataLoader(fileName,null);
		String args[] = null;
		DefaultDataModel def = new DefaultDataModel();
		args = TestProperties.loadProperties("/RLParameters.properties");
		int indexLP = Arrays.indexOf(args, "-lp");
		int indexDP = Arrays.indexOf(args, "-dp");
		learnParams = new LearnerParameters(Arrays.subArray(args,
				indexLP + 1, indexDP));
		dataModel =tcsvl.loadData(itemSeparator);
		dataModel = dataModel.trainCV(learnParams.getNumFolds(), 0);
		if (!dataModel.isDiscretized() && dataModel.numContinuousAttributes() > 0) {
			Util.discDataModel(dataModel, learnParams.getDiscretizerIndex(),
					learnParams.getDiscretizerValue());
		}
		this.setDataModel(dataModel);
		this.setLearnerparams(learnParams);
	}
	public void setDataModel(DefaultDataModel dataModel) {
		this.dataModel = dataModel;
	}
	public void setLearnerparams(LearnerParameters learnParams) {
		this.learnParams = learnParams;
	}
	private DefaultDataModel dataModel = null;
	private LearnerParameters learnParams = null;
}
