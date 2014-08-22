package structures.data.converters.output;

import data.dataset.Dataset;

public class OutputTabOrCSV extends OutputDataset {

	public OutputTabOrCSV(Dataset data, String outFileName, String sep) {
		super(data, outFileName);
		data.setSeperator(sep);
	}

	public void printDataset(boolean useDisc) {
		outPS.print(data.print(useDisc));
		if (outPS != System.out)
			outPS.close();
	}
}