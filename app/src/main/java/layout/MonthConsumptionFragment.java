package layout;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.prsma.org.energyspectrum.customUI.LineConsumptionChart;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.prsma.org.energyspectrum.R;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MonthConsumptionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MonthConsumptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonthConsumptionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //View stuff
    private TextView _total_month_kwh;
    private LineConsumptionChart _chart;

    private OnFragmentInteractionListener mListener;

    public MonthConsumptionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MonthConsumptionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MonthConsumptionFragment newInstance(String param1, String param2) {
        MonthConsumptionFragment fragment = new MonthConsumptionFragment();
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
        View v = inflater.inflate(R.layout.fragment_month_consumption, container, false);
        initView(v);
        return v;
    }

    private void initView(View v){
        _total_month_kwh = (TextView) v.findViewById(R.id.month_total_kwh);
        _chart           = (LineConsumptionChart) v.findViewById(R.id.month_consumption_chart);
        _chart.setText_size(_total_month_kwh.getTextSize());
        _chart.setMode(LineConsumptionChart.MODE_MONTH);
        createDummyValues();
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
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void createDummyValues(){
        double[] cons_data = new double[31];
        for(int i=0;i<cons_data.length;i++)
            cons_data[i]=100+(800*Math.random());
        //   for(int i=25;i<48;i++)
        //  _cons_data[i]=0;
        double[] avg_cons_data = new double[31];
        for(int i=0;i<avg_cons_data.length;i++)
            avg_cons_data[i]=500+(400*Math.random());

        _chart.set_avg_cons_data(avg_cons_data);
        _chart.set_cons_data(cons_data);
        _chart.requestRender();
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
