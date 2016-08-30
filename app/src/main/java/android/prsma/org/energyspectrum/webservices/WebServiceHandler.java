package android.prsma.org.energyspectrum.webservices;

import android.content.ContentValues;
import android.content.Context;
import android.prsma.org.energyspectrum.database.DBManager;
import android.prsma.org.energyspectrum.dtos.RuntimeConfigs;
import android.prsma.org.energyspectrum.webservices.services.DaysAverage;
import android.prsma.org.energyspectrum.webservices.services.EnergyProdPrediction;
import android.prsma.org.energyspectrum.webservices.services.EnergyProduction;
import android.prsma.org.energyspectrum.webservices.services.MonthsAverage;
import android.prsma.org.energyspectrum.webservices.services.TodayConsService;
import android.prsma.org.energyspectrum.webservices.services.TodayDetailedCons;
import android.prsma.org.energyspectrum.webservices.services.WeeksAverage;
import android.util.Log;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

public final class WebServiceHandler {

	public static final String MODULE = "Web Service Handler";
	public static final String DAY_KEY = "today";
	public static final String YESTERDAY_KEY =	"yesterday";
	public static final String MONTH_KEY = "month";
	public static final String LAST_MONTH_KEY = "last_month";
	public static final String WEEK_KEY = "week";
	public static final String LAST_WEEK_KEY = "last_week";
	public static DBManager _dbManager;

	// variables to hold the data
	public ArrayList<ContentValues> month_cons;
	public ArrayList<ContentValues> last_month_cons;

	public ArrayList<ContentValues> week_cons;
	public ArrayList<ContentValues> last_week_cons;

	public ArrayList<ContentValues> day_cons;
	public ArrayList<ContentValues> yesterday_cons;

	public double today_total;
	public double yesterday_total;

	public double week_total;
	public double last_week_total;

	public double month_total;
	public double last_month_total;

	public Context _ctx;

	public WebServiceHandler(){

	}

	public static class WebServiceHandlerHolder{
		public static WebServiceHandler INSTANCE = new WebServiceHandler();
	}

	public static WebServiceHandler get_WS_Handler(){
		return WebServiceHandlerHolder.INSTANCE;
	}

	public ContentValues getInitData(){

		ContentValues result = new ContentValues();

		Calendar today = Calendar.getInstance();
		Timestamp tm = new Timestamp(today.getTimeInMillis());

		//if(day_cons==null){
			day_cons    = new ArrayList<ContentValues>();
			day_cons    = getDayConsumptionByHour(tm);
			today_total = calculateTotal(day_cons);
		//}
		result.put(DAY_KEY, today_total);						

		today.add(Calendar.DAY_OF_YEAR, -1);					//| -> removes a day to go to yesterday
		tm = new Timestamp(today.getTimeInMillis());			//|	
//		if(yesterday_cons==null){
			yesterday_cons = new ArrayList<ContentValues>();
			yesterday_cons  = getDayConsumptionByHour(tm);
			yesterday_total = calculateTotal(yesterday_cons);
//		}
		result.put(YESTERDAY_KEY, yesterday_total);

		today = Calendar.getInstance(); 						// back to today


//		if(month_cons==null){
			month_cons  = getMonthAverage(today.get(Calendar.MONTH)+1, today.get(Calendar.YEAR));
			month_total = calculateTotal(month_cons);
//		}
		result.put(MONTH_KEY,month_total);

//		if(last_month_cons==null){
			last_month_cons		 = getMonthAverage(today.get(Calendar.MONTH),today.get(Calendar.YEAR));
			last_month_total = calculateTotal(last_month_cons); 
//		}
		result.put(LAST_MONTH_KEY, last_month_total);

//		if(week_cons==null){
			week_cons 	= getWeekAverage(today.get(Calendar.WEEK_OF_YEAR), today.get(Calendar.YEAR));
			week_total  = calculateTotal(week_cons);
//		}
		result.put(WEEK_KEY, week_total);

//		if(last_week_cons==null){
			last_week_cons 		= getWeekAverage(today.get(Calendar.WEEK_OF_YEAR)-1, today.get(Calendar.YEAR));
			last_week_total = calculateTotal(last_week_cons);
//		}
		result .put(LAST_WEEK_KEY, last_week_total);
		
		getEnergyProduction();

		return result;
	}
	
	

