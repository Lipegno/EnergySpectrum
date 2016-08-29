package android.prsma.org.energyspectrum.webservices.services;

import android.content.ContentValues;
import android.prsma.org.energyspectrum.webservices.ConsumptionHttpRequest;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EnergyProdPrediction extends ConsumptionHttpRequest {

	private static final String MODULE = "EnergyPredictionRequest";
	private static final String DATE_KEY = "date";

	public EnergyProdPrediction(String request){
		super.hhhaaackk = request;
	}

	@Override
	public void parseData(String data) {
		try {
			JSONObject result = new JSONObject(data);
			JSONArray prod_data = result.getJSONArray("prod_data");
			ArrayList<ContentValues> parsed_result = new ArrayList<ContentValues>();

			for(int i=0;i<prod_data.length();i++){
				ContentValues temp = new ContentValues();
				JSONObject value = prod_data.getJSONObject(i);
//				Log.i(MODULE,"--");
//				Log.i(MODULE,value.getString("timestamp"));
				//				Log.i("Energy Production",value.getInt("total")+"");
				//				Log.i("Energy Production",value.getInt("termica")+"");
				//				Log.i("Energy Production",value.getInt("hidrica")+"");
				//				Log.i("Energy Production",value.getInt("eolica")+"");
				//				Log.i("Energy Production",value.getInt("biomassa")+"");
				//				Log.i("Energy Production",value.getInt("foto")+"");
				String date = value.getString("timestamp");
				String day_time = date.split(" ")[1];
				int hour = Integer.parseInt(day_time.split(":")[0]);
				int minutes = Integer.parseInt(day_time.split(":")[1]);
				int timeslot = (int) (((hour)*4)+Math.ceil(minutes/15));
//				Log.i(MODULE, "->"+timeslot);
				temp.put("timestamp", value.getString("timestamp"));
				temp.put("total", value.getInt("total"));
				temp.put("termica", value.getInt("termica"));
				temp.put("hidrica", value.getInt("hidrica"));
				temp.put("eolica", value.getInt("eolica"));
				temp.put("biomassa", value.getInt("biomassa"));
				temp.put("foto", value.getInt("foto"));
				temp.put("timeslot", timeslot);
				parsed_result.add(temp);
				ContentValues temp2 = new ContentValues();
				temp2.put("timestamp", value.getString("timestamp"));
				temp2.put("total", value.getInt("total"));
				temp2.put("termica", value.getInt("termica"));
				temp2.put("hidrica", value.getInt("hidrica"));
				temp2.put("eolica", value.getInt("eolica"));
				temp2.put("biomassa", value.getInt("biomassa"));
				temp2.put("foto", value.getInt("foto"));
				temp2.put("timeslot", timeslot+1);
				parsed_result.add(temp2);
			}
			Log.i("Energy prediction", data);
			super.setData(parsed_result);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run(){

		RequestQueue queue = Volley.newRequestQueue(_appCtx);

		Log.i(MODULE,"running request");
		//String request = 		buildRequest();
		String request = "http://aveiro.m-iti.org/sinais_energy_production/services/today_prediction_request.php";
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
		String date = s.format(new Date());
		request = request+"?"+DATE_KEY+"="+date;
		RequestFuture<String> future = RequestFuture.newFuture();
		StringRequest stringRequest = new StringRequest(Request.Method.GET, request,future,future);
		queue.add(stringRequest);

		try {
			String response = future.get(15, TimeUnit.SECONDS);
			parseData(response);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e){
			e.printStackTrace();
		}

	}

}
