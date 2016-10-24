package android.prsma.org.energyspectrum.customUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Vibrator;
import android.prsma.org.energyspectrum.R;
import android.prsma.org.energyspectrum.dtos.EventSampleDTO;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by flp_ on 10/08/2016.
 */
public class LineConsumptionChart extends SurfaceView implements SurfaceHolder.Callback {

    public static final String MODE_DAY = "day";
    public static final String MODE_MONTH = "month";

    private float _height;
    private float _width;

    private ArrayList<EventSampleDTO> _events;

    private double[] _cons_data;// = new double[48];
    private double[] _avg_cons_data;// = new double[48];

    private ArrayList<double[]> _comparison_data = new ArrayList<double[]>();
    private int[] _comparison_colors = {0,0,0,0,0};

    private float _text_size = 50;

    private float _vertical_axis_label_width =0;
    private float _horizontal_axis_label_height =0;

    private float _separators_width = 1f;
    private int _maxValue;
    double [] _empty = new double[48];

    private String _token="";
    private String _mode;

    private Context _app_context;
    private int _max_event_delta;

    private double[][] _chart_coords;

    private Vibrator v;

    private ChartInteractionListener _listener;

    private ViewGroup.LayoutParams _normalLayout;

    public LineConsumptionChart(Context context, AttributeSet attrs) {
        super(context, attrs);
      //  createDummyValues();
        getHolder().addCallback(this);
        setFocusable(true);
        _app_context = context;
        v = (Vibrator) _app_context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i("ConsChart", "surface created");
        initComparisonData();
        _height = getHeight();
        _width = getWidth();
    //    createDummyValues();


        _height = _width*0.65f;
        android.view.ViewGroup.LayoutParams lp = this.getLayoutParams();
        lp.height = (int)_height; // required height
        lp.width = (int)_width;
        this.setLayoutParams(lp);
        _height = getHeight();
        _width = getWidth();

       // _normalLayout = this.getLayoutParams();*/
        requestRender();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
       /* _width = getWidth();

        Log.i("LineChart","width "+_width);
        _height = _width*0.65f;
        android.view.ViewGroup.LayoutParams lp = this.getLayoutParams();
        lp.height = (int)_height; // required height
        lp.width = (int)_width;
        this.setLayoutParams(lp);

        requestRender();*/
        Log.i("ConsChart", "surface changed");
        _height = getHeight();
        _width = getWidth();
        requestRender();
    }

    public void adjustSize(){
       /* Log.i("ConsChart", "adjusting size big");
        android.view.ViewGroup.LayoutParams lp = this.getLayoutParams();
        lp.width  = ViewGroup.LayoutParams.MATCH_PARENT;//(int)_width;
        this.setLayoutParams(lp);
        float width = getWidth();
        android.view.ViewGroup.LayoutParams lp2 = this.getLayoutParams();
        lp2.width = ViewGroup.LayoutParams.MATCH_PARENT;//(int)_width;
        float temp2 = width*0.55f;
        lp2.height =  Math.round(temp2);//, ViewGroup.LayoutParams.WRAP_CONTENT(int)_height; // required height

        Log.e("ConsChart","height "+_width+" ");*/

        android.view.ViewGroup.LayoutParams lp = this.getLayoutParams();
        lp.width  = Math.round(_width*1.8f);
        lp.height  = Math.round(_height*1.8f);

        this.setLayoutParams(lp);
    }

    public void adjustSizeNormal(){
        Log.i("ConsChart", "adjusting size small");
      //  this.setLayoutParams(_normalLayout);
        android.view.ViewGroup.LayoutParams lp = this.getLayoutParams();
        lp.width  = Math.round(_width/1.8f);
        lp.height  = Math.round(_height/1.8f);
        this.setLayoutParams(lp);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
//
//    @Override
//    public Object getcurrentSelection() {
//        return null;
//    }

    @Override
    public void onDraw(Canvas c){
        if(c!=null){
            c.drawColor(Color.parseColor("#ffffff"));
            drawVerticalAxis(c);
            drawHorizontalAxis(c,_token);
            drawAveragePath(c);
            drawChartPath(c);
            for(int i=0;i<_comparison_data.size();i++){
                if(_comparison_data.get(i)[0]!=0)
                 drawComparisonPath(c,_comparison_data.get(i),_comparison_colors[i]);
            }
            //Log.i("Cons Chart", "rendering");
            if(_events!=null && _events.size()>0)
                drawEventData(c);

        }
    }
    private void initComparisonData(){
        for(int i=0;i<5;i++)
            _comparison_data.add(_empty);
    }

    //getters and setters
    public void setText_size(float text_size) { 		this._text_size = text_size; 	}

    // custom made functions

    public void requestRender(){
        //System.gc();
        Canvas c = null;
        SurfaceHolder sh = getHolder();
        Log.i("LineConsChart","rendering");
        try {
            c = sh.lockCanvas(null);
            synchronized (sh){
                onDraw(c);
            }
        }finally {
            if(c!=null){
                sh.unlockCanvasAndPost(c);
            }
        }
    }

    private double getMaxValue(){
        double max = 0;
        for(int i = 0; i< _avg_cons_data.length; i++)
           if(_avg_cons_data[i]>max)
               max= _avg_cons_data[i];

        for(int i = 0; i< _cons_data.length; i++)
            if(_cons_data[i]>max)
                max= _cons_data[i];

        return max*1.3;
    }

    private void drawVerticalAxis(Canvas c){
        Paint p = new Paint(); // paint for text
        p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.FILL);
        p.setTextSize(_text_size);
        p.setAntiAlias(true);
        _maxValue = (int)Math.ceil(getMaxValue());
        String maxValueText = _maxValue+"";
        String middleValueText = (int)Math.ceil(_maxValue/2)+"";
        c.drawText(maxValueText+"",0,_text_size,p);
        c.drawText("0",0,_height-_text_size,p);

         c.drawText(middleValueText,0,(_height-_text_size)/2,p);
        _vertical_axis_label_width = p.measureText(maxValueText)+2f;
        c.drawRect(_vertical_axis_label_width,0, _vertical_axis_label_width + _separators_width,_height,p);
    }

