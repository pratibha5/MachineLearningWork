package org.probe.algo;

import org.probe.data.dataset.DataModel;

public interface DataLearner {
	void setDataModel(DataModel dataModel);
	void runAlgo() throws Exception;
}
