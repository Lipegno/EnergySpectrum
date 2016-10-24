package layout;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.prsma.org.energyspectrum.R;
import android.prsma.org.energyspectrum.customUI.LineConsumptionChart;
import android.prsma.org.energyspectrum.database.DBManager;
import android.prsma.org.energyspectrum.dtos.EventSampleDTO;
import android.prsma.org.energyspectrum.dtos.RuntimeConfigs;
import android.prsma.org.energyspectrum.webservices.WebServiceHandler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidplot.xy.SimpleXYSeries;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DayConsumptionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DayConsumptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DayConsumptionFragment extends Fragment implements LineConsumptionChart.ChartInteractionListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * VIEW STUFF
     */
    private LineConsumptionChart _consumption_chart;
    private TextView _peakConsump;
    private TextView _totalConsump;
    private TextView _totalC02;
    private TextView _totalCost;
    private Button _plusDateBtn;
    private Button _minusDateBtn;
    private TextView _dayDateLabel;

    private TextView _event_name_label;
    private CheckBox _day1box;
    private CheckBox _day2box;
    private CheckBox _day3box;
    private CheckBox _day4box;
    private CheckBox _day5box;
    private LinearLayout _daySideBar;
    private final CheckBoxHandler _comparison_handler = new CheckBoxHandler();
    /*
    * OTHER STUFF
    * */
    private Date _today;
    private Date _queryDate;
    private int diff=0;
    // private ComparisonWidget _comp;
    private float _defaultTextSize=0f;

    private ArrayList<ContentValues> day_cons;
    private ArrayList<ContentValues> today_cons;

    private RuntimeConfigs _configs;
    private static final WebServiceHandler web_handler = WebServiceHandler.get_WS_Handler();
    private UI_Handler ui_handler = new UI_Handler();
    private static final String MODULE = "DAY CONSUMPTION";

    public DayConsumptionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DayConsumptionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DayConsumptionFragment newInstance(String param1, String param2) {
        DayConsumptionFragment fragment = new DayConsumptionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        _configs = RuntimeConfigs.getConfigs();
        web_handler._ctx = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_day_consumption, container, false);
        initViewFragment(v);
        return v;
    }

    private void initViewFragment(View v){
      /*  _peakConsump   = (TextView)v.findViewById(R.id.frag_peak_consump_day);
        _totalConsump  = (TextView)v.findViewById(R.id.frag_total_consump_day);
        _totalC02	   = (TextView)v.findViewById(R.id.frag_total_co2_day);
        _totalCost	   = (TextView)v.findViewById(R.id.frag_total_money_day);
        _dayDateLabel  = (TextView)v.findViewById(R.id.frag_date_label_day);
        _comp		   = (ComparisonWidget)v.findViewById(R.id.frag_comparisonWidgetDay);

        _defaultTextSize = ((TextView)v.findViewById(R.id.frag_total_day_label)).getTextSize();*/
        _consumption_chart = (LineConsumptionChart)v.findViewById(R.id.day_consumption_chart);
        _consumption_chart.setMode(LineConsumptionChart.MODE_DAY);
        _consumption_chart.SetChartInteractionListener(this);
        _event_name_label = (TextView)v.findViewById(R.id.event_info_label);
        _today = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
        //_dayDateLabel.setText(_today.getDate()+"/"+(_today.getMonth()+1)+"/"+2013);
        _queryDate=_today;
        _defaultTextSize = ((TextView)v.findViewById(R.id.day_total_kwh)).getTextSize();
        _consumption_chart.setText_size(_defaultTextSize);
        _daySideBar        = (LinearLayout)v.findViewById(R.id.day_side_bar);
        new DayConsumptionRequestWorker().execute("day");
        createDummyValues();
        initCheckBoxes(v);

        Button _full_screen_btn = (Button)v.findViewById(R.id.maximize_chart_day);
        _full_screen_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                maximizeScreen();
            }
        });
    }

    private void initCheckBoxes(View v){
        _day1box = (CheckBox) v.findViewById(R.id.checkbox_1day);
        _day2box = (CheckBox) v.findViewById(R.id.checkbox_2day);
        _day3box = (CheckBox) v.findViewById(R.id.checkbox_3day);
        _day4box = (CheckBox) v.findViewById(R.id.checkbox_4day);
        _day5box = (CheckBox) v.findViewById(R.id.checkbox_5day);
        _day1box.setOnCheckedChangeListener(_comparison_handler);
        _day2box.setOnCheckedChangeListener(_comparison_handler);
        _day3box.setOnCheckedChangeListener(_comparison_handler);
        _day4box.setOnCheckedChangeListener(_comparison_handler);
        _day5box.setOnCheckedChangeListener(_comparison_handler);


    }

    private void maximizeScreen(){

        if(_daySideBar.getVisibility()==View.VISIBLE) {
            _daySideBar.setVisibility(View.GONE);
            _consumption_chart.invalidate();
            _consumption_chart.updateSize();
            Log.i("DayFragment", "width"+_consumption_chart.getWidth());
        }else{
            _daySideBar.setVisibility(View.VISIBLE);
            _consumption_chart.invalidate();
            _consumption_chart.updateSize();
            Log.i("DayFragment", "width"+_consumption_chart.getWidth());
        }
    }

    private void initDateSelector(View v){
       /*
        _plusDateBtn = (Button)v.findViewById(R.id.frag_day_selector_plus);
        _minusDateBtn = (Button)v.findViewById(R.id.frag_day_selector_minus);

        _plusDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(MODULE, "plus date pressed");
                handleDateSelection(true);
            }
        });
        _minusDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(MODULE,"minus date pressed");
                handleDateSelection(false);
            }
        });
    */

    }

    private void createDummyValues(){
        double[] cons_data = new double[24];
        for(int i=0;i<cons_data.length;i++)
            cons_data[i]=500+(300*Math.random());
        //   for(int i=25;i<48;i++)
        //  _cons_data[i]=0;
        double[] avg_cons_data = new double[48];
        for(int i=0;i<avg_cons_data.length;i++)
            avg_cons_data[i]=500+(200*Math.random());

        ArrayList<EventSampleDTO> events = new ArrayList<EventSampleDTO>();

        for(int i=0;i<24;i++){
            EventSampleDTO sample = new EventSampleDTO("Event "+i,null,
                    (int)Math.round(Math.random()*200),
                    i,
                    i,
                    this+""+System.currentTimeMillis(),
                    null);

            sample.set_color((ContextCompat.getColor(getContext(), R.color.event_color_1)));
            events.add(sample);
        }
        _consumption_chart.set_events(events);
        _consumption_chart.set_avg_cons_data(avg_cons_data);
        _consumption_chart.set_cons_data(cons_data);
        _consumption_chart.requestRender();
    }
      /**
     * Handles the click on the date selector on the top of the chart
     * @param selection - true plus - false minus
     */
    public void handleDateSelection(boolean selection){

        if(!selection){
            //_comp.requestRender();

            diff++;
            Log.i(MODULE, diff+"");
            Calendar today = Calendar.getInstance();
            // Subtract 1 day
            today.add(Calendar.DAY_OF_YEAR, -diff);
            // Make an SQL Date out of that
            java.sql.Date yesterday = new java.sql.Date(today.getTimeInMillis());
            _queryDate = yesterday;
            _dayDateLabel.setText(yesterday.getDate()+"/"+(yesterday.getMonth()+1)+"/"+2012);

            //  DayConsumptionActivity.DayConsumptionRequestWorker test = new DayConsumptionActivity.DayConsumptionRequestWorker();
            //test.execute("day");

        }
        else if(selection){
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
                        DayConsumptionRequestWorker test = new DayConsumptionRequestWorker();
                        test.execute("day");
                        Log.i(MODULE, "to implement");
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
                    DayConsumptionRequestWorker test = new DayConsumptionRequestWorker();
                    test.execute("day");
                    Log.i(MODULE, "to implement");
                }
            }
        }
    }

    public void initCompWidget(){
        Log.i(MODULE, "initializing comp widget");
        ArrayList<ContentValues> day_average = DBManager.getDBManager().getDayAverage();
       /* if(day_cons!=null){
            double total = calculateTotal(day_cons);
            double average = calculateAverageTotal(day_average);
            double max = total>average?total:average;
            _comp.setmax_cons((int)Math.round(max*1.5));
            _comp.setToday_cons((int) Math.round(total));
            _comp.setAvg_cons((int) Math.round(average));
            _comp.setLegend("Hoje");
            new InitWidget().start();
        }*/
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
    /**
     * Update the boxes with the information regarding the daily consumption (at the right of the chart)
     */
    private void updateInfoBoxes(){

        /* double precent_day = getDayComparison();
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
        _peakConsump.setText(""+peak_hour+":00 - "+(peak_hour+1)+":00");*/
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
            //   _dayPlot.clear();

            SimpleXYSeries series = new SimpleXYSeries(
                    Arrays.asList(cons),          // SimpleXYSeries takes a List so turn our array into a List
                    SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                    null);
            //  _dayPlot.setRangeBoundaries(0, max*1.2, BoundaryMode.AUTO);
            //  _dayPlot.addSeries(series, series1Format);
            //  _dayPlot.redraw();


            if(day_cons.size()>0)   // only updates the chart if there is data.
                updateInfoBoxes();
        }catch(Exception e){
            e.printStackTrace();
            Log.i(MODULE, "erro de net");
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            //  throw new RuntimeException(context.toString()
            //        + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * handler of the stuff that happens in the cons chart
     * @param event
     */
    @Override
    public void onEventSelect(EventSampleDTO event) {
        Log.i("Received Event", event.get_guess());
        _event_name_label.setText(event.get_guess());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    /**
     *  HTTP CONSUMPTION REQUEST STUFF
     */
    private class DayConsumptionRequestWorker extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if(params[0].equals("day")){
                Log.i(MODULE, "Querying day "+_queryDate.toGMTString());
                day_cons = web_handler.getDayConsumptionByHour(new Timestamp(_queryDate.getTime()));

                return "Executed";
            }else{
                return "error";
            }
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

    /**
     * Check box handlers stuff
     */

    private class CheckBoxHandler implements CompoundButton.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            switch (compoundButton.getId()){
                case R.id.checkbox_1day:
                    if(b)
                        addComparisonData(0,getResources().getColor(R.color.comp1_color));
                    else
                        removeComparisonData(0);
                    break;
                case R.id.checkbox_2day:
                    if(b)
                        addComparisonData(1,getResources().getColor(R.color.comp2_color));
                    else
                        removeComparisonData(1);
                    break;
                 case R.id.checkbox_3day:
                     if(b)
                         addComparisonData(2,getResources().getColor(R.color.comp3_color));
                     else
                         removeComparisonData(2);
                    break;
                case R.id.checkbox_4day:
                    if(b)
                        addComparisonData(3,getResources().getColor(R.color.comp4_color));
                    else
                        removeComparisonData(3);
                    break;
                case R.id.checkbox_5day:
                    if(b)
                        addComparisonData(4,getResources().getColor(R.color.comp5_color));
                    else
                        removeComparisonData(4);
                    break;

                default:
                    break;
            }

        }

        private void addComparisonData(int index, int color){
            final double[] stupid_array = new double[48];
            for (int i = 0; i < 48; i++)
                stupid_array[i] = 500 + (400 * Math.random());
            _consumption_chart.addComparisonData(stupid_array,index,color);
            _consumption_chart.requestRender();
        }
        private void removeComparisonData(int index){
            _consumption_chart.removeComparisonData(index);
            _consumption_chart.requestRender();
        }
    }

    /**
     * UI HANDLER STUFF THE SECOND ONE COULD BE REMOVED
     */
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
            // _comp.requestRender();
        }
    }
}
