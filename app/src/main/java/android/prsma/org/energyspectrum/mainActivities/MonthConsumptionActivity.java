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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

public class MonthConsumptionActivity extends Activity implements Observer {

	private LinearLayout _mainView;
	private LinearLayout _dayBtn;
	private LinearLayout _homeBtn;
	private LinearLayout _weekBtn;
 	private LinearLayout _prodBtn;
	
	private TextView _viewLabel;
	private TextView _monthID_label;

	private TextView _comparisonBox;
	private TextView _totalMonth;
	private TextView _monthPeak;
	private TextView _totalC02;
	private TextView _totalCost;
	
	private final static String[] months={"Janeiro","Fevereiro","Mar�o","Abril","Maio","Junho","Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"};

	private static final String MODULE = "MONTH CONSUMPTION";

	private RuntimeConfigs _configs;
	private ArrayList<ContentValues> month_cons;
	private LastEvtBroadCastReceiver evt_receiver = new LastEvtBroadCastReceiver(); // broadcast receiver
	private ComparisonWidget _comp;
	/**
	 * Variable used to display the month name and query the month consumption
	 */
	private int _currentMonth;
	private int _queryMonth;
	private int _queryYear;
	private int _was_touched=1;
	public int _countNewEvents=0;
	private TextView _eventsCount;

	private StepFormatter series1Format;
	private XYPlot month_cons_plot;

	private final static WebServiceHandler web_handler = WebServiceHandler.get_WS_Handler();

