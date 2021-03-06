package android.prsma.org.energyspectrum.webservices;

import android.content.ContentValues;
import android.content.Context;
import android.prsma.org.energyspectrum.dtos.RuntimeConfigs;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class ConsumptionHttpRequest extends Thread {
	
	private static final String MODULE = "HttpRequest";
	
	private ArrayList<ContentValues> data;
	@SuppressWarnings("unused")
	private String url="http://madeiratecnopolo.pt/restfull/";
	private String requestCode;
	private String requestType;

	public String hhhaaackk; 
	public Context _appCtx;

	public static int NEW_REQUEST_MODE = 2;
	public static int OLD_REQUEST_MODE = 1;

	private int _request_mode;

	private String _request;

	public void getConsumption(String token){
		
		if(token.equals("dummy")){
			url+="dummy function";
			this.run();
		}
		
	}

	public void setRequestMode(int mode){
		this._request_mode = mode;
	}
	
	public String buildRequest(){
		
		String request;
		
		int iid   = RuntimeConfigs.getConfigs().getInstallation_id();
		String ip = RuntimeConfigs.getConfigs().getMeterIp();
		String web_serbice_port = RuntimeConfigs.getConfigs().getWebserver_port()+"";
		if(requestCode!=null)
			request = "http://"+ip+":"+web_serbice_port+"/slimREST/iid_"+requestType+"/"+iid+"/"+requestCode;
		else
			request = "http://"+ip+":"+web_serbice_port+"/slimREST/iid_"+requestType+"/"+iid;
		
		Log.i(MODULE, request);
		return request;
		
	}


	
	public String buildRequest(String path, String keys){
		
		String request = path+"/"+keys;
		this._request = request;
		return request;
		
	}

	@Override
	public void run(){
		String request = "";

		if(_request_mode==OLD_REQUEST_MODE) {
			request = buildRequest();
		}else{
			request = _request;
		}

		RequestQueue queue = Volley.newRequestQueue(_appCtx);
		RequestFuture<String> future = RequestFuture.newFuture();
		StringRequest stringRequest = new StringRequest(Request.Method.GET, request,future,future);
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(
				5000,
				3,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		queue.add(stringRequest);

		try {
			Log.e(MODULE, "-----   running request  ------");
			String response = future.get();
			parseData(response);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

	}

	public synchronized void run(String path,String keys) throws InterruptedException, ExecutionException, TimeoutException {

		String request = buildRequest(path, keys);

		RequestQueue queue = Volley.newRequestQueue(_appCtx);

		RequestFuture<String> future = RequestFuture.newFuture();
		StringRequest stringRequest = new StringRequest(Request.Method.GET, request, future, future);
		queue.add(stringRequest);

		//	try {
		String response = future.get();
		parseData(response);
	}
		//String request = hhhaaackk;
		/*
		HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        
	        try {
	            response = httpclient.execute(new HttpGet(request));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	                
	            	parseData(responseString);
	            	
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	            
	        } catch (ClientProtocolException e) {
	            e.printStackTrace();
	        	
	        } catch (IOException e) {
	        	e.printStackTrace();
	        } catch (Exception e){
	        	e.printStackTrace();
	        }*/
	        
	public ArrayList<ContentValues> getData() {
		return data;
	}

	public void setData(ArrayList<ContentValues> data) {
		this.data = data;
	}
	
	public String getRequestCode() {
		return requestCode;
	}

	public void setRequestCode(String requestCode) {
		this.requestCode = requestCode;
	}
	
	/**
	 * Parses the data received from the webservice, the children have to implement this according to the type of data they are expecting 
	 * @param data Bytes from the webservice
	 */
	abstract public void parseData(String data);


	public String getRequestType() {
		return requestType;
	}


	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

}
