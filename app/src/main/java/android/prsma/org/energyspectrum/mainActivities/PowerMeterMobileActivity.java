package android.prsma.org.energyspectrum.mainActivities;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.prsma.org.energyspectrum.R;
import android.prsma.org.energyspectrum.customUI.SummaryComparisonWidget;
import android.prsma.org.energyspectrum.customUI.SummaryWidget;
import android.prsma.org.energyspectrum.customUI.TabbedMenuHandler;
import android.prsma.org.energyspectrum.database.DBManager;
import android.prsma.org.energyspectrum.dtos.EventSampleDTO;
import android.prsma.org.energyspectrum.dtos.RuntimeConfigs;
import android.prsma.org.energyspectrum.services.EventsSocketService;
import android.prsma.org.energyspectrum.services.SocketConnectionService;
import android.prsma.org.energyspectrum.settings.ApplicationSettings;
import android.prsma.org.energyspectrum.webservices.WebServiceHandler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class PowerMeterMobileActivity extends Activity implements Observer {

	private LinearLayout _dayBtn;
	private LinearLayout _weekBtn;
	private LinearLayout _monthBtn;
	private LinearLayout _prodBtn;

	/*private TextView _compDay;
	private TextView _compWeek;
	private TextView _compTMonth;


	private TextView _todayTotal;
	private TextView _weekTotal;
	private TextView _monthTotal;*/

	private LinearLayout _connectionStatusIcon;

	private TextView _currentConsLabel;
	private TextView _connectionStatusLabel;
	private TextView _recomendationsLabel;

	private Button  _resetButton;

	private SummaryComparisonWidget _summaryComp;
	private SummaryWidget _summaryWidget;

	//variable to hold the consumptiondata
	private double today_cons_total=0;
	@SuppressWarnings("unused")
	private double yesterday_cons_total=0;
	private double week_cons_total=0;
	@SuppressWarnings("unused")
	private double last_week_cons_total=0;
	private double month_cons_total=0;
	@SuppressWarnings("unused")
	private double last_month_cons_total=0;

	public final int GO_TO_SS = 1;
	private boolean _exit = false;
	@SuppressWarnings("unused")
	private ExitHandler exit_h;
	private int _was_touched=0;
	private TabbedMenuHandler _touchHandler;
	private static final String MODULE = "HOME MAC";

	// variables used to gather the current consumption
	private SocketConnectionService current_cons_service;   			// service
	private ServiceConn service_connection;								// connection handler 
	private currentConsReceiver receiver = new currentConsReceiver();	// broadcast receiver

	//variables used to receive the power events
	private EventsSocketService events_service;										// service
	private EventsConn  evt_service_connection;										// connection handler
	private LastEvtBroadCastReceiver evt_receiver = new LastEvtBroadCastReceiver(); // broadcast receiver

	//variables to count and display the new events
	private TextView _newEvents;
	private int _countNewEvents = 0;
	private PowerManager powerManager;
	private WakeLock wakeLock;

	//instance of the runtimeConfigurations
	private RuntimeConfigs _configs;
	public ContentValues agg_data;
	private UI_Handler _handler;
	//private SummaryWidget _comp;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		
		if (! DBManager.databaseExists())
            DBManager.initDatabase();
        else
        	Log.i(MODULE, "Database already exists!!");
		
		_handler = new UI_Handler();
		_configs = RuntimeConfigs.getConfigs();
		_configs.getScreenHandler().start();
		_countNewEvents = _configs.getEventsCount();
		_touchHandler = _configs.getMenuHandler();
		_touchHandler.setContext(getApplicationContext());
		if(_touchHandler.isOnline())
			initServices();
	}

	@Override
	public void onResume(){
		super.onResume();
		_exit=false;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		_configs.setMeterIp(sp.getString("meter_ip", "2"));
		_configs.setInstallation_id(Integer.parseInt(sp.getString("installation_ID", "1")));
		_configs.getScreenHandler().addObserver(this);
		_configs.setWebserver_port(Integer.parseInt(sp.getString("webserver_port", "11")));
		String teste_binary="vamos testar isto, parece que esta a funcionar"; 
		_touchHandler.resetTouch();
	//	Log.i(MODULE,"touch");
		_was_touched=1;
		findViewById(R.id.home_main_layout).setOnTouchListener(_touchHandler);
		//Log.e(MODULE, "creating screesaver handler thread");
		DBManager.getDBManager().insertUserEvent(MODULE);
		initView();
		updateRecomendation();
//		exit_h = new ExitHandler();
//		exit_h.start();
	}
	@Override
	public void onStart(){
		super.onStart();
//		powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
//		wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP, "PowerMeterMobile");
		Runtime.getRuntime().gc();
	}
	@Override 
	public void onBackPressed(){
		super.onBackPressed();
		moveTaskToBack(true);
		this.finish();
		System.exit(0);
		super.onDestroy();
	}
	
	private void initView(){

		LongOperation test = new LongOperation();
		test.execute("init");
		_summaryComp			= (SummaryComparisonWidget)findViewById(R.id.summaryCompWidget);
		_summaryWidget			= (SummaryWidget)findViewById(R.id.summaryWidget);
		//_comp					= (SummaryWidget)findViewById(R.id.summaryWidget);
		_dayBtn    				= (LinearLayout)findViewById(R.id.today_btn);
		_weekBtn   				= (LinearLayout)findViewById(R.id.week_btn);
		_monthBtn 				= (LinearLayout)findViewById(R.id.month_btn);
		_prodBtn				= (LinearLayout)findViewById(R.id.home_prod_btn);
		
		_dayBtn.setBackgroundResource(R.drawable.tab_selected_not_hc3);
		_weekBtn.setBackgroundResource(R.drawable.tab_selected_not_hc3);
		_monthBtn.setBackgroundResource(R.drawable.tab_selected_not_hc3);
		_prodBtn.setBackgroundResource(R.drawable.tab_selected_right_prod);
		//_eventsBtn 				= (LinearLayout)findViewById(R.id.events_btn);
		_currentConsLabel		= (TextView)findViewById(R.id.current_cons_label);
		_connectionStatusLabel	= (TextView)findViewById(R.id.connection_status);
		_connectionStatusIcon	= (LinearLayout)findViewById(R.id.connection_status_icon);
		_resetButton			= (Button)findViewById(R.id.reset_connection);
		
		// dummy init
		_summaryComp.set_maxDailyCons(50);
		_summaryComp.set_maxWeeklyCons(50);
		_summaryComp.set_maxMonthlyCons(50);
		_summaryComp.setYesterday_con(10);
		_summaryComp.set_daily_cons(15);
		_summaryComp.setLastWeek_cons(30);
		_summaryComp.set_weekly_cons(20);
		_summaryComp.setLastMonth_cons(40);
		_summaryComp.set_monthly_cons(50);
		
//		_compDay				= (TextView)findViewById(R.id.day_comparisonBox_main);
//		_compWeek				= (TextView)findViewById(R.id.week_comparisonBox_main);
//		_compMonth				= (TextView)findViewById(R.id.month_comparisonBox_main);
//
//		_todayTotal				= (TextView)findViewById(R.id.today_total_main_value);
//		_weekTotal				= (TextView)findViewById(R.id.week_total_main_value);
//		_monthTotal				= (TextView)findViewById(R.id.month_total_main_value);

		_recomendationsLabel	= (TextView)findViewById(R.id.recomendation_label);

		//_newEvents				= (TextView)findViewById(R.id.num_of_pastEvents);
		//_newEvents.setVisibility(View.INVISIBLE);
		//updateEventsCounter();
		//        changeComparisonBoxes(_compDay,_compWeek,_compMonth,0,5,2);
		//        _compDay.setBackgroundResource(R.drawable.round_box_green);
		//        _compWeek.setBackgroundResource(R.drawable.round_box_orange);
		//        _compMonth.setBackgroundResource(R.drawable.round_box_red);

		_dayBtn.setOnTouchListener(_touchHandler);
		_weekBtn.setOnTouchListener(_touchHandler);
		_monthBtn.setOnTouchListener(_touchHandler);
		_prodBtn.setOnTouchListener(_touchHandler);
		
		//_eventsBtn.setOnTouchListener(_touchHandler);
		_resetButton.setVisibility(View.INVISIBLE);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		if(!_configs.handlerRunning()){
			if(sp.getBoolean("art_eco", false)){
				Log.d(MODULE, "landscape mode ON");
				_configs.startHandler();
			}else{
				Log.d(MODULE, "landscape mode OFF");
			}
		}
		_countNewEvents = RuntimeConfigs.getConfigs().getEventsCount();
		registerReceivers();
		new InitWidget().start();
		//updateEventsCounter();
	}

	private void updateRecomendation(){
		_recomendationsLabel.setText(DBManager.getDBManager().getRecomendation());
	}

	/**
	 * Uses the data retrieved by the webservice to calculate the precent difference 
	 * @return difference between days in percentage
	 */
	private double getDayComparison(){
		
		try{
			ArrayList<ContentValues> today 	   = WebServiceHandler.get_WS_Handler().day_cons;
			double today_total				   = WebServiceHandler.get_WS_Handler().today_total;
			ArrayList<ContentValues> yesterday = WebServiceHandler.get_WS_Handler().yesterday_cons;

			int last_hour = today.get(today.size()-1).getAsInteger("hour");

			double yesterday_total=0;
			int i=0;
			while(yesterday.get(i).getAsInteger("hour")<=last_hour){
				yesterday_total+=yesterday.get(i).getAsDouble("cons");
				i++;
			}

			double precent_day    = today_total>yesterday_total?-1*(1-(today_total/yesterday_total)):-1*(1-(today_total/yesterday_total));

			precent_day = precent_day*100; 

			return precent_day>2000?0:Math.round(precent_day);
		}catch (Exception e){
			Log.e(MODULE, "null pointer exception return 0 as day percentage");
			return 0;
		}
	}
	/**
	 * Uses the data retrieved by the webservice to calculate the precent difference 
	 * @return difference between week in percentage
	 */
	private double getWeekComparison(){
		
		try{
			ArrayList<ContentValues> week 	   = WebServiceHandler.get_WS_Handler().week_cons;
			double week_total				   = WebServiceHandler.get_WS_Handler().week_total;
			ArrayList<ContentValues> last_week = WebServiceHandler.get_WS_Handler().last_week_cons;

			int last_day = week.get(week.size()-1).getAsInteger("weekday");

			double last_week_total=0;
			int i=0;
			while(last_week.get(i).getAsInteger("weekday")<=last_day){
				last_week_total+=last_week.get(i).getAsDouble("cons");
				i++;
			}

			double precent_week    = week_total>last_week_total?-1*(1-(week_total/last_week_total)):-1*(1-(week_total/last_week_total));
			precent_week = precent_week*100; 
			return precent_week>2000?0:Math.round(precent_week);
		}catch (Exception e){
			Log.e(MODULE, "null pointer exception return 0 as week percentage");
			return 0;
		}
	}

	/**
	 * Uses the data retrieved by the webservice to calculate the precent difference 
	 * @return difference between month in percentage
	 */
	private double getMonthComparison(){

		try{
			ArrayList<ContentValues> month 	    = WebServiceHandler.get_WS_Handler().month_cons;
			double month_total				    = WebServiceHandler.get_WS_Handler().month_total;
			ArrayList<ContentValues> last_month = WebServiceHandler.get_WS_Handler().last_month_cons;

			int last_day = month.get(month.size()-1).getAsInteger("day");

			double last_month_total=0;
			int i=0;
			while(last_month.get(i).getAsInteger("day")<=last_day){
				last_month_total+=last_month.get(i).getAsDouble("cons");
				i++;
			}

			double precent_month    = month_total>last_month_total?-1*(1-(month_total/last_month_total)):-1*(1-(month_total/last_month_total));

			precent_month = precent_month*100; 

			return precent_month>2000?0:Math.round(precent_month);
		}catch (Exception e){
			Log.e(MODULE, "null pointer exception return 0 as day percentage");
			return 0;
		}
	}
	private void updateTotals(){
		DecimalFormat df = new DecimalFormat("#.#");
	
		today_cons_total = agg_data.getAsDouble(WebServiceHandler.DAY_KEY);
		yesterday_cons_total = agg_data.getAsDouble(WebServiceHandler.YESTERDAY_KEY);
		week_cons_total = agg_data.getAsDouble(WebServiceHandler.WEEK_KEY);
		last_week_cons_total = agg_data.getAsDouble(WebServiceHandler.LAST_WEEK_KEY);
		month_cons_total = agg_data.getAsDouble(WebServiceHandler.MONTH_KEY);
		last_month_cons_total = agg_data.getAsDouble(WebServiceHandler.LAST_MONTH_KEY);
		
		Log.e(MODULE, "total "+today_cons_total+" avg "+yesterday_cons_total);
		Log.e(MODULE, "total "+week_cons_total+" avg "+last_week_cons_total);
		Log.e(MODULE, "total "+month_cons_total+" avg "+last_month_cons_total);

		//dummy init
	/*	_summaryWidget.set_total_month(Math.round(month_cons_total)/1000);
		_summaryWidget.set_total_week(Math.round(week_cons_total)/1000);
		double val = (today_cons_total)/1000;
		if(val>1)
			_summaryWidget.set_total_day(Math.round(today_cons_total)/1000);
		else
			_summaryWidget.set_total_day(1);
		
		_summaryWidget.requestRender();*/


		//dummy init
		_summaryWidget.set_total_month(30);
		_summaryWidget.set_total_week(10);
		_summaryWidget.set_total_day(5);
		_summaryWidget.requestRender();

	/*	int max_daily = Math.round(yesterday_cons_total)/1000 > Math.round(today_cons_total)/1000 ? (int)Math.round((Math.round(yesterday_cons_total)/1000)*1.3) : (int)Math.round((Math.round(today_cons_total)/1000)*1.3);
		int max_weekly = Math.round(last_week_cons_total)/1000 > Math.round(week_cons_total)/1000 ? (int)Math.round((Math.round(last_week_cons_total)/1000)*1.3) : (int)Math.round((Math.round(week_cons_total)/1000)*1.3);
		int max_monthly = Math.round(last_month_cons_total)/1000 > Math.round(month_cons_total)/1000 ? (int)Math.round((Math.round(last_month_cons_total)/1000)*1.3) : (int)Math.round((Math.round(month_cons_total)/1000)*1.3);
		_summaryComp.set_maxDailyCons(max_daily);
		_summaryComp.set_maxWeeklyCons(max_weekly);
		_summaryComp.set_maxMonthlyCons(max_monthly);

		_summaryComp.setYesterday_con(Math.round(yesterday_cons_total)/1000);
		_summaryComp.set_daily_cons(Math.round(today_cons_total)/1000);
		
		_summaryComp.setLastWeek_cons(Math.round(last_week_cons_total)/1000);
		_summaryComp.set_weekly_cons(Math.round(week_cons_total)/1000);
		
		_summaryComp.setLastMonth_cons(Math.round(last_month_cons_total)/1000);
		_summaryComp.set_monthly_cons(Math.round(month_cons_total)/1000);*/

		_summaryComp.set_maxDailyCons(8);
		_summaryComp.set_maxWeeklyCons(20);
		_summaryComp.set_maxMonthlyCons(50);

		_summaryComp.setYesterday_con(3);
		_summaryComp.set_daily_cons(5);

		_summaryComp.setLastWeek_cons(15);
		_summaryComp.set_weekly_cons(8);

		_summaryComp.setLastMonth_cons(43);
		_summaryComp.set_monthly_cons(33);
		
		_summaryComp.requestRender();
//		changeComparisonBoxes(_compDay,_compWeek,_compMonth,Math.round(percent_day*10)/10,Math.round(percent_week*10)/10,Math.round(percent_month*10)/10);
	}
	private void initServices(){

		Log.i(MODULE, "Starting Services");
		//
		if(service_connection==null){
			service_connection = new ServiceConn();
			Intent bindIntent = new Intent(this, SocketConnectionService.class);
			bindService(bindIntent, service_connection, Context.BIND_AUTO_CREATE);
		}else{
			displayConnectionSuccessful();
		}

		if(evt_service_connection==null){
			evt_service_connection = new EventsConn();
			Intent bindIntent2 = new Intent(this, EventsSocketService.class);
			bindService(bindIntent2, evt_service_connection, Context.BIND_AUTO_CREATE);
		}
		registerReceivers();
	}
	/**
	 * Register receivers for the current cons and events service
	 */
	private void registerReceivers(){
		Log.i(MODULE, "Registing new receivers");
		IntentFilter filter = new IntentFilter(SocketConnectionService.ACTION_KEY);
		registerReceiver(receiver, filter);	
		
		IntentFilter evt_filter = new IntentFilter(EventsSocketService.ACTION_KEY);
		registerReceiver(evt_receiver,evt_filter);
	}

	/**
	 * Update the counter with num of new events 
	 */
	private void updateEventsCounter(){

		_countNewEvents = RuntimeConfigs.getConfigs().getEventsCount();
		
		Log.i(MODULE, "count new events"+_countNewEvents);
		
		if(_countNewEvents==0){
			_newEvents.setVisibility(View.INVISIBLE);
		}else{
			_newEvents.setText(_countNewEvents+"");
			_newEvents.setVisibility(View.VISIBLE);
		}

	}
	/**
	 * Changes the values and the color in the comparison boxes of this view
	 * @param dayComp		day comparison label
	 * @param weekComp		week comparison label
	 * @param monthComp		month comparison label
	 * @param precent_day	 day consumption difference in % 
	 * @param precent_week	 week consumption difference in %
	 * @param precent_month  month consumption difference in %
	 */
	private void changeComparisonBoxes(TextView dayComp, TextView weekComp, TextView monthComp, double precent_day, double precent_week, double precent_month){
//		int mOffset[] = new int[2];
//		dayComp.getLocationOnScreen(mOffset);
//		Log.i(MODULE, "x"+mOffset[0]+" y"+mOffset[1]+" width:"+dayComp.getWidth()+" height:"+dayComp.getHeight());
//
//		//day comparison
//		if(precent_day==0){
//			dayComp.setText("igual a ontem");
//			dayComp.setBackgroundResource(R.drawable.round_box_yellow);
//		}else if(precent_day<0){
//			dayComp.setText(precent_day+"% vs ontem");
//			dayComp.setBackgroundResource(R.drawable.round_box_green);
//		}else if(precent_day>4){
//			dayComp.setText("+"+precent_day+"% vs ontem");
//			dayComp.setBackgroundResource(R.drawable.round_box_red);
//		}else if(precent_day>0){
//			dayComp.setText("+"+precent_day+"% vs ontem");
//			dayComp.setBackgroundResource(R.drawable.round_box_orange);
//		}
//		//Week comparison
//		if(precent_week==0){
//			weekComp.setText("igual ˆ ultima semana");
//			weekComp.setBackgroundResource(R.drawable.round_box_yellow);
//		}else if(precent_week<0){
//			weekComp.setText(precent_week+"% vs semana passada");
//			weekComp.setBackgroundResource(R.drawable.round_box_green);
//		}else if(precent_week>4){
//			weekComp.setText("+"+precent_week+"% vs semana passada");
//			weekComp.setBackgroundResource(R.drawable.round_box_red);
//		}else if(precent_week>0){
//			weekComp.setText("+"+precent_week+"% vs semana passada");
//			weekComp.setBackgroundResource(R.drawable.round_box_orange);
//		}
//		//Month comparison
//		if(precent_month==0){
//			monthComp.setText("igual ao ultimo m�s");
//			monthComp.setBackgroundResource(R.drawable.round_box_yellow);
//		}else if(precent_month<0){
//			monthComp.setText(precent_month+"% vs m�s passado");;
//			monthComp.setBackgroundResource(R.drawable.round_box_green);
//		}else if(precent_month>4){
//			monthComp.setText("+"+precent_month+"% vs m�s passado");
//			monthComp.setBackgroundResource(R.drawable.round_box_red);
//		}else if(precent_month>0){
//			monthComp.setText("+"+precent_month+"% vs m�s passado");
//			monthComp.setBackgroundResource(R.drawable.round_box_orange);
//		}
//
//		dayComp.getLocationOnScreen(mOffset);
//		Log.i(MODULE, "x"+mOffset[0]+" y"+mOffset[1]+" width:"+dayComp.getWidth()+" height:"+dayComp.getHeight());

	}

