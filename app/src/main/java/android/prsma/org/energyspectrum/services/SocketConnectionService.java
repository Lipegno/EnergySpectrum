package android.prsma.org.energyspectrum.services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.prsma.org.energyspectrum.database.DBManager;
import android.prsma.org.energyspectrum.dtos.RuntimeConfigs;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
/**
 * Service responsible for connecting to the powermeter and sending the data to any activity that wants to have it
 * @author filipequintal
 *
 */
public class SocketConnectionService extends Service implements Runnable {

	public final IBinder binder = new MyBinder();
	private final int iid = RuntimeConfigs.getConfigs().getInstallation_id();
	
	public static final String FUNCTION_KEY = "function";
	public static final String CONS_KEY = "consump";
	public static final String ACTION_KEY 	= "current_consump_result";	

	private static final String MODULE = "Current consumption Socket Service";
	
	public static final String SOCKET_ERROR_MSG = "ComunicationError";
	
	private Thread _requestThread;
	public ComunicationHandler _comunicationHandler;
	public boolean _finished = false; 
	private String _ip_adress;
	private int _port;
	private Socket socket = null;


	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return binder;

	}

	//Instance of the Service is now in MyBinder
	public class MyBinder extends Binder{

		public SocketConnectionService getService(){
			return SocketConnectionService.this; 
			
		}
	}

	public void start() throws UnknownHostException, IOException{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		_ip_adress = sp.getString("meter_ip", "2");
		_port 	   = Integer.parseInt(sp.getString("socket_port", "1"));
		_port = _port+9900;
		_finished = false;
		Log.i("SOCKET SERVICE", "started to port "+_port+" , ip:"+_ip_adress);
		
		LongOperation nova = new LongOperation();
		
		nova.execute("");
		

	}
	
	private void initComunication(){
		
		_comunicationHandler = new ComunicationHandler();
		_requestThread = new Thread(this);
		_requestThread.start();
		
	}
	
	public void start2() throws UnknownHostException, IOException{
		//SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		_finished = false;
		Log.i("SOCKET SERVICE", "started to port 9991, ip:"+_ip_adress);
		socket = new Socket(_ip_adress,_port);

		_requestThread = new Thread(this);
		_requestThread.start();
	}

	private class ComunicationHandler extends Handler{
		
		@Override
		public void handleMessage(Message msg){
			try {
				requestCurrentCons();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				_finished=true;
				//Log.e(MODULE, "error in the current cons socket");
				broadCast("current_cons",SOCKET_ERROR_MSG);
				//e.printStackTrace();
			}
		}
	}
	/**
	 * 
	 * @param request 1 - currentCons / 2-Load Appliances / 3-Classifica��o
	 * @throws Exception - Communication exception
	 */
	
	
	/**
	 * 
	 * @param request 1-CurrentCons, 2-load appliances, 3- classify
	 * @param params - int with parameters of the request [installation_id]-> for load appliances ; [event_id, app_id]-> for classify
	 * @param app_name - String with the name of the new appliance to be inserted in the system.
	 * @throws Exception - communication exception
	 */
	public void communicateWithSocket(int request,String app_name, int... params) throws Exception{
		
		switch(request){
		case 1:
			requestCurrentCons();
			break;
		case 2:
			 //Log.i(MODULE, this.toString());
			loadAppliances(params[0]);
			break;
		case 3:
			classify(params[0],params[1], app_name);
			break;
		
		}
		
	}
	
	/**Broadcast the consumption data to be displayed in the main activity interface 
	 * @param key function key
	 * @param data	double with the current consumption
	 */
	private void broadCast(String key,String data){
		Intent intent = new Intent();
		//		//Bundle the counter value with Intent
		intent.putExtra(FUNCTION_KEY, key);
		intent.putExtra(CONS_KEY, data);
		intent.setAction(ACTION_KEY); //Define intent-filter
		sendBroadcast(intent); // finally broadcast 
	}
	
	/*
	 * Requests the current consumption from the server 
	 */
	public void requestCurrentCons() throws Exception {
	
		// TODO Auto-generated method stub
		DataOutputStream dataOutputStream = null;
		DataInputStream dataInputStream = null;

		dataOutputStream = new DataOutputStream(socket.getOutputStream());
		dataInputStream = new DataInputStream(socket.getInputStream());
		//Log.e(MODULE, "sending request Current Consumption");

		dataOutputStream.writeBytes("map|lastSampleRealPower"+Character.MIN_VALUE);
		
		StringBuffer inputLine = new StringBuffer();
		byte[] buf = new byte[512];
        dataInputStream.read(buf);
        String soFar=new String(buf);

      //  Log.i(MODULE, this.toString());
		
		broadCast("current_cons",parseXML(soFar));
		
	}
	
	/**
	 * sends a msg to the server with a user's guess for an appliance
	 */
	private void classify(int event_id,int app_id, String app_name) throws Exception{

		ClassificationThread t = new ClassificationThread(event_id,app_id,app_name);
		t.start();
	}
	
	/**
	 * Loads the appliances already listed  for this house
	 * @param installationId - installation id of the device
	 */
	private void loadAppliances(int installationId) throws Exception{
		
		Thread t = new Thread(){
			
			@Override
			public void run(){
			
				
			DataOutputStream dataOutputStream = null;
			DataInputStream dataInputStream = null;

			try {
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
			dataInputStream = new DataInputStream(socket.getInputStream());
			//Log.e(MODULE, "sending request load appliances");
			
			dataOutputStream.writeBytes("loadAppliances|"+iid+Character.MIN_VALUE);
			StringBuffer inputLine = new StringBuffer();
			byte[] buf = new byte[512];
	        dataInputStream.read(buf);
	        String soFar=new String(buf);
	        
	       // Log.i(MODULE, soFar);
	        
	        parseAppliances(soFar);
	        
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			}
		};
		
		t.start();
		
		
	}

	private String parseXML(String xml) throws Exception{

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		String result="";
		xpp.setInput( new StringReader ( xml ) );
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_DOCUMENT) {
				//System.out.println("Start document");
			} else if(eventType == XmlPullParser.START_TAG) {
				//System.out.println("Start tag "+xpp.getName());
			} else if(eventType == XmlPullParser.END_TAG) {
				//System.out.println("End tag "+xpp.getName());
			} else if(eventType == XmlPullParser.TEXT) {
				result=xpp.getText();
				//System.out.println("Text "+xpp.getText());
			}
			eventType = xpp.next();
		}
		double tes = Double.valueOf(result);
		result=Math.round(tes)+"";
		//Log.d(MODULE, result);
		return result;
	}
	
	private void parseAppliances(String xml){

		DocumentBuilderFactory factory =
				DocumentBuilderFactory.newInstance();
		InputSource source = new InputSource(new StringReader(xml));
		Document doc;
		try {
			doc = factory.newDocumentBuilder().parse(source);

			Element root = doc.getDocumentElement();
			NodeList nl  = root.getChildNodes();
			NodeList nl2;
			int app_id;
			String name;
			int group_id;
			
			ArrayList<ContentValues> result = new ArrayList<ContentValues>();
			
			for(int i=0; i<nl.getLength();i++){
				Node n = nl.item(i);
				ContentValues item = new ContentValues();
				
				app_id	 = Integer.valueOf( n.getAttributes().getNamedItem("id").getNodeValue());
				name     = n.getAttributes().getNamedItem("name").getNodeValue();
				group_id = Integer.valueOf(n.getAttributes().getNamedItem("group_id").getNodeValue());
				
				item.put("app_id", app_id);
				item.put("name", name);
				item.put("group_id",group_id);
				result.add(item);
			}
			if(result.size()>0)
		     	DBManager.getDBManager().insertAppliances(result);
			
			// iserir na BD aqui

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while(!_finished){

				Thread.sleep(5000);
				//_comunicationHandler.sendEmptyMessage(0);
				communicateWithSocket(1,"");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//Log.e(MODULE, "sending error message");
			broadCast("current_cons",SOCKET_ERROR_MSG);
			e.printStackTrace();
		} catch (Exception e) {
			//Log.e(MODULE, "sending error message");
			broadCast("current_cons",SOCKET_ERROR_MSG);
			e.printStackTrace();
		}

	}
	
	private class LongOperation extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				socket = new Socket(_ip_adress,_port);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "";
		}      

		@Override
		protected void onPostExecute(String result) {
			initComunication();
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
	
	private class ClassificationThread extends Thread{

		private int event_id;
		private int app_id;
		private String app_name;
		
		public ClassificationThread(int _event_id,int _app_id,String _app_name){
			this.event_id=_event_id;
			this.app_id=_app_id;
			this.app_name=_app_name;
		}



		@Override
		public void run(){

			DataOutputStream dataOutputStream = null;
			DataInputStream dataInputStream = null;

			try {
				dataOutputStream = new DataOutputStream(socket.getOutputStream());
		
			dataInputStream = new DataInputStream(socket.getInputStream());
			//Log.e(MODULE, "sending request classification");
			if(app_id==-1){
			//	Log.i(MODULE, "insert and class");
				dataOutputStream.writeBytes("insert-and-classify|" + event_id + "|" + app_name + "|human"+Character.MIN_VALUE);
			}else{
			//	Log.i(MODULE, "class");
				dataOutputStream.writeBytes("classify|" + event_id + "|" + app_id + "|human"+Character.MIN_VALUE);
			}
			StringBuffer inputLine = new StringBuffer();
			byte[] buf = new byte[512];
			dataInputStream.read(buf);
			String soFar=new String(buf);
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
