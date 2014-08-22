package org.probe.stats;

import java.util.List;

import org.probe.data.DataModel;

public class PriorCalculator {
	public static double calculatePriorForClass(DataModel dataModel, String forClass){
		List<String> classColumn = dataModel.getClassColumn();
		
		int numItemsWithClassValue = 0;
		for(String classItem : classColumn){
			if(classItem.equals(forClass)){
				numItemsWithClassValue++;
			}
		}
		
		return (double)numItemsWithClassValue / (double)classColumn.size();
	}
}
