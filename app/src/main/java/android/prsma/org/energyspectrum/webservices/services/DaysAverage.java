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

public class DaysAverage extends ConsumptionHttpRequest {

	private int hour; 
	private int day;
	private int month;
	private int year;

	public DaysAverage(int day, int month, int year){
		super.setRequestCode(year+"-"+month+"-"+day);
		super.setRequestType("day");

		this.hour=hour;
		this.day=day;
		this.month=month;
		this.year=year;
	}

	@Override
	public void parseData(String data) {
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
				
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				
				hour = hour==0?23:hour-1; 
				
				temp.put("cons", cons);
				temp.put("month", (cal.get(Calendar.MONTH)+1));
				temp.put("weekday",cal.get(Calendar.DAY_OF_WEEK));
				temp.put("day",this.day);
				temp.put("week", cal.get(Calendar.WEEK_OF_YEAR));
				temp.put("year",cal.get(Calendar.YEAR));
				temp.put("hour", hour);

				result.add(temp);
				//Log.i("Day consump", "timestamp"+(timestamp*1000)+"hour "+hour+" cons: "+cons+" month "+(cal.get(Calendar.MONTH)+1)+" day of the week "+cal.get(Calendar.DAY_OF_WEEK)+" day "+cal.get(Calendar.DAY_OF_MONTH)+" week "+cal.get(Calendar.WEEK_OF_YEAR));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 


			super.setData(result);
		}

		public int getHour() {   
			return hour;
		}

		public int getDay() {
			return day;
		}

		public int getMonth() {
			return month;
		}


		public int getYear() {
			return year;
		}


	}
