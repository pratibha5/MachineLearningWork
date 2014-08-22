/* @(#)PredictionComparator.java */

package structures.result;

import data.dataset.*;

/**
 * PredictionComparator orders Predictions according to the order of the values
 * of the ID attribute their data, and then by the data indeces.
 * 
 * @version 2004-04-22
 * @author Philip Ganchev Made into public class
 * 
 * @author Jonathan Lustgarten
 * @version 2008/2/12 Made the Comparator a little bit more encapsulated by
 *          comparing just the prediciton of the sample names and using the look
 *          up ability of the Dataset Since this violates the comparator
 *          operator I had to remove implement comparator
 */
public class PredictionComparator {
	int nIDAttribute;

	public PredictionComparator(int nIDAttribute) {
		this.nIDAttribute = nIDAttribute;
	}

	public int compare(Object o1, Object o2, Dataset set) {
		if (o1 == o2)
			return 0;
		String d1 = ((Prediction) o1).getPredictedDatum();
		String d2 = ((Prediction) o2).getPredictedDatum();
		if (nIDAttribute >= 0) {
			try {
				double d1cval = set.attributeValue(d1, nIDAttribute);
				double d2cval = set.attributeValue(d2, nIDAttribute);
				return (d1cval < d2cval ? -1 : d1cval == d2cval ? 0 : 1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return -1;
		}
		try {
			Attribute satt = set.idAttribute();
			return (satt.getRepresentation(d1) < satt.getRepresentation(d2)) ? -1
					: (satt.getRepresentation(d1) == satt.getRepresentation(d2)) ? 0
							: 1;
		} catch (AttributeDoesNotExistException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public boolean equals(Object o, Dataset train) {
		return (compare(this, o, train) == 0);
	}
}
