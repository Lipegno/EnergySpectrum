package android.prsma.org.energyspectrum.webservices.services;

import android.content.ContentValues;
import android.prsma.org.energyspectrum.webservices.ConsumptionHttpRequest;

import java.util.ArrayList;
import java.util.Calendar;

public class TodayConsService extends ConsumptionHttpRequest {
	
	public void parseData(String data){
		
		double cons_data[] =   {402.593217607722,1304211575,
								281.843561409125,1304215172,
								282.025298522286,1304218772,
								282.942610540035,1304222382,
								284.98327716083,1304225985,
								281.64084550663,1304229577,
								280.465323073604,1304233180,
								277.510656220074,1304236779,
								293.857481173876,1304240380,
								455.964656804465,1304243984,
								607.791580873065,1304247584,
								831.968975786991,1304251185,
								383.063914971789,1304254787,
								346.880464429331,1304258388,
								981.449277501231,1304261979,
								642.924048531662,1304265596,
								469.970950300084,1304269186,
								426.175961239617,1304272780,
								402.461770665041,1304276377,
								574.854946179503,1304279975,
								2290.48016517747,1304283571,
								2734.65997847127,1304287197,
								1603.53573340523,1304290790,
								973.921177110596,1304294385};
		
		ArrayList<ContentValues> result = new ArrayList<ContentValues>();
		
		for(int i=0;i<cons_data.length-1;i=i+2){

			double cons = cons_data[i];
			Calendar cal = Calendar.getInstance();

			cal.setTimeInMillis(((long) cons_data[i+1])*1000);
			ContentValues temp = new ContentValues();
			
			temp.put("cons", cons);
			temp.put("month", cal.get(Calendar.MONTH)+1);
			temp.put("weekday",cal.get(Calendar.DAY_OF_WEEK));
			temp.put("day",cal.get(Calendar.DAY_OF_MONTH));
			temp.put("week", cal.get(Calendar.WEEK_OF_YEAR));
			temp.put("year",cal.get(Calendar.YEAR));
			temp.put("hour", cal.get(Calendar.HOUR));
			result.add(temp);
			//Log.i("Month consump", "hour "+cal.get(Calendar.HOUR)+" cons: "+cons+" month "+(cal.get(Calendar.MONTH)+1)+" day of the week "+cal.get(Calendar.DAY_OF_WEEK)+" day "+cal.get(Calendar.DAY_OF_MONTH)+" week "+cal.get(Calendar.WEEK_OF_YEAR));
		}
		
		
		super.setData(result);
	}
	
}