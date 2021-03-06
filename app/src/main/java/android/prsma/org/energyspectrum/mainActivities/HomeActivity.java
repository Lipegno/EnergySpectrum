package android.prsma.org.energyspectrum.mainActivities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.prsma.org.energyspectrum.R;

import layout.ConsumptionLogFragment;
import layout.MonthConsumptionFragment;
import layout.WeekConsumptionFragment;
import android.prsma.org.energyspectrum.customUI.LineConsumptionChart;
import android.prsma.org.energyspectrum.database.DBManager;
import android.prsma.org.energyspectrum.dtos.RuntimeConfigs;
import android.prsma.org.energyspectrum.services.SocketConnectionService;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import layout.DayConsumptionFragment;
import layout.ProductionFragment;
import layout.SummaryFragment;

import static android.prsma.org.energyspectrum.webservices.WebServiceHandler.MODULE;

public class HomeActivity extends AppCompatActivity implements ConsumptionLogFragment.OnFragmentInteractionListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     *  CREATED IN DESKTOP
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager _viewPager;
    private SocketConnectionService _current_cons_service;   			// service
    private ServiceConn _service_connection;								// connection handler
    private currentConsReceiver _receiver = new currentConsReceiver();	// broadcast _receiver
    private RuntimeConfigs _configs;

    private SummaryFragment _summaryFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

       // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        _viewPager = (ViewPager) findViewById(R.id.container);
        _viewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(_viewPager);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                PlaceholderFragment.renderChart();
            }
        });*/

        _configs = RuntimeConfigs.getConfigs();

        if (! DBManager.databaseExists())
            DBManager.initDatabase();
        else
            Log.i(MODULE, "Database already exists!!");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            _summaryFrag._currentConsLabel.setText("changed from main");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        static LineConsumptionChart chart;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
           // chart =(ConsumptionChart)rootView.findViewById(R.id.consumption_chart);
          //  chart.requestRender();
           // chart.setText_size(((TextView)rootView.findViewById(R.id.days_chart_title)).getTextSize());
          //  TextView textView = (TextView) rootView.findViewById(R.id.section_label);
           // textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        public static void renderChart(){
            chart.requestRender();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                _summaryFrag =  SummaryFragment.newInstance("aqui1", "aqui2");
                return _summaryFrag;
            }else if (position == 1){
                return DayConsumptionFragment.newInstance("aqui1","aqui2");
            } else if(position == 2) {
                return WeekConsumptionFragment.newInstance("test", "test");
            }else if(position == 3){
                return MonthConsumptionFragment.newInstance("test", "test");
            }else if(position == 4)
                return ProductionFragment.newInstance("aqui1", "aqui2");
            else
                return ConsumptionLogFragment.newInstance("dummy","dummy");

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Summary";
                case 1:
                    return "Daily Consumption";
                case 2:
                    return "Weekly Consumption";
                case 3:
                    return "Monthly Consumption";
                case 4:
                    return "Energy Availability";
                case 5:
                    return "Consumption Log";
            }
            return null;
        }
    }

    /*
    Services Stuff
     */

    /*
    Current cons service
     */
    /**
     * Class that handles the connection to the current consumption socket
     * @author filipequintal
     *
     */
    private class ServiceConn implements ServiceConnection
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            _current_cons_service = ((SocketConnectionService.MyBinder)service).getService();
            try {
                _configs.setRequestReplySocket(_current_cons_service);
                _current_cons_service.start();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                _current_cons_service.stopSelf();
                e.printStackTrace();
            }
            Log.e(MODULE,"binding the service");
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            _current_cons_service = null;
        }
    }
    /**
     * Broadcast _receiver responsible for handling the data from the current consumption service/socket
     * @author filipequintal
     */
    private class currentConsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            Bundle extras = arg1.getExtras();
            String func = extras.getString(SocketConnectionService.CONS_KEY);
            Log.i(MODULE,func);
            if(func.equals(_current_cons_service.SOCKET_ERROR_MSG)){
                _current_cons_service.stopSelf();
                Log.e(MODULE, "error in the current cons socket");
               // displayConnectionError();

            }else{
              /*  Log.i(MODULE, "connection status "+_summaryFrag._connectionStatusLabel.getText());
                if(_summaryFrag._connectionStatusLabel.getText().equals("connection problem") || _summaryFrag._connectionStatusLabel.getText().equals("Ligando-se ao medidor...")){
                    Log.i(MODULE, "yoooooooooooo");
                    _summaryFrag.displayConnectionSuccessful();
                }
                _summaryFrag._currentConsLabel.setText(func+" W");*/
            }
            Log.d(MODULE, "current cons "+func);
        }

    }
}
