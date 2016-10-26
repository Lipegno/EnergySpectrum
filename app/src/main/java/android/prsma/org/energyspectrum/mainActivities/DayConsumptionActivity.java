package android.prsma.org.energyspectrum.mainActivities;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.prsma.org.energyspectrum.R;
import android.prsma.org.energyspectrum.customUI.ComparisonWidget;
import android.prsma.org.energyspectrum.database.DBManager;
import android.prsma.org.energyspectrum.dtos.EventSampleDTO;
import android.prsma.org.energyspectrum.dtos.RuntimeConfigs;
import android.prsma.org.energyspectrum.services.EventsSocketService;
import android.prsma.org.energyspectrum.webservices.WebServiceHandler;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import layout.ConsumptionLogFragment;

public class DayConsumptionActivity extends Activity implements ConsumptionLogFragment.OnFragmentInteractionListener {

	private LinearLayout _mainView;
	private LinearLayout _homeBtn;
	private LinearLayout _weekBtn;
	private LinearLayout _monthBtn;
	//private LinearLayout _eventsBtn;
	private LinearLayout _prodBtn;


	private TextView _homedBtn;

	private StepFormatter series1Format;
	private XYPlot _dayPlot;

	private TextView _peakConsump;
	private TextView _totalConsump;
	private TextView _comparisonBox;
	private TextView _viewLabel;
	private TextView _eventsCount;
	private TextView _totalC02;
	private TextView _totalCost;
	private int _was_touched=1;
	private int _countNewEvents;

	private LastEvtBroadCastReceiver evt_receiver = new LastEvtBroadCastReceiver(); // broadcast receiver
	private RuntimeConfigs _configs;

	private TextView _dayDateLabel;
	private Date _today;
	private Date _queryDate;
	private int diff=0;

	private ArrayList<ContentValues> day_cons;
	private ArrayList<ContentValues> today_cons;
	private static final WebServiceHandler web_handler = WebServiceHandler.get_WS_Handler();

	private static final String MODULE = "DAY CONSUMPTION";
	private UI_Handler ui_handler = new UI_Handler();

