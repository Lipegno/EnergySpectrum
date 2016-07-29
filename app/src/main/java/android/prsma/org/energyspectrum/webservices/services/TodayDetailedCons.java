package android.prsma.org.energyspectrum.webservices.services;

import android.content.ContentValues;
import android.prsma.org.energyspectrum.webservices.ConsumptionHttpRequest;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilderFactory;

public class TodayDetailedCons extends ConsumptionHttpRequest {

	public TodayDetailedCons(){
		super.setRequestType("todayDetailed");
	}
	
	@Override
	public void parseData(String data) {
		double cons;
		ArrayList<ContentValues> result = new ArrayList<ContentValues>();
		long timestamp;
		String tms;

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
				timestamp     = Long.valueOf(elem.getAttribute("UNIX_Timestamp"));
				tms		      = elem.getAttribute("timestamp");
				Calendar cal  = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

				cal.setTimeInMillis(((long) timestamp*1000));
				ContentValues temp = new ContentValues();
				
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				//SLOT
				int slot = (hour)*15 + (Math.round(cal.get(Calendar.MINUTE)/4));
				temp.put("cons", cons);
				temp.put("tm_slot", slot);
				Log.i("TodayDetailed cons", "hour "+(hour)+"  "+tms+" "+cal.getTime()+" slot "+slot);
				result.add(temp);
				//Log.i("Day consump", "timestamp"+(timestamp*1000)+"hour "+hour+" cons: "+cons+" month "+(cal.get(Calendar.MONTH)+1)+" day of the week "+cal.get(Calendar.DAY_OF_WEEK)+" day "+cal.get(Calendar.DAY_OF_MONTH)+" week "+cal.get(Calendar.WEEK_OF_YEAR));
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		super.setData(result);

}
}