    private void drawHorizontalAxis(Canvas c,String token){
        Paint p = new Paint(); // paint for text
        p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.FILL);
        p.setTextSize(_text_size);
        p.setAntiAlias(true);

        if(_mode.equals(MODE_DAY)){
            c.drawText("0"+token, _vertical_axis_label_width,_height,p);
            float diff = (_width- _vertical_axis_label_width)/6;
            for(int i=1;i<=6;i++){
                String hour = i*4+token;
                float adjust = p.measureText(hour);
                c.drawText(hour, _vertical_axis_label_width +(i*diff)-adjust+2f,_height,p);
        }
        }else {
            float diff = (_width- _vertical_axis_label_width)/_cons_data.length;
            for (int i = 1; i <= _cons_data.length; i=i+2) {
                String hour = i + "";
                float adjust = p.measureText(hour);
                c.drawText(hour, _vertical_axis_label_width + (i*diff) - adjust + 2f, _height, p);
            }
        }
        c.drawRect(_vertical_axis_label_width,_height-_text_size,_width,_height-_text_size+ _separators_width,p);
        _horizontal_axis_label_height = _text_size+ _separators_width;
    }

    private void drawChartPath(Canvas c){
       double[] drawing_coords = createDrawingCoords(_cons_data);
        Paint p = new Paint();
        p.setColor(getResources().getColor(R.color.app_main));  // color of the line and fill
        p.setAntiAlias(true);
        float diff = (_width- _vertical_axis_label_width) /drawing_coords.length;
        float increment = _vertical_axis_label_width + _separators_width;
        Path path = new Path();
        path.moveTo(_vertical_axis_label_width + _separators_width, _height- _horizontal_axis_label_height);	//starts drawing
        for(int i=0;i<drawing_coords.length;i++){
            path.lineTo(increment, (float)drawing_coords[i]);
            increment=increment+diff;
        }

        path.lineTo(increment-diff,_height- _horizontal_axis_label_height);
        c.drawPath(path, p);

        p.setColor(getResources().getColor(R.color.app_main_darker));  // color of the line and fill
        p.setStrokeWidth(5);
        diff = (_width- _vertical_axis_label_width) /drawing_coords.length;
        increment = _vertical_axis_label_width + _separators_width;
        float c_x=0;
        float c_y=0;

        _chart_coords = new double[drawing_coords.length][2];
        _chart_coords[0][0]=(int)increment;
        _chart_coords[0][1]=(int)drawing_coords[0];
        for(int i=1;i<drawing_coords.length;i=i+1){
            /*if(drawing_coords[i]==getHeight()-text_size*1.1){
                c_x = increment;
                c_y = (float) drawing_coords[i-1];
                break;
            }*/
            c.drawLine((int)increment,(int)drawing_coords[i-1],(int)increment+diff, (int)drawing_coords[i], p);
            _chart_coords[i][0]=(int)increment+diff;
            _chart_coords[i][1]=(int)drawing_coords[i];
            increment=increment+diff;
        }
    }

    private void drawAveragePath(Canvas c){

        double[] drawing_coords = createDrawingCoords(_avg_cons_data);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(getResources().getColor(R.color.app_main_dark));  // color of the line and fill
        p.setStrokeWidth(5);
        p.setStrokeJoin(Paint.Join.MITER);
        p.setPathEffect(new DashPathEffect(new float[]{4,4}, 0));

        float diff = (_width- _vertical_axis_label_width) /drawing_coords.length;
        float increment = _vertical_axis_label_width + _separators_width;

        for(int i=1;i<drawing_coords.length;i=i+1){
            c.drawLine((int)increment,(int)drawing_coords[i-1],(int)increment+diff, (int)drawing_coords[i], p);
            increment=increment+diff;

        }

    }

    private void drawComparisonPath(Canvas c, double[] cons, int color){

        double[] drawing_coords = createDrawingCoords(cons);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(color);  // color of the line and fill
        p.setStrokeWidth(2);
        p.setStrokeJoin(Paint.Join.MITER);
        p.setPathEffect(new DashPathEffect(new float[]{3,3}, 2));

        float diff = (_width- _vertical_axis_label_width) /drawing_coords.length;
        float increment = _vertical_axis_label_width + _separators_width;

        for(int i=1;i<drawing_coords.length;i=i+1){
            c.drawLine((int)increment,(int)drawing_coords[i-1],(int)increment+diff, (int)drawing_coords[i], p);
            increment=increment+diff;
        }

    }

    private void drawEventData(Canvas c){
        Paint p = new Paint();
        p.setAntiAlias(true);

        float max_radius =  _height*0.08f;
        float y = _height/2f;
        float shift = _vertical_axis_label_width + _separators_width;
        for(int i=0; i<_events.size();i++){
            p.setColor(_events.get(i).get_color());
            float radius = (_events.get(i).get_deltaPMean())*max_radius/_max_event_delta;
            float[] cords = calculateEventYPos(_events.get(i),i,0);
            c.drawCircle(cords[0],
                    cords[1],
                    radius,
                    p);

            _events.get(i).set_cords(cords);
        }
    }

    private float[] calculateEventYPos(EventSampleDTO event, int hour, int minutes){
        //tera q ser qqer coisa assim
        //event.getHour;
        //event.getMinutes;

        int tms_in_minutes = (60*hour)+minutes;
        int total_day_minutes = 1440;
        int chart_pos = (int)(tms_in_minutes*_cons_data.length)/total_day_minutes;
        chart_pos = chart_pos>47?47:chart_pos;
        float[] result = {(float)_chart_coords[chart_pos][0],(float)_chart_coords[chart_pos][1]};
        return result;
    }

    private double[] createDrawingCoords(double[] data){
        double [] result = new double[data.length];
        for(int i = 0; i< data.length; i++){
            result[i] =(_height- _horizontal_axis_label_height) - ((data[i]*(_height- _horizontal_axis_label_height))/ _maxValue);
        }
        return result;
    }

