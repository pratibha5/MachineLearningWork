package structures.data.converters.input;

import java.util.ArrayList;

import data.dataset.Attribute;
import data.dataset.Dataset;
import data.dataset.IncompatibleDatatypeException;


public class LoadArffDataset implements LoadDataset {

	private ArrayList<Attribute> atts;
	private Dataset newData;

	//@Override - causes compile errors with javac 1.5. PG2009
	public Dataset loadData() throws IncompatibleDatatypeException {
		// TODO Auto-generated method stub
		return null;
	}

	public Dataset loadData(String sep) throws IncompatibleDatatypeException {
		// TODO Auto-generated method stub
		return null;
	}
}