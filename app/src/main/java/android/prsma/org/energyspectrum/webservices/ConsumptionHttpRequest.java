package android.prsma.org.energyspectrum.webservices;

import android.content.ContentValues;
import android.content.Context;
import android.prsma.org.energyspectrum.dtos.RuntimeConfigs;
import android.util.Log;

import java.util.ArrayList;

public abstract class ConsumptionHttpRequest extends Thread {
	
	private static final String MODULE = "HttpRequest";
	
	private ArrayList<ContentValues> data;
	@SuppressWarnings("unused")
	private String url="http://madeiratecnopolo.pt/restfull/";
	private String requestCode;
	private String requestType;

	public String hhhaaackk; 

	public Context _appCtx;
	
	public void getConsumption(String token){
		
		if(token.equals("dummy")){
			url+="dummy function";
			this.run();
		}
		
	}
	
	private String buildRequest(){
		
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
	
	private String buildRequest(String request){
		
		
		int iid   = RuntimeConfigs.getConfigs().getInstallation_id();
		String ip = RuntimeConfigs.getConfigs().getMeterIp();
		
		request = "http://"+ip+":8000/slimREST/iid_"+requestType+"/"+iid+"/"+requestCode;
		Log.e(MODULE, "AQUI "+request);
		return request;
		
	}

	public void run(){
		Log.i(MODULE,"running request");
		String request = 		buildRequest();
		
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
	        
	}
	
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
