package org.probe.stats;

import java.util.List;

import org.probe.data.DataAttribute;
import org.probe.data.DataModel;

public class MaximumLikelihoodCalculator {
	public static double caclulateMaximumLikelihood(DataModel model,
			int columnIndex, String valueInColumn, String valueInClass) {
		List<String> column = model.getColumnItemsByIndex(columnIndex);
		List<String> labels = model.getClassColumn();

		assert (column.size() == column.size());

		int numItemsInClass = 0;
		for (int index = 0; index < column.size(); index++) {
			if (column.get(index).equals(valueInColumn)
					&& labels.get(index).equals(valueInClass)) {
				numItemsInClass++;
			}
		}

		double maximumLikelihoodValue = (double) numItemsInClass
				/ (double) column.size();

		return maximumLikelihoodValue;
	}
	
	public static double caclulateMaximumLikelihood(DataModel model,
			DataAttribute attribute, String valueInColumn, String valueInClass) {
		int index = attribute.getAttributeIndex();
		return caclulateMaximumLikelihood(model,index,valueInColumn,valueInClass);
	}
}