	public double calculateTotal(ArrayList<ContentValues> data){

		if(data!=null){
		
		double result =0;
		for(int i=0;i<data.size();i++)
			result=result+data.get(i).getAsDouble("cons");

		return result;
		}
		else{
			return 0;
		}

	}

	/**
	 * Gets today's consumption so far
	 */
	public String getTodayConsumption(){
		TodayConsService dummy = new TodayConsService();
		dummy.run();
		try {
			dummy.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return  dummy.getData().get(0).get("cons")+"";
	}

	/**
	 * Gets the month energy. It performs a query to check if the requested data is in the local database, if not query through the webservice.
	 * @param month - curent month (0- January ... 12 - December)
	 * @return Content Values array with the consumption for each day of the month (keys:"cons","month","weekday","day","week","year") 
	 */
	public ArrayList<ContentValues> getMonthAverage(int month, int year){

		_dbManager = DBManager.getDBManager();

		Log.i(MODULE, "started request month");
		Calendar cal = Calendar.getInstance();


		if(month< cal.get(Calendar.MONTH)+1){				// its not the current month
			ArrayList<ContentValues> cons_data =_dbManager.getDayConsumptionDataMonth(month);
			Log.i(MODULE, "previous month");
			if(cons_data.size()>0){
				ContentValues lastval = cons_data.get(cons_data.size()-1);
				Log.i(MODULE, "querying local db");

				if(lastval.getAsInteger("day")<cal.getActualMaximum(Calendar.DAY_OF_MONTH)){	 			// if there some days missing query againu				
					// do request de todos, insert Distinct asseguir
					Log.i(MODULE, "asking webserver");
					cons_data=processMonthRequest(month,year);
					if(cons_data!=null && cons_data.size()>0)		// we have this historical consumption
						_dbManager.insertDayConsumptionData(cons_data);

				}
			}
			else{
				Log.i(MODULE, "asking webserver");
				cons_data=processMonthRequest(month,year);			// do the actual request
				if(cons_data!=null && cons_data.size()>0)
					_dbManager.insertDayConsumptionData(cons_data);				
			}
			//month_cons=cons_data;
			return cons_data;
		}else{
			Log.i(MODULE, "current month");
			ArrayList<ContentValues> cons_data =_dbManager.getDayConsumptionDataMonth(month);
			Log.i(MODULE, "querying local db");

			if(cons_data.size()>0){
				Calendar cal2 = Calendar.getInstance();
				ContentValues lastval = cons_data.get(cons_data.size()-1);

				if(lastval.getAsInteger("day")<cal2.get(Calendar.DAY_OF_MONTH)){
					Log.i(MODULE, "querying webserver");
					cons_data=processMonthRequest(month,year);			// do the actual request
					if(cons_data!=null && cons_data.size()>0)
						_dbManager.insertDayConsumptionData(cons_data);				

					month_cons=cons_data;  // mais dia de hoje
					return cons_data;

				}else{
					Log.i(MODULE, "local data ok");

					// return consumo do mes + hoje.
					month_cons=cons_data;
					return cons_data;
				}

			}
			else{
				Log.i(MODULE, "querying webserver");
				cons_data=processMonthRequest(month,year);			// do the actual request
				if(cons_data!=null && cons_data.size()>0)
					_dbManager.insertDayConsumptionData(cons_data);				
			}
			month_cons = cons_data;
			return cons_data;
		}
	}

	/**
	 * Gets the week energy. It performs a query to check if the requested data is in the local database, if not query through the webservice.
	 * @param week - curent week (1-  fist week of January ... 52 -  last week of December)
	 * @return Content Values array with the consumption for each day of the week(keys:"cons","month","weekday","day","week","year") 
	 */
	public ArrayList<ContentValues> getWeekAverage(int week, int year){

		_dbManager = DBManager.getDBManager();

		Log.i(MODULE, "started request");
		Calendar cal = Calendar.getInstance();


		if(week< cal.get(Calendar.WEEK_OF_YEAR)){				// its not the current week

			ArrayList<ContentValues> cons_data =_dbManager.getDayConsumptionDataWeek(week);

			if(cons_data.size()>0){

				ContentValues lastval = cons_data.get(cons_data.size()-1);

				if(lastval.getAsInteger("weekday")<7){	 			// if there some days missing query again				
					// do request de todos, insert Distinct asseguir
					cons_data=processWeekRequest(week,year);   // do the actual request
					if(cons_data!=null && cons_data.size()>0)	// we have this historical consumption
						_dbManager.insertDayConsumptionData(cons_data);

				}
			}
			else{
				cons_data=processWeekRequest(week,year);   // do the actual request
				if(cons_data!=null && cons_data.size()>0)	// we have this historical consumption
					_dbManager.insertDayConsumptionData(cons_data);
			}
			//week_cons=cons_data;
			return cons_data;
		}else{

			ArrayList<ContentValues> cons_data =_dbManager.getDayConsumptionDataWeek(week);

			if(cons_data.size()>0){
				Calendar cal2 = Calendar.getInstance();
				ContentValues lastval = cons_data.get(cons_data.size()-1);
				if(lastval.getAsInteger("weekday")<cal2.get(Calendar.DAY_OF_WEEK)){
					cons_data=processWeekRequest(week,year);			// do the actual request
					if(cons_data!=null && cons_data.size()>0)
						_dbManager.insertDayConsumptionData(cons_data);				

					week_cons=cons_data;  // mais dia de hoje
					return cons_data;

				}else{
					// return consumo da semana + hoje.
					week_cons=cons_data;
					return cons_data;
				}

			}
			else{
				cons_data=processWeekRequest(week,year);			// do the actual request
				if(cons_data!=null&&cons_data.size()>0)
					_dbManager.insertDayConsumptionData(cons_data);		
				
				week_cons = cons_data;
				return cons_data;
			}

		}

	}

	public ArrayList<ContentValues> getDayConsumptionByHour(Timestamp tm){
		_dbManager = DBManager.getDBManager();

		Log.i(MODULE, "started request");
		Calendar cal = Calendar.getInstance();

		Calendar queryCal = Calendar.getInstance();
		queryCal.setTimeInMillis(tm.getTime());

		if(queryCal.get(Calendar.DAY_OF_YEAR)< cal.get(Calendar.DAY_OF_YEAR)){				// its not the current day

			ArrayList<ContentValues> cons_data =_dbManager.getDayConsumptionByHour(queryCal.get(Calendar.DAY_OF_MONTH), queryCal.get(Calendar.MONTH)+1, queryCal.get(Calendar.YEAR));
			if (cons_data.size()>0) {
				ContentValues lastval = cons_data.get(cons_data.size()-1);

				if(lastval.getAsInteger("hour")<23 || cons_data.size()<20){	 			// if there some days missing query againu				
					// do request de todos, insert Distinct asseguir
					cons_data=processDayRequest(queryCal);   // do the actual request
					if(cons_data!=null && cons_data.size()>0)	// we have this historical consumption
						_dbManager.insertDayConsumptionByHour(cons_data);
				}
			} else{
				cons_data=processDayRequest(queryCal);   // do the actual request
				if(cons_data!=null && cons_data.size()>0)	// we have this historical consumption
					_dbManager.insertDayConsumptionByHour(cons_data);
			}

			//day_cons=cons_data;
			return cons_data;
		}else{

			ArrayList<ContentValues> cons_data =_dbManager.getDayConsumptionByHour(queryCal.get(Calendar.DAY_OF_MONTH), queryCal.get(Calendar.MONTH)+1, queryCal.get(Calendar.YEAR));

			if(cons_data.size()>0){
				Calendar cal2 = Calendar.getInstance();
				ContentValues lastval = cons_data.get(cons_data.size()-1);

				if(lastval.getAsInteger("hour")<cal2.get(Calendar.HOUR_OF_DAY)){
					cons_data=processDayRequest(queryCal);   // do the actual request
					if(cons_data!=null && cons_data.size()>0)
						_dbManager.insertDayConsumptionByHour(cons_data);				

					day_cons=cons_data;  // mais dia de hoje
					return cons_data;

				}else{
					// return consumo da semana + hoje.
					cons_data=processDayRequest(queryCal);   // do the actual request
					if(cons_data!=null && cons_data.size()>0){
						_dbManager.insertDayConsumptionByHour(cons_data);				
						_dbManager.updateLastValue(cons_data);
					}
					
					day_cons=cons_data;
					return cons_data;
				}

			}
			else{
				cons_data=processDayRequest(queryCal);   // do the actual request
				if(cons_data!=null && cons_data.size()>0)
					_dbManager.insertDayConsumptionByHour(cons_data);				
			
			day_cons = cons_data;
			return cons_data;

		}	}
	}
	
	public ArrayList<ContentValues> getEnergyProduction(){
		ArrayList<ContentValues> cons_data = new ArrayList<ContentValues>(); 

		EnergyProduction prod = new EnergyProduction("http://aveiro.m-iti.org/sinais_energy_production/services/today_production_request.php");
		prod._appCtx = _ctx;
		prod.run();
		try {
			prod.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cons_data = prod.getData();

		return cons_data;
	}
	
	public ArrayList<ContentValues> getEnergyProductionPrediction(){
		ArrayList<ContentValues> cons_data = new ArrayList<ContentValues>();
		EnergyProdPrediction pred = new EnergyProdPrediction("http://aveiro.m-iti.org/sinais_energy_production/services/today_prediction_request.php");
		pred._appCtx = _ctx;
		pred.run();
		try {
			pred.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cons_data = pred.getData();
		Log.i(MODULE,"AQUIIII "+cons_data.size());
		return cons_data;
	}
	
	public ArrayList<ContentValues> getTodayDetailedCons(){
		TodayDetailedCons detail_cons = new TodayDetailedCons();
		detail_cons.run();
		try {
			detail_cons.join();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		return detail_cons.getData();
	}

	private ArrayList<ContentValues> processMonthRequest(int month, int year){

		ArrayList<ContentValues> cons_data = new ArrayList<ContentValues>();
		if(isOnline()){
		MonthsAverage dummy = new MonthsAverage(month,year);
		dummy._appCtx=_ctx;
		dummy.run();
		try {
			dummy.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		cons_data = dummy.getData();

		return cons_data;
		}else{
			Log.d(MODULE, "System offline");
			_dbManager = DBManager.getDBManager();
			cons_data = _dbManager.getDayConsumptionDataMonth(month);
			return cons_data;
		}
	}

	private ArrayList<ContentValues> processWeekRequest(int week, int year){

		ArrayList<ContentValues> cons_data = new ArrayList<ContentValues>();
		if(isOnline()){
			WeeksAverage dummy = new WeeksAverage(week,year);
			dummy._appCtx=_ctx;
			dummy.run();
			try {
				dummy.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			cons_data = dummy.getData();

			return cons_data;
		}else{
			Log.d(MODULE, "System offline");
			_dbManager = DBManager.getDBManager();
			cons_data = _dbManager.getDayConsumptionDataWeek(week);
			return cons_data;
		}
	}

	public ArrayList<ContentValues> processDayRequest(Calendar queryCal){
		ArrayList<ContentValues> cons_data = new ArrayList<ContentValues>();
		Log.i(MODULE,queryCal.get(Calendar.DAY_OF_MONTH)+" "+(queryCal.get(Calendar.MONTH)+1)+" "+queryCal.get(Calendar.YEAR));
		if(isOnline()){
			DaysAverage dummy = new DaysAverage(queryCal.get(Calendar.DAY_OF_MONTH), queryCal.get(Calendar.MONTH)+1, queryCal.get(Calendar.YEAR));
			dummy._appCtx=_ctx;
			dummy.run();
			try {	
				dummy.join();
			}catch (InterruptedException e){

			}
			cons_data = dummy.getData();
            cons_data = getDummyValues(15,"hour");
			return cons_data;
		}else{
			Log.d(MODULE, "System offline");
			_dbManager = DBManager.getDBManager();
			cons_data =_dbManager.getDayConsumptionByHour(queryCal.get(Calendar.DAY_OF_MONTH), queryCal.get(Calendar.MONTH)+1, queryCal.get(Calendar.YEAR));
			return cons_data;
		}	
	}

    private ArrayList<ContentValues> getDummyValues(int size, String key){
       ArrayList cons_data = new ArrayList<ContentValues>();
        for(int i=0;i<size;i++){
            ContentValues cv = new ContentValues();
            cv.put(key,i);
            cv.put("cons",(200+Math.random()*800));
            cons_data.add(cv);
        }
        return cons_data;
    }

	private boolean isOnline(){
		return RuntimeConfigs.getConfigs().getMenuHandler().isOnline();
	}

	//	public void getWeekConsumptionByDay(String date){
	//		
	//	}
	//	
	//	public void getMonthConsumptionByDay(String date){
	//		
	//	}
	//	
	//	public void getDayConsumption(String date){
	//		
	//	}
	//	
	//	public void getDayTotal(String date){
	//		
	//	}
	//
	//    public void getWeekTotal(String date){
	//		
	//	}
	//	
	//	public void getMonthTotal(String date){
	//		
	//	}

}
