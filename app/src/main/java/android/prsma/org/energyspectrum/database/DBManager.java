package android.prsma.org.energyspectrum.database;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.prsma.org.energyspectrum.dtos.EventSampleDTO;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public final class DBManager {

	private static final String MODULE = "Db Manager";

	private static final String DB_NAME = "pm.sqlite";
	private static final String EVENTS_TABLE = "power_event";
	private static final String DAY_CONS_TABLE = "day_consumption";
	private static final String DAILY_CONS_TABLE = "daily_consumption";
	private static final String RECOMENDATIONS_TABLE = "recomendations";
	private static final String APPLIANCES_TABLE =	"appliances";

	private static SQLiteDatabase db;

	public long last_evt_Inserted;

	public DBManager(){

		//		File test  = new File(Environment.getExternalStorageDirectory() + "/documents/");
		//		String[] test2 = test.list();

		try {
			if (db == null)
				db = SQLiteDatabase.openDatabase(
						Environment.getExternalStorageDirectory() + "/PowerMeterMobile/" + DB_NAME,
						null,
						0);
			//Log.i(MODULE,"Db openned");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class DBHolder {
		public static final DBManager INSTANCE = new DBManager();
	}

	public static DBManager getDBManager(){

		return DBHolder.INSTANCE;
	}

	public static boolean databaseExists()	{

		File storage_file = new File(Environment.getExternalStorageDirectory(), "/PowerMeterMobile/"+DB_NAME);

		return storage_file.exists();
	}
	public static void initDatabase(){
		try{
			File db_storage_file = new File(Environment.getExternalStorageDirectory(), "/PowerMeterMobile");
			if(!db_storage_file.exists())
				if(! db_storage_file.mkdir()){
					//Log.e(MODULE,"error creating /touchCloud directory");
					return;
				}
			db_storage_file = new File(db_storage_file, DB_NAME);

			if(! db_storage_file.exists()){
				db_storage_file.createNewFile();
				createDatabase(db_storage_file.getAbsolutePath());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public synchronized boolean insertEventData(ArrayList<EventSampleDTO> events){

		boolean res = false;
		SQLiteStatement stm = null;

		db.beginTransaction();
		try {
			// Create the insert statement
			String sql = 
					"insert into " +
							"	" + EVENTS_TABLE 
							+ " ( event_id, appliance_id,appliance_guess,deltaPMean)" +
							"	values (?,?,?,?)";
			stm = db.compileStatement(sql);

			// Iterate over the array, extracting values as necessary
			for (EventSampleDTO row : events) {
				stm.bindLong(1, row.get_event_id());
				stm.bindString(3, row.get_guess());
				stm.bindLong(2, row.get_appliance_id());
				stm.bindLong(4,row.get_deltaPMean());
				if (stm.executeInsert() <= 0)
					Log.d(MODULE, "Failed insertion of event into database");
			}
			// Signal success and update result value
			db.setTransactionSuccessful();
			res = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			stm.close();
			db.endTransaction();
			last_evt_Inserted = events.get(events.size()-1).get_event_id();
			//Log.d(MODULE, "new event Data inserted");
		}

		return res;
	}

	public synchronized boolean insertEventData(EventSampleDTO event){

		boolean res = false;
		SQLiteStatement stm = null;

		db.beginTransaction();
		try {
			// Create the insert statement
			String sql = 
					"insert into " +
							"	" + EVENTS_TABLE 
							+ " ( event_id, appliance_id,appliance_guess,deltaPMean,timestamp)" +
							"	values (?,?,?,?,?)";
			stm = db.compileStatement(sql);

			stm.bindLong(1, event.get_event_id());
			stm.bindString(3, event.get_guess());
			stm.bindLong(2, event.get_appliance_id());
			stm.bindLong(4,event.get_deltaPMean());
			stm.bindString(5, event.get_timestamp()+"");
			if (stm.executeInsert() <= 0)
				//Log.d(MODULE, "Failed insertion of event into database");

			// Signal success and update result value
			db.setTransactionSuccessful();
			res = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			stm.close();
			db.endTransaction();
			last_evt_Inserted = event.get_event_id();
			//Log.d(MODULE, "new event Data inserted");
		}

		return res;
	}

	public synchronized ArrayList<EventSampleDTO> getLastEvents(){

		Cursor c;
		ArrayList<EventSampleDTO> result = new ArrayList<EventSampleDTO>();
		int event_id					 = 0;
		int appliance_id 				 = 0;
		int deltaPMean					 = 0;
		String appliance_guess			 = "";
		String timestamp				 = "";

		int[] cluster_info = {1,2,3};

		try{
			c = db.query(EVENTS_TABLE, new String[]{"event_id, appliance_id , appliance_guess , deltaPMean, timestamp"}, null, null, null, null, "id DESC","0,20");

			while(c.moveToNext()){
				event_id	 	= c.getInt(0);
				appliance_id 	= c.getInt(1);
				appliance_guess	= c.getString(2);
				deltaPMean		= c.getInt(3);
				timestamp 		= c.getString(4);
				result.add(0,new EventSampleDTO(appliance_guess, null, deltaPMean, appliance_id, event_id, timestamp+"",cluster_info ));

			}

		}catch(Exception e){
			e.printStackTrace();
		}

		return result;

	}

	public synchronized void insertUserEvent(String view){

		int view_id=0;

		if(view.equals("Animation Activity"))
			view_id=0;
		if(view.equals("HOME"))
			view_id=1;
		if(view.equals("DAY CONSUMPTION"))
			view_id=2;
		if(view.equals("WEEK CONSUMPTION"))
			view_id=3;
		if(view.equals("MONTH CONSUMPTION"))
			view_id=4;
		if(view.equals("EVENTS CHART"))
			view_id=5;

		String timestamp = System.currentTimeMillis()+"";

		SQLiteStatement stm = null;

		db.beginTransaction();

		String sql="Insert Into user_events(view, view_id, timestamp) values('"+view+"',"+view_id+",'"+timestamp+"')";
		stm = db.compileStatement(sql);
		stm.executeInsert();
		db.setTransactionSuccessful();
		stm.close();
		db.endTransaction();
	}

	/**
	 * Inserts new appliances in the local database
	 * @param apps ContentValue with the name gourp_id and id of the appliance
	 * @return
	 */
	public synchronized boolean insertAppliances(ArrayList<ContentValues> apps){

		SQLiteStatement stm = null;
		boolean res=false;
		db.beginTransaction();
		try {

			String sql="";

			for(int i=0;i<apps.size();i++){

				sql ="INSERT INTO "+APPLIANCES_TABLE+"(appliance,group_id,appliance_id)"+ 
						"SELECT '"+apps.get(i).getAsString("name")+"',"+apps.get(i).getAsInteger("group_id")+","+apps.get(i).getAsInteger("app_id")+
						" WHERE NOT EXISTS (SELECT 1 FROM appliances WHERE appliance='"+apps.get(i).getAsString("name")+"' and group_id="+apps.get(i).getAsInteger("group_id")+" and appliance_id="+apps.get(i).getAsInteger("app_id")+")";

				stm = db.compileStatement(sql);

				if (stm.executeInsert() <= 0)
					//Log.d(MODULE, "Failed insertion of appliance into database");

				// Signal success and update result value

				res = true;
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			res=false;
			e.printStackTrace();
		} finally	{
			stm.close();
			db.endTransaction();
			//Log.d(MODULE, "new appliance data inserted");

		}

		return true;
	}

	/**
	 * Queries the local database for the appliance with the given name, if it exists return their app_id
	 * @param app_name - appliance name
	 * @return
	 */
	public synchronized int getApplianceByName(String app_name){

		Cursor c;
		int app_id=-1;
		try{
			c = db.rawQuery("SELECT appliance_id from "+APPLIANCES_TABLE+" where appliance='"+app_name+"'", null);

			while(c.moveToNext()){
				app_id=c.getInt(0);
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return app_id;
	}
	/**
	 * function that returns the name of the specified appliance
	 * @param app_id - appliance id
	 * @return
	 */
	public synchronized String getApplianceName(int app_id){
		Cursor c;


		c =  db.rawQuery("SELECT appliance from "+APPLIANCES_TABLE+" where appliance_id="+app_id, null); 
		String name  = "";
		while(c.moveToNext()){
			name = c.getString(0);
		}

		return name;
	}
	public synchronized ArrayList<ContentValues> getMonthAverage(){
		Cursor c;
		int day  	=	0;
		double cons	=	0.0;
		ArrayList<ContentValues> result = new ArrayList<ContentValues>();
		
		c = db.rawQuery("SELECT AVG(energy_consumption), day from day_consumption GROUP BY day", null);
		while(c.moveToNext()){
			day = c.getInt(1);
			cons = c.getDouble(0);
			ContentValues temp = new ContentValues();
			temp.put("avg", cons);
			temp.put("day", day);
			result.add(temp);
		}
		return result;		
		}
	public synchronized ArrayList<ContentValues> geWeekAverage(){
		Cursor c;
		int day_of_week  	=	0;
		double cons	=	0.0;
		ArrayList<ContentValues> result = new ArrayList<ContentValues>();
		
		c = db.rawQuery("SELECT AVG(energy_consumption), day_of_week from day_consumption GROUP BY day_of_week", null);
		while(c.moveToNext()){
			day_of_week = c.getInt(1);
			cons = c.getDouble(0);
			ContentValues temp = new ContentValues();
			temp.put("avg", cons);
			temp.put("day", day_of_week);
			result.add(temp);
		}
		return result;		
		}
	public synchronized ArrayList<ContentValues> getDayConsumptionDataMonth(int month_id){
		Cursor c;
		ArrayList<ContentValues> result = new ArrayList<ContentValues>();
		int day					  = 0;
		int day_of_week			  = 0;
		int week				  = 0;
		int month			      = 0;
		int year		      	  =0;
		double energy_consumption = 0.0;

		try{
			c = db.rawQuery("Select day, day_of_week , week , month, year, energy_consumption from "+DAY_CONS_TABLE+" where month="+month_id,null);

			while(c.moveToNext()){
				day	 	           = c.getInt(0);
				day_of_week 	   = c.getInt(1);
				week	           = c.getInt(2);
				month		       = c.getInt(3);
				year 		       = c.getInt(4);
				energy_consumption = c.getDouble(5);

				ContentValues temp = new ContentValues();
				temp.put("cons", energy_consumption);
				temp.put("month", month);
				temp.put("weekday", day_of_week);
				temp.put("day", day);
				temp.put("week",week);
				temp.put("year",year);
				result.add(temp);

			}

		}catch(Exception e){
			e.printStackTrace();
		}

		return result;
	}

	public synchronized ArrayList<ContentValues> getDayConsumptionDataWeek(int week_id){
		Cursor c;
		ArrayList<ContentValues> result = new ArrayList<ContentValues>();
		int day					  = 0;
		int day_of_week			  = 0;
		int week				  = 0;
		int month			      = 0;
		int year		      	  =0;
		double energy_consumption = 0.0;

		try{
			c = db.rawQuery("Select day, day_of_week , week , month, year, energy_consumption from "+DAY_CONS_TABLE+" where week="+week_id,null);

			while(c.moveToNext()){
				day	 	           = c.getInt(0);
				day_of_week 	   = c.getInt(1);
				week	           = c.getInt(2);
				month		       = c.getInt(3);
				year 		       = c.getInt(4);
				energy_consumption = c.getDouble(5);

				ContentValues temp = new ContentValues();
				temp.put("cons", energy_consumption);
				temp.put("month", month);
				temp.put("weekday", day_of_week);
				temp.put("day", day);
				temp.put("week",week);
				temp.put("year",year);
				result.add(temp);

			}

		}catch(Exception e){
			e.printStackTrace();
		}

		return result;
	}

	public synchronized boolean insertDayConsumptionData(ArrayList<ContentValues> data){

		boolean res = false;
		SQLiteStatement stm = null;

		db.beginTransaction();
		try {
			// Create the insert statement
			for(ContentValues row:data){
				String sql = 
						"INSERT INTO day_consumption (day, day_of_week , week , month, year, energy_consumption)"+
								" SELECT "+row.getAsInteger("day")+" , "+row.getAsInteger("weekday")+ ", "+row.getAsInteger("week")+" , "+row.getAsInteger("month")+" , "+row.getAsInteger("year")+" , "+row.getAsDouble("cons")+
								" WHERE NOT EXISTS (SELECT 1 FROM "+DAY_CONS_TABLE+" WHERE day= "+row.getAsInteger("day")+" and month="+row.getAsInteger("month")+")";

				stm = db.compileStatement(sql);

				if (stm.executeInsert() <= 0)
					//Log.d(MODULE, "Failed insertion of day sample into database");

				// Signal success and update result value

				res = true;
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			res=false;
			e.printStackTrace();
		} finally	{
			stm.close();
			db.endTransaction();
			//Log.d(MODULE, "new day data inserted");

		}

		return res;

	}

	public synchronized ArrayList<ContentValues> getDayConsumptionByHour(int day_id, int month_id, int year_id){

		Cursor c;
		ArrayList<ContentValues> result = new ArrayList<ContentValues>();
		int hour				  = 0;
		int day					  = 0;
		int week				  = 0;
		int month			      = 0;
		int year				  = 0;
		double energy_consumption = 0.0;

		try{
			c = db.rawQuery("select hour, day, week, month, energy_consumption, year from "+DAILY_CONS_TABLE+
					" where day="+day_id+" and month="+month_id+" and year="+year_id+" group by hour order by day,hour",null);

			while(c.moveToNext()){
				hour 	           = c.getInt(0);
				day	 	           = c.getInt(1);
				week		       = c.getInt(2);
				month		       = c.getInt(3);
				energy_consumption = c.getDouble(4);
				year			   = c.getInt(5);

				ContentValues temp = new ContentValues();
				temp.put("cons", energy_consumption);
				temp.put("hour", hour);
				temp.put("month", month);
				temp.put("day", day);
				temp.put("week",week);
				temp.put("year", year);
				result.add(temp);

			}  

		}catch(Exception e){
			e.printStackTrace();
		}

		return result;
	}

	public synchronized boolean insertDayConsumptionByHour(ArrayList<ContentValues> data){

		boolean res = false;
		SQLiteStatement stm = null;

		db.beginTransaction();
		try {
			// Create the insert statement
			for(ContentValues row:data){
				String sql = 
						"INSERT INTO "+DAILY_CONS_TABLE+" (hour , day , week , month, energy_consumption, year)"+
								" SELECT "+row.getAsInteger("hour")+" , "+row.getAsInteger("day")+" , "+row.getAsInteger("week")+" , "+row.getAsInteger("month")+"  , "+row.getAsDouble("cons")+"  , "+row.getAsInteger("year")+
								" WHERE NOT EXISTS (SELECT 1 FROM "+DAILY_CONS_TABLE+" WHERE day= "+row.getAsInteger("day")+" and month="+row.getAsInteger("month")+" and year="+row.getAsInteger("year")+" and hour="+row.getAsInteger("hour")+")";

				stm = db.compileStatement(sql);

				if (stm.executeInsert() <= 0)
					//Log.d(MODULE, "Failed insertion of day sample into database");


				res = true;
			}
			db.setTransactionSuccessful();
			stm.close();
		} catch (Exception e) {
			res=false;
			e.printStackTrace();
		} finally	{
			db.endTransaction();
			//Log.d(MODULE, "new day data inserted");
			updateDayTotal(data);	
		}

		return res;

	}

	public synchronized boolean updateLastValue(ArrayList<ContentValues> cons_data){
		boolean res = false;
		SQLiteStatement stm = null;
		db.beginTransaction();
		ContentValues data = cons_data.get(cons_data.size()-1);
		try{
			String sql = "UPDATE "+DAILY_CONS_TABLE+" set energy_consumption="+data.getAsDouble("cons")+" " +
					"where hour="+data.getAsInteger("hour")+" and day="+data.getAsInteger("day")+" and week="+data.getAsInteger("week")+" and month="+data.getAsInteger("month");

			stm = db.compileStatement(sql);

			if(stm.executeInsert()<=0)
				//Log.d(MODULE, "Failed updtating the dailyconsumption");

			res = true;
			db.setTransactionSuccessful();
		}catch(Exception e){
			res=false;
			e.printStackTrace();
		}finally{
			stm.close();
			db.endTransaction();
			//Log.i(MODULE, "Daily consumption updated");
			updateDayTotal(cons_data);					// updates the total of the current day with the sum of each hour 

		}


		return res;
	}

	public synchronized boolean updateDayTotal(ArrayList<ContentValues> cons_data){

		boolean res = false;
		SQLiteStatement stm = null;
		ContentValues data = cons_data.get(cons_data.size()-1);
		double total_cons = 0;// WebServiceHandler.get_WS_Handler().calculateTotal(cons_data);
		db.beginTransaction();
		try {
			// Create the insert statement
			String sql = 
					"UPDATE day_consumption set energy_consumption="+total_cons+" where day="+data.getAsInteger("day")+" and week="+data.getAsInteger("week")+" and month="+data.getAsInteger("month")+" and year="+data.getAsInteger("year");

			stm = db.compileStatement(sql);

			if (stm.executeInsert() <= 0)
				//Log.d(MODULE, "Failed insertion of day sample into database");

			// Signal success and update result value

			res = true;

			db.setTransactionSuccessful();
		} catch (Exception e) {
			res=false;
			e.printStackTrace();
		} finally	{
			stm.close();
			db.endTransaction();
			//Log.d(MODULE, "new day data updated");
		}

		return res;

	}

	public synchronized String getRecomendation(){

		Cursor c;


		c =  db.rawQuery("SELECT recomendation from "+RECOMENDATIONS_TABLE, null); 
		String[] data = new String[c.getCount()+1];
		int i=0;
		while(c.moveToNext()){
			data[i] = c.getString(0);
			i++;
		}
		data[i]="";

		return data[(int) Math.round(Math.random()*33)];
	}

	public synchronized ArrayList<ContentValues> getDayAverage(){

		Cursor c;
		ArrayList<ContentValues> result = new ArrayList<ContentValues>();
		int hour				  = 0;
		double energy_consumption = 0.0;

		try{
			c = db.rawQuery("select hour,avg(energy_consumption) from "+DAILY_CONS_TABLE+" group by hour",null);

			while(c.moveToNext()){
				hour 	           = c.getInt(0);
				energy_consumption = c.getDouble(1);

				ContentValues temp = new ContentValues();
				temp.put("cons", energy_consumption);
				temp.put("hour", hour);
				result.add(temp);

			}  

		}catch(Exception e){
			e.printStackTrace();
		}

		return result;
	}
	public boolean insertProductionData(String tms, int total, int termica, int hidrica, int eolica, int biomassa, int foto){

		SQLiteStatement stm;
		
		String sql = "INSERT INTO production_values(timestamp,total,termica,hidrica,eolica,biomassa,foto) "+
						" Select '"+tms+"', "+total+", "+termica+", "+hidrica+", "+eolica+", "+biomassa+", "+foto+" "+
							"where not exists (SELECT 1 FROM production_values where timestamp='"+tms+"')";
	
		stm = db.compileStatement(sql);
		
		if(stm.executeInsert() != -1){
			//Log.d(MODULE, "prod data inserted");
			return true;
			}
		else {
			//Log.d(MODULE, "no new data to add");
			return false;
		}
		
	}
	public ArrayList<ContentValues> getProductionAverage(){
		Cursor c;
		ArrayList<ContentValues> result = new ArrayList<ContentValues>();
		int hour  = 0;
		double total = 0.0;
		double termica = 0.0;
		double hidrica = 0.0;
		double eolica = 0.0;
		double biomassa = 0.0;
		double foto = 0.0;
		
// 	"Select avg(total) as total, avg(termica) as termica, avg(hidrica) as hidrica, avg(eolica) as eolica, avg(biomassa) as biomassa, avg(foto) as foto, strftime('%H',timestamp)" +
//		" from production_values group by strftime('%H',timestamp)";
		try{
			c = db.rawQuery("select * from prod_daily_avg",null);

			while(c.moveToNext()){
				total 	 = c.getDouble(0);
				termica  = c.getDouble(1);
				hidrica  = c.getDouble(2);
				eolica   = c.getDouble(3);
				biomassa = c.getDouble(4);
				foto     = c.getDouble(5);
				hour     = c.getInt(6);

				ContentValues temp = new ContentValues();
				temp.put("total", total);
				temp.put("termica", termica);
				temp.put("hidrica", hidrica);
				temp.put("eolica", eolica);
				temp.put("biomassa", biomassa);
				temp.put("foto", foto);
				temp.put("hour", hour);
				result.add(temp);

			}  

		}catch(Exception e){
			e.printStackTrace();
		}
		return result;

	}
	/*
	 * Demo comment yo yo yo .... yo yo yo 2
	 */
	public double getTodayRenewPrecentage(){
		double result=0.0;
		Calendar cal = Calendar.getInstance();
		String day = cal.get(Calendar.DAY_OF_MONTH)>9?cal.get(Calendar.DAY_OF_MONTH)+"":"0"+cal.get(Calendar.DAY_OF_MONTH);
		String month = (cal.get(Calendar.MONTH)+1)>9?(cal.get(Calendar.MONTH)+1)+"":"0"+(cal.get(Calendar.MONTH)+1);
		String data = cal.get(Calendar.YEAR)+"-"+month+"-"+day;
		double total = 0,hidrica = 0,eolica = 0,foto = 0;
		try{
			Cursor c;
			c = db.rawQuery("select avg(total), avg(hidrica), avg(eolica), avg(foto) from production_values where timestamp > '"+data+"' ", null);
			while(c.moveToNext()){
				total = c.getDouble(0);
				hidrica = c.getDouble(1);
				eolica  = c.getDouble(2);
				foto    = c.getDouble(3);
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		//Log.i(MODULE, data);
		result = (hidrica+eolica+foto)/total;
		//Log.i(MODULE, "total "+total+" hidrica "+hidrica+" eolica "+eolica+" "+" foto "+foto+" percentage "+result);
		
		return Double.isNaN(result) ? 0 : result;
	}
	public double getThisMonthRenewPrecentage(){
		double result=0.0;
		Calendar cal = Calendar.getInstance();
		String month = (cal.get(Calendar.MONTH)+1)>9?(cal.get(Calendar.MONTH)+1)+"":"0"+(cal.get(Calendar.MONTH)+1);
		String data = cal.get(Calendar.YEAR)+"-"+month;
		double total = 0,hidrica = 0,eolica = 0,foto = 0;
		try{
			Cursor c;
			c = db.rawQuery("select avg(total), avg(hidrica), avg(eolica), avg(foto) from production_values where timestamp > '"+data+"' ", null);
			while(c.moveToNext()){
				total = c.getDouble(0);
				hidrica = c.getDouble(1);
				eolica  = c.getDouble(2);
				foto    = c.getDouble(3);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		//Log.i(MODULE, data);
		result = (hidrica+eolica+foto)/total;
		//Log.i(MODULE, "total "+total+" hidrica "+hidrica+" eolica "+eolica+" "+" foto "+foto+" percentage "+result);
		return Double.isNaN(result) ? 0 : result;
	}
	public double getThisWeekRenewPercentage(){
		double result =0;
		Calendar cal = Calendar.getInstance();
		int week = cal.get(Calendar.WEEK_OF_YEAR)-1;
		double total = 0,hidrica = 0,eolica = 0,foto = 0;
		Cursor c;
		c = db.rawQuery("select avg(total), avg(hidrica), avg(eolica), avg(foto) from production_values where strftime('%W', timestamp)='"+week+"'", null);
		while(c.moveToNext()){
			total = c.getDouble(0);
			hidrica = c.getDouble(1);
			eolica  = c.getDouble(2);
			foto    = c.getDouble(3);
		}
		//Log.i(MODULE, "semana "+week);
		result = (hidrica+eolica+foto)/total;
		//Log.i(MODULE, "total "+total+" hidrica "+hidrica+" eolica "+eolica+" "+" foto "+foto+" percentage "+result);
		return Double.isNaN(result) ? 0 : result;
	}
	
	
	private static void createDatabase(String path){
		//Log.i(MODULE, "Creating new database");
		db = SQLiteDatabase.openDatabase(
				path,
				null,
				SQLiteDatabase.CREATE_IF_NECESSARY);
		// create anchor events table


		String  sql = "CREATE TABLE day_consumption ("+
				" id integer NOT NULL PRIMARY KEY AUTOINCREMENT, "+
				" day integer NOT NULL,"+
				" day_of_week integer NOT NULL,"+
				" week  integer NOT NULL,"+
				" month  integer NOT NULL,"+
				" year  integer NOT NULL,"+
				" energy_consumption real NOT NULL )";
		db.execSQL(sql);

		sql = " CREATE TABLE month_consumption ("+
				" id integer NOT NULL PRIMARY KEY AUTOINCREMENT,"+
				" month integer NOT NULL,"+
				" year integer NOT NULL,"+
				" energy_consumption real NOT NULL)";
		db.execSQL(sql);

		sql =" CREATE TABLE power_event ("+
				" id integer NOT NULL PRIMARY KEY AUTOINCREMENT,"+
				" event_id integer,"+
				" appliance_id integer,"+
				" appliance_guess text,"+
				" deltaPMean real,"+
				" timestamp text)";
		db.execSQL(sql);

		sql = " CREATE TABLE user_events ("+
				" id integer NOT NULL PRIMARY KEY AUTOINCREMENT,"+
				" view text NOT NULL,"+
				" view_id integer NOT NULL,"+
				" timestamp text NOT NULL )";
		db.execSQL(sql);

		sql = " CREATE TABLE week_consumption ("+
				" id integer NOT NULL PRIMARY KEY AUTOINCREMENT,"+
				" week integer NOT NULL,"+
				" month integer NOT NULL,"+
				" year integer NOT NULL,"+
				" energy_consumption real NOT NULL)";
		db.execSQL(sql);
		
		sql = "CREATE TABLE production_values ("+
				" id integer PRIMARY KEY AUTOINCREMENT,"+
				" timestamp text NOT NULL,"+
				" total integer NOT NULL,"+
				" termica integer NOT NULL,"+
				" hidrica integer NOT NULL,"+
				" eolica integer NOT NULL,"+
				" biomassa integer NOT NULL,"+
				" foto integer NOT NULL)"	;
		db.execSQL(sql);

		sql = "CREATE TABLE recomendations ("+
				" id integer NOT NULL PRIMARY KEY AUTOINCREMENT,"+
				" recomendation text NOT NULL)";
		db.execSQL(sql);
		
		sql = "CREATE TABLE daily_consumption ("+
				 "id integer NOT NULL PRIMARY KEY AUTOINCREMENT, "+
				 "hour integer NOT NULL, "+
				 "day integer NOT NULL, "+
				 "week integer NOT NULL, "+
				 "month integer NOT NULL, "+
				 "energy_consumption real NOT NULL, "+
				 "year integer NOT NULL)";
		db.execSQL(sql);	
		
		sql = "CREATE VIEW prod_daily_avg AS " +
				"Select avg(total) as total, avg(termica) as termica, avg(hidrica) as hidrica, avg(eolica) as eolica, avg(biomassa) as biomassa, avg(foto) as foto, strftime('%H',timestamp)" +
				" from production_values group by strftime('%H',timestamp)";
		db.execSQL(sql);

		sql =   "INSERT INTO recomendations VALUES (1, 'Usar uma consola de jogos para ver um DVD gasta 24 vezes mais electricidade do que um leitor de DVD normal.')";
		db.execSQL(sql);     
		sql =	"INSERT INTO recomendations VALUES (2, 'Lavar grandes quantidades de roupa usa menos energia do que lavar quantidades mais pequenas e com maior frequência.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (3, 'As l‰mpadas de eficiência energética usam um quarto da electricidade e duram dez vezes mais quando comparadas com as lâmpadas normais')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (4, 'Usar uma máquina de lavar a loiçaa é mais eficiente do que lavar os pratos à mão.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (5, 'Lavar a roupa a elevadas temperaturas usa mais energia do que lavar a baixas temperaturas.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (6, 'Usar o modo hiberanção/suspensão num computador pode poupar 135€ e 837 kg de CO2 por ano (assumindo a utilização de 3 computadores)')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (7, 'Ao mudar electrodomésticos velhos por novos, poupa agua, dinheiro e energia alem disso o seu investimento inicial será recuperado em 3 anos (valor estimado)')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (8, 'Sabia que ao evitar o modo stand-by em equipamentos poderá reduzir em cerca de 10% a sua conta de electricidade.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (10, 'A reciclagem do papel gasta cerca de 70% menos energia do que a necess‡ria para produzir papel novo.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (11, 'A energia poupada na reciclagem de uma garrafa de vidro é suficiente para alimentar um computador por 20 minutos.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (12, 'Estudos provam que pilhas recarregáveis são mais eficientes (custo) do que as pilhas descartáveis.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (13, 'A utilização de screen savers não reduz o consumo de energia de um computador, para isso utilize os modos suspens‹o e hibernação.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (14, 'No Verão, troque o ar condicionado por uma ventoinha de tecto, de janela ou de pé e poupe cerca de 10% na factura da luz.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (15, 'No Inverno ligue a ventoinha de tecto ao contrário e conseguirá aquecer a casa, pois a ventoinha irá baixar o ar quente que se acumula no tecto.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (16, 'Um recuperador de calor é 3 vezes mais eficiente do que uma lareira aberta')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (17, 'Crie o habito de desligar a luz sempre que sai de uma divisão para a qual não vai voltar tão cedo.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (18, 'Troque lâmpadas tradicionais por lâmpadas de baixo consumo e/ou lâmpadas fluorescentes. Estas últimas duram aproximadamente 2 anos e consomem cerca de 35% menos energia.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (19, 'Desligue a caldeira ou esquentador sempre que sai de casa ou durante a noite.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (20, 'Evite modo standby em electrodomésticos, mesmo se n‹o estiverem a ser utilizados, se a luz standby estiver acesa estes continuam a consumir energia')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (21, 'Na aquisição de qualquer electrodoméstico escolha sempre os modelos com a maior eficiência energética - o investimento inicial pode ser maior, mas as poupanças futuras serão de longo prazo.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (22, 'No Verão, mantenha as cortinas e estores corridos para não deixar entrar o calor e no Inverno faça o contrário, para que o sol aqueça a casa')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (23, 'Certifique-se que todas as janelas e portas de casa est‹o bem isoladas - cerca de 30% do calor/frio entra em casa pelas janelas.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (24, 'Certifique-se que n‹o tem nenhuma torneira a pingar, gastando agua desnecessariamente')";
		db.execSQL(sql);
	    sql = "INSERT INTO recomendations VALUES (25, 'Use as máquinas de lava roupa e loiçaa apenas quando estiverem cheias.')";
	    db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (26, 'Sempre que puder, lave a roupa e loiça com água fria, utilize ainda agua fria nas limpezas domésticas.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (27, 'Não encha em demasiado a máquina de secar a roupa -  caso contr‡rio a roupa demorará muito mais tempo a ficar seca.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (28, 'Programe a máquina de lavar loiçaa para terminar antes do programa de secagem e deixe a loiçaa secar ao ar livre')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (29, 'Evite a acumulação de gelo no congelador ou arcas frigoríficas, para manter o equipamento  eficiente')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (30, 'Evite abrir a porta do forno quando estiver a cozer algo, caso contrário poder‡ perder cerca de 25% da energia.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (31, 'Deve adequar a panela à boca do fog‹o onde vai cozinhar para evitar desperdício de energia, no entanto saiba que as bocas mais pequenas gastam cerca 10% menos.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (32, 'Evite encher as panelas com demasiada água na hora de cozinhar, porque irá apenas prolongar o tempo de fervura e gastar energia desnecessariamente.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (33, 'Não coloque alimentos ainda quentes ou mornos no frigorífico - caso contrário o frigorifico terá de consumir mais energia para os arrefecer eficazmente.')";
		db.execSQL(sql);
		sql = "INSERT INTO recomendations VALUES (34, '')";
		db.execSQL(sql);
	
	}
}
