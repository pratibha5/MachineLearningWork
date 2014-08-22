package org.probe.data;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.probe.util.UniqueItemFinder;

public class DataSubModel implements DataModel {
	public DataSubModel(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	@Override
	public DataAttribute getInstanceAttribute() {
		return dataModel.getInstanceAttribute();
	}

	@Override
	public DataAttribute getClassAttribute() {
		return dataModel.getClassAttribute();
	}

	@Override
	public String getClassLabelForRow(int row) {
		if (filteredRows.contains(row)) {
			return dataModel.getClassLabelForRow(row);
		}

		return null;
	}

	@Override
	public List<String> getHeader() {
		return dataModel.getHeader();
	}

	@Override
	public List<String> getRow(int row) {
		if (filteredRows.contains(row)) {
			return dataModel.getRow(row);
		}

		return null;
	}

	@Override
	public List<String> getColumnItemsByAttribute(DataAttribute attribute) {
		return getColumnItemsByIndex(attribute.getAttributeIndex());
	}

	@Override
	public List<String> getColumnItemsByIndex(int columnIndex) {
		List<String> returnValues = new LinkedList<String>();
		for (Integer rowIndex : filteredRows) {
			List<String> rowItems = dataModel.getRow(rowIndex);

			String value = rowItems.get(columnIndex);
			returnValues.add(value);
		}
		
		return returnValues;
	}

	@Override
	public String getItemAt(int row, int column) {
		if (filteredRows.contains(row)
				&& onAttribute.getAttributeIndex() != column)
			return dataModel.getItemAt(row, column);
		else
			return null;
	}

	@Override
	public List<String> getClassColumn() {
		if (filteredClassColumn == null) {
			initFilteredColumns();
		}
		return filteredClassColumn;
	}

	@Override
	public List<String> getClassLabels() {
		if (filteredClassLabels == null) {
			initClassLabels();
		}
		return filteredClassLabels;
	}

	private void initClassLabels() {
		filteredClassLabels = new LinkedList<String>();

		List<String> classLabels =  UniqueItemFinder.findUnqiueItems(filteredClassColumn);
		filteredClassLabels.addAll(classLabels);
	}

	@Override
	public List<DataAttribute> getAttributes() {
		return filteredAttributes;
	}

	@Override
	public int size() {
		return filteredRows.size();
	}

	@Override
	public void clear() {
		filteredRows.clear();
		dataModel.clear();
	}

	@Override
	public int getNumRows() {
		return filteredRows.size();
	}

	@Override
	public int getNumCols() {
		return filteredAttributes.size();
	}

	@Override
	public boolean isTrainData() {
		return dataModel.isTrainData();
	}

	@Override
	public void setDataSetAsTraining() {
		dataModel.setDataSetAsTraining();
	}

	@Override
	public boolean isValidationData() {
		return dataModel.isValidationData();
	}

	@Override
	public void setDataSetAsValidation() {
		dataModel.setDataSetAsValidation();
	}

	public void applyFilterOnAttributeValue(DataAttribute attribute,
			String attributeValue) throws Exception {
		if (attribute.equals(dataModel.getClassAttribute())
				|| attribute.equals(dataModel.getInstanceAttribute()))
			throw new Exception("Cannot filter on a Class or Instance field.");

		onAttribute = attribute;
		onAttributeValue = attributeValue;
		if (onAttribute == null || onAttributeValue == null) {
			throw new Exception(
					"The specified Attribute does not exisit in the DataModel");
		}

		initFilteredContainers();
		initFilteredAttributes();
		initFilteredRows(attributeValue);
		initFilteredColumns();
	}

	private void initFilteredContainers() {
		filteredAttributes = new LinkedList<DataAttribute>();
		filteredRows = new HashSet<Integer>();
	}

	private void initFilteredAttributes() {
		List<DataAttribute> originalAttributes = dataModel.getAttributes();
		for (DataAttribute newAttribute : originalAttributes) {
			if (!newAttribute.equals(onAttribute)) {
				filteredAttributes.add(newAttribute);
			}
		}
	}

	private void initFilteredRows(String attributeValue) {
		for(Integer index : dataModel.getAllValidIndexes()){
			List<String> row = dataModel.getRow(index);
			String currentAttrValue = row.get(onAttribute.getAttributeIndex());
			if (currentAttrValue.equals(attributeValue)) {
				filteredRows.add(index);
			}	
		}
	}

	private void initFilteredColumns() {
		filteredClassColumn = new LinkedList<String>();
		
		List<Integer> indecesInDataModel = dataModel.getAllValidIndexes();
		int classAttributeIndex = dataModel.getClassAttribute().getAttributeIndex();
		
		for(Integer index : indecesInDataModel){
			List<String> row = dataModel.getRow(index);
			String colVal = row.get(classAttributeIndex);
			
			if(filteredRows.contains(index)){
				filteredClassColumn.add(colVal);
			}
		}
	}

	@Override
	public void print() {

		for (String header : dataModel.getHeader()) {
			System.out.print(header + ",");
		}
		System.out.println();

		for (Integer rowIndex : filteredRows) {
			List<String> rowItems = dataModel.getRow(rowIndex);

			for (String column : rowItems) {
				System.out.print(column + ",");
			}
			System.out.println();
		}
	}

	/**
	 * EMPTY IMPLEMENTED METHODS
	 */

	public void parseAndAddHeader(String header, String separator) {
		// do nothing
	}

	@Override
	public void parseAndAddRow(String rowStr, String separator) {
		// do nothing
	}

	@Override
	public void addItemsAsRow(List<String> items) {
		// do nothing
	}

	@Override
	public void addItemsAsRow(String[] items) {
		// do nothing
	}

	@Override
	public void setAttributes(List<DataAttribute> header) {
		// do nothing
	}

	@Override
	public boolean doesNotContain(DataModel otherDataModel) {
		return false;
	}
	
	@Override
	public boolean hasRowIndex(int index) {
		if(filteredRows.contains(index))
			return true;
		else return false;
	}
	
	@Override
	public List<Integer> getAllValidIndexes() {
		if(validIndexes == null){
			initValidIndexes();
		}
		
		return validIndexes;
	}

	@Override
	public void printPayload() {
		System.out.println(onAttribute.getAttributeName() + "-->" + onAttributeValue);
	}
	
	private List<Integer> initValidIndexes() {
		validIndexes = new LinkedList<Integer>();
		for(Integer index : filteredRows){
			validIndexes.add(index);
		}
		
		return validIndexes;
	}

	public DataAttribute getOnAttribute() {
		return onAttribute;
	}

	public void setOnAttribute(DataAttribute onAttribute) {
		this.onAttribute = onAttribute;
	}

	public String getOnAttributeValue() {
		return onAttributeValue;
	}

	public void setOnAttributeValue(String onAttributeValue) {
		this.onAttributeValue = onAttributeValue;
	}

	private final DataModel dataModel;
	private DataAttribute onAttribute = null;
	private String onAttributeValue = null;
	private HashSet<Integer> filteredRows = null;
	private List<Integer> validIndexes = null;
	private List<String> filteredClassLabels = null;
	private List<DataAttribute> filteredAttributes = null;
	private List<String> filteredClassColumn = null;
}
