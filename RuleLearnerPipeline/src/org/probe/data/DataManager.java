package org.probe.data;

import org.probe.algo.rule.LearnerParameters;
import org.probe.data.DataModel;

public interface DataManager {
	DataModel getDataModel();

	LearnerParameters getLearnerparams();
}
