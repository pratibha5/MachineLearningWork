package structures.result;

import java.util.Comparator;

public class CrossValInferenceResultComparator implements Comparator {

	//@Override - causes compile errors with javac 1.5. PG2009
	public int compare(Object o1, Object o2) {

		if (!(o1 instanceof CrossValClassificationResult) 
				|| !(o2 instanceof CrossValClassificationResult))
			return 0;

		CrossValClassificationResult cvir1 = (CrossValClassificationResult) o1;
		CrossValClassificationResult cvir2 = (CrossValClassificationResult) o2;
		
		double rci1 = cvir1.rciMSE()[0];
		double rci2 = cvir2.rciMSE()[0];
		double ba1 = cvir1.balancedAccuracy();
		double ba2 = cvir2.balancedAccuracy();

		if (rci1 < rci2 || ba1 < ba2)
			return 1;

		if (rci1 > rci2 || ba1 > ba2)
			return -1;
	
		return 0;
	}
}