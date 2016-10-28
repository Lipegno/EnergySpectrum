package layout;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.prsma.org.energyspectrum.R;
import android.prsma.org.energyspectrum.customUI.ProductionChart;
import android.prsma.org.energyspectrum.database.DBManager;
import android.prsma.org.energyspectrum.dtos.RuntimeConfigs;
import android.prsma.org.energyspectrum.webservices.WebServiceHandler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.prsma.org.energyspectrum.webservices.WebServiceHandler.MODULE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProductionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProductionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    public ProductionChart _chart;

    // serviço es
    private static final WebServiceHandler web_handler = WebServiceHandler.get_WS_Handler();

    // data stuff
    private ArrayList<ContentValues> prod_data;
    private ArrayList<ContentValues> pred_data;
    private ArrayList<ContentValues> cons_data;
    private double [] cons;
    private double[] avg_cons;
    private ArrayList<ContentValues> day_cons;
    private ArrayList<ContentValues> average_cons; // MUDAR PARA M�DIA IMPORTANTE...

    //view stuff
    private TextView _totalCons;
    private TextView _totalCost;
    private TextView _totalEmissions;
    private TextView _comparison;
    private TextView _renewQuota;
    private TextView _solarPrecent;
    private TextView _waterPrecent;
    private TextView _windPrecent;
    private TextView _termalPrecent;
    private RuntimeConfigs _configs;


    public ProductionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProductionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProductionFragment newInstance(String param1, String param2) {
        ProductionFragment fragment = new ProductionFragment();
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
        View v = inflater.inflate(R.layout.fragment_production, container, false);
        initViewFragment(v);
        return v;
    }
    private void initViewFragment(View v){
       // day_cons = web_handler.day_cons;
       // average_cons = DBManager.getDBManager().getDayAverage();

        _chart = (ProductionChart)v.findViewById(R.id.frag_prod_chart);

        _totalCost = (TextView)v.findViewById(R.id.frag_prod_total_cost);
        _totalEmissions = (TextView)v.findViewById(R.id.frag_prod_total_co2);
        _totalCons = (TextView)v.findViewById(R.id.frag_prod_total);
        _renewQuota = (TextView)v.findViewById(R.id.frag_renew_quota);
        _comparison = (TextView)v.findViewById(R.id.frag_prod_cons_comparison);
        _solarPrecent = (TextView)v.findViewById(R.id.frag_solar_precent);
        _waterPrecent = (TextView)v.findViewById(R.id.frag_water_precent);
        _windPrecent = (TextView)v.findViewById(R.id.frag_wind_precent);
        _termalPrecent = (TextView)v.findViewById(R.id.frag_termal_precent);

        _chart.setColor("#d0f154");
        _chart.setBgColor("#FFFFFF");
        _chart.setMax_scale(250);
        _chart.setMin_scale(0);
        _chart.setStart_time("17:04");
        _chart.setFinsh_time("20:04");

      //  Button button_test = (Button)v.findViewById(R.id.prod_request_simple);
      //  button_test.setOnClickListener(new View.OnClickListener() {
        //    @Override
          //  public void onClick(View v) {
       ProductionHandler nova = new ProductionHandler();
       nova.execute();

       ConsumptionHandler c_handler = new ConsumptionHandler();
       c_handler.execute();
            //}
        //});

      //  RequestHandler nova = new RequestHandler();
      //  nova.start();
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
        web_handler._ctx = getContext();
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
           // throw new RuntimeException(context.toString()
             //       + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Production stuff calculations
     */

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p<a href="http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * production and consumption stuff like calculate averages and populating the charts
     */

    private void handleConsumptionData(){
        cons = new double[24];
        double max_cons = 0;

        for(int i=0;i<cons.length;i++)
            cons[i]=1;

        for(int i=0; i<cons_data.size();i++){
            cons[cons_data.get(i).getAsInteger("Hour")]=(cons_data.get(i)).getAsDouble("Power");
//			Log.i(MODULE, "cons detailed "+cons[cons_data.get(i).getAsInteger("tm_slot")]+" time slot "+cons_data.get(i).getAsInteger("tm_slot"));
        }
        if(cons_data.get(cons_data.size()-1).getAsInteger("Hour")<cons.length){
            for(int j=cons_data.get(cons_data.size()-1).getAsInteger("Hour");j<cons.length;j++)
                cons[j]=0;
        }

        for(int i=0;i<cons.length;i++){
            if(cons[i]>max_cons)
                max_cons=cons[i];
        }

        avg_cons = new double[24];

        _chart.setMaxCons(max_cons*1.3);
        _chart.setCons_data(cons);
        _chart.setAvg_cons_data(cons);
        _chart.requestRender();
        calculateConsMetrics();
        /*for(int i=0; i<average_cons.size();i++)
            avg_cons[average_cons.get(i).getAsInteger("hour")]=(average_cons.get(i)).getAsDouble("cons");

        double max_cons = 0;
        for(int i=0;i<cons.length;i++){
            if(cons[i]>max_cons)
                max_cons=cons[i];
        }
        for(int i=0;i<avg_cons.length;i++){
            if(avg_cons[i]>max_cons)
                max_cons=avg_cons[i];
        }
        _chart.setCons_data(cons);
        _chart.setMaxCons(max_cons*1.3);
        _chart.setAvg_cons_data(avg_cons);
        _chart.requestRender();
        */
    }
    private void createProductionData(){
        int size =96;
        int[] termica = new int[size];
        int[] foto = new int[size];
        int[] hidrica = new int[size];
        int[] eolica = new int[size];
        int[] biomassa = new int[size];
        int max_index=0;
        double total_renew = 0;
        double total_solar = 0;
        double total_water = 0;
        double total_wind = 0;
        double total_termal = 0;
        double total = 0;
        int max =0;

        for(int i=0;i<prod_data.size();i++){			// get the production data
            int index = prod_data.get(i).getAsInteger("timeslot");
            max_index = index>max_index?index:max_index;
            termica[index]  = prod_data.get(i).getAsInteger("termica");
            foto[index] 	= prod_data.get(i).getAsInteger("foto");
            hidrica[index]  = prod_data.get(i).getAsInteger("hidrica");
            eolica[index]   = prod_data.get(i).getAsInteger("eolica");
            biomassa[index] = prod_data.get(i).getAsInteger("biomassa");
            total = total + prod_data.get(i).getAsInteger("total");
            total_solar = total_solar+foto[index];
            total_water = total_water+hidrica[index];
            total_wind  = total_wind+eolica[index];

            total_renew = total_renew + foto[index] + hidrica[index] + eolica[index];
            total_termal = total_termal + ( prod_data.get(i).getAsInteger("total") - (foto[index] + hidrica[index] + eolica[index]));

            max = max<(prod_data.get(i).getAsInteger("total")+foto[index] + hidrica[index] + eolica[index])?(prod_data.get(i).getAsInteger("total")+foto[index] + hidrica[index] + eolica[index]):max;
        }
        _renewQuota.setText(Math.round((total_renew/total)*100)+"%");
        // we use this vars to displays the "current" produciton
        float last_solar = prod_data.get(0).getAsInteger("foto");
        float last_water = prod_data.get(0).getAsInteger("hidrica");
        float last_wind  = prod_data.get(0).getAsInteger("eolica");
        float last_total = prod_data.get(0).getAsInteger("total");
        float last_termal = last_total - (last_solar+last_wind+last_water);
        DecimalFormat df = new DecimalFormat("##.#");
        _solarPrecent.setText( df.format((last_solar/last_total)*100)+"%");
        _waterPrecent.setText( df.format((last_water/last_total)*100)+"%");
        _windPrecent.setText( df.format((last_wind/last_total)*100)+"%");
        _termalPrecent.setText(Math.round((last_termal/last_total)*100)+"%");
        Log.i(MODULE, "total today "+total+" total renew today"+total_renew+" quota "+(total_renew/total) + "  sol"+total_solar/total);

        for(int i=0;i<pred_data.size();i++){		// populates the rest of the array with predicted data
            int index = pred_data.get(i).getAsInteger("timeslot");
            if(index> max_index && index<96){
                termica[index]  = pred_data.get(i).getAsInteger("termica");
                foto[index] 	= pred_data.get(i).getAsInteger("foto");
                hidrica[index]  = pred_data.get(i).getAsInteger("hidrica");
                eolica[index]   = pred_data.get(i).getAsInteger("eolica");
                biomassa[index] = pred_data.get(i).getAsInteger("biomassa");
                max = max<pred_data.get(i).getAsInteger("total")?pred_data.get(i).getAsInteger("total"):max;
            }
        }
        for(int i=1;i<termica.length-1;i++){			// checks for "holes" in the data
            if(termica[i]==0){
                termica[i]  = Math.round((nextNonZero(termica,i)+termica[i-1])/2);
                foto[i] 	= Math.round((nextNonZero(foto,i)+foto[i-1])/2);
                hidrica[i]  = Math.round((nextNonZero(hidrica,i)+hidrica[i-1])/2);
                eolica[i]   = Math.round((nextNonZero(eolica,i)+eolica[i-1])/2);
                biomassa[i] = Math.round((nextNonZero(biomassa,i)+biomassa[i-1])/2);
            }
        }
        _chart.setText_size(_renewQuota.getTextSize());
        _chart.setMax_scale((int) Math.round(max));
        _chart.setData(termica, hidrica, eolica, biomassa, foto);
        _chart.setTotal_renewables((int) Math.round(total_renew));
        _chart.setTotal((int)Math.round(total));
        _chart.requestRender();

       // Log.i(MODULE,"max chart "+max);

      //  _chart.requestRender();
    }
    private void calculateRenewAverages(){
        ArrayList<ContentValues> prod_average = DBManager.getDBManager().getProductionAverage();
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        double total=0;
        double total_renew=0;
        for(int i=0; i<hour;i++){
            total= total +prod_average.get(i).getAsDouble("total");
            total_renew = total_renew +prod_average.get(i).getAsDouble("hidrica")+prod_average.get(i).getAsDouble("eolica")+prod_average.get(i).getAsDouble("foto");
        }
        _chart.setAverage_renew((float) ((float) total_renew/total));
        Log.i(MODULE, "hour "+hour+" avg total "+total+" avg total_renew "+total_renew);
        _renewQuota.setText(Math.round((total_renew/total)*100)+"%");
        _chart.requestRender();
    }
    private void calculateConsMetrics(){
        double total_sofar=0;
        double avg_sofar=0;
        double percent = 0;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int limit = hour<avg_cons.length? hour: avg_cons.length;
        for(int i=0;i<limit;i++)
            avg_sofar = avg_sofar + avg_cons[i];

        int minutes = Calendar.getInstance().get(Calendar.MINUTE);
        double prec=0;
        if(minutes!=0)
            prec = 60/minutes;

        hour = hour ==0?1:hour;
        avg_sofar = avg_sofar + avg_sofar*((1/hour)*prec);

        for(int i=0;i<cons.length;i++)
            total_sofar = total_sofar + cons[i];

//		double test1 = total_sofar/(hour*60);
//		double test2 = avg_sofar/(hour*4);

        if((total_sofar/(hour*60))>(avg_sofar/(hour*4))){
            Log.i(MODULE, "total bigger");
            percent = (total_sofar/(hour*60))/(avg_sofar/(hour*4));
            _comparison.setText("+"+Math.round((percent-1)*100)+"%");
        }else{
            percent = (avg_sofar/(hour*4))/(total_sofar/(hour*60));
            _comparison.setText(Math.round((1-percent)*100)+"%");
        }
        double [] hourly_cons = new double[(int)Math.round(cons.length/3)];
        int j=0;
        int k=0;
        double total_temp = 0;
        total_sofar = 0;
        for(int i=0;i<cons.length;i++){
            if(j<=15){
                total_temp = (total_temp + cons[i]);
                j++;
            }else{
                hourly_cons[k]=total_temp;
                total_sofar = total_sofar + total_temp/15;
                total_temp = 0;
                j=0;
                k++;
            }
        }
        DecimalFormat df = new DecimalFormat("#.#");
        _totalCons.setText(df.format((total_sofar/1000)));
        _totalCost.setText(df.format((total_sofar/1000)*0.12)+"");
        double co2Day = (total_sofar/1000)*0.762;
        co2Day = co2Day - co2Day*DBManager.getDBManager().getTodayRenewPrecentage();
        _totalEmissions.setText(df.format(co2Day));
    }
    private void populateWithAverage(){
        ArrayList<ContentValues> prod_average = DBManager.getDBManager().getProductionAverage();
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int size =24;
        int[] termica = new int[size];
        int[] foto = new int[size];
        int[] hidrica = new int[size];
        int[] eolica = new int[size];
        int[] biomassa = new int[size];
        int total_renew = 0;
        int total = 0;

        for(int i=0; i<hour;i++){
            termica[i]  = (int) Math.round(prod_average.get(i).getAsDouble("total"));
            foto[i]     = (int) Math.round(prod_average.get(i).getAsDouble("foto"));
            hidrica[i]  = (int) Math.round(prod_average.get(i).getAsDouble("hidrica"));
            eolica[i]   = (int) Math.round(prod_average.get(i).getAsDouble("eolica"));
            biomassa[i] = (int) Math.round(prod_average.get(i).getAsDouble("biomassa"));
            total = (int) (total + prod_average.get(i).getAsDouble("total"));
            total_renew = total_renew + foto[i] + hidrica[i] + eolica[i];
        }
        _chart.setData(termica, hidrica, eolica, biomassa, foto);
        _chart.setTotal_renewables(total_renew);
        _chart.setTotal(total);
        _chart.requestRender();
        calculateRenewAverages();
    }
    private int nextNonZero(int[] data, int index){
        int result = 1;
        for(int i=index;i<data.length;i++){
            if(data[i]!=0){
                result = data[i];
                return result;
            }
        }
        return result;
    }
    /**
       Request stuff - services and handlers modafaquer
     */
    private class ProductionHandler extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.i(MODULE, "Querying day ");
            try{
                prod_data = web_handler.getEnergyProduction();
                pred_data = web_handler.getEnergyProductionPrediction();
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
          if(prod_data!=null && pred_data!=null)
                createProductionData();
            else
                populateWithAverage();
        }
    }
    private class ConsumptionHandler extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.i(MODULE, "Querying day detailed ");
            try{
                Date cDate = new Date();
                String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
                cons_data = web_handler.getTodayDetailedCons("http://aveiro.m-iti.org/hybridnilm/public/api/v1/plugwise/samples/hourly",fDate);
                Log.i(MODULE, "Querying day detailed ");
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
            if(cons_data!=null)
                handleConsumptionData();
            else{
               cons_data = new ArrayList<ContentValues>();
               average_cons =  new ArrayList<ContentValues>();
               for(int i=0;i<100;i++) {
                  ContentValues cv = new ContentValues();
                   cv.put("tm_slot",i);
                   cv.put("cons", Math.random()*500+500);
                   cons_data.add(cv);
               }

                for(int i=0;i<24;i++) {
                    ContentValues cv = new ContentValues();
                    cv.put("hour",i);
                    cv.put("cons", Math.random()*1000);
                    average_cons.add(cv);
                }

                handleConsumptionData();
            }

        }
    }
    private class RequestHandler extends Thread{
        @Override
        public void run(){
            //running
            while(true){
                ProductionHandler nova = new ProductionHandler();
                nova.execute();

                ConsumptionHandler req = new ConsumptionHandler();
                req.execute();

                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
