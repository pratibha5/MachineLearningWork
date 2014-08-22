package org.probe.data.fold;

import java.util.List;

import org.probe.data.DataModel;

public interface FoldGenerator {
	List<DataModel> generateFolds(DataModel dataModel);
}
