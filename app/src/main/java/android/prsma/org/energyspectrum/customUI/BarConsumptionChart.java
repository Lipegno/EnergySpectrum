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
public class BarConsumptionChart extends SurfaceView implements SurfaceHolder.Callback {

    private float _height;
    private float _width;

    private double[] _cons_data = new double[7];
    private double[] _comp_data = new double[7];

    private ArrayList<double[]> _comparison_data = new ArrayList<double[]>();
    private int[] _comparison_colors = {0,0,0,0,0};

    private float _text_size = 50;

    private float _vertical_axis_lablel_width =0;
    private float _horizontal_axis_lablel_height =0;

    private float _separators_width = 1f;
    private int _maxValue;
    double [] _empty = new double[48];

    public BarConsumptionChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        createDummyValues();
        getHolder().addCallback(this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        _height = getHeight();
        _width = getWidth();
        createDummyValues();
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
            drawHorizontalAxis(c,"h");
            drawComparisonPath(c);
            drawChartPath(c);
//            for(int i=0;i<_comparison_data.size();i++){
//                if(_comparison_data.get(i)[0]!=0)
//                // drawComparisonPath(c,_comparison_data.get(i),_comparison_colors[i]);
//            }
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
        for(int i = 0; i< _comp_data.length; i++)
           if(_comp_data[i]>max)
               max= _comp_data[i];

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
        _vertical_axis_lablel_width = p.measureText(maxValueText)+4f;
        c.drawRect(_vertical_axis_lablel_width,0, _vertical_axis_lablel_width + _separators_width,_height,p);
    }

    private void drawHorizontalAxis(Canvas c,String token){
        Paint p = new Paint(); // paint for text
        p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.FILL);
        p.setTextSize(_text_size);
        p.setAntiAlias(true);

        float diff = (_width- _vertical_axis_lablel_width)/6;
        c.drawText("0"+token, _vertical_axis_lablel_width,_height,p);

        for(int i=1;i<=6;i++){
            String hour = i*4+token;
            float adjust = p.measureText(hour);
            c.drawText(hour, _vertical_axis_lablel_width +(i*diff)-adjust+2f,_height,p);
        }

        c.drawRect(_vertical_axis_lablel_width,_height-_text_size,_width,_height-_text_size+ _separators_width,p);
        _horizontal_axis_lablel_height = _text_size+ _separators_width;
    }

    private void drawChartPath(Canvas c){

        double[] drawing_coords = createDrawingCoords(_comp_data);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(getResources().getColor(R.color.orange));  // color of the line and fill
        p.setStrokeWidth(5);
        p.setStrokeJoin(Paint.Join.MITER);
        p.setPathEffect(new DashPathEffect(new float[]{4,4}, 0));

        float diff = (_width-_vertical_axis_lablel_width) /drawing_coords.length;
        float increment = _vertical_axis_lablel_width + _separators_width;
        float bar_width = (_width-_vertical_axis_lablel_width)/7;

        for(int i=0;i<drawing_coords.length;i=i+1){
            c.drawRect(2f+_vertical_axis_lablel_width+(i*bar_width),(float)drawing_coords[i],_vertical_axis_lablel_width+(i*bar_width)+bar_width/2,_height - _horizontal_axis_lablel_height,p);
        }

    }

    private void drawComparisonPath(Canvas c){

        double[] drawing_coords = createDrawingCoords(_cons_data);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(getResources().getColor(R.color.app_main_dark));  // color of the line and fill
        p.setStrokeWidth(5);
        p.setStrokeJoin(Paint.Join.MITER);
        p.setPathEffect(new DashPathEffect(new float[]{4,4}, 0));

        float bar_width = (_width-_vertical_axis_lablel_width)/7;
        float adjusted_vertical_axis_width = 2f+ _vertical_axis_lablel_width;

        for(int i=0;i<drawing_coords.length;i=i+1){
            c.drawRect(adjusted_vertical_axis_width+(i*bar_width)+(bar_width/2),(float)drawing_coords[i],adjusted_vertical_axis_width+(i*bar_width)+(bar_width),_height - _horizontal_axis_lablel_height,p);
        }

    }

    private double[] createDrawingCoords(double[] data){
        double [] result = new double[data.length];
        for(int i = 0; i< data.length; i++){
            result[i] =(_height- _horizontal_axis_lablel_height) - ((data[i]*(_height- _horizontal_axis_lablel_height))/ _maxValue);
        }
        return result;
    }

    public void set_comp_data(double[] _comp_data) {
        this._comp_data = _comp_data;
        requestRender();
    }

    public void addComparisonData(double[] cons,int index, int color){
        _comparison_data.set(index,cons);
        _comparison_colors[index] = color;
    }
    public void removeComparisonData(int index){
        _comparison_data.set(index,_empty);
    }

    //DEBUG FUNCTIONS

    private void createDummyValues(){
        for(int i=0;i<7;i++)
            _cons_data[i]=100+(800*Math.random());
        //   for(int i=25;i<48;i++)
          //  _cons_data[i]=0;

        for(int i=0;i<7;i++)
            _comp_data[i]=500+(400*Math.random());
    }

}
