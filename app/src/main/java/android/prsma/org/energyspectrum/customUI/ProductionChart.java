package android.prsma.org.energyspectrum.customUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.prsma.org.energyspectrum.R;
import android.prsma.org.energyspectrum.dtos.EventSampleDTO;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import junit.framework.Assert;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

@SuppressLint("WrongCall")
public class ProductionChart extends SurfaceView implements SurfaceHolder.Callback {

	private static final String MODULE = "Production   Chart";
	private int height;
	private int width;
	private String color = "#FF0000";
	private String bg_color = "#FFFFFF";
	private ArrayList<EventSampleDTO> events;
	private double[] cons_data;
	private double[] avg_cons_data;
	private int[] termal_data;
	private int[] hydro_data;
	private int[] wind_data;
	private int[] solar_data;
	private float average_renew;
	private int total_renewables;
	private int total;
	private int horizontal_caption_size = 30;
	private int vertical_caption_size = 50; 
	private int vertical_caption_size_prod =0;  // pro char y VER ISTO
	private float prod_chart_percent_height = 0.40f;
	private float cons_chart_percent_height = 0.37f;
	private float left_margin = 20;
	private int max_scale_prod;
	private int max_scale_cons;
	private int min_scale;
	private String start_time="";
	private String finish_time="";

	private String chart_title="Produção de Energia";
	private EventSampleDTO currentSelection;
	
	float termal_start = 0f;
	float water_start = 0f;
	float wind_start = 0f;
	float solar_start = 0f;
	float cons_start = 0f;
	private int right_margin = 5;

	private float text_size = 0f;
	private float text_size_h = 0f;
	Typeface roboto_thin;
	private float y_cons_title=0;

