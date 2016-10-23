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

public class WeekConsumptionActivity extends Activity implements Observer {

	private LinearLayout _mainView;
	private LinearLayout _homeBtn;
	private LinearLayout _dayBtn;
	private LinearLayout _monthBtn;
	private LinearLayout _prodBtn;
	
	private TextView _weekId_label;
	private TextView _viewLabel;
    private XYPlot week_cons_plot;
    
    private StepFormatter series1Format;
    private XYSeries series1 ;
    
    private TextView _peakConsump;
    private TextView _totalConsump;
    private TextView _comparisonBox;
	private TextView _totalC02;
	private TextView _totalCost;
	
    private ArrayList<ContentValues> week_cons;
    private UI_Handler ui_handler;
	private ComparisonWidget _comp;

    /**
     * Variable used to display the week name and query the week consumption
     */
    private int _currentWeek;
    private int _queryWeek;
	
    private static final String MODULE = "WEEK CONSUMPTION";
    private TextView _eventsCount;
    
    private int _countNewEvents;
    
	private LastEvtBroadCastReceiver evt_receiver = new LastEvtBroadCastReceiver(); // broadcast receiver
    private RuntimeConfigs _configs;

	private int _was_touched=1;
	private final static WebServiceHandler web_handler = WebServiceHandler.get_WS_Handler();

	private LongOperation services_handler;
	