//
//	@Override
//	public void onDestroy(){
//		super.onDestroy();
//		this.finish();
//		System.exit(0);
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			MenuInflater mi = getMenuInflater();
			mi.inflate(R.menu.options_menu, menu);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
			case R.id.main_options:
				startActivity(new Intent(getApplicationContext(), ApplicationSettings.class));
				break;
			
			case R.id.reset_connection:
				Intent i = getBaseContext().getPackageManager()
				.getLaunchIntentForPackage(getBaseContext().getPackageName() );
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(i);
				break;
//			case R.id.goto_production:
//				startActivity(new Intent(getApplicationContext(),ProductionActivity.class));
//				break;
//			case R.id.goto_cluster:
//				//	startActivity(new Intent(getApplicationContext(), AnimationActivity.class));
//					break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}


	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		Log.e(MODULE, "Beat");
		Log.i(MODULE,""+_touchHandler.getTouchStatus());
		Log.i(MODULE,"touch "+_was_touched);
		if(_touchHandler.getTouchStatus()==1){
			_was_touched=1;
		}
		else if(_was_touched==0){
			Log.e(MODULE,"ARRENKA");
			Intent newInt = new Intent(getApplicationContext(), ProductionActivity.class);
			newInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);	
			startActivity(newInt);
		}
		else if(_touchHandler.getTouchStatus()==0){
			Log.e(MODULE, "PRIMEIRO");
			_was_touched=0;
		}
		_touchHandler.resetTouch();
	}

	private void displayConnectionSuccessful(){
		_connectionStatusLabel.setText("ligação estabelecida");
		_connectionStatusIcon.setBackgroundResource(R.drawable.semaphore_green);
		_resetButton.setVisibility(View.INVISIBLE);
	}
	
	private void displayConnectionError(){
		_connectionStatusLabel.setText("problema na ligação");
		_connectionStatusIcon.setBackgroundResource(R.drawable.semaphore_red);
		//.setVisibility(View.VISIBLE);
	}

	private void displayConnectingMsg(){
		_connectionStatusLabel.setText("Ligando-se ao medidor...");
		//	_connectionStatusIcon.setBackgroundDrawable(R.drawable.)
		_resetButton.setVisibility(View.INVISIBLE);
	}

	public void handleButtonClick(View v){

		if(v.getId()==R.id.reset_connection){
			displayConnectingMsg();

//			Intent i = getBaseContext().getPackageManager()
//					.getLaunchIntentForPackage(getBaseContext().getPackageName() );
//
//			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
//			startActivity(i);

			service_connection=null;
			initServices();
		}

		
	}

	/**
	 * Removes the receivers from this activity before going to another
	 */
	private void unregiterReceivers(){
	//	unregisterReceiver(evt_receiver);
		unregisterReceiver(receiver);
	}

	private synchronized void goto_ScreeSaver(){
//		Log.e(MODULE, "GOING TO SCREEN SAVER");
//		_exit = true;
//		startActivity(new Intent(this, AnimationActivity.class));

	}

	private void clean_up(){
		_configs.getScreenHandler().deleteObserver(this);
		try{
			unregiterReceivers();

		}catch(IllegalArgumentException e){
			Log.w(MODULE, "the receiver wasn't registed");
		}
		//this.finish();
	}

	@Override
	public void onPause(){
		clean_up();
		//			_configs.setEventsCount(_countNewEvents);
		try{
			unbindService(service_connection);
		}catch(RuntimeException e){
			Log.e(MODULE, "service not binded");
		}
		super.onPause();
	}

	private class ExitHandler extends Thread{

		@Override
		public void run(){
			Log.d("Main Activity","Exit thread created"); 
			while(!_exit){
				try {
					Thread.sleep(60000);
					if(_was_touched==0){
						Thread.sleep(30000);
						if(_was_touched==0)
							goto_ScreeSaver();
					}
					_was_touched=0;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * Broadcast receiver responsible for handling the power events from the service/socket
	 * @author filipequintal
	 *
	 */
	private class LastEvtBroadCastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			Bundle b = arg1.getExtras();
			if(b!=null){
				Bundle func = b.getBundle(EventsSocketService.CONS_KEY);
				EventSampleDTO dto = new EventSampleDTO(func);
				//	_events.add(dto);
				Log.e(MODULE, "data: "+dto.get_deltaPMean()+"");
					_countNewEvents++;
				updateEventsCounter();
			}

		}

	}

	/**
	 * Broadcast receiver responsible for handling the data from the current consumption service/socket
	 * @author filipequintal
	 */
	private class currentConsReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			Bundle extras = arg1.getExtras();
			String func = extras.getString(SocketConnectionService.CONS_KEY);
			Log.i(MODULE,func);
			if(func.equals(current_cons_service.SOCKET_ERROR_MSG)){
				current_cons_service.stopSelf();
				Log.e(MODULE, "error in the current cons socket");
				displayConnectionError();

			}else{	
				Log.i(MODULE, "connection status "+_connectionStatusLabel.getText());
				if(_connectionStatusLabel.getText().equals("connection problem") || _connectionStatusLabel.getText().equals("Ligando-se ao medidor...")){
					Log.i(MODULE, "yoooooooooooo");
					displayConnectionSuccessful();
				}	
				_currentConsLabel.setText(func+" W");
			}
			Log.d(MODULE, "current cons "+func);
		}

	}


	/**
	 * Class that handles the connection to the current consumption socket
	 * @author filipequintal
	 *
	 */
	private class ServiceConn implements ServiceConnection
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			current_cons_service = ((SocketConnectionService.MyBinder)service).getService();
			try {
				_configs.setRequestReplySocket(current_cons_service);
				current_cons_service.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				current_cons_service.stopSelf();
				e.printStackTrace();
			}
			Log.e(MODULE,"binding the service");
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			current_cons_service = null;
		}
	}

	/**
	 * Class that handles the connection with the events socket
	 * @author filipequintal
	 *
	 */
	private class EventsConn implements ServiceConnection
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			events_service = ((EventsSocketService.MyBinder)service).getService();
			try {
				events_service.start();
			} catch (Exception e) {
				e.printStackTrace();
				// TODO Auto-generated catch block
				displayConnectionError();
				events_service.stopSelf();
				Log.e(MODULE, "error in the events socket");

			} 
			Log.e(MODULE,"binding the service2  ");
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			events_service = null;
		}
	}


	private class LongOperation extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			if(params[0].equals("init")){
				agg_data=WebServiceHandler.get_WS_Handler().getInitData();
				return "Executed";
			}else
				return "";
		}      

		@Override
		protected void onPostExecute(String result) {
			Log.i(MODULE, "executed");
			Message msg = new Message();
			month_cons_total 	  = agg_data.getAsDouble("month");
			last_month_cons_total = agg_data.getAsDouble("last_month");
			today_cons_total      = agg_data .getAsDouble(WebServiceHandler.DAY_KEY);
			yesterday_cons_total  = agg_data.getAsDouble(WebServiceHandler.YESTERDAY_KEY);
			week_cons_total       = agg_data.getAsDouble(WebServiceHandler.WEEK_KEY);
			last_week_cons_total  = agg_data.getAsDouble(WebServiceHandler.LAST_WEEK_KEY);
			msg.arg1=1;
			if(agg_data!=null)
			_handler.sendMessage(msg);
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
	
	private class UI_Handler extends Handler {
		@Override
		public void handleMessage(Message msg) 
		{ 
			switch(msg.arg1){
			case 1:
				updateTotals();
				break;
			default:
				break;
			}

		} 
	}
	private class InitWidget extends Thread{
		@Override
		public void run(){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i(MODULE, "Running !!!");
			_summaryWidget.requestRender();
			_summaryComp.requestRender();
		}
	}
}