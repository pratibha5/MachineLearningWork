package org.probe.data;

import java.util.List;

import org.probe.data.fold.FoldGenerator;

public interface DataManager {
	DataModel getDataModel();

	List<DataModel> createFolds(FoldGenerator foldGenerator);
}
