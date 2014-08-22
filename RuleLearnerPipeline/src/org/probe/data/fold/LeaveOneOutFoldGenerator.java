package org.probe.data.fold;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.probe.data.DataModel;
import org.probe.data.DefaultDataModel;

public class LeaveOneOutFoldGenerator implements FoldGenerator {

	@Override
	public List<DataModel> generateFolds(DataModel dataModel) {
		Random random = new Random();
		int randomIndex = random.nextInt(dataModel.size());
		
		DataModel valDataModel = createValidationDataModel(randomIndex,
				dataModel);
		DataModel trainDataModel = createTrainingDataModel(randomIndex,
				dataModel);

		List<DataModel> folds = new LinkedList<DataModel>();
		folds.add(trainDataModel); 
		folds.add(valDataModel);
		
		return folds;
	}

	private DataModel createValidationDataModel(int rowIndex,
			DataModel dataModel) {
		DataModel valDataModel = new DefaultDataModel();

		List<String> items = dataModel.getRow(rowIndex);
		valDataModel.addItemsAsRow(items);
		valDataModel.setDataSetAsValidation();

		return valDataModel;
	}

	private DataModel createTrainingDataModel(int randomIndex,
			DataModel dataModel) {
		DataModel trainDataModel = new DefaultDataModel();

		for (int x = 0; x < dataModel.size(); x++) {
			if (x == randomIndex)
				continue;

			List<String> items = dataModel.getRow(x);
			trainDataModel.addItemsAsRow(items);
		}
		trainDataModel.setDataSetAsTraining();
		
		return trainDataModel;
	}

}