	private UI_Handler ui_handler;
	private int i2;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.month_view);
	}

	@Override
	public void onResume(){
		_configs			  = RuntimeConfigs.getConfigs();
		_configs.getScreenHandler().addObserver(this);
		_countNewEvents = _configs.getEventsCount();
		Log.e(MODULE, ""+_countNewEvents);

		DBManager.getDBManager().insertUserEvent(MODULE);

		ui_handler = new UI_Handler();

		month_cons = web_handler.month_cons;

		Log.i(MODULE, "aqui");
		initView();
		super.onResume();
	}

	/**
	 * initializes the view elements and add listeners
	 */
	private void initView(){

		_mainView	   = (LinearLayout)findViewById(R.id.month_main_layout);
		//_homeBtn  	   = (LinearLayout)findViewById(R.id.home_btn_month);
		_dayBtn	   	   = (LinearLayout)findViewById(R.id.day_btn_month);
		_weekBtn   	   = (LinearLayout)findViewById(R.id.week_btn_month);
		_prodBtn 	   = (LinearLayout)findViewById(R.id.month_prod_btn);
		_viewLabel 	   = (TextView)findViewById(R.id.month_label);
		_monthID_label = (TextView)findViewById(R.id.month_ID_label); 
//		_eventsCount   = (TextView)findViewById(R.id.num_of_pastEvents_month);
		_comp		   = (ComparisonWidget)findViewById(R.id.month_comp_widget);
		_totalMonth	   = (TextView)findViewById(R.id.total_month_cons);
		_monthPeak	   = (TextView)findViewById(R.id.peak_month);
		_totalC02 	   = (TextView)findViewById(R.id.total_co2_month);
		_totalCost	   = (TextView)findViewById(R.id.total_money_month);


		Calendar cal = Calendar.getInstance();
		_currentMonth=cal.get(Calendar.MONTH)+1;
		_queryMonth = _currentMonth;
		_queryYear  = cal.get(Calendar.YEAR);
		updateLabel(_queryMonth);
		
		LongOperation test = new LongOperation();
		test.execute("month");

	//	ComparisonBoxHandler.changeComparisonBoxes(_comparisonBox, 0, "month");

		SpannableString s = new SpannableString("Mês"); 
		s.setSpan(new StyleSpan(Typeface.BOLD), 0, 3, 0); 
		_viewLabel.setText(s);
//		updateEventsCounter();
		initPlot();
		IntentFilter filter_evt = new IntentFilter(EventsSocketService.ACTION_KEY);
		registerReceiver(evt_receiver, filter_evt);	
		
//		updateInfoBoxes();
	}

	private void initPlot(){
		// Initialize our XYPlot reference:
		month_cons_plot = (XYPlot) findViewById(R.id.month_cons_Plot);

		// Create two arrays of y-values to plot:
		Number[] series1Numbers = {21,1, 8, 5, 2, 7,4,1, 8, 5, 2, 7, 4,4,8,0,0,0,3,2,1,3,5,6,2,0,0,0,0,0,22,22};
		//   Number[] series2Numbers = {4, 6, 3, 8, 2, 10};

		// Turn the above arrays into XYSeries:
		XYSeries series1 = new SimpleXYSeries(
				Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
				null);                             // Set the display title of the series

		// Same as above, for series2
		//        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, 
		//                "Series2");

		// Create a formatter to use for drawing a series using LineAndPointRenderer:
		series1Format = new StepFormatter(
				Color.rgb(240, 89, 45),                                     // line color
                Color.rgb(240, 89, 45));              // fill color (optional)

		Paint p = new Paint();
		p.setARGB(0, 255, 255, 255);
		series1Format.setVertexPaint(p);  
		// Add series1 to the xyplot:
		month_cons_plot.addSeries(series1, series1Format);
		month_cons_plot.getLayoutManager().remove((month_cons_plot.getLegendWidget()));
		month_cons_plot.setTicksPerRangeLabel(1);
		month_cons_plot.setDomainStepValue(5);
		month_cons_plot.setRangeBoundaries(0, 40, BoundaryMode.AUTO);
		month_cons_plot.setDomainValueFormat(new DecimalFormat("#"));
		month_cons_plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 35);
		month_cons_plot.setDomainLabel("   Dias       1      3       6       9       12      15      18     21     24     28     30   ");
		month_cons_plot.setRangeLabel("consumo kWh");
		month_cons_plot.disableAllMarkup();
		month_cons_plot.getGraphWidget().setMargins(10, 10, 10, 10);
		month_cons_plot.getGraphWidget().getBackgroundPaint().setColor(getResources().getColor(R.color.bg_color));
        month_cons_plot.getGraphWidget().getGridBackgroundPaint().setColor(getResources().getColor(R.color.bg_color));
        month_cons_plot.getGraphWidget().getDomainLabelPaint().setColor(Color.BLACK);
        month_cons_plot.getGraphWidget().getRangeLabelPaint().setColor(Color.BLACK);
        month_cons_plot.getGraphWidget().setDomainLabelTickExtension(10);
        month_cons_plot.setBorderStyle(XYPlot.BorderStyle.NONE, null, null);
        month_cons_plot.getDomainLabelWidget().getLabelPaint().setColor(Color.BLACK);
        month_cons_plot.getRangeLabelWidget().getLabelPaint().setColor(Color.BLACK);
        month_cons_plot.getTitleWidget().getLabelPaint().setColor(Color.BLACK);
        month_cons_plot.getGraphWidget().getDomainOriginLabelPaint().setColor(getResources().getColor(R.color.bg_color));
        month_cons_plot.getGraphWidget().getRangeOriginLabelPaint().setColor(Color.BLACK);
        month_cons_plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        month_cons_plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);
        month_cons_plot.getGraphWidget().setMarginRight(5);
		Number[] values = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

		if(month_cons!=null){   // adds the data to the chart if we have it
		
			for(int i=0;i<month_cons.size();i++){
				
				values[month_cons.get(i).getAsInteger("day")-1]=Math.round((month_cons.get(i).getAsDouble("cons")/1000));
			}
	
			month_cons_plot.clear();
	
			SimpleXYSeries series = new SimpleXYSeries(
					Arrays.asList(values),          // SimpleXYSeries takes a List so turn our array into a List
					SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
					null);
	
			month_cons_plot.addSeries(series, series1Format);
			month_cons_plot.redraw();
		}
	}
	public void handleHomeClick(View v){

		if(v.getId()==R.id.home_btn_month){
			clean_up();
		}

	}

	private void clean_up(){

		SpannableString s = new SpannableString("M�s"); 
		s.setSpan(new StyleSpan(Typeface.NORMAL), 0, 3, 0); 
		_viewLabel.setText(s);
		_configs.getScreenHandler().deleteObserver(this);

		try{
			unregisterReceiver(evt_receiver);
		}catch(IllegalArgumentException e){
			Log.w(MODULE, "the receiver wasn't registed");
		}

		Log.i(MODULE, "CLEAN UP ACTIVITY");

		this.finish();
	}


	public void handleButtonClick(View v){

		if(v.getId()==R.id.minus_month){

			if(_queryMonth>1){
				
				_queryMonth--;
				updateLabel(_queryMonth);
				LongOperation test = new LongOperation();
				test.execute("month");
			}

		}
		else if(v.getId()==R.id.plus_month){
			if(_queryMonth<_currentMonth){
				_queryMonth++;
				updateLabel(_queryMonth);
				LongOperation test = new LongOperation();
				test.execute("month");
			}

			//updateMonthCons();
		}

	}
	/**
	 * Uses the data retrieved by the webservice to calculate the precent difference 
	 * @return
	 */
	private double getMonthComparison(){
		
			double precent_month = 0;
			double last_month_total=0;
			
			ArrayList<ContentValues> month 	   = WebServiceHandler.get_WS_Handler().month_cons;
			double month_total				   = WebServiceHandler.get_WS_Handler().month_total;
			ArrayList<ContentValues> last_month = WebServiceHandler.get_WS_Handler().last_month_cons;
		try{
			int last_day = month.get(month.size()-1).getAsInteger("day");
			int i=0;
			while(last_month.get(i).getAsInteger("day")<=last_day){
				last_month_total+=last_month.get(i).getAsDouble("cons");
				i++;
			}

			precent_month    = month_total>last_month_total?-1*(1-(month_total/last_month_total)):-1*(1-(month_total/last_month_total));
			precent_month = precent_month*100; 
			Log.i(MODULE," "+ Math.round(month_total*100)+" "+Math.round(last_month_total*120)+" "+Math.round(month_total*140));
	
		}catch(Exception e){
			Log.e(MODULE, "exception when calculating month comparison, returning 0");
		}
		
		return precent_month>2000?0:Math.round(precent_month);
	}
	public void initCompWidget(){
		Log.i(MODULE, "initializing comp widget");
		ArrayList<ContentValues> month_average = DBManager.getDBManager().getMonthAverage();
		if(month_cons!=null){
			double total = calculateTotal(month_cons);
			double average = calculateTotal(month_average, month_cons.size());
			double max = total>average?total:average;
			_comp.setmax_cons((int)Math.round(max*1.3));
			_comp.setToday_cons((int) Math.round(total));
			_comp.setAvg_cons((int) Math.round(average));
			_comp.setLegend("Este mês");
			new InitWidget().start();
			}
	}
	/**
	 * Update the boxes with the information regarding the daily consumption (at the right of the chart)
	 */
	private void updateInfoBoxes(){
		
		double precent_month = getMonthComparison();
		DecimalFormat df = new DecimalFormat("#.#");
		double total_cons = calculateTotal(month_cons);
		_totalMonth.setText(Math.round(total_cons/1000)+"");
		_totalCost.setText(df.format((total_cons/1000)*0.12)+"");
		double total_CO2 = (total_cons/1000)*0.762; 
		Log.e(MODULE, total_CO2+" g");
		total_CO2 =total_CO2 -total_CO2*DBManager.getDBManager().getThisMonthRenewPrecentage();
		Log.e(MODULE, total_CO2+" g weighted");
		_totalC02.setText(df.format(total_CO2));
		int peak_day=0;
		double peak_cons;
		
		if(month_cons!=null && month_cons.size()>0){
			peak_cons = month_cons.get(0).getAsDouble("cons");
			for(int i=0;i<month_cons.size();i++){
				double temp = month_cons.get(i).getAsDouble("cons");
				if(temp>=peak_cons){
					peak_cons= temp;
					peak_day= month_cons.get(i).getAsInteger("day");
				}
			}
			_monthPeak.setText(""+peak_day);
		}
		else{
			_monthPeak.setText("--");
		}
	}

	private double calculateTotal(ArrayList<ContentValues> data){
		double result =0;
		for(int i=0;i<data.size();i++)
			result=result+data.get(i).getAsDouble("cons");

		return result;
	}
	private double calculateTotal(ArrayList<ContentValues> data, int max){
		double result =0;
		for(int i=0;i<max && i<data.size();i++)
			result=result+data.get(i).getAsDouble("avg");

		return result;
	}
	/**
	 * Updates the label on top of the chart with information about the current month
	 * @param month
	 */
	private void updateLabel(int month){

		int monthsDiffs = _currentMonth-_queryMonth;

		if(monthsDiffs==0)
			_monthID_label.setText(months[_currentMonth-1]);
		if(monthsDiffs>0)
			_monthID_label.setText(months[_queryMonth-1]);
	}
	private void updateMonthCons(){
 
		Number[] values = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		Log.i(MODULE, "Updating month consumption");
		double max = 0;
		for(int i=0;i<month_cons.size();i++){
			
			values[month_cons.get(i).getAsInteger("day")-1]=Math.round((month_cons.get(i).getAsDouble("cons")/1000));
			
			if(max<month_cons.get(i).getAsDouble("cons")/1000)
				max = month_cons.get(i).getAsDouble("cons")/1000;
		}

		month_cons_plot.clear();

		SimpleXYSeries series = new SimpleXYSeries(
				Arrays.asList(values),          // SimpleXYSeries takes a List so turn our array into a List
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
				null);
		month_cons_plot.setRangeBoundaries(0, max*1.2,BoundaryMode.AUTO);
		month_cons_plot.addSeries(series, series1Format);
		month_cons_plot.redraw();
		updateInfoBoxes();
	}
	@Override
	public void onStop(){
		clean_up();
		//_configs.setEventsCount(_countNewEvents);
		super.onStop();
	}
	@Override
	public void update(Observable observable, Object data) {
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
			if(params[0].equals("month")){
				Log.i(MODULE, "Querying month "+_queryMonth);
				month_cons = web_handler.getMonthAverage(_queryMonth, _queryYear );
				return "Executed";
			}else
				return "";
		}      

		@Override
		protected void onPostExecute(String result) {
			Log.i(MODULE, "executed");
			Message msg = new Message();
			msg.arg1=1;
			if(month_cons!=null)
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
				updateMonthCons();
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
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			_comp.requestRender();
		}
	}
}
