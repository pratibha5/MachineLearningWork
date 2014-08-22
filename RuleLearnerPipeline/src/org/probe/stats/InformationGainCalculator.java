package org.probe.stats;

import org.probe.data.DataModel;
import org.probe.data.DataSubModel;

public class InformationGainCalculator {

	public static double calculateInformationGainOnSubModels(
			DataModel dataModel, DataSubModel[] subModels) {
		double originalEntropy = EntropyCalculator.calculateEntropy(dataModel);
		
		double subModelEntropy = 0.;
		for(DataSubModel subModel : subModels){
			double currentEntropy = EntropyCalculator.calculateEntropy(subModel);
			subModelEntropy += currentEntropy;
		}
		
		double informationGain = originalEntropy - subModelEntropy;
		return informationGain;
	}
}
