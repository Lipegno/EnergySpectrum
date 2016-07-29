package android.prsma.org.energyspectrum.services;


import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.prsma.org.energyspectrum.database.DBManager;
import android.prsma.org.energyspectrum.dtos.EventSampleDTO;
import android.prsma.org.energyspectrum.dtos.RuntimeConfigs;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilderFactory;

public class EventsSocketService extends Service implements Runnable {
	
	private static final String MODULE 	=	"Event service";
	public final IBinder binder = new MyBinder();
	public static final String FUNCTION_KEY = "function";
	public static final String CONS_KEY = "event";
	public static final String ACTION_KEY 	= "lastEvent";	
	private String _ip_adress;
	private int _port;
	private boolean _finished = false;
	private RuntimeConfigs _configs;
	
	//variables used to store the events in the database
	private DBManager db;
//	private int _eventsCount;
//	private  ArrayList<EventSampleDTO> _events = new ArrayList<EventSampleDTO>();
	
	private Thread _eventsThread;
	
	//variable to be used by the comunication
	public Socket requestSocket=null;
	DataOutputStream out=null;
	DataInputStream in=null;
 	String message="";
 	byte[] buf = new byte[1024];
 	StringBuffer soFar = new StringBuffer();
	
	@Override
	public IBinder onBind(Intent arg0)  {
		// TODO Auto-generated method stub
		
		_configs = RuntimeConfigs.getConfigs();
		db = DBManager.getDBManager();
		return binder;
		
	}
	/**
	 * Creates a new socket if the conection is lost
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public void createNewSocket() throws UnknownHostException, IOException{
		try {
			
			requestSocket = new Socket(_ip_adress, _port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//start();
	}

	public class MyBinder extends Binder{
	
		public EventsSocketService getService(){
			return EventsSocketService.this;
		}
		
	}
	
//	/**
//	 * function that updates a buffer with data from the events... once the buffer has 5 events the database is updated
//	 */
//	private void updateBuffer(EventSampleDTO evt){
//		
//		if(_events.size()<2){
//			_events.add(evt);
//			Log.i(MODULE, "added values to the buffer");
//		}else{
//			ArrayList<EventSampleDTO> temp = _events;
//			db.insertEventData(temp);
//			_events.clear();
//		}
//		
//	}
	
	private void insertEvent(EventSampleDTO evt){
		
		db.insertEventData(evt);
	}
	
	/**Broadcast the consumption data to be displayed in the main activity interface 
	 * @param key function key
	 * @param data	double with the current consumption
	 */
	private void broadCast(String key,EventSampleDTO data){
		
		if(data!=null){
		
		Intent intent = new Intent();
		//		//Bundle the counter value with Intent
		intent.putExtra(FUNCTION_KEY, key);
		intent.putExtra(CONS_KEY, data.toBundle());
		intent.setAction(ACTION_KEY); //Define intent-filter
		sendBroadcast(intent); // finally broadcast 
		Log.i(MODULE, "data sent");
		//		//		handler.postDelayed(this, 1*1000); // Repeat the block for every 1 sec and keep 
		//		//		//broadcasting until service destroyed
		}
	}
	
	public void start() throws UnknownHostException, IOException{
		
		//1. creating a socket to connect to the server
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		_ip_adress = sp.getString("meter_ip", "2");
		_port 	   = Integer.parseInt(sp.getString("socket_port", "1"));
		_port = _port+8880;
		Log.i(MODULE, "trying to create stream socket to " + _ip_adress+" in the port "+_port);
		
		if(requestSocket!=null)
			Log.d(MODULE,requestSocket.isConnected()+"");
	
		LongOperation nova  = new LongOperation();
		nova.execute("");
	
	}
	
