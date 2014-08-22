package structures.data.converters.input;

import java.io.FileNotFoundException;
import java.io.IOException;

import data.dataset.DataModel;
import data.dataset.IncompatibleDatatypeException;

public class LoadTrainTestDataModels implements LoadDataModel {

	private String trnData, tstData;
	private DataModel trnD, tstD;

	public LoadTrainTestDataModels(DataModel trn, String tst) {
		trnD = trn;
		tstData = tst;
	}

	private void equalizeDataModels() {
		trnD.equalizeFromTestDataModel(tstD);
		tstD.equalizeFromTrainDataModel(trnD);
	}

	//@Override - causes compile errors with javac 1.5. PG2009
	public DataModel loadData() throws Exception {
		TabCsvDataLoader ltcd = new TabCsvDataLoader(tstData);
		tstD = ltcd.loadData();
		equalizeDataModels();
		return tstD;
	}

	public DataModel loadData(String sep) throws Exception {
		TabCsvDataLoader ltcd = new TabCsvDataLoader(tstData);
		tstD = ltcd.loadData();
		equalizeDataModels();
		return tstD;
	}
}