/*    private double createDrawingCoords(double value){
        double  result = 0;
        result =(_height- _horizontal_axis_label_height) - ((value*(_height- _horizontal_axis_label_height))/ _maxValue);
        return result;
    }*/

    public void addComparisonData(double[] cons,int index, int color){
        _comparison_data.set(index,cons);
        _comparison_colors[index] = color;
    }
    public void removeComparisonData(int index){
        _comparison_data.set(index,_empty);
    }

    //DEBUG FUNCTIONS



    public void set_cons_data(double[] _cons_data) {

        this._cons_data = _cons_data;
    }

    public void set_events(ArrayList<EventSampleDTO> events) {
        this._events = events;
        int max=0;
        for(int i=0;i<_events.size();i++){
            if(_events.get(i).get_deltaPMean()>max){
                max=_events.get(i).get_deltaPMean();
            }
        }

        this._max_event_delta = max;
    }

    public void set_avg_cons_data(double[] _avg_cons_data) {
        this._avg_cons_data = _avg_cons_data;
    }

    public void setMode(String mode) {
        this._mode = mode;
        if(_mode==MODE_DAY)
            _token = "h";
        else
            _token = "";
    }

    /*
     *   Chart touch stuff
     */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        synchronized (getHolder()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                //Log.d("Line Cons Chart", "touch Down");

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                checkTouch(event.getX(),event.getY());
                //Log.d("Line Cons Chart","touch Up");
            }
            return true;
        }
    }

    private void checkTouch(float x, float y){


        int itemr=20;
        int select_color = ContextCompat.getColor(_app_context,R.color.event_color_2);
        int normal_color = ContextCompat.getColor(_app_context,R.color.event_color_1);

        for(int i=0;i<_events.size();i++){

            float[] cords = _events.get(i).get_cords();

            if((x>cords[0]-itemr)&&(x<cords[0]+itemr)){
                if((y>cords[1]-itemr)&&(y<cords[1]+itemr)){
                  //  Log.d("Line Chart","EXISTE AQUI ->"+i);
                    _events.get(i).set_color(select_color);
                    v.vibrate(10);
                    this._listener.onEventSelect(_events.get(i));
                }
            }else{
                _events.get(i).set_color(normal_color);
            }
        }
        requestRender();
    }


    public void SetChartInteractionListener(ChartInteractionListener _listener) {
        this._listener = _listener;
    }

    public interface ChartInteractionListener{

        void onEventSelect(EventSampleDTO event);
    }
}
