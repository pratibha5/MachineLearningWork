package org.probe.data.discretize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.probe.data.DataAttribute;
import org.probe.data.DataModel;

public class EqualFrequencyBinGenerator implements BinGenerator {

	@Override
	public List<String> generate(DataModel dataModel, DataAttribute attribute,
			int numBins) throws Exception {
		if (numBins < 2)
			throw new Exception(
					"Discretizer : Number of bins must be equal to or more than 2");

		List<String> columnItems = dataModel.getColumnItemsByAttribute(attribute);
		List<String> columnItemCopy = createShallowCopy(columnItems);
		Collections.sort(columnItemCopy);
		
		int divIndex = columnItemCopy.size() / numBins;

		List<String> ranges = new ArrayList<String>(numBins);
		for (int rangeIndex = 0; rangeIndex < numBins; rangeIndex++) {
			String range = getRange(columnItemCopy, divIndex, rangeIndex);
			ranges.add(range);
		}

		return ranges;
	}

	private List<String> createShallowCopy(List<String> columnItems) {
		List<String> copyColumnItems = new LinkedList<String>();
		for(String columnItem : columnItems){
			copyColumnItems.add(columnItem);
		}
		return copyColumnItems;
	}

	private String getRange(List<String> columnItems, int divIndex,
			int rangeIndex) {
		int startIndex = divIndex * rangeIndex;
		int endIndex = startIndex + divIndex;

		StringBuilder sb = new StringBuilder();
		if (isFirstRange(startIndex)) {
			String startRange = "-Inf";
			String endRange = columnItems.get(endIndex);
			formatAsRange(sb, startRange, endRange);
		} else if (isLastRange(endIndex, columnItems.size())) {
			String startRange = columnItems.get(startIndex);
			String endRange = "Inf";
			formatAsRange(sb, startRange, endRange);
		} else {
			String startRange = columnItems.get(startIndex);
			String endRange = columnItems.get(endIndex);
			formatAsRange(sb, startRange, endRange);
		}

		return sb.toString();
	}

	private boolean isFirstRange(int startIndex) {
		return startIndex == 0;
	}

	private boolean isLastRange(int endIndex, int numitems) {
		boolean isEven = numitems % 2 == 0;
		if (isEven)
			return endIndex >= numitems;
		else
			return endIndex >= numitems-1;
	}

	private void formatAsRange(StringBuilder sb, String startRange,
			String endRange) {
		sb.append("[").append(startRange).append(",").append(endRange)
				.append("]");
	}

	@Override
	public List<String> generate(DataModel dataModel, DataAttribute attribute)
			throws Exception {
		return generate(dataModel, attribute, DEFAULT_NUM_BINS);
	}

	public static final int DEFAULT_NUM_BINS = 2;
}