	private ComparisonWidget _comp;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.day_view);

	}


	//initializes all the "runtime" variables used in this activity
	@Override
	public void onResume(){
		_configs 		= RuntimeConfigs.getConfigs();
//		_configs.getScreenHandler().addObserver(this);

		_countNewEvents = _configs.getEventsCount();

		DBManager.getDBManager().insertUserEvent(MODULE);
		day_cons = web_handler.day_cons;
		initView();
		super.onResume();
	}

	/**
	 * initializes the view elements and add listeners
	 */
	private void initView(){
		_mainView	   = (LinearLayout)findViewById(R.id.day_main_layout);
		_homeBtn   	   = (LinearLayout)findViewById(R.id.home_btn_day);
		_weekBtn   	   = (LinearLayout)findViewById(R.id.week_btn_day);
		_monthBtn  	   = (LinearLayout)findViewById(R.id.month_btn_day);
		_prodBtn       = (LinearLayout)findViewById(R.id.day_prod_btn);
		_peakConsump   = (TextView)findViewById(R.id.peak_consump_day);
		_totalConsump  = (TextView)findViewById(R.id.total_consump_day);
		_totalC02	   = (TextView)findViewById(R.id.total_co2_day);
		_totalCost	   = (TextView)findViewById(R.id.total_money_day);
		_viewLabel 	   = (TextView)findViewById(R.id.day_label);
		_dayDateLabel  = (TextView)findViewById(R.id.date_label_day);
		_comp		   = (ComparisonWidget)findViewById(R.id.comparisonWidgetDay);

		//_homeBtn.setOnTouchListener(_touchHandler);

		SpannableString s = new SpannableString("Dia"); 
		s.setSpan(new StyleSpan(Typeface.BOLD), 0, 3, 0); 
		_viewLabel.setText(s);

		// Make an SQL Date 
		_today = new java.sql.Date(Calendar.getInstance().getTimeInMillis());  
		_dayDateLabel.setText(_today.getDate()+"/"+(_today.getMonth()+1)+"/"+2013);
		_queryDate=_today;

		initPlot();
		//		updateEventsCounter();

		IntentFilter filter_evt = new IntentFilter(EventsSocketService.ACTION_KEY);
		registerReceiver(evt_receiver, filter_evt);	
		//
		new LongOperation().execute("day");
	}

	/**
	 * Handles the click on the home button on this activity
	 * @param v	- the view that was pressed
	 */
	public void handleHomeClick(View v){
		if(v.getId()==R.id.home_btn_day){
			clean_up();
		}
	}
	/**
	 * Handles the click on the date selector on the top of the chart
	 * @param v
	 */
	public void handleDateSelection(View v){

		if(v.getId()==R.id.day_selector_minus){
			_comp.requestRender();

			diff++;
			Log.i(MODULE, diff+"");
			Calendar today = Calendar.getInstance();  
			// Subtract 1 day  
			today.add(Calendar.DAY_OF_YEAR, -diff);  
			// Make an SQL Date out of that  
			java.sql.Date yesterday = new java.sql.Date(today.getTimeInMillis());  
			_queryDate = yesterday;
			_dayDateLabel.setText(yesterday.getDate()+"/"+(yesterday.getMonth()+1)+"/"+2012);
			LongOperation test = new LongOperation();
			test.execute("day");

		}
		else if(v.getId()==R.id.day_selector_plus){
			if(diff==0)
				Log.i(MODULE, "cant predict the future");
			else{
				diff--;
				if(diff==0){
					Calendar today = Calendar.getInstance();  
					java.sql.Date yesterday = new java.sql.Date(today.getTimeInMillis());
					_queryDate = yesterday;
					_dayDateLabel.setText(yesterday.getDate()+"/"+(yesterday.getMonth()+1)+"/"+2012);
					if(today_cons!=null){
						day_cons=today_cons;
						updateDayCons();
					}else{
						LongOperation test = new LongOperation();
						test.execute("day");
					}
				}
				else{
					Calendar today = Calendar.getInstance();  
					// Subtract 1 day  
					today.add(Calendar.DAY_OF_YEAR, -diff);  

					// Make an SQL Date out of that  
					java.sql.Date yesterday = new java.sql.Date(today.getTimeInMillis());
					_queryDate = yesterday;
					_dayDateLabel.setText(yesterday.getDate()+"/"+(yesterday.getMonth()+1)+"/"+2012);
					LongOperation test = new LongOperation();
					test.execute("day");
				}
			}
		}

		//		if(_touchHandler.isOnline()){

		//		}
	}
	private void clean_up(){

		SpannableString s = new SpannableString("Dia"); 
		s.setSpan(new StyleSpan(Typeface.NORMAL), 0, 3, 0); 
		_viewLabel.setText(s);
		//_configs.getScreenHandler().deleteObserver(this);

		try{
			unregisterReceiver(evt_receiver);
		}catch(IllegalArgumentException e){
			Log.w(MODULE, "the receiver wasn't registed");
		}

		this.finish();
	}
	private void initPlot(){
		// Initialize our XYPlot reference:
		_dayPlot = (XYPlot) findViewById(R.id.day_cons_Plot);
		// Create two arrays of y-values to plot:
		Number[] series1Numbers = {0,1, 8, 5, 2, 7,4,1, 8, 5, 2, 7, 4,4,8,0,0,0,0,0,0,0,0,0,0};
		//   Number[] series2Numbers = {4, 6, 3, 8, 2, 10};
		// Turn the above arrays into XYSeries:
		XYSeries series1 = new SimpleXYSeries(
				Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
				null);                             // Set the display title of the series

		series1Format = new StepFormatter(
				Color.rgb(240, 89, 45),                                     // line color
				Color.rgb(240, 89, 45));              // fill color (optional)

		Paint p = new Paint();
		p.setARGB(0, 255, 255, 255);
		series1Format.setVertexPaint(p);
		// Add series1 to the xyplot:
		_dayPlot.addSeries(series1, series1Format);
		// removes the legend from the chart
		_dayPlot.getLayoutManager().remove((_dayPlot.getLegendWidget()));
		_dayPlot.setRangeStepValue(5);
		_dayPlot.setDomainStepValue(5);
		_dayPlot.setRangeBoundaries(0, 3500, BoundaryMode.AUTO);
		_dayPlot.setRangeStepValue(10);
		_dayPlot.setDomainValueFormat(new DecimalFormat("#"));
		_dayPlot.setDomainLabel("horas      0h          4h          8h          12h          16h          20h           24h");
		_dayPlot.setRangeLabel("consumo Wh");
		_dayPlot.disableAllMarkup();
		_dayPlot.getGraphWidget().setMargins(10, 10, 10, 10);
		_dayPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 28);
		_dayPlot.getGraphWidget().getBackgroundPaint().setColor(getResources().getColor(R.color.bg_color));
		_dayPlot.getGraphWidget().getGridBackgroundPaint().setColor(getResources().getColor(R.color.bg_color));
		_dayPlot.getGraphWidget().getDomainLabelPaint().setColor(Color.BLACK);
		_dayPlot.getGraphWidget().getRangeLabelPaint().setColor(Color.BLACK);
		_dayPlot.getGraphWidget().setDomainLabelTickExtension(10);
		_dayPlot.setBorderStyle(XYPlot.BorderStyle.NONE, null, null);
		_dayPlot.getDomainLabelWidget().getLabelPaint().setColor(Color.BLACK);
		_dayPlot.getRangeLabelWidget().getLabelPaint().setColor(Color.BLACK);
		_dayPlot.getTitleWidget().getLabelPaint().setColor(Color.BLACK);
		_dayPlot.getGraphWidget().getDomainOriginLabelPaint().setColor(getResources().getColor(R.color.bg_color));
		_dayPlot.getGraphWidget().getRangeOriginLabelPaint().setColor(Color.BLACK);
		_dayPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
		_dayPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);
		_dayPlot.getGraphWidget().setMarginRight(5);
		// update the chart for the first time
		Number[] cons = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		if(day_cons!=null){
			for(int i=0;i<day_cons.size();i++)
				cons[day_cons.get(i).getAsInteger("hour")]=(day_cons.get(i)).getAsDouble("cons");
		}
		_dayPlot.clear();
		SimpleXYSeries series = new SimpleXYSeries(
				Arrays.asList(cons),          // SimpleXYSeries takes a List so turn our array into a List
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
				null);
		_dayPlot.addSeries(series, series1Format);
		_dayPlot.redraw();
		//			if(day_cons.size()>0)
		//				updateInfoBoxes();
		//	}

	}
	/**
	 * Uses the data retreived by the webservice to calculate the precent difference 
	 * @return
	 */
	private double getDayComparison(){

		ArrayList<ContentValues> today 	   = WebServiceHandler.get_WS_Handler().day_cons;
		double today_total				   = WebServiceHandler.get_WS_Handler().today_total;
		ArrayList<ContentValues> yesterday = WebServiceHandler.get_WS_Handler().yesterday_cons;
		double yesterday_total=0;
		try{
			int last_hour = today.get(today.size()-1).getAsInteger("hour");
			int i=0;
			while(yesterday.get(i)!=null && yesterday.get(i).getAsInteger("hour")<=last_hour){
				yesterday_total+=yesterday.get(i).getAsDouble("cons");
				i++;
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		double precent_day    = today_total>yesterday_total?-1*(1-(today_total/yesterday_total)):-1*(1-(today_total/yesterday_total));
		precent_day = precent_day*100; 
		
		return precent_day>2000?0:Math.round(precent_day);
	}
	public void initCompWidget(){
		Log.i(MODULE, "initializing comp widget");
		ArrayList<ContentValues> day_average = DBManager.getDBManager().getDayAverage();
		if(day_cons!=null){
			double total = calculateTotal(day_cons);
			double average = calculateAverageTotal(day_average);
			double max = total>average?total:average;
			_comp.setmax_cons((int)Math.round(max*1.5));
			_comp.setToday_cons((int) Math.round(total));
			_comp.setAvg_cons((int) Math.round(average));
			_comp.setLegend("Hoje");
			new InitWidget().start();
		}
	}
	/**
	 * Update the boxes with the information regarding the daily consumption (at the right of the chart)
	 */
	private void updateInfoBoxes(){

		double precent_day = getDayComparison();
		DecimalFormat df = new DecimalFormat("#.#");
		double total_cons = calculateTotal(day_cons);
		_totalConsump.setText(Math.round(total_cons/1000)+"");
		_totalCost.setText(df.format((total_cons/1000)*0.12)+"");
		double co2Day = (total_cons/1000)*0.762; 
		co2Day = co2Day - co2Day*DBManager.getDBManager().getTodayRenewPrecentage();
		_totalC02.setText(df.format(co2Day));
		int peak_hour=0;
		double peak_cons;
		peak_cons = day_cons.get(0).getAsDouble("cons");
		for(int i=1;i<day_cons.size();i++){
			if(day_cons.get(i).getAsDouble("cons")>=peak_cons){
				peak_cons= day_cons.get(i).getAsDouble("cons");
				peak_hour=i;
			}
		}
		_peakConsump.setText(""+peak_hour+":00 - "+(peak_hour+1)+":00");
	}
	/**
	 * Calculates de sum of the consumption when it receives an array with the consumption
	 * @param data ContentValues array with consumption of the time slot in particular 
	 * @return double with a
	 */
	private double calculateTotal(ArrayList<ContentValues> data){
		double result =0;
		for(int i=0;i<data.size();i++)
			result=result+data.get(i).getAsDouble("cons");

		return result;
	}
	private double calculateAverageTotal(ArrayList<ContentValues> data){
		double result =0;
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		for(int i=0;i<data.size() && i<hour;i++)
			result=result+data.get(i).getAsDouble("cons");

		return result;
	}
	/**
	 * Updates the chart with new values
	 */
	private void updateDayCons(){

		try{
			double max = 0;
			Number[] cons = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			Log.i(MODULE, "Updating day consumption");
			for(int i=0;i<day_cons.size();i++){
				cons[day_cons.get(i).getAsInteger("hour")]=Math.round((day_cons.get(i)).getAsDouble("cons"));
				if(max<day_cons.get(i).getAsDouble("cons"))
					max = day_cons.get(i).getAsDouble("cons");
			}
			_dayPlot.clear();

			SimpleXYSeries series = new SimpleXYSeries(
					Arrays.asList(cons),          // SimpleXYSeries takes a List so turn our array into a List
					SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
					null);
			_dayPlot.setRangeBoundaries(0, max*1.2, BoundaryMode.AUTO);
			_dayPlot.addSeries(series, series1Format);
			_dayPlot.redraw();


			if(day_cons.size()>0)   // only updates the chart if there is data.
				updateInfoBoxes();
		}catch(Exception e){
			e.printStackTrace();
			Log.i(MODULE, "erro de net");
		}
	}
	/**
	 * Update the counter with num of new events 
	 */
	private void updateEventsCounter(){
		_countNewEvents = _configs.getEventsCount();
		if(_countNewEvents==0){
			_eventsCount.setVisibility(View.INVISIBLE);
		}else{
			_eventsCount.setText(_countNewEvents+"");
			_eventsCount.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onStop(){
		clean_up();
		super.onStop();
	}

	@Override
	public void onFragmentInteraction(Uri uri) {

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
				Log.e("teste", dto.get_deltaPMean()+"");
				//_countNewEvents++;
				updateEventsCounter();
			}
		}
	}

	private class LongOperation extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			if(params[0].equals("day")){
				Log.i(MODULE, "Querying day "+_queryDate.toGMTString());
				day_cons = web_handler.getDayConsumptionByHour(new Timestamp(_queryDate.getTime()));

				return "Executed";
			}else
				return "error";
		}      
		@Override
		protected void onPostExecute(String result) {
			Log.i(MODULE, "executed");
			Message msg = new Message();
			msg.arg1=1;
			Log.i(MODULE, _queryDate.getDay() +"  -  "+ new Date().getDay());
			if(_queryDate.getDay() == new Date().getDay())
				today_cons=day_cons;
			ui_handler.sendMessage(msg);
			initCompWidget();
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
				updateDayCons();
				break;
			default:
				break;
			}

		} 
	}
	private class InitWidget extends Thread{
		@Override
		public void run(){
			Log.i(MODULE, "Running !!!");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			_comp.requestRender();
		}
	}
}