package android.prsma.org.energyspectrum.customUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.prsma.org.energyspectrum.R;
import android.prsma.org.energyspectrum.database.DBManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.text.DecimalFormat;

public class SummaryWidget extends SurfaceView implements SurfaceHolder.Callback{
	
	private float _total_month;
	private float _total_week;
	private float _total_day;
	int _color_month;
	int _color_week;
	int _color_day;
	private float _text_size;
	private final static String MODULE = "Summary widget";
	private float _width;
	private float _height;

	public SummaryWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		getHolder().addCallback(this);
		_color_month = getResources().getColor(R.color.app_main_darker);
		_color_week = getResources().getColor(R.color.app_main_dark);
		_color_day   = getResources().getColor(R.color.app_main);
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
	private void drawComp(Canvas c) {
		_width = getWidth()*0.9f;
		_height = getHeight()*0.9f;

		float r0 = (int) Math.round(_height/2.3);
		float r1 = calculateRadious(_total_month,r0);//(_total_month*r0)/max_cons;
		float r2 = calculateRadious(_total_week,r0);//(_total_week*r0)/max_cons;
		float r3 = calculateRadious(_total_day,r0);//(_total_day*r0)/max_cons;
		Log.i(MODULE,"radious: month"+r1+" week"+r2+" day"+r3);
		
		Paint paint_month = new Paint();
		paint_month.setColor(_color_month);
		paint_month.setAntiAlias(true);
		
		Paint paint_week = new Paint();
		paint_week.setColor(_color_week);
		paint_week.setAntiAlias(true);
		
		Paint paint_day = new Paint();
		paint_day.setColor(_color_day);
		paint_day.setAntiAlias(true);
		
		if( _total_month > _total_week){
			c.drawCircle(_width/2, _height/2, r1, paint_month);
			c.drawCircle(_width/2, _height/2, r2, paint_week);
		}else{
			c.drawCircle(_width/2, _height/2, r2, paint_week);
			c.drawCircle(_width/2, _height/2, r1, paint_month);
		}

	
		c.drawCircle(_width/2, _height/2,r3, paint_day);
		
		drawConnections(c,r1,r2,r3);
	}
	private void drawConnections(Canvas c, float r1, float r2, float r3){
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(2);
		paint.setAntiAlias(true);
		paint.setColor(Color.parseColor("#D9D9D9"));
		if(r1<r2){
			float temp = r1;
			r1=r2;
			r2=temp;
		}
		
		float h = _height;
		double y = (_width/3)*Math.tan(Math.PI/6);
		float c_x = _width/2;
		
		float month_x = (_width/2)+r1+10;
		float month_y= (float) (_height/1.2);
		float week_x =  (_width/2)+r1+10;
		float week_y= (float) (h/2);
		float day_x = (_width/2)+r1+10;
		float day_y= (float) ( (h/2)-y);
			
		c.drawLine(c_x, (h/2+r1)-10, month_x-20, month_y, paint);
		c.drawLine( month_x-20, month_y, month_x+_text_size, month_y, paint);
		c.drawLine(_width/2+r2-5, (h/2), week_x+_text_size, week_y, paint);
		c.drawLine(c_x, (h/2),day_x-20 ,day_y , paint);
		c.drawLine(day_x-20 ,day_y,day_x+_text_size ,day_y , paint);
		
		c.drawCircle(c_x, (h/2+r1)-10, 5, paint);
		c.drawCircle(_width/2+r2-5, (h/2), 5, paint);
		c.drawCircle(c_x,  (h/2), 5, paint);
		
		drawAverages(c,month_x,month_y,week_x,week_y,day_x,day_y);
	}
	private void drawAverages(Canvas c,float month_x, float month_y, float week_x,float week_y, float day_x, float day_y) {
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setTextSize(_text_size);
		double co2Month = (_total_month)*0.762;
		co2Month = co2Month - co2Month* DBManager.getDBManager().getThisMonthRenewPrecentage();
		double co2Week = (_total_week)*0.762;
		co2Week = co2Week - co2Week*DBManager.getDBManager().getThisWeekRenewPercentage();
		double co2Day = (_total_week)*0.762;
		co2Day = co2Day - co2Day*DBManager.getDBManager().getTodayRenewPrecentage();
		
		if(_total_month > _total_week){
			p.setColor(_color_month);
			drawAverage(c,month_x+10,month_y, _total_month,co2Month,p);
			p.setColor(_color_week);
			drawAverage(c,week_x+10,week_y, _total_week,co2Week,p);
		}else{
			p.setColor(_color_week);
			drawAverage(c,month_x+10,month_y, _total_week,co2Week,p);
			p.setColor(_color_month);
			drawAverage(c,week_x+10,week_y, _total_month,co2Month,p);
		}
		p.setColor(_color_day);
		drawAverage(c,day_x+10,day_y, _total_day,co2Day,p);
	}
	private void drawAverage(Canvas c, float x, float y,float val,double co2,Paint p){
		DecimalFormat df = new DecimalFormat("#.#");
		c.drawText(df.format(val)+" kWh", x+_text_size, y, p);
		c.drawText(df.format(co2)+" g CO2",x+_text_size,y+5+_text_size,p);
		c.drawText(df.format((val)*0.12)+" €", x+_text_size, y+5+_text_size*2, p);
	}
	private void drawDescription(Canvas c){
		Paint p = new Paint();
		p.setTextSize(_text_size);
		p.setColor(Color.BLACK);
		p.setAntiAlias(true);
		/*c.drawText("Aqui poderá ver um resumo do seu", 0f ,_text_size, p);
		c.drawText("consumo hoje nesta semana", 0f ,_text_size*2, p);
		c.drawText("e neste mes. O tamanho", 0f ,_text_size*3, p);
		c.drawText(" O tamanho de cada circulo representa", 0f ,_text_size*4, p);
		c.drawText("o consumo no respectivo ", 0f ,_text_size*5, p);
		c.drawText("periodo.", 0f ,_text_size*6, p);*/
		c.drawText("O tamanho de cada circulo representa", 0f ,_text_size, p);
		c.drawText("o consumo no respectivo ", 0f ,_text_size*2, p);
		c.drawText("periodo.", 0f ,_text_size*3, p);

	}
	private float calculateRadious(float cons, float max_r){
		float max_cons = _total_month > _total_week ? _total_month : _total_week;
		float result = 0f;
		result = (cons*max_r)/max_cons;
		double dat_data = (Math.log((cons/max_cons)));
		Log.e(MODULE, "Dat data "+dat_data);
		return result+ (float)(result*Math.abs(dat_data));
	}
	@Override
	public void onDraw(Canvas c){
		if(c!=null){
			c.drawColor(Color.parseColor("#FFFFFF"));
			if(_total_day !=0){
				drawComp(c);
				drawLegend(c);
				drawDescription(c);
			}else{
				drawLoading(c);
			}
		}
	}
	private void drawLegend(Canvas c) {
		float x = 0;

		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setTextSize(_text_size);
		
		p.setColor(_color_month);
		c.drawRect(x, _height-_text_size*3-10, 10, _height-30, p);
		c.drawText("Mês", 15,  _height-_text_size*2-10, p);
		
		p.setColor(_color_week);
		c.drawRect(x, _height-_text_size*2-10, 10, _height-15, p);
		c.drawText("Semana", 15,  _height-_text_size-10, p);
		
		p.setColor(_color_day);
		c.drawRect(x, _height-_text_size-10, 10, _height-10, p);
		c.drawText("Hoje", 15,  _height-10, p);
		
	}
	public void drawLoading(Canvas c){
		float y = _height/2;
		float x = _width/2;
		Paint p = new Paint();
		p.setTextSize(_text_size);
		p.setAntiAlias(true);

		p.setColor(getResources().getColor(R.color.app_main));
		c.drawText("Loading consumption data...", x-p.measureText("Loading consumption data..."), y, p);
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		requestRender();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		requestRender();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}
	public float get_total_month() {
		return _total_month;
	}
	public void set_total_month(float _total_month) {
		this._total_month = _total_month;
	}
	public float get_total_week() {
		return _total_week;
	}
	public void set_total_week(float _total_week) {
		this._total_week = _total_week;
	}
	public float get_total_day() {
		return _total_day;
	}
	public void set_total_day(float _total_day) {
		this._total_day = _total_day;
	}

	public float get_text_size() {
		return _text_size;
	}

	public void set_text_size(float _text_size) {
		this._text_size = _text_size;
	}
}
