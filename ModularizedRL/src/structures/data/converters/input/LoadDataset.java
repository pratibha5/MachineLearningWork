package structures.data.converters.input;

import java.io.FileNotFoundException;
import java.io.IOException;

import data.dataset.*;

public interface LoadDataset {
	public Dataset loadData() throws IncompatibleDatatypeException,
			FileNotFoundException, IOException, Exception;

	public Dataset loadData(String sep) throws IncompatibleDatatypeException,
			FileNotFoundException, IOException, Exception;
}
