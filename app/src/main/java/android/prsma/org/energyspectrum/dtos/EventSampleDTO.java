package android.prsma.org.energyspectrum.dtos;

import android.os.Bundle;
import android.util.Log;

public class EventSampleDTO implements Bundleable {

	private static final String MODULE="EVENT SAMPLE DTO";
	
	private int _event_id;
	private int _appliance_id;
	private String _guess;
	private int[] _transient;
	private int _deltaPMean;
	private String _timestamp;
	private int[] _clusterResults;
	
	public EventSampleDTO(String guess, int[] trans, int deltaPMean,int appliance_id, int event_id, String timestamp, int[] clusterResults){
		set_guess(guess);
		set_transient(trans);
		set_deltaPMean(deltaPMean);
		set_appliance_id(appliance_id);
		set_event_id(event_id);
		set_timestamp(timestamp);
		set_clusterResults(clusterResults);
		Log.e(MODULE, "Cluster Result "+clusterResults[0]+" "+clusterResults[1]+" "+clusterResults[2]);
//		Timestamp temp	 = new Timestamp(timestamp);
	}
	
	public EventSampleDTO(Bundle b){
		fromBundle(b);
	}

	public void set_deltaPMean(int _deltaPMean) {
		this._deltaPMean = _deltaPMean;
	}

	public int get_deltaPMean() {
		return _deltaPMean;
	}

	public void set_transient(int[] _transient) {
		this._transient = _transient;
	}

	public int[] get_transient() {
		return _transient;
	}

	public void set_guess(String _guess) {
		this._guess = _guess;
	}

	public String get_guess() {
		return _guess;
	}

	@Override
	public Bundle toBundle() {
		// TODO Auto-generated method stub
		Bundle b = new Bundle();
		b.putInt("delta", this._deltaPMean);
		b.putIntArray("trans", this._transient);
		b.putString("guess",this._guess);
		b.putInt("event_id", this._event_id);
		b.putInt("appliance_id",this._appliance_id);
		b.putString("timestamp", this._timestamp);
		b.putIntArray("clusterResults", _clusterResults);
		return b;
	}

	@Override
	public void fromBundle(Bundle b) {
		// TODO Auto-generated method stub
		_deltaPMean     = b.getInt("delta");
		_transient      = b.getIntArray("trans");
		_guess		    = b.getString("guess");
		_event_id	    = b.getInt("event_id");
		_appliance_id   = b.getInt("applicance_id");
		_timestamp      = b.getString("timestamp");
		_clusterResults = b.getIntArray("clusterResults");
	}

	public void set_event_id(int _event_id) {
		this._event_id = _event_id;
	}

	public int get_event_id() {
		return _event_id;
	}

	public void set_appliance_id(int _appliance_id) {
		this._appliance_id = _appliance_id;
	}

	public int get_appliance_id() {
		return _appliance_id;
	}

	public void set_timestamp(String _timestamp) {
		Log.i(MODULE, _timestamp);
		this._timestamp = _timestamp.substring(0, 16);  // removes the seconds and miliseconds from the hour name
	}

	public String get_timestamp() {
		return _timestamp;
	}

	public int[] get_clusterResults() {
		return _clusterResults;
	}

	public void set_clusterResults(int[] _clusterResults) {
		this._clusterResults = _clusterResults;
	}
	
}
