package layout;

import android.os.AsyncTask;
import android.os.Message;
import android.prsma.org.energyspectrum.webservices.WebServiceHandler;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.prsma.org.energyspectrum.dtos.EventSampleDTO;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.prsma.org.energyspectrum.R;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConsumptionLogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConsumptionLogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConsumptionLogFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ListView _eventLogView;
    private Context _appContext;

    private TextView _eventLabel;
    private TextView _eventTms;
    private TextView _eventCons;
    private TextView _eventOccur;
    private TextView _eventHap;

    private static final WebServiceHandler web_handler = WebServiceHandler.get_WS_Handler();
    private static final String TAG = "ConsumptionLogFragment";

    final ArrayList<EventSampleDTO> _event_data = new ArrayList<EventSampleDTO>();
    ConsumptionLogAdapter _adapter;

    ConsumptionLogUIHandler _handler = new ConsumptionLogUIHandler();

    public ConsumptionLogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConsumptionLogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConsumptionLogFragment newInstance(String param1, String param2) {
        ConsumptionLogFragment fragment = new ConsumptionLogFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_consumption_log, container, false);
        initView(v);
        return v;
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
        _appContext = context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    private void initView(View layout){

        _eventLogView = (ListView)layout.findViewById(R.id.events_log);
        _eventLabel   = (TextView) layout.findViewById(R.id.event_detail_label);
        _eventTms     = (TextView) layout.findViewById(R.id.event_detail_timestamp);
        _eventCons    = (TextView) layout.findViewById(R.id.event_detail_consumption);
        _eventOccur   = (TextView) layout.findViewById(R.id.event_detail_occurrences);
        _eventHap     = (TextView) layout.findViewById(R.id.event_detail_usual_occurrences);

        _adapter = new ConsumptionLogAdapter(_appContext,R.layout.event_item_layout,_event_data);
        _eventLogView.setAdapter(_adapter);

       // _event_data.add(new EventSampleDTO("MERDAAA",null,2000,10,10,"123123123123123123",null));
        _adapter.notifyDataSetChanged();

        _eventLogView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("ConsumptionLog","The "+i+" was selected");
                _eventLabel.setText(_event_data.get(i).get_guess());
                _eventTms.setText(_event_data.get(i).get_timestamp());
                _eventCons.setText(_event_data.get(i).get_deltaPMean()+" Watts");
                _eventOccur.setText("Also happened today at 7:30 and 10:30");
                _eventHap.setText("Usally happens in the morning");
            }
        });

        new EventHistoryRequestWorker().execute();
    }

    private ArrayList<EventSampleDTO> getDummyValues(){
        ArrayList<EventSampleDTO> events = new ArrayList<EventSampleDTO>();

        for(int i=0;i<24;i++){
            EventSampleDTO sample = new EventSampleDTO("Event "+i,null,
                    (int)Math.round(Math.random()*200),
                    i,
                    i,
                    this+""+System.currentTimeMillis(),
                    null);

            sample.set_color((ContextCompat.getColor(_appContext, R.color.event_color_1)));
            events.add(sample);
        }
        return events;
    }

    private class ConsumptionLogAdapter extends ArrayAdapter<EventSampleDTO>{

        private final Context _context;
        private final int _layout_resource_id;
        private ArrayList<EventSampleDTO> _values;

        public ConsumptionLogAdapter(Context context, int resource, ArrayList<EventSampleDTO> objects) {
            super(context, resource, objects);
            _context = context;
            _layout_resource_id = resource;
            _values = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View current_item = convertView;
            if(current_item == null){
                LayoutInflater inflater = ((android.app.Activity)_context).getLayoutInflater();
                current_item = inflater.inflate(_layout_resource_id,parent,false);
            }
            //current_item.setBackgroundColor(Color.parseColor("#FF0000"));
            EventItemHolder holder = new EventItemHolder();
            holder.event_icon  = (ImageView)current_item.findViewById(R.id.event_img);
            holder.event_label = (TextView) current_item.findViewById(R.id.event_list_label);
            holder.event_label.setText(_values.get(position).get_guess());
            setIcon(holder, position);
            return current_item;
        }

        private void setIcon(EventItemHolder currentItem, int position){

            String title = _values.get(position).get_guess().toLowerCase();
            if(title.contains("monitor")){
                currentItem.event_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.analytics,null ));
            }else if(title.contains("tv")) {
                currentItem.event_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.analytics,null ));
            }else if(title.contains("macbook")) {
                currentItem.event_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.laptop,null ));
            }else if(title.contains("router")){
                currentItem.event_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.router,null ));
            }else if(title.contains("freezer")){
                currentItem.event_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.minibar,null ));
            }else if(title.contains("refrigerator")){
                currentItem.event_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.minibar,null ));
            }else if(title.contains("vacuum")){
                currentItem.event_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.vacuum_cleaner,null ));
            }
        }

        class EventItemHolder{

            ImageView event_icon;
            TextView event_label;

        }
}
    private class EventHistoryRequestWorker extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... strings) {
            ArrayList<EventSampleDTO> event_data = web_handler.getEventsList("http://aveiro.m-iti.org/hybridnilm/public/api/v1/plugwise/samples/hourly","status");
            if(event_data!=null && event_data.size()>0){
                for(int i=0;i<event_data.size();i++){
                    _event_data.add(event_data.get(i));
                }
                _handler.sendEmptyMessage(0);
            }
            Log.i(TAG," got new events");
            return null;
        }
    }

    private class ConsumptionLogUIHandler extends android.os.Handler{
        @Override
        public void handleMessage(Message msg) {
            _adapter.notifyDataSetChanged();
        }
    }
}
