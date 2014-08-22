package org.probe.data;

import java.util.List;

import org.probe.algo.rule.id3.TreeNodePayload;

public interface DataModel extends TreeNodePayload {

	void parseAndAddHeader(String header, String separator);
	void parseAndAddRow(String rowStr, String separator);
	void addItemsAsRow(List<String> items);
	void addItemsAsRow(String[] items);
	
	void setAttributes(List<DataAttribute> header);

	DataAttribute getInstanceAttribute();
	DataAttribute getClassAttribute();
	String getClassLabelForRow(int row);
	List<String> getHeader();
	
	List<String> getRow(int row);
	List<Integer> getAllValidIndexes();
	boolean hasRowIndex(int index);
	
	List<String> getColumnItemsByAttribute(DataAttribute attribute);
	List<String> getColumnItemsByIndex(int columnIndex);
	String getItemAt(int row, int column);
	List<String> getClassColumn();
	List<String> getClassLabels();
	
	List<DataAttribute> getAttributes();
	
	int size();
	void clear();
	void print();
	
	boolean isTrainData();
	void setDataSetAsTraining(); 
	boolean isValidationData();
	void setDataSetAsValidation();
	
	boolean doesNotContain(DataModel otherDataModel);
	int getNumRows();
	int getNumCols();
}
