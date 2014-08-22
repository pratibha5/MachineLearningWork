package structures.data.converters.input;

import java.io.FileNotFoundException;
import java.io.IOException;

import data.dataset.Dataset;
import data.dataset.IncompatibleDatatypeException;

public class LoadTrainTestDatasets implements LoadDataset {

	private String trnData, tstData;
	private Dataset trnD, tstD;

	public LoadTrainTestDatasets(Dataset trn, String tst) {
		trnD = trn;
		tstData = tst;
	}

	private void equalizeDatasets() {
		trnD.equalizeFromTestDataset(tstD);
		tstD.equalizeFromTrainDataset(trnD);
	}

	//@Override - causes compile errors with javac 1.5. PG2009
	public Dataset loadData() throws Exception {
		TabCsvDataLoader ltcd = new TabCsvDataLoader(tstData);
		tstD = ltcd.loadData();
		equalizeDatasets();
		return tstD;
	}

	public Dataset loadData(String sep) throws Exception {
		TabCsvDataLoader ltcd = new TabCsvDataLoader(tstData);
		tstD = ltcd.loadData();
		equalizeDatasets();
		return tstD;
	}
}
