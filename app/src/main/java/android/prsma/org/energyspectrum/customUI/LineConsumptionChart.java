package android.prsma.org.energyspectrum.customUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.prsma.org.energyspectrum.R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by flp_ on 10/08/2016.
 */
public class LineConsumptionChart extends SurfaceView implements SurfaceHolder.Callback {

    public static final String MODE_DAY = "day";
    public static final String MODE_MONTH = "month";

    private float _height;
    private float _width;


    private double[] _cons_data;// = new double[48];
    private double[] _avg_cons_data;// = new double[48];

    private ArrayList<double[]> _comparison_data = new ArrayList<double[]>();
    private int[] _comparison_colors = {0,0,0,0,0};

    private float _text_size = 50;

    private float _vertical_axis_lablel_width =0;
    private float _horizontal_axis_lablel_height =0;

    private float _separators_width = 1f;
    private int _maxValue;
    double [] _empty = new double[48];

    private String _token="";
    private String _mode;

    public LineConsumptionChart(Context context, AttributeSet attrs) {
        super(context, attrs);
      //  createDummyValues();
        getHolder().addCallback(this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        _height = getHeight();
        _width = getWidth();
    //    createDummyValues();
        initComparisonData();
        requestRender();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        requestRender();
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
            c.drawColor(Color.parseColor("#FFFFFF"));
            drawVerticalAxis(c);
            drawHorizontalAxis(c,_token);
            drawAveragePath(c);
            drawChartPath(c);
            for(int i=0;i<_comparison_data.size();i++){
                if(_comparison_data.get(i)[0]!=0)
                 drawComparisonPath(c,_comparison_data.get(i),_comparison_colors[i]);
            }
            Log.i("Cons Chart", "rendering");

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
        System.gc();
        Canvas c = null;
        SurfaceHolder sh = getHolder();
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

        return max;
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
        _vertical_axis_lablel_width = p.measureText(maxValueText)+2f;
        c.drawRect(_vertical_axis_lablel_width,0, _vertical_axis_lablel_width + _separators_width,_height,p);
    }

    private void drawHorizontalAxis(Canvas c,String token){
        Paint p = new Paint(); // paint for text
        p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.FILL);
        p.setTextSize(_text_size);
        p.setAntiAlias(true);

        if(_mode.equals(MODE_DAY)){
            c.drawText("0"+token, _vertical_axis_lablel_width,_height,p);
            float diff = (_width- _vertical_axis_lablel_width)/6;
            for(int i=1;i<=6;i++){
                String hour = i*4+token;
                float adjust = p.measureText(hour);
                c.drawText(hour, _vertical_axis_lablel_width +(i*diff)-adjust+2f,_height,p);
        }
        }else {
            float diff = (_width- _vertical_axis_lablel_width)/_cons_data.length;
            for (int i = 1; i <= _cons_data.length; i=i+2) {
                String hour = i + "";
                float adjust = p.measureText(hour);
                c.drawText(hour, _vertical_axis_lablel_width + (i*diff) - adjust + 2f, _height, p);
            }
        }
        c.drawRect(_vertical_axis_lablel_width,_height-_text_size,_width,_height-_text_size+ _separators_width,p);
        _horizontal_axis_lablel_height = _text_size+ _separators_width;
    }

    private void drawChartPath(Canvas c){
       double[] drawing_coords = createDrawingCoords(_cons_data);
        Paint p = new Paint();
        p.setColor(getResources().getColor(R.color.app_main));  // color of the line and fill
        p.setAntiAlias(true);
        float diff = (_width-_vertical_axis_lablel_width) /drawing_coords.length;
        float increment = _vertical_axis_lablel_width + _separators_width;
        Path path = new Path();
        path.moveTo(_vertical_axis_lablel_width + _separators_width, _height-_horizontal_axis_lablel_height);	//starts drawing
        for(int i=0;i<drawing_coords.length;i++){
            path.lineTo(increment, (float)drawing_coords[i]);
            increment=increment+diff;
        }
        path.lineTo(increment-diff,_height- _horizontal_axis_lablel_height);
        c.drawPath(path, p);


        p.setColor(getResources().getColor(R.color.app_main_darker));  // color of the line and fill
        p.setStrokeWidth(5);
        diff = (_width-_vertical_axis_lablel_width) /drawing_coords.length;
        increment = _vertical_axis_lablel_width + _separators_width;
        float c_x=0;
        float c_y=0;
        for(int i=1;i<drawing_coords.length;i=i+1){
            /*if(drawing_coords[i]==getHeight()-text_size*1.1){
                c_x = increment;
                c_y = (float) drawing_coords[i-1];
                break;
            }*/
            c.drawLine((int)increment,(int)drawing_coords[i-1],(int)increment+diff, (int)drawing_coords[i], p);
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

        float diff = (_width-_vertical_axis_lablel_width) /drawing_coords.length;
        float increment = _vertical_axis_lablel_width + _separators_width;

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

        float diff = (_width-_vertical_axis_lablel_width) /drawing_coords.length;
        float increment = _vertical_axis_lablel_width + _separators_width;

        for(int i=1;i<drawing_coords.length;i=i+1){
            c.drawLine((int)increment,(int)drawing_coords[i-1],(int)increment+diff, (int)drawing_coords[i], p);
            increment=increment+diff;
        }

    }

    private double[] createDrawingCoords(double[] data){
        double [] result = new double[data.length];
        for(int i = 0; i< data.length; i++){
            result[i] =(_height- _horizontal_axis_lablel_height) - ((data[i]*(_height- _horizontal_axis_lablel_height))/ _maxValue);
        }
        return result;
    }

    public void addComparisonData(double[] cons,int index, int color){
        _comparison_data.set(index,cons);
        _comparison_colors[index] = color;
    }
    public void removeComparisonData(int index){
        _comparison_data.set(index,_empty);
    }

    //DEBUG FUNCTIONS


    public double[] get_cons_data() {
        return _cons_data;
    }

    public void set_cons_data(double[] _cons_data) {
        this._cons_data = _cons_data;
    }


    public double[] get_avg_cons_data() {
        return _avg_cons_data;
    }

    public void set_avg_cons_data(double[] _avg_cons_data) {
        this._avg_cons_data = _avg_cons_data;
    }


    public String getMode() {
        return _mode;
    }

    public void setMode(String mode) {
        this._mode = mode;
        if(_mode==MODE_DAY)
            _token = "h";
        else
            _token = "";
    }


}
