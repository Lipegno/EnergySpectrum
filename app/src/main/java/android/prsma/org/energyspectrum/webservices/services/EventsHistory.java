package android.prsma.org.energyspectrum.webservices.services;

import android.content.ContentValues;
import android.prsma.org.energyspectrum.dtos.EventSampleDTO;
import android.prsma.org.energyspectrum.webservices.ConsumptionHttpRequest;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Filipe on 04/11/2016.
 * Requests the last events that happened in the current hour
 */
public class EventsHistory extends ConsumptionHttpRequest {
    private static final String TAG = "EventHistoryRequest";
    private ArrayList<EventSampleDTO> _event_data;

    public ArrayList<EventSampleDTO> getEventData(){
        return _event_data;
    }

    @Override
    public void parseData(String data) {
        Log.i(TAG,data);

        try {
            JSONArray result = new JSONArray(data);
            ArrayList<EventSampleDTO> cons_data = new ArrayList<EventSampleDTO>();
            for(int i=0;i<result.length();i++){
                Log.i(TAG,"------- Object "+i+"--------");
                Log.i(TAG, "Appliance " + ((JSONObject)(result.get(i))).getString("Appliance"));
                Log.i(TAG, "Timestamp " + ((JSONObject)(result.get(i))).getString("Timestamp"));
                Log.i(TAG, "Power " + ((JSONObject)(result.get(i))).getString("Power"));
                EventSampleDTO dummy = new EventSampleDTO(
                        ((JSONObject)(result.get(i))).getString("Appliance"),
                        null,
                        Math.round(Float.parseFloat(((JSONObject)(result.get(i))).getString("Power"))),
                        0,
                        0,
                         (((JSONObject)(result.get(i))).getString("Timestamp")),
                         null);


                cons_data.add(dummy);
            }
            _event_data = cons_data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
