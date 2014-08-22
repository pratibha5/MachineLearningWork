package structures.data.converters.input;

import java.util.ArrayList;

import data.dataset.Attribute;
import data.dataset.DataModel;
import data.dataset.IncompatibleDatatypeException;


public class LoadArffDataModel implements LoadDataModel {

	private ArrayList<Attribute> atts;
	private DataModel newData;

	//@Override - causes compile errors with javac 1.5. PG2009
	public DataModel loadData() throws IncompatibleDatatypeException {
		// TODO Auto-generated method stub
		return null;
	}

	public DataModel loadData(String sep) throws IncompatibleDatatypeException {
		// TODO Auto-generated method stub
		return null;
	}
}