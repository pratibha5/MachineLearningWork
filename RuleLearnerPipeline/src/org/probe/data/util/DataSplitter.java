package org.probe.data.util;

import java.util.List;

import org.probe.data.DataAttribute;
import org.probe.data.DataModel;
import org.probe.data.DataSubModel;
import org.probe.util.UniqueItemFinder;

public class DataSplitter {
	public static DataSubModel[] splitOnAttribute(DataModel dataModel, DataAttribute attribute) throws Exception{
		List<String> columnVals = dataModel.getColumnItemsByAttribute(attribute);
		List<String> columnBins = UniqueItemFinder.findUnqiueItems(columnVals);
		
		DataSubModel[] subModels = new DataSubModel[columnBins.size()];
		int subModelCount = 0;
		for(String columnBin : columnBins){
			DataSubModel subModel = new DataSubModel(dataModel);
			subModel.applyFilterOnAttributeValue(attribute, columnBin);
			subModels[subModelCount++] = subModel;
		}

		return subModels;
	}
}
