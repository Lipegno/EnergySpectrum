package layout;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.prsma.org.energyspectrum.R;
import android.prsma.org.energyspectrum.customUI.LineConsumptionChart;
import android.prsma.org.energyspectrum.database.DBManager;
import android.prsma.org.energyspectrum.dtos.EventSampleDTO;
import android.prsma.org.energyspectrum.dtos.RuntimeConfigs;
import android.prsma.org.energyspectrum.webservices.WebServiceHandler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private static final String TAG = "DayFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * VIEW STUFF
     */
    private LineConsumptionChart _consumption_chart;

    private ArrayList<ContentValues> prod_data;

    private LinearLayout _mainContainer;
    private TextView _event_name_label;
    private CheckBox _day1box;
    private CheckBox _day2box;
    private CheckBox _day3box;
    private CheckBox _day4box;
    private CheckBox _day5box;

    private TextView _day1text;
    private TextView _day2text;
    private TextView _day3text;
    private TextView _day4text;
    private TextView _day5text;

    private LinearLayout _daySideBar;
    private LinearLayout _chartSideBar;
    private final CheckBoxHandler _comparison_handler = new CheckBoxHandler();
    /*
    * OTHER STUFF
    * */
    private Date _today;
    private Date _queryDate;
    private int diff=0;
    // private ComparisonWidget _comp;
    private float _defaultTextSize=0f;

    private ArrayList<ContentValues> _day_cons;
    private ArrayList<ContentValues> today_cons;
    private ArrayList<ContentValues> _yesterday_cons;

    private RuntimeConfigs _configs;
    private static final WebServiceHandler web_handler = WebServiceHandler.get_WS_Handler();
    private UI_Handler ui_handler = new UI_Handler();
    private static final String MODULE = "DAY CONSUMPTION";

    private int width_b;
    private int width_s;

    private Context _app_context;
    private TextView _peakConsump;
    private TextView _totalConsump;
    private TextView _totalC02;
    private TextView _totalCost;
    private TextView _hydro_average;
    private TextView _renewable_total;
    private TextView _factComp;
    private TextView _averageCom;
    private TextView _yesterdayComp;
    private TextView _solar_average;
    private TextView _wind_average;

    private ProgressBar _loading_bar;

    DecimalFormat df = new DecimalFormat("#0.00");

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
        _peakConsump   = (TextView)v.findViewById(R.id.frag_day_peak_cons);
        _totalConsump  = (TextView)v.findViewById(R.id.frag_day_total_kwh);
        _totalC02	   = (TextView)v.findViewById(R.id.frag_day_total_c02);
        _totalCost	   = (TextView)v.findViewById(R.id.frag_day_total_cost);

        _factComp      = (TextView)v.findViewById(R.id.frag_day_comp_fact);
        _yesterdayComp = (TextView)v.findViewById(R.id.frag_day_comp_yesterday);
        _averageCom    = (TextView)v.findViewById(R.id.frag_day_comp_average);


        _renewable_total = (TextView)v.findViewById(R.id.frag_day_renew_today);
        _solar_average   = (TextView)v.findViewById(R.id.frag_day_solar);
        _hydro_average   = (TextView)v.findViewById(R.id.frag_day_hydro);
        _wind_average    = (TextView)v.findViewById(R.id.frag_day_wind);

        _defaultTextSize = ((TextView)v.findViewById(R.id.frag_day_peak_cons)).getTextSize();
        _consumption_chart = (LineConsumptionChart)v.findViewById(R.id.day_consumption_chart);
        _event_name_label  = (TextView)v.findViewById(R.id.event_info_label);
        _defaultTextSize   = ((TextView)v.findViewById(R.id.frag_day_total_kwh)).getTextSize();
        _mainContainer     = (LinearLayout)v.findViewById(R.id.day_chart_container);
        _daySideBar        = (LinearLayout)v.findViewById(R.id.day_side_bar);
        _chartSideBar      = (LinearLayout)v.findViewById(R.id.chart_day_sidebar);
        _loading_bar       = (ProgressBar)v.findViewById(R.id.frag_day_loading_bar);

        _consumption_chart.setMode(LineConsumptionChart.MODE_DAY);
        _consumption_chart.SetChartInteractionListener(this);

        _today = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
        //_dayDateLabel.setText(_today.getDate()+"/"+(_today.getMonth()+1)+"/"+2013);
        _queryDate=_today;

        _consumption_chart.setText_size(_defaultTextSize);
       // createDummyValues();
        initCheckBoxes(v);

     /*   Button _full_screen_btn = (Button)v.findViewById(R.id.maximize_chart_day);
        _full_screen_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                maximizeScreen();
            }
        });*/

        doFirstRequest();
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

        _day1text = (TextView) v.findViewById(R.id.day_1_check);
        _day2text = (TextView) v.findViewById(R.id.day_2_check);
        _day3text = (TextView) v.findViewById(R.id.day_3_check);
        _day4text = (TextView) v.findViewById(R.id.day_4_check);
        _day5text = (TextView) v.findViewById(R.id.day_5_check);

        Date dt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        SimpleDateFormat fDate = new SimpleDateFormat("yyyy-MM-dd");

        c.add(Calendar.DATE, -1);
        dt = c.getTime();
        _day1text.setText(fDate.format(dt));
        c.add(Calendar.DATE, -1);
        dt = c.getTime();
        _day2text.setText(fDate.format(dt));
        c.add(Calendar.DATE, -1);
        dt = c.getTime();
        _day3text.setText(fDate.format(dt));
        c.add(Calendar.DATE, -1);
        dt = c.getTime();
        _day4text.setText(fDate.format(dt));
        c.add(Calendar.DATE, -1);
        dt = c.getTime();
        _day5text.setText(fDate.format(dt));
    }

    private void maximizeScreen(){

        if(_daySideBar.getVisibility()==View.VISIBLE) {
            _daySideBar.setVisibility(View.GONE);
            _consumption_chart.adjustSize();

        }else{
            _daySideBar.setVisibility(View.VISIBLE);
            _consumption_chart.adjustSizeNormal();
          //  _consumption_chart.updateSize();
        }

    }

    public void doFirstRequest(){
        new DayConsumptionRequestWorker().execute("day");
        new ProductionRequestWorker().execute("prod");
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
     /*   double[] cons_data = new double[24];
        for(int i=0;i<cons_data.length;i++)
            cons_data[i]=500+(300*Math.random());
        //   for(int i=25;i<48;i++)
        //  _cons_data[i]=0;
        double[] avg_cons_data = new double[48];
        for(int i=0;i<avg_cons_data.length;i++)
            avg_cons_data[i]=500+(200*Math.random());*/

        ArrayList<EventSampleDTO> events = new ArrayList<EventSampleDTO>();

        for(int i=0;i<_day_cons.size();i++){
            EventSampleDTO sample = new EventSampleDTO("Event "+i,null,
                    (int)Math.round(Math.random()*200),
                    i,
                    i,
                    this+""+System.currentTimeMillis(),
                    null);

            sample.set_color((ContextCompat.getColor(_app_context, R.color.event_color_1)));
            events.add(sample);
        }
        _consumption_chart.set_events(events);
        // _consumption_chart.set_avg_cons_data(avg_cons_data);
        //_consumption_chart.set_cons_data(cons_data);
        _consumption_chart.requestRender();
    }

    public void initCompWidget(){
        Log.i(MODULE, "initializing comp widget");
        ArrayList<ContentValues> day_average = DBManager.getDBManager().getDayAverage();
       /* if(_day_cons!=null){
            double total = calculateTotal(_day_cons);
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
            double[] cons = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            Log.i(MODULE, "Updating day consumption");
            for(int i = 0; i< _day_cons.size(); i++){
                cons[_day_cons.get(i).getAsInteger("Hour")]=Math.round((_day_cons.get(i)).getAsDouble("Power"));
                if(max< _day_cons.get(i).getAsDouble("Power"))
                    max = _day_cons.get(i).getAsDouble("Power");
            }
            _consumption_chart.set_cons_data(cons);
           // _consumption_chart.requestRender();

            updateTotal(cons,null,null);

        }catch(Exception e){
            e.printStackTrace();
            Log.i(MODULE, "erro de net");
        }
    }

    private void updateTotal(double[] today_cons, double[] yesterday_cons, double[] average_cons){
        double placeholder = 0;
        double max=0;
        int time=0;

        for(int i=0;i<today_cons.length;i++) {
            placeholder += today_cons[i];
            if(today_cons[i]>max) {
                max = today_cons[i];
                time = i;
            }
        }

        _totalConsump.setText(String.format("%s Wh", df.format(placeholder)));
        _totalCost.setText(String.format("%s €", df.format((placeholder / 1000) * 0.16)));
        _peakConsump.setText(String.format("Between %d:00 and %d:00", time, time + 1));


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
        web_handler._ctx = context;
        _app_context     = context;
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

    @Override
    public void onDoubleClick() {
        maximizeScreen();
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
                Date cDate = new Date();
                String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
                _day_cons = web_handler.getTodayDetailedCons("http://aveiro.m-iti.org/hybridnilm/public/api/v1/plugwise/samples/hourly",fDate);
                Message msg = new Message();
                msg.arg1=1;
                ui_handler.sendMessage(msg);
                Calendar c = Calendar.getInstance();
                c.setTime(cDate);
                c.add(Calendar.DATE, -1);
                cDate = c.getTime();
                fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
                _yesterday_cons = web_handler.getTodayDetailedCons("http://aveiro.m-iti.org/hybridnilm/public/api/v1/plugwise/samples/hourly",fDate);
                return "Executed";
            }else{
                return "error";
            }
        }
    }

    private class HistoricalConsumptionRequestWorker extends AsyncTask<Integer, Void, String>{

        @Override
        protected String doInBackground(Integer... params) {
            Date dt = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(dt);
            c.add(Calendar.DATE, -1*(params[0]+1));
            dt = c.getTime();
            String fDate = new SimpleDateFormat("yyyy-MM-dd").format(dt);
            ArrayList<ContentValues> result = web_handler.getTodayDetailedCons("http://aveiro.m-iti.org/hybridnilm/public/api/v1/plugwise/samples/hourly",fDate);

            if(result!=null && result.size()>0){

                final double[] stupid_array = new double[24];

                for(int i=0;i<result.size();i++){
                    stupid_array[result.get(i).getAsInteger("Hour")]=Math.round((result.get(i)).getAsDouble("Power"));
                }

                Message msg = Message.obtain();
                msg.arg1=2;
                Bundle data = new Bundle();
                data.putDoubleArray("ConsData",stupid_array);
                data.putInt("Index",params[0]);
                msg.setData(data);
                ui_handler.sendMessage(msg);

                return "Executed";
            }else
                return "Request Error";
        }
    }

    /**
     Request stuff - services and handlers modafaquer
     */
    private class ProductionRequestWorker extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.i(MODULE, "Querying day ");
            try{
                prod_data = web_handler.getEnergyProduction();
            }catch(Exception e){
                Log.e(MODULE, "YOOOO DAWWW YOU GOT SOME EXCEPTION UP IN THIS SHIT");
            }
            return "Executed";
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
        @Override
        protected void onPostExecute(String result) {

            float solar,thermal,wind,hydro = 0;
            if(prod_data!=null && prod_data.size()>0) {
                solar = prod_data.get(0).getAsInteger("foto");
                wind = prod_data.get(0).getAsInteger("eolica");
                hydro = prod_data.get(0).getAsInteger("hidrica");
                thermal = prod_data.get(0).getAsInteger("biomassa") + prod_data.get(0).getAsInteger("termica");

                float quotas[] = {solar, wind, hydro, thermal};
                Message msg = Message.obtain();
                msg.arg1 = 3;
                Bundle data = new Bundle();
                data.putFloatArray("renew_quotas", quotas);
                msg.setData(data);
                ui_handler.sendMessage(msg);
            }
            Log.i("DayFragment","got new data");
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
            _loading_bar.setVisibility(View.VISIBLE);
            new HistoricalConsumptionRequestWorker().execute(index);
       /*   final double[] stupid_array = new double[48];
            Date dt = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(dt);
            c.add(Calendar.DATE, -1*index);
            dt = c.getTime();

            String fDate = new SimpleDateFormat("yyyy-MM-dd").format(dt);
            _day_cons = web_handler.getTodayDetailedCons("http://aveiro.m-iti.org/hybridnilm/public/api/v1/plugwise/samples/hourly",fDate);

            _consumption_chart.addComparisonData(stupid_array,index,color);
            _consumption_chart.requestRender();*/
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
                case 2:
                    double[] data = msg.getData().getDoubleArray("ConsData");
                    int index = msg.getData().getInt("Index");
                    addComparisonData(data,index);
                    break;
                case 3:
                    float[] quotas = msg.getData().getFloatArray("renew_quotas");
                    updateRenewQuotas(quotas);
                case 4:
                    updateYesterDayComp();
                    break;
                default:
                    break;
            }
        }

        private void updateYesterDayComp(){
            // first the labels;
            double today_total=0;
            double yesterday_total=0;
            for(int i =0;i<_day_cons.size();i++){
                today_total     +=  _day_cons.get(i).getAsDouble("Power");
                yesterday_total +=  _yesterday_cons.get(i).getAsDouble("Power");
            }
            double comp = today_total/yesterday_total;
            if(comp > 1){
                comp = 1-comp;
                comp = Math.abs(comp);
                String comp_text = df.format(comp*100);
                _yesterdayComp.setText("+ "+comp_text+" % \n than yesterday");
            }else{
                comp = 1-comp;
                String comp_text = df.format(comp*100);
                _yesterdayComp.setText("- "+comp_text+" % \n than yesterday");
            }
            // now the chart
            double[] cons = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            for(int i=0;i<_yesterday_cons.size();i++){
                cons[_yesterday_cons.get(i).getAsInteger("Hour")]=Math.round((_yesterday_cons.get(i)).getAsDouble("Power"));
            }

            _consumption_chart.set_avg_cons_data(cons);
            _consumption_chart.requestRender();
            createDummyValues();  // we can create dummy event values now
            Log.i(TAG,((today_total/yesterday_total)*100)+" %");
        }

        private void addComparisonData(double[] stupid_array, int index){
            _consumption_chart.addComparisonData(stupid_array,index, Color.parseColor("#FF0000"));
            _consumption_chart.requestRender();
            _loading_bar.setVisibility(View.GONE);
        }

        private void updateRenewQuotas(float[] quotas){
            float total         = quotas[0]+quotas[1]+quotas[2]+quotas[3];
            float renew_precent = (quotas[0]+quotas[1]+quotas[2])/total;

            _renewable_total.setText(String.format("%s %% total", df.format(renew_precent* 100)));
            _solar_average.setText(String.format("%s %% solar", df.format(quotas[0] * 100 / total)));
            _wind_average.setText(String.format("%s %% wind", df.format(quotas[1] * 100 / total )));
            _hydro_average.setText(String.format("%s %% hydro", df.format(quotas[2] * 100 / total )));
        }
    }

}