	private void initComunication() throws IOException{
		//2. get Input and Output streams
		try{out = new DataOutputStream(requestSocket.getOutputStream());
		out.flush();
		in = new DataInputStream(requestSocket.getInputStream());
		_eventsThread = new Thread(this);
		_eventsThread.start();
		}catch(Exception e){
			Log.e(MODULE, "problem creating events socket, sistem offline, or wrong address/port");
		}
	}
	public synchronized EventSampleDTO handleMessage2(String message){
		int[] p_points = new int[100];
		int deltaPMean =0;
		int deltaQMean =0;
		String applicance_guess ="";
		int app_id=0;
		int event_id=0;
		int appliance_id=0;
		String timestamp = "";
		try {

			System.out.println(message);
			DocumentBuilderFactory factory =
					DocumentBuilderFactory.newInstance();
			InputSource source = new InputSource(new StringReader(message));
			Document doc = factory.newDocumentBuilder().parse(source);
			Element root = doc.getDocumentElement();
			NodeList nl  = root.getChildNodes();
			NodeList nl2;
			for(int i=0; i<nl.getLength();i++){
				Node n = nl.item(i);
				if((n.getNodeType() == Node.ELEMENT_NODE) && (((Element)n).getTagName().equals("deltaPMean"))){
					deltaPMean= (int) Math.round(Double.valueOf(n.getChildNodes().item(0).getNodeValue()));
				}
				if((n.getNodeType() == Node.ELEMENT_NODE) && (((Element)n).getTagName().equals("transient"))){
					nl2 = n.getChildNodes();
					for(int j=0; j<nl2.getLength();j++){
						n = nl2.item(j);
						if((n.getNodeType() == Node.ELEMENT_NODE) && (((Element)n).getTagName().equals("p"))){
							p_points[j] = (int) Math.round(Double.valueOf((n.getChildNodes().item(0).getNodeValue())));
						}
					}
				}
				if((n.getNodeType() == Node.ELEMENT_NODE) && (((Element)n).getTagName().equals("knn"))){
					nl2 = n.getChildNodes();
					for(int j=0; j<nl2.getLength();j++){
						n = nl2.item(j);
						if((n.getNodeType() == Node.ELEMENT_NODE) && (((Element)n).getTagName().equals("appliance"))){
							applicance_guess = (n.getChildNodes().item(0).getNodeValue());
							appliance_id    = Integer.valueOf(n.getAttributes().item(0).getNodeValue());
							Log.d(MODULE, applicance_guess+" "+appliance_id);
							break;
						}
					}
				}
				if((n.getNodeType() == Node.ELEMENT_NODE) && (((Element)n).getTagName().equals("id"))){
					event_id= (int) Math.round(Double.valueOf(n.getChildNodes().item(0).getNodeValue()));
					Log.d(MODULE, "event id "+event_id);
				}
				if((n.getNodeType() == Node.ELEMENT_NODE) && (((Element)n).getTagName().equals("timestamp"))){
					timestamp= String.valueOf(n.getChildNodes().item(0).getNodeValue());
					Log.d(MODULE, "event id "+timestamp);
				}
			}
						int[] clusterResult = {};// ClusterCalculator.calculateCluster(deltaPMean, deltaQMean);
						applicance_guess = DBManager.getDBManager().getApplianceName(appliance_id);
						Log.w(MODULE, "appliance "+appliance_id+" "+applicance_guess);
						EventSampleDTO temp =  new EventSampleDTO(applicance_guess,p_points,deltaPMean,appliance_id,event_id,timestamp, clusterResult);
						insertEvent(temp);
						int count = _configs.getEventsCount();
						count++;
						_configs.setEventsCount(count);
			return temp;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public synchronized EventSampleDTO handleMessage(String message){

		int[] p_points = new int[100];
		int deltaPMean =0;
		int deltaQMean =0;
		String applicance_guess ="";
		int app_id=0;
		int event_id=0;
		int appliance_id=0;
		String timestamp = "";
		try {
			
			System.out.println(message);
			DocumentBuilderFactory factory =
				DocumentBuilderFactory.newInstance();
			InputSource source = new InputSource(new StringReader(message));
			Document doc = factory.newDocumentBuilder().parse(source);

			Element root = doc.getDocumentElement();
			NodeList nl  = root.getChildNodes();
			NodeList nl2;
			
			
		for(int i=0; i<nl.getLength();i++){
			
			Node n1 = nl.item(i);
			
			if(n1.getNodeName().equals("event")){
				
			NodeList n2 = n1.getChildNodes();
					
				for(int j=0; j<n2.getLength();j++){
				
					Node n = n2.item(j);
						
					if((n.getNodeType() == Node.ELEMENT_NODE) && (((Element)n).getTagName().equals("deltaPMean"))){
						deltaPMean= (int) Math.round(Double.valueOf(n.getChildNodes().item(0).getNodeValue()));
						Log.i(MODULE, "detaPMean "+deltaPMean);
					}
					if((n.getNodeType() == Node.ELEMENT_NODE) && (((Element)n).getTagName().equals("deltaQMean"))){
						deltaQMean= (int) Math.round(Double.valueOf(n.getChildNodes().item(0).getNodeValue()));
						Log.i(MODULE, "detaQMean "+deltaQMean);
					}
//					if((n.getNodeType() == Node.ELEMENT_NODE) && (((Element)n).getTagName().equals("transient"))){
//						nl2 = n.getChildNodes();
//						for(int z=0; j<nl2.getLength();z++){
//							n = nl2.item(z);
//							if((n.getNodeType() == Node.ELEMENT_NODE) && (((Element)n).getTagName().equals("p"))){
//								p_points[z] = (int) Math.round(Double.valueOf((n.getChildNodes().item(0).getNodeValue())));
//							}
//						}
//					}
//					
//					if((n.getNodeType() == Node.ELEMENT_NODE) && (((Element)n).getTagName().equals("knn"))){
//						nl2 = n.getChildNodes();
//						for(int z=0; z<nl2.getLength();z++){
//							n = nl2.item(z);
//							if((n.getNodeType() == Node.ELEMENT_NODE) && (((Element)n).getTagName().equals("appliance"))){
//							
//								applicance_guess = (n.getChildNodes().item(0).getNodeValue());
//								appliance_id    = Integer.valueOf(n.getAttributes().item(0).getNodeValue());
//								Log.d(MODULE, applicance_guess+" "+appliance_id);
//								break;
//							}
//						}
//					}
//					
//					if((n.getNodeType() == Node.ELEMENT_NODE) && (((Element)n).getTagName().equals("id"))){
//						event_id= (int) Math.round(Double.valueOf(n.getChildNodes().item(0).getNodeValue()));
//						Log.d(MODULE, "event id "+event_id);
//					}
					if((n.getNodeType() == Node.ELEMENT_NODE) && (((Element)n).getTagName().equals("timestamp"))){
						timestamp= String.valueOf(n.getChildNodes().item(0).getNodeValue());
						Log.d(MODULE, "event id "+timestamp);
					}
					
				}
				
				}
			Log.i(MODULE, "test"+n1.getNodeName());
			if(n1.getNodeName().equals("classification")){
				app_id = Integer.parseInt(n1.getAttributes().getNamedItem("appliance_id").getNodeValue());
			}
			
			}
			
			int[] clusterResult ={}; // ClusterCalculator.calculateCluster(deltaPMean, deltaQMean);
			
			//Log.i(MODULE,"cluster "+clusterResult[0]+" certainty "+clusterResult[2]);
			applicance_guess = "";//getAppName(app_id); //DBManager.getDBManager().getApplianceName(app_id);
			EventSampleDTO temp =  new EventSampleDTO(applicance_guess,p_points,deltaPMean,appliance_id,event_id,timestamp, clusterResult);
			insertEvent(temp);
			
			int count = _configs.getEventsCount();
			count++;
			Log.w(MODULE, count+" events");
			
			_configs.setEventsCount(count);
			return temp;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

    private String getAppName(int app_id){
        String  result="";
        switch(app_id){
            case 1:
                result = "Desk Lamp ON";
                break;
            case 2:
                result = "Desk Lamp OFF";
                break;
            case 3:
                result = "Kettle ON";
                break;
            case 4:
                result = "Kettle OFF";
                break;
        }
        Log.i(MODULE,result);
        return result;
    }
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int avail;
		try {
		while(!_finished){	
			Thread.sleep(150);
			avail = in.available();
	    while (avail > 0) {
	    	int amt = avail;
	    	if (amt > buf.length) amt = buf.length;
	    	amt = in.read(buf, 0, amt);
	            int marker = 0;
	            for (int i=0; i<amt; i++) {
	                // scan for the zero-byte EOM delimiter
	                if (buf[i] == (byte)0) {
	                    String tmp = new String(buf, marker, i - marker);
	                    soFar.append(tmp);
	                    Log.e(MODULE, soFar.toString() );
	                    broadCast("lastEvent",handleMessage2(soFar.toString()));
	                    soFar.setLength(0);
	                    marker = i + 1;
	                }
	            }
	            if (marker < amt) {
	                // save all so far, still waiting for the final EOM
	                soFar.append( new String(buf, marker, amt-marker) );
	            }
	    	avail = in.available();
	    } 	}
		}catch (Exception e) {
			// TODO Auto-generated catch block
			_finished=true; // finishes the service when any problem is detected in the connection
			e.printStackTrace();
		}
}
	
	private class LongOperation extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				requestSocket = new Socket(_ip_adress, _port);
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
			try {
				initComunication();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
}