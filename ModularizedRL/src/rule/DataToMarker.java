package rule;

public class DataToMarker {
	private String sampName;
	private int classMarker;
	private MarkerToData[] Markers;

	public DataToMarker(String SN, int cM) {
		sampName = SN;
		classMarker = cM;
	}

	public void setMarkers(MarkerToData[] mks) {
		Markers = mks;
	}

	public MarkerToData getMarker(int attNum) {
		return Markers[attNum];
	}
}