	public ProductionChart(Context context, AttributeSet attr) {
		super(context,attr);
		getHolder().addCallback(this);
		setFocusable(true);
		roboto_thin = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
	}
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3){
		// empty on purpose
	}
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		height = getHeight();
		vertical_caption_size_prod = (int)Math.round(height*0.4)+vertical_caption_size;
		width  = getWidth();
		requestRender();
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
			//empty on purpose
	}
	public void setData(int[] term, int[] hid, int[] eol, int[] bio, int[]foto){
		
		termal_data = term;
		hydro_data = hid;
		wind_data = eol;
		solar_data = foto;
		for(int i=0;i<hydro_data.length;i++)
			hydro_data[i]=termal_data[i]+hid[i];
		
		for(int i=0;i<wind_data.length;i++)
			wind_data[i]=hydro_data[i]+eol[i];
		
		for(int i=0;i<solar_data.length;i++)
			solar_data[i]=wind_data[i]+foto[i];	
		
		requestRender();
	}
	public void dummyChange(){
//		termal_data = new double[96];
//		hydro_data = new double[96];
//		wind_data = new double[96];
//		solar_data = new double[96];
//		
//		for(int i=0;i<termal_data.length;i++){
//			termal_data[i]=200*Math.random();
//			hydro_data[i]=200*Math.random();
//			wind_data[i]=200*Math.random();
//			solar_data[i]=200*Math.random();	
//		}
//		
//		for(int i=0;i<hydro_data.length;i++){
//			hydro_data[i]=termal_data[i]+hydro_data[i];
//			wind_data[i]=hydro_data[i]+wind_data[i];
//			solar_data[i]=wind_data[i]+solar_data[i];	
//		}
		
		requestRender();
	}
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
//	@Override
//	public Object getcurrentSelection() {
//			return currentSelection;
//	}
	private double[] createDrawingCoords(int []cons){
		cons = cons==null?new int[20]:cons;
	double [] result = new double[cons.length];	
		for(int i=0;i<cons.length;i++){
			result[i] =((height- vertical_caption_size_prod) - ((cons[i]*height)*prod_chart_percent_height)/max_scale_prod);
		}
	return result;
	}
	private double[] createLineCoords(double []cons){
		cons = cons==null?new double[60*24]:cons;
		double [] result = new double[cons.length];	
		for(int i=0;i<cons.length;i++){
			result[i] =((height-text_size*1.1) - ((cons[i]*height)*cons_chart_percent_height)/max_scale_cons);
			//Log.i(MODULE,"cons "+cons[i]);
		}
	return result;
	}
	@Override
	public void onDraw(Canvas c){
		if(c!=null){
		c.drawColor(Color.parseColor("#E2E0DB"));
		drawChartLayout(c);
		solar_start = drawChartPath(c,solar_data,getResources().getColor(R.color.solar_color));
		wind_start = drawChartPath(c,wind_data,getResources().getColor(R.color.wind_color));
		water_start = drawChartPath(c,hydro_data,getResources().getColor(R.color.water_color));
		termal_start = drawChartPath(c,termal_data,getResources().getColor(R.color.temar_color));
		drawSeparators(c,(float)Math.random()*width);
		drawAverageCons(c,avg_cons_data);
		cons_start =  drawConsLine(c,cons_data,"#FF0000");
	
		}
	}	
	private float drawChartPath(Canvas c, int[] cons, int _color){
		double[] drawing_coords = createDrawingCoords(cons);
		Paint p = new Paint();
		p.setColor(_color);  // color of the line and fill
		p.setAntiAlias(true);
		float diff = (float)((width-right_margin )-text_size_h)/drawing_coords.length;
		float increment =text_size_h;
		Path path = new Path();
		path.moveTo(text_size_h, height- vertical_caption_size_prod);	//starts drawing
		for(int i=0;i<drawing_coords.length;i++){
			path.lineTo(increment, (float)drawing_coords[i]);
			increment=increment+diff;
		}
		path.lineTo(increment-diff, height- vertical_caption_size_prod);
		path.lineTo(0, height- vertical_caption_size_prod);	// finishes drawing
		c.drawPath(path, p);
		return (float) drawing_coords[0];
	}

	private void drawChartLayout(Canvas c){
		Log.i(MODULE, "Drawing chart layout");
		Paint p = new Paint(); // paint for text
		p.setColor(Color.BLACK);
		p.setStyle(Style.FILL);
		p.setTextSize(text_size);
		p.setAntiAlias(true);

		float y_title = 0+text_size;//-(vertical_caption_size_prod + prod_chart_percent_height*getHeight());
		float y_prod_title = y_title+text_size*2;
		c.drawText("Produção e consumo de energia", left_margin-5, y_title, p); // chart title
		p.setTextSize(text_size*0.9f);
		c.drawText("Produção de energia (MWh)", left_margin, y_prod_title, p); // prod title
		vertical_caption_size_prod = (int)Math.round(height*0.4)+Math.round(y_prod_title)+Math.round(text_size*2f);
		y_cons_title = vertical_caption_size_prod + text_size;
		c.drawText("Consumo de energia (kWh)", left_margin, y_cons_title, p);  // cons title

		//draw scales prod
		//p.setTypeface(roboto_thin);
		c.drawText(max_scale_prod+"", left_margin, y_prod_title+text_size, p); // prod max
		c.drawText("0", left_margin, getHeight() - vertical_caption_size_prod -5 , p);  //prod min
		float middle = (   (getHeight()- vertical_caption_size_prod) - y_prod_title)/2;
		c.drawText(max_scale_prod/2+"", left_margin, y_prod_title+middle  , p);  // prod middle
		text_size_h = p.measureText((max_scale_prod/2+""));
		// draw scales cons
		DecimalFormat df = new DecimalFormat("##.##");
		double vale = ((double)max_scale_cons/1000);
		c.drawText(df.format(vale)+"", left_margin, y_cons_title+text_size, p); // prod max
		c.drawText("0", left_margin, getHeight() - horizontal_caption_size  , p);  //prod min
		middle = (   (getHeight()-horizontal_caption_size) - y_cons_title)/2;
		String text_demo = +vale+" ";
		text_size_h = p.measureText(text_demo)+left_margin;
		//text_size_h =  text_size_h<p.measureText(text_demo)?p.measureText(max_scale_prod/2+""):text_size_h;
		//text_size_h = text_size_h+left_margin;

		if(max_scale_cons<1500){
			double val = (((double)max_scale_cons/2)/1000);
			c.drawText(df.format(val), left_margin, y_cons_title+middle  , p);
		}else{
			double val = (((double)max_scale_cons/2)/1000);
			c.drawText(df.format(val), left_margin, y_cons_title+middle  , p);  // prod middle
		}
			//draw time
		c.drawText("0h", vertical_caption_size, getHeight()-horizontal_caption_size+15, p);
		c.drawText("24h", getWidth()-right_margin-p.measureText("24h"), getHeight()-horizontal_caption_size+15 , p);
		middle = ((getWidth()-right_margin)-vertical_caption_size)/2+vertical_caption_size;
		c.drawText("12h", middle-p.measureText("12h"), getHeight()-horizontal_caption_size+15, p);

		Log.e("production chart","captio size "+text_size_h);
	}
	private void drawSeparators(Canvas c, float length){

		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minutes = cal.get(Calendar.MINUTE);
		minutes = Math.round(minutes/15);
		float diff = ((float)(width-vertical_caption_size-right_margin)/96);
		float length2 =  (diff*((hour)*4) + diff*minutes)+text_size_h;
//		Rect rect  = new Rect((int)length2,0,width-40, getHeight()-90);
		Paint p = new Paint();
//		p.setStyle(Paint.Style.FILL);
//		p.setColor(Color.parseColor("#331A00"));
//		p.setAlpha(150);
//		c.drawRect(rect, p);// for now don't do this
		Paint p2 = new Paint();
		p2.setStyle(Style.STROKE);
		p2.setColor(Color.parseColor("#6D6E71"));
		p2.setStrokeWidth(1.5f);
		//c.drawLine((int)length2,getHeight()-horizontal_caption_size,(int)length2,getHeight()-(horizontal_caption_size + cons_chart_percent_height*getHeight())+10, p2);
		//drawAverages( c, length2);
		// draw top separator
		p.setStrokeWidth(2f);
		p.setColor(Color.parseColor("#6D6E71"));
		float y = getHeight()-(horizontal_caption_size + cons_chart_percent_height*getHeight()+vertical_caption_size);	//separator between prod and cons
	//	c.drawLine(left_margin, y, getWidth(), y, p2);
		
		y = getHeight()-(vertical_caption_size_prod)+text_size;
		p.setShader(new LinearGradient(left_margin-5, y, getWidth(), y, Color.parseColor("#6D6E71"), Color.parseColor("#E2E0DB"), Shader.TileMode.MIRROR));
		c.drawLine(left_margin-5, y, getWidth(), y, p);
		
		p2.setColor(Color.parseColor("#E2E0DB"));
		p2.setPathEffect(new DashPathEffect(new float[]{4,4}, 0));
		p2.setStrokeWidth(2.5f);
		c.drawLine((int)length2,getHeight()- vertical_caption_size_prod,(int)length2,getHeight()-(vertical_caption_size_prod + prod_chart_percent_height*getHeight())+10, p2);
	}
	private float drawConsLine(Canvas c, double[] cons, String color){
		double[] drawing_coords = createLineCoords(cons);
		Paint p = new Paint();
		p.setColor(Color.parseColor("#f0592D"));  // color of the line and fill
		p.setAntiAlias(true);
		p.setStrokeWidth(3f);
		p.setStrokeJoin(Join.MITER);
		p.setStrokeCap(Cap.ROUND);
		float diff = (float)(width-right_margin-vertical_caption_size)/drawing_coords.length;
		float increment =vertical_caption_size;
		increment =text_size_h;
		float c_x=0;
		float c_y=0;
		for(int i=1;i<drawing_coords.length;i=i+1){
			if(drawing_coords[i]==getHeight()-text_size*1.1){
				c_x = increment;
				c_y = (float) drawing_coords[i-1];
				break;
			}
			c.drawLine((int)increment,(int)drawing_coords[i-1],(int)increment+diff, (int)drawing_coords[i], p);
			increment=increment+diff;
		}
		p.setColor(Color.parseColor("#FAA440"));
		c.drawCircle(c_x, c_y, 5, p);
		return (float) drawing_coords[0];
	}
	private float drawAverageCons(Canvas c,double[] cons){
		double[] drawing_coords = createLineCoords(cons);
		Paint p = new Paint();
		p.setColor(Color.DKGRAY);  // color of the line and fill
		p.setAntiAlias(true);
		p.setStrokeWidth(1.5f);
		p.setStrokeJoin(Join.ROUND);
		p.setStrokeCap(Cap.ROUND);
		p.setPathEffect(new DashPathEffect(new float[]{4,4}, 0));
		float diff = (float)(width-text_size_h)/drawing_coords.length;
		float increment =text_size_h;
		float[] pts = new float[drawing_coords.length*2];
		int j=0;
		for(int i=1;i<drawing_coords.length;i=i+1){
			c.drawLine((int)increment,(int)drawing_coords[i-1],(int)increment+diff, (int)drawing_coords[i], p);
			increment=increment+diff;
		}
		c.drawLines(pts, p);
		return (float) drawing_coords[0];
	}
	private float drawAverageCons(Canvas c,int[] cons){
		try{
		double[] cast_cons = new double[cons.length];
		for(int i=0;i<cons.length;i++)
			cast_cons[i]=(double)cons[i];
			
		double[] drawing_coords = createLineCoords(cast_cons);
		Paint p = new Paint();
		p.setColor(Color.DKGRAY);  // color of the line and fill
		p.setAntiAlias(true);
		p.setStrokeWidth(2.5f);
		p.setStrokeJoin(Join.ROUND);
		p.setStrokeCap(Cap.ROUND);
		float diff = (float)(width-right_margin-vertical_caption_size)/drawing_coords.length;
		float increment =vertical_caption_size;
		float[] pts = new float[drawing_coords.length*2];
		int j=0;
		for(int i=1;i<drawing_coords.length;i=i+1){
//			if(drawing_coords[i]==getHeight()-horizontal_caption_size){
//				c_x = increment;
//				c_y = (float) drawing_coords[i-1];
//				break;
//			}
			c.drawLine((int)increment,(int)drawing_coords[i-1],(int)increment+diff, (int)drawing_coords[i], p);
			increment=increment+diff;
		}
		c.drawLines(pts, p);
		return (float) drawing_coords[0];
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
	}

	public void setConsumption(double[] cons){
		setCons_data(cons);
	}
	public void setConsumption(int[] cons){
	   double[] double_cons = new double[cons.length];  
		for (int i=0; i<cons.length; ++i)  
			double_cons[i] =  cons[i]; 
		
		setCons_data(double_cons);
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getBgColor() {
		return bg_color;
	}
	public void setBgColor(String bg_color) {
		this.bg_color = bg_color;
	}
	public String getStart_time() {
		return start_time;
	}
	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}
	public String getFinsh_time() {
		return finish_time;
	}
	public void setFinsh_time(String finsh_time) {
		this.finish_time = finsh_time;
	}
	public void setMax_scale(int maxscale){
		this.max_scale_prod=maxscale;
	}
	public void setMin_scale(int minscale){
		this.min_scale=minscale;
	}
	private int getDrawable(Context context, String name)
	{
		Assert.assertNotNull(context);
		Assert.assertNotNull(name);

		return context.getResources().getIdentifier(name,
				"drawable", context.getPackageName());
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		synchronized (getHolder()) {

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				//if(events!=null && events.size()>0) checkTouch(event.getX(),event.getY());
				Log.d(MODULE, "touch Down "+event.getX()+" "+event.getY());

			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				Log.d(MODULE,"touch Up");
				requestRender();
			}
			return true;
		}
	}
	public int getTotal_renewables() {
		return total_renewables;
	}
	public void setTotal_renewables(int total_renewables) {
		this.total_renewables = total_renewables;
	}


	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}


	public float getAverage_renew() {
		return average_renew;
	}
	public void setAverage_renew(float average_renew) {
		this.average_renew = average_renew;
	}
	public void setMaxCons(double max){
		max_scale_cons = (int) Math.round(max);
	}
	public double[] getCons_data() {
		return cons_data;
	}
	public void setCons_data(double[] cons_data) {
		this.cons_data = cons_data;
	}
	public double[] getAvg_cons_data() {
		return avg_cons_data;
	}
	public void setAvg_cons_data(double[] avg_cons_data) {
		this.avg_cons_data = avg_cons_data;
	}
	public float getText_size() {		return text_size; 	}
	public void setText_size(float text_size) { 		this.text_size = text_size; 	}

	private class ChartHandler extends Thread{
		
		public void run(){
			while(true){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				dummyChange();
		}
		}
	}
	
}
/*
	private void drawAverages(Canvas c, float pos){
		float renew = 0;
		if (total!=0)
			renew = Math.round((float)total_renewables*100/total);

		Paint p = new Paint();
		p.setStyle(Style.STROKE);
		p.setColor(Color.BLACK);
		p.setAntiAlias(true);
		p.setStyle(Style.FILL);
		p.setTextSize(text_size*0.9f);
		p.setAntiAlias(true);
		c.drawText(renew+"% renewables", pos-80, getHeight()-50, p);
		c.drawText("Average 1", pos-80, getHeight()-70, p);

		p.setTextSize(text_size);
		p.setColor(Color.WHITE);
		p.setTypeface(Typeface.DEFAULT_BOLD);
		c.drawText("Estimativa", pos+10, 30, p);

		float renew_avg = 0;
		if (total!=0)
			renew_avg = Math.round((float)average_renew*100);

		String p_e_m= renew-renew_avg==0?"=": renew-renew_avg < 0?"":"+";
		String message = p_e_m.equals("=")?"equal to your average":p_e_m+(renew-renew_avg)+" % than your average";

		Paint p2 = new Paint();
		p2.setStyle(Style.STROKE);
		p2.setColor(Color.BLACK);
		p2.setAntiAlias(true);
		p2.setStyle(Style.FILL);
		p2.setTextSize(text_size*0.9f);
		p2.setAntiAlias(true);
		p2.setTypeface(Typeface.DEFAULT_BOLD);
		c.drawText(message, pos-80, getHeight()-30, p2);
	}
	private void drawTitles(Canvas c){
		Paint p = new Paint();
		p.setStyle(Style.STROKE);
		p.setColor(Color.BLACK);
		p.setAntiAlias(true);
		p.setStyle(Style.FILL);
		p.setTextSize(text_size);
		p.setTypeface(Typeface.DEFAULT_BOLD);
		c.drawText(chart_title, vertical_caption_size, 30, p);
		c.drawText("O seu consumo", vertical_caption_size, height- vertical_caption_size_prod +20, p);
	}
 */
//private float drawChartPath2(Canvas c, int[] cons, int _color){
//Calendar cal = Calendar.getInstance();
//int hour = cal.get(Calendar.HOUR_OF_DAY);
//int minutes = cal.get(Calendar.MINUTE);
//minutes = Math.round(minutes/15);
//double[] drawing_coords = createDrawingCoords(cons);
//Paint p = new Paint();
//p.setColor(_color);  // color of the line and fill
//p.setAntiAlias(true);
//float diff = (float)((width-right_margin )-vertical_caption_size)/drawing_coords.length;
//float increment =vertical_caption_size;
//Path path = new Path();
//path.moveTo(vertical_caption_size, height-vertical_caption_size_prod);	//starts drawing
//float length2 =  (diff*((hour)*4) + diff*minutes)+vertical_caption_size;
//int i;
//int init=0;
//for(i=0;i<drawing_coords.length;i++){
//	path.lineTo(increment, (float)drawing_coords[i]);
//	increment=increment+diff;
//	if(increment<=length2)
//		init=i;
//		
//}
//path.lineTo(increment-diff, height-vertical_caption_size_prod);
//path.lineTo(0, height-vertical_caption_size_prod);	// finishes drawing
//c.drawPath(path, p);
//increment = increment-diff;
//p.setAntiAlias(true);
//p.setStrokeWidth(.5f);
//p.setStrokeJoin(Join.MITER);
//p.setStrokeCap(Cap.ROUND);
//p.setColor(Color.WHITE);
//for(int j=init;j<drawing_coords.length;j=j+1){
//	c.drawLine((int)increment,(int)drawing_coords[j-1],(int)increment+diff, (int)drawing_coords[j], p);
//	increment=increment+diff;
//}
//return (float) drawing_coords[0];
//}