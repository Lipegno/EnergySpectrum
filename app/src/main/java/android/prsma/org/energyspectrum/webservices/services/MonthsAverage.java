package android.prsma.org.energyspectrum.webservices.services;

import android.content.ContentValues;
import android.prsma.org.energyspectrum.webservices.ConsumptionHttpRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilderFactory;

public class MonthsAverage extends ConsumptionHttpRequest {
	
	public MonthsAverage(int month, int year){
		super.setRequestCode(month+"/"+year);
		super.setRequestType("month");
	}
	
	public void parseData(String data){
		
		double cons;
		ArrayList<ContentValues> result = new ArrayList<ContentValues>();
		long timestamp;
		
		DocumentBuilderFactory factory =
			DocumentBuilderFactory.newInstance();
		InputSource source = new InputSource(new StringReader(data));
		Document doc;
		try {
			doc = factory.newDocumentBuilder().parse(source);
		

		Element root = doc.getDocumentElement();
		NodeList nl  = root.getChildNodes();
		
		for(int i=0; i<nl.getLength();i++){
			Node n = nl.item(i);
			
			Element elem = (Element) n;
			
				cons          = Double.valueOf(elem.getAttribute("Average_Power"));
			    timestamp     = Long.valueOf(elem.getAttribute("UNIX_Timestamp"));;
			    Calendar cal  = Calendar.getInstance();

				cal.setTimeInMillis(((long) timestamp*1000));
				ContentValues temp = new ContentValues();
				
				temp.put("cons", cons);
				temp.put("month", cal.get(Calendar.MONTH)+1);
				temp.put("weekday",cal.get(Calendar.DAY_OF_WEEK));
				temp.put("day",cal.get(Calendar.DAY_OF_MONTH));
				temp.put("week", cal.get(Calendar.WEEK_OF_YEAR));
				temp.put("year",cal.get(Calendar.YEAR));
			//	Log.i("Month consump", "cons: "+cons+" month "+(cal.get(Calendar.MONTH)+1)+" day of the week "+cal.get(Calendar.DAY_OF_WEEK)+" day "+cal.get(Calendar.DAY_OF_MONTH)+" week "+cal.get(Calendar.WEEK_OF_YEAR));
				result.add(temp);
		
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		super.setData(result);

	}
}
