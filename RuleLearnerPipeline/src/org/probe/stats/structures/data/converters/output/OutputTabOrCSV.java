package structures.data.converters.output;

import data.dataset.DataModel;

public class OutputTabOrCSV extends OutputDataModel {

	public OutputTabOrCSV(DataModel data, String outFileName, String sep) {
		super(data, outFileName);
		data.setSeperator(sep);
	}

	public void printDataModel(boolean useDisc) {
		outPS.print(data.print(useDisc));
		if (outPS != System.out)
			outPS.close();
	}
}