package android.prsma.org.energyspectrum.customUI;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.prsma.org.energyspectrum.R;
import android.prsma.org.energyspectrum.mainActivities.DayConsumptionActivity;
import android.prsma.org.energyspectrum.mainActivities.MonthConsumptionActivity;
import android.prsma.org.energyspectrum.mainActivities.ProductionActivity;
import android.prsma.org.energyspectrum.mainActivities.WeekConsumptionActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Handles the touch on the top Menu of each activity,
 * It also handles the touch in the main view of each activity. This is used to time when was the last interaction with the system and to trigger the screensaver.
 * @author filipequintal
 *
 */
public class TabbedMenuHandler implements View.OnTouchListener {
	
	private Context _ctx;
	private int _wasTouched=0;
	
	@SuppressWarnings("unused")
	private   LinearLayout vg = null;
	Intent newInt;
	
	private final static String MODULE = "Tabbed Menu Handler";
	
	public void setContext(Context ctx){
		_ctx = ctx;
		
	}
	
//	public void startAct(Context ctx){
//		
//		Log.e("test", R.id.appliance_label+"");
//		Intent _int = new Intent(ctx, PowerMeterMobileActivity.class);
//		_int.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		ctx.startActivity(_int);
//	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(v instanceof LinearLayout)
			vg=  (LinearLayout) v;
		else
		 return false;
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(isButton(v.getId()))
				v.setBackgroundColor((Color.parseColor("#4682b4")));
			break;
	
		case MotionEvent.ACTION_UP: // launches the new activity on UP (this prevents the system from launching several activities in a row) also updates a touch on the screen
			
			//FIRST CHECK IF THE USER CLICKED IN ANY OF THE LAYOUTS USED AS BUTTONS
			if(v.getId()== R.id.home_btn_week || v.getId()==R.id.home_btn_events || v.getId()==R.id.home_btn_month || v.getId()==R.id.home_btn_day || v.getId() == R.id.home_btn_prod){
//				newInt = new Intent(_ctx, PowerMeterMobileActivity.class);
//				newInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				screenTouched();								// the screen was touched
//				_ctx.startActivity(newInt);
//				((Activity) _ctx).finish();
			}
			else if(v.getId()==R.id.day_btn_week || v.getId()==R.id.day_btn_events || v.getId()==R.id.day_btn_month || v.getId()==R.id.today_btn || v.getId()==R.id.day_btn_prod){
				newInt = new Intent(_ctx, DayConsumptionActivity.class);
				newInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				screenTouched();								// the screen was touched
				_ctx.startActivity(newInt);
				v.setBackgroundColor(Color.parseColor("#E2E0DB"));
			}
			else if(v.getId()==R.id.week_btn_day || v.getId()==R.id.week_btn_events || v.getId()==R.id.week_btn_month || v.getId()==R.id.week_btn || v.getId()==R.id.week_btn_prod){
				newInt = new Intent(_ctx, WeekConsumptionActivity.class);
				newInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				screenTouched();								// the screen was touched
				_ctx.startActivity(newInt);
				v.setBackgroundColor(Color.parseColor("#E2E0DB"));
			}
			else if(v.getId()==R.id.month_btn_week || v.getId()==R.id.month_btn_events || v.getId()==R.id.month_btn || v.getId()==R.id.month_btn_day || v.getId()==R.id.month_btn_prod){
				newInt = new Intent(_ctx, MonthConsumptionActivity.class);
				newInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				screenTouched();								// the screen was touched
				_ctx.startActivity(newInt);
				v.setBackgroundColor(Color.parseColor("#E2E0DB"));
			}
			else if(v.getId()==R.id.month_prod_btn || v.getId()==R.id.week_prod_btn || v.getId()==R.id.home_prod_btn || v.getId()==R.id.day_prod_btn){
				newInt = new Intent(_ctx, ProductionActivity.class);
				newInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				screenTouched();								// the screen was touched
				_ctx.startActivity(newInt);
				v.setBackgroundColor(Color.parseColor("#E2E0DB"));
			}
//			else if(v.getId()==R.id.events_btn_week || v.getId()==R.id.events_btn || v.getId()==R.id.events_btn_month || v.getId()==R.id.events_btn_day){
//				newInt = new Intent(_ctx, EventChartActivity.class);
//				newInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);		
//				screenTouched();								// the screen was touched
//				_ctx.startActivity(newInt);
//				v.setBackgroundColor(Color.parseColor("#fefada"));
//			}
			//FINALLY CHECK IF THE USER CLICKED IN THE LAYOUT (TO BE USED TO TRIGGER THE SCREENSAVER)
			else if(!isButton(v.getId())){
				Log.i(MODULE, "screen was touched");
				screenTouched();
			}
			break;
			
		default:
			break;
		}
		return true;
	}

	public void screenTouched() {
		this._wasTouched = 1;
	}
	
	public int getTouchStatus(){
		return _wasTouched;
	}

	public int resetTouch() {
		return _wasTouched=0;
	}
	
	/**
	 * Checks if a view is part of the tab menu
	 * @param id - id of the View that was just pressed
	 * @return
	 */
	public boolean isButton(int id){
		if(id==R.id.home_main_layout || id==R.id.month_main_layout || id==R.id.week_main_layout || id==R.id.day_main_layout || id==R.id.events_main_layout)
			return false;
		else
			return true;
	}
	
	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) _ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}

}