	private String []dias = {"Domingo","Segunda","Terça","Quarta","Quinta","Sexta","Sabado"};

	
    @Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.week_view);
	}
  //initializes all the "runtime" variables used in this activity
	@Override
    public void onResume(){
    	_configs 		= RuntimeConfigs.getConfigs();
		DBManager.getDBManager().insertUserEvent(MODULE);
		week_cons = new ArrayList<ContentValues>();
	    ui_handler = new UI_Handler();
	    initView();
    	super.onResume();
    }
    
	/**
	 * initializes the view elements and add listeners
	 */
	private void initView(){
		
		_mainView		= (LinearLayout)findViewById(R.id.week_main_layout);
		//_homeBtn        = (LinearLayout)findViewById(R.id.home_btn_week);
        _dayBtn         = (LinearLayout)findViewById(R.id.day_btn_week);
        _monthBtn       = (LinearLayout)findViewById(R.id.month_btn_week);
        _prodBtn        = (LinearLayout)findViewById(R.id.week_prod_btn);
        
        _viewLabel 	    = (TextView)findViewById(R.id.week_label);
        _weekId_label   = (TextView)findViewById(R.id.week_ID_label);
    //   _eventsCount	    = (TextView)findViewById(R.id.num_of_pastEvents_week);
        _peakConsump	= (TextView)findViewById(R.id.peak_consump_week);
        _totalConsump	= (TextView)findViewById(R.id.total_consump_week);
        _totalC02		= (TextView)findViewById(R.id.total_co2_week);
        _totalCost		= (TextView)findViewById(R.id.total_money_week);
        _comp		    = (ComparisonWidget)findViewById(R.id.comp_widget_week);

        SpannableString s = new SpannableString("Semana"); 
        s.setSpan(new StyleSpan(Typeface.BOLD), 0, 6, 0); 
        _viewLabel.setText(s);
        initPlot();
        
       // ComparisonBoxHandler.changeComparisonBoxes(_comparisonBox, 5, "week");
        
    	Calendar cal = Calendar.getInstance();
        _currentWeek=cal.get(Calendar.WEEK_OF_YEAR);
        _queryWeek=_currentWeek;
        
        IntentFilter filter_evt = new IntentFilter(EventsSocketService.ACTION_KEY);
        registerReceiver(evt_receiver, filter_evt);	
       //updateEventsCounter();

        services_handler = new LongOperation();
        services_handler.execute("week");
//        updateInfoBoxes();
	}

	private void initPlot(){
		// Initialize our XYPlot reference:
		week_cons_plot = (XYPlot) findViewById(R.id.week_cons_Plot);
        Number[] series1Numbers = {10,20,34, 4, 10, 12,3};
        series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                null);                             // Set the display title of the series
       series1Format = new StepFormatter(
                Color.rgb(240, 89, 45),                                     // line color
                Color.rgb(240, 89, 45));              // fill color (optional)
 
        Paint p = new Paint();
        p.setARGB(0, 255, 255, 255);
        
        Paint p1 = new Paint();
        p1.setARGB(255, 0, 0, 0);
        
        series1Format.setVertexPaint(p);
        week_cons_plot.addSeries(series1, series1Format);
        week_cons_plot.getLayoutManager().remove((week_cons_plot.getLegendWidget()));
        week_cons_plot.setTicksPerRangeLabel(1);
        week_cons_plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 8);
        week_cons_plot.setRangeBoundaries(0, 40, BoundaryMode.AUTO);
        week_cons_plot.setDomainBoundaries(0, 7, BoundaryMode.AUTO);
        week_cons_plot.setDomainLabel("Dias        Dom.      Seg.      Ter.        Qua.       Qui.        Sex.        Sab.");
        week_cons_plot.setRangeLabel("consumo kWh");
        week_cons_plot.setDomainValueFormat(new DecimalFormat("#"));
        week_cons_plot.getGraphWidget().setMargins(10, 10, 10, 10);
        week_cons_plot.setBorderPaint(p);
        week_cons_plot.getGraphWidget().getBackgroundPaint().setColor(getResources().getColor(R.color.bg_color));
        week_cons_plot.getGraphWidget().getGridBackgroundPaint().setColor(getResources().getColor(R.color.bg_color));
        week_cons_plot.getGraphWidget().getDomainLabelPaint().setColor(Color.BLACK);
        week_cons_plot.getGraphWidget().getRangeLabelPaint().setColor(Color.BLACK);
        week_cons_plot.setBorderStyle(XYPlot.BorderStyle.NONE, null, null);
        week_cons_plot.getDomainLabelWidget().getLabelPaint().setColor(Color.BLACK);
        week_cons_plot.getRangeLabelWidget().getLabelPaint().setColor(Color.BLACK);
        week_cons_plot.getTitleWidget().getLabelPaint().setColor(Color.BLACK);
        week_cons_plot.getGraphWidget().getDomainOriginLabelPaint().setColor(Color.BLACK);
        week_cons_plot.getGraphWidget().getRangeOriginLabelPaint().setColor(Color.BLACK);
        week_cons_plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        week_cons_plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);
        week_cons_plot.getGraphWidget().setMarginRight(5);
        week_cons_plot.disableAllMarkup();
      
	}
	public void handleHomeClick(View v){
		
		if(v.getId()==R.id.home_btn_week){
			clean_up();
		}
	}
	@Override
	public void update(Observable observable, Object data) {
	}
	
	public void handleButtonClick(View v){
		if(v.getId()==R.id.minus_week){
			_queryWeek--;
			services_handler = new LongOperation();
			services_handler.execute("week");
			updateLabel(_queryWeek);
		}
		else if(v.getId()==R.id.plus_week){
			if(_queryWeek<_currentWeek){
				_queryWeek++;
				services_handler = new LongOperation();
				services_handler.execute("week");
			}
			updateLabel(_queryWeek);
		}
	}
	/**
	 * updates the label on top of the chart with information about the current week
	 * @param week
	 */
	private void updateLabel(int week){
		
		int weeksDiffs = _currentWeek-_queryWeek;
	
		if(weeksDiffs==0)
				_weekId_label.setText("esta semana");
		if(weeksDiffs== 1)
				_weekId_label.setText("semana passada");
		if(weeksDiffs>1)
				_weekId_label.setText("ha "+weeksDiffs+" semana");
			
	}
	/**
	 * Uses the data retrieved by the webservice to calculate the precent difference 
	 * @return  
	 */
	private double getWeekComparison(){
			double last_week_total=0;
			double precent_week = 0;
			ArrayList<ContentValues> week 	   = WebServiceHandler.get_WS_Handler().week_cons;
			double week_total				   = WebServiceHandler.get_WS_Handler().week_total;
			ArrayList<ContentValues> last_week = WebServiceHandler.get_WS_Handler().last_week_cons;
		try{
			int last_day = week.get(week.size()-1).getAsInteger("weekday");
			
			int i=0;    
			while(last_week.get(i).getAsInteger("weekday")<=last_day){
				last_week_total+=last_week.get(i).getAsDouble("cons");
				i++;
			}
			Log.i(MODULE,"week total "+week_total+" avg "+last_week_total+" cons "+week_total);
			precent_week    = week_total>last_week_total?-1*(1-(week_total/last_week_total)):-1*(1-(week_total/last_week_total));
			precent_week = precent_week*100; 
			
		}catch(Exception e){
			e.printStackTrace();
			//return 0;
		}
		return precent_week>2000?0:Math.round(precent_week);
		
	}
	public void initCompWidget(){
		Log.i(MODULE, "initializing comp widget");
		ArrayList<ContentValues> week_average = DBManager.getDBManager().getMonthAverage();
		if(week_cons!=null){
			double total = calculateTotal(week_cons);
			double average = calculateTotal(week_average, week_cons.size());
			double max = total>average?total:average;
			_comp.setmax_cons((int)Math.round(max*1.5));
			_comp.setToday_cons((int) Math.round(total));
			_comp.setAvg_cons((int) Math.round(average));
			_comp.setLegend("Esta semana");
			new InitWidget().start();
		}
	}
	/**
	 * Update the boxes with the information regarding the daily consumption (at the right of the chart)
	 */
	private void updateInfoBoxes(){
		
		double precent_week = getWeekComparison();
		DecimalFormat df = new DecimalFormat("#.#");
//		
		double total_cons = calculateTotal(week_cons);
		_totalConsump.setText(Math.round(total_cons/1000)+"");
		_totalCost.setText(df.format((total_cons/1000)*0.12)+"");
		double total_CO2 = (total_cons/1000)*0.762; 
		Log.e(MODULE, total_CO2+" g");
		total_CO2 =total_CO2 -total_CO2*DBManager.getDBManager().getThisWeekRenewPercentage();
		Log.e(MODULE, total_CO2+" g weighted");
		_totalC02.setText(df.format(total_CO2)+"");
		int peak_day=1;
		double peak_cons;
		if(week_cons!=null && week_cons.size()>0){
			peak_cons = week_cons.get(0).getAsDouble("cons");
			for(int i=1;i<week_cons.size();i++){

				if(week_cons.get(i).getAsDouble("cons")>=peak_cons){
					peak_cons= week_cons.get(i).getAsDouble("cons");
					peak_day= week_cons.get(i).getAsInteger("weekday");
				}
			}
			_peakConsump.setText(""+dias[peak_day-1]);
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
	 * Creates random values for demo
	 */
	private void updateWeekData(){
		Number[] values = {0,0,0,0,0,0,0,0};

		Log.i(MODULE, "Updating week consumption");
		double max = 0;
		for(int i=0;i<week_cons.size();i++){
			
			values[week_cons.get(i).getAsInteger("weekday")-1]=Math.round((week_cons.get(i).getAsDouble("cons")/1000));
			if(week_cons.get(i).getAsDouble("cons")/1000 > max)
					max=week_cons.get(i).getAsDouble("cons")/1000;
		}

		week_cons_plot.clear();

		SimpleXYSeries series = new SimpleXYSeries(
				Arrays.asList(values),          // SimpleXYSeries takes a List so turn our array into a List
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
				null);

		 week_cons_plot.setRangeBoundaries(0, max*1.3, BoundaryMode.AUTO);
		week_cons_plot.addSeries(series, series1Format);
		week_cons_plot.redraw();
		
		updateInfoBoxes();
	}
	private void clean_up(){
		SpannableString s = new SpannableString("Week"); 
	    s.setSpan(new StyleSpan(Typeface.NORMAL), 0, 4, 0); 
	    _viewLabel.setText(s);
	    
	    _configs.getScreenHandler().deleteObserver(this);
	    
	    try{
    		unregisterReceiver(evt_receiver);
    	}catch(IllegalArgumentException e){
    		Log.w(MODULE, "the receiver wasn't registed");
    	}
	    
		this.finish();
	}
	@Override
	public void onStop(){
		clean_up();
		super.onStop();
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
				//updateEventsCounter();
			}
		}
	}
    /**
     * Class responsible for querying the database/serices for historical data regarding the week consumption
     * @author filipequintal
     *
     */
	private class LongOperation extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			if(params[0].equals("week")){
				Log.i(MODULE,"Querying month "+_queryWeek);
				Calendar cal = Calendar.getInstance();
				week_cons = web_handler.getWeekAverage(_queryWeek,cal.get(Calendar.YEAR));
				Log.i(MODULE, "executed");
				Message msg = new Message();
				msg.arg1=1;
				if(week_cons!=null)
					ui_handler.sendMessage(msg);
				
				initCompWidget();
				return "Executed";
			}else
				return "";
		}      

		@Override
		protected void onPostExecute(String result) {
			//    TextView txt = (TextView) findViewById(R.id.output);
			//  txt.setText("Executed"); // txt.setText(result);
			//might want to change "executed" for the returned string passed into onPostExecute() but that is upto you
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
	
	/**
	 * Class responsible for updtaing the view from  inner threads
	 * @author filipequintal
	 *
	 */
	private class UI_Handler extends Handler {
		@Override
		public void handleMessage(Message msg) 
		{ 
			switch(msg.arg1){
			case 1:
				updateWeekData();
				break;
			default:
				break;
			}

		} 
	}
	private class InitWidget extends Thread{

		@Override
		public void run(){
			Log.i(MODULE, "Running !!! Init GUI thread");
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
