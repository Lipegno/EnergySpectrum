package layout;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.prsma.org.energyspectrum.R;
import android.prsma.org.energyspectrum.customUI.BarConsumptionChart;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WeekConsumptionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WeekConsumptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeekConsumptionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //View Stuff
    private BarConsumptionChart _chart;
    private RadioGroup _week_comparison_first_group;
    private RadioGroup _week_comparison_second_group;
    private final CheckBoxHandler _comparison_handler = new CheckBoxHandler();
    private TextView _total_week_kwh;

    public WeekConsumptionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WeekConsumptionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WeekConsumptionFragment newInstance(String param1, String param2) {
        WeekConsumptionFragment fragment = new WeekConsumptionFragment();
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
        View v = inflater.inflate(R.layout.fragment_week_consumption, container, false);
        _chart = (BarConsumptionChart)v.findViewById(R.id.week_cons_chart);
        initView(v);
        return v;
    }

    private void initView(View v){

        initCheckBoxes(v);
        _total_week_kwh = (TextView)v.findViewById(R.id.week_total_kwh);
        _chart.setText_size(_total_week_kwh.getTextSize());
    }

    private void initCheckBoxes(View v){

        _week_comparison_first_group = (RadioGroup) v.findViewById(R.id.week_selector_first_line);
        _week_comparison_second_group = (RadioGroup) v.findViewById(R.id.week_selector_second_line);

        _week_comparison_first_group.setOnCheckedChangeListener(_comparison_handler);
        _week_comparison_second_group.setOnCheckedChangeListener(_comparison_handler);

        /*_week1box = (CheckBox) v.findViewById(R.id.checkbox_1week);
        _week2box = (CheckBox) v.findViewById(R.id.checkbox_2week);
        _week3box = (CheckBox) v.findViewById(R.id.checkbox_3week);
        _week4box = (CheckBox) v.findViewById(R.id.checkbox_4week);
        _week5box = (CheckBox) v.findViewById(R.id.checkbox_5week);
        _week1box.setOnCheckedChangeListener(_comparison_handler);
        _week2box.setOnCheckedChangeListener(_comparison_handler);
        _week3box.setOnCheckedChangeListener(_comparison_handler);
        _week4box.setOnCheckedChangeListener(_comparison_handler);
        _week5box.setOnCheckedChangeListener(_comparison_handler);*/


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
           //         + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Check box handlers stuff
     */
    private class CheckBoxHandler implements RadioGroup.OnCheckedChangeListener{

        private boolean first_flag = true;
        private boolean second_flag = false;

        private void addComparisonData(){
            final double[] stupid_array = new double[48];
            for (int i = 0; i < 7; i++)
                stupid_array[i] = 500 + (400 * Math.random());
            _chart.set_comp_data(stupid_array);
        }
        private void removeComparisonData(int index){
           // _consumption_chart.removeComparisonData(index);
           // _consumption_chart.requestRender();
        }

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {

            switch (i){
                case R.id.radio_average:
                    if(second_flag) {
                        _week_comparison_second_group.clearCheck();
                        second_flag=false;
                    }
                    first_flag = true;
                    addComparisonData();
                     break;
                case R.id.radio_1week:
                    if(second_flag) {
                        _week_comparison_second_group.clearCheck();
                        second_flag=false;
                    }
                    first_flag = true;
                    addComparisonData();
                    break;
                case R.id.radio_2week:
                    if(second_flag) {
                        _week_comparison_second_group.clearCheck();
                        second_flag=false;
                    }
                    first_flag = true;
                    addComparisonData();
                    break;
                case R.id.radio_3week:
                    if(second_flag) {
                        _week_comparison_second_group.clearCheck();
                        second_flag=false;
                    }
                    first_flag = true;
                     addComparisonData();
                    break;
                case R.id.radio_high_week:
                    if(first_flag) {
                        _week_comparison_first_group.clearCheck();
                        first_flag=false;
                    }
                    second_flag = true;
                    addComparisonData();
                    break;
                case R.id.radio_low_week:
                    if(first_flag) {
                        _week_comparison_first_group.clearCheck();
                        first_flag=false;
                    }
                    second_flag = true;
                    addComparisonData();
                    break;

                default:
                    break;
            }
        }

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
