package org.probe.stats;

import java.util.List;

import org.probe.data.DataModel;

public class EntropyCalculator {
	public static double calculateEntropy(DataModel dataModel){
		List<String> classLabels = dataModel.getClassLabels();
		
		double entropyValue = 0.;
		for(String classLabel : classLabels){
			double classPrior = PriorCalculator.calculatePriorForClass(dataModel, classLabel);
			double logClassPrior = Math.log(classPrior);
			
			entropyValue += (classPrior * logClassPrior);
		}
		
		return (-1.0 * entropyValue);	
	}
}
