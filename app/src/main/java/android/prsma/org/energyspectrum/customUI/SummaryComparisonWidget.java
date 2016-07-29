package android.prsma.org.energyspectrum.customUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.prsma.org.energyspectrum.R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SummaryComparisonWidget  extends SurfaceView implements SurfaceHolder.Callback{
	
	private int _maxDailyCons;
	private int _maxWeeklyCons;
	private int _maxMonthlyCons;
	private float _daily_cons;
	private float _daily_avg;
	private float _weekly_cons;
	private float _weekly_avg;
	private float _monthly_cons;
	private float _monthly_avg;

	private int _comparisonBarHeight = 30;


	private static final String MODULE = "Summary Comparison Widget";


	public SummaryComparisonWidget(Context context,AttributeSet attrs) {
		super(context, attrs);
		getHolder().addCallback(this);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onDraw(Canvas c){
		if(c!=null){
			c.drawColor(Color.parseColor("#FFFFFF"));
			drawComp(c);
		}
	}
	private void drawComp(Canvas c) {
		float w = getWidth();
		float h = getHeight();
		int rect1_h = (int) Math.round(h/10);
		int starY = 10;
		Paint textPaint = new Paint();
		textPaint.setTextSize(22);
		textPaint.setAntiAlias(true);
		c.drawLine(w/2, starY, w/2, h,textPaint);
		drawComparison(starY+35, c, _daily_cons, _daily_avg, get_maxDailyCons(),"Ontem "," Hoje");
		drawComparison(starY + 90, c, _weekly_cons, _weekly_avg, get_maxWeeklyCons(), "Semana passada ", " Esta semana ");
		drawComparison(starY + 145, c, _monthly_cons, _monthly_avg, get_maxMonthlyCons(), "Mês passado ", " Este Mês");
	}
	private int calculateBarWidth(float avg,float max){
		return Math.round((avg*getWidth()/2)/max);
	}
	private void drawComparison(int y,Canvas c,float cons, float avg, float max, String period1, String period2){
			
		int cons_width = calculateBarWidth(cons,max);
		int avg_width  = calculateBarWidth(avg,max);
		Paint p = new Paint();
		Paint p2 = new Paint();
		Paint textPaint = new Paint();
		textPaint.setTextSize(15);
		textPaint.setAntiAlias(true);

		p.setColor(getResources().getColor(R.color.app_main));
		p.setAntiAlias(true);
		p2.setColor(getResources().getColor(R.color.app_main_dark));
		p2.setAntiAlias(true);
		float w = getWidth();
		int middle = (int)(w/2);
		
		Rect r  = new Rect(middle - avg_width, y,middle,y+_comparisonBarHeight);
		float text = textPaint.measureText(period1);
    	c.drawText(period1, (w / 2) - text, y - 5, textPaint);
		c.drawRect(r, p);
		//c.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.left_tip), middle - avg_width, y, p);
		drawTriangle(false,c,middle - avg_width,y,p);
    	Rect r2 = new Rect(middle, y,middle+cons_width,y+_comparisonBarHeight);
    	text = textPaint.measureText(cons+"");
    	float tx = (middle+cons_width-text) > middle ? (middle+cons_width-text) : middle;
    	
		c.drawText(period2, w / 2, y - 5, textPaint);
		c.drawRect(r2, p2);
		//c.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.right_tip),middle+cons_width, y, p);
		drawTriangle(true, c, middle + cons_width, y, p2);
		p2.setColor(Color.WHITE);
		c.drawText(cons+"", tx, y+18, p2);

	}
	private void drawTriangle(boolean orientation, Canvas c, int x, int y,Paint p){
		Point point1_draw = new Point(x, y);
		Point point3_draw = new Point(x, y + _comparisonBarHeight);
		Point point2_draw;
		if(orientation)
			point2_draw = new Point(x+10,y+_comparisonBarHeight/2);
		else
			point2_draw = new Point(x-10,y+_comparisonBarHeight/2);

		Path path = new Path();
		path.setFillType(Path.FillType.EVEN_ODD);
		path.moveTo(point1_draw.x,point1_draw.y);
		path.lineTo(point2_draw.x,point2_draw.y);
		path.lineTo(point3_draw.x,point3_draw.y);
		path.lineTo(point1_draw.x,point1_draw.y);
		path.close();
		c.drawPath(path, p);
	}
	@SuppressLint("WrongCall")
	public void requestRender(){
		System.gc(); // ainda n‹o se se Ž necess‡rio mas pronto s— para ter certeza chamamos o GC agora.. aqui Ž um lugar seguro
		Canvas c = null;
		SurfaceHolder sh = getHolder();
		try {
			c = sh.lockCanvas(null);
			synchronized (sh) {
				onDraw(c);
			}
		} finally {
			// do this in a finally so that if an exception is thrown
			// during the above, we don't leave the Surface in an
			// inconsistent state
			if (c != null) {
				sh.unlockCanvasAndPost(c); 
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		requestRender();
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.e("COMP","COMP WIDGET CREATED");
		requestRender();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	public int get_maxDailyCons() {
		return _maxDailyCons;
	}

	public void set_maxDailyCons(int _maxDailyCons) {
		this._maxDailyCons = _maxDailyCons;
	}

	public int get_maxWeeklyCons() {
		return _maxWeeklyCons;
	}

	public void set_maxWeeklyCons(int _maxWeeklyCons) {
		this._maxWeeklyCons = _maxWeeklyCons;
	}

	public int get_maxMonthlyCons() {
		return _maxMonthlyCons;
	}

	public void set_maxMonthlyCons(int _maxMonthlyCons) {
		this._maxMonthlyCons = _maxMonthlyCons;
	}


	public void set_daily_cons(float _daily_cons) {
		this._daily_cons = _daily_cons;
	}


	public void setYesterday_con(float daily_avg) {
		this._daily_avg = daily_avg;
	}

	public void set_weekly_cons(float _weekly_cons) {
		this._weekly_cons = _weekly_cons;
	}

	public void setLastWeek_cons(float weekly_avg) {
		this._weekly_avg = weekly_avg;
	}

	public void set_monthly_cons(float _monthly_cons) {
		this._monthly_cons = _monthly_cons;
	}

	public void setLastMonth_cons(float monthly_avg) {
		this._monthly_avg = monthly_avg;
	}
}
