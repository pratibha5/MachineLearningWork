package org.probe.algo;
import org.probe.data.DefaultDataModel;

public interface DataLearner {
	void setDataModel(DefaultDataModel dataModel);
	void runAlgo() throws Exception;
}
