package layout;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.prsma.org.energyspectrum.R;
import android.prsma.org.energyspectrum.customUI.SummaryComparisonWidget;
import android.prsma.org.energyspectrum.customUI.SummaryWidget;
import android.prsma.org.energyspectrum.dtos.RuntimeConfigs;
import android.prsma.org.energyspectrum.webservices.WebServiceHandler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SummaryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SummaryFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    // Interface stuff
    public LinearLayout _connectionStatusIcon;
    public TextView _currentConsLabel;
    public TextView _connectionStatusLabel;
    public TextView _recomendationsLabel;
    public Button _resetButton;
    public SummaryComparisonWidget _summaryComp;
    public SummaryWidget _summaryWidget;

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
    private static final String MODULE = "SummaryFragment";

    private static WebServiceHandler _web_handler;

    /* variables used to gather the current consumption
    private SocketConnectionService current_cons_service;   			// service
    private PowerMeterMobileActivity.ServiceConn service_connection;								// connection handler
    private PowerMeterMobileActivity.currentConsReceiver receiver = new PowerMeterMobileActivity.currentConsReceiver();	// broadcast receiver*/


    /*variables to count and display the new events
    private TextView _newEvents;
    private int _countNewEvents = 0;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;*/

    //instance of the runtimeConfigurations

    private UI_Handler ui_handler = new UI_Handler();

    private RuntimeConfigs _configs;
    public ContentValues agg_data;
    private ArrayList<ContentValues> day_cons;

    public SummaryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SummaryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SummaryFragment newInstance(String param1, String param2) {
        SummaryFragment fragment = new SummaryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        _web_handler = WebServiceHandler.get_WS_Handler();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_summary, container, false);
        initViewFragment(v);
        return v;
    }
    private void initViewFragment(View v){
        _summaryComp			= (SummaryComparisonWidget)v.findViewById(R.id.frag_summaryCompWidget);
        _summaryWidget			= (SummaryWidget)v.findViewById(R.id.frag_summaryWidget);

        _currentConsLabel		= (TextView)v.findViewById(R.id.frag_current_cons_label);

        _connectionStatusLabel	= (TextView)v.findViewById(R.id.frag_connection_status);
        _connectionStatusIcon	= (LinearLayout)v.findViewById(R.id.frag_connection_status_icon);

        _resetButton			= (Button)v.findViewById(R.id.frag_reset_connection);

        _recomendationsLabel	= (TextView)v.findViewById(R.id.frag_recomendation_label);

        _summaryComp.set_maxDailyCons(50);
        _summaryComp.set_maxWeeklyCons(50);
        _summaryComp.set_maxMonthlyCons(80);

        _summaryComp.setYesterday_con(10);
        _summaryComp.set_daily_cons(15);

        _summaryComp.setLastWeek_cons(30);
        _summaryComp.set_weekly_cons(20);

        _summaryComp.setLastMonth_cons(40);
        _summaryComp.set_monthly_cons(50);

        _summaryWidget.set_total_month(30);
        _summaryWidget.set_total_week(10);
        _summaryWidget.set_total_day(5);

        _summaryWidget.set_text_size(_currentConsLabel.getTextSize()*0.8f);
        _summaryComp.set_textSize(_currentConsLabel.getTextSize()*0.8f);
        _summaryWidget.requestRender();

        new InitWidget().start();
        new DailyRequestWorker().execute("day");
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
       //     throw new RuntimeException(context.toString()
        //            + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    public void displayConnectionSuccessful(){
        _connectionStatusLabel.setText("ligação estabelecida");
        _connectionStatusIcon.setBackgroundResource(R.drawable.semaphore_green);
        _resetButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Communication stuff
     */
    /**
     *  HTTP CONSUMPTION REQUEST STUFF
     */
    private class DailyRequestWorker extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if(params[0].equals("day")){
                Date cDate = new Date();
                String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
                day_cons = _web_handler.getTodayDetailedCons("http://aveiro.m-iti.org/hybridnilm/public/api/v1/plugwise/samples/hourly",fDate);

                final double[] stupid_array = new double[24];

                for(int i=0;i<day_cons.size();i++){
                    stupid_array[day_cons.get(i).getAsInteger("Hour")]=Math.round((day_cons.get(i)).getAsDouble("Power"));
                }

                Message msg = Message.obtain();
                msg.arg1=1;
                Bundle data = new Bundle();
                data.putDoubleArray("ConsData",stupid_array);
                msg.setData(data);

                ui_handler.sendMessage(msg);

                return "executed";
            }else{
                return "error";
            }
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
                    double [] cons_data = msg.getData().getDoubleArray("ConsData");
                    updateCurrentCons(cons_data);
                    break;
            }
        }

        private void updateCurrentCons(double[] today_cons){
           int i=0;

           while(today_cons[i]>0)
               i++;

           _currentConsLabel.setText(today_cons[i-1]+" Watts");
        }
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
}
