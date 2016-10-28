package android.prsma.org.energyspectrum.customUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.prsma.org.energyspectrum.R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressLint("WrongCall")
public class ComparisonWidget extends SurfaceView implements SurfaceHolder.Callback{

	private int today_cons;
	private int avg_cons;
	private int max_cons;
	private String legend;
	public ComparisonWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		getHolder().addCallback(this);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onDraw(Canvas c){
		if(c!=null){
			c.drawColor(Color.parseColor("#E2E0DB"));
			if(max_cons!=0){
				drawComp(c);
				drawLegend(c);
			}
		}
	}
	private void drawComp(Canvas c) {

		int w = Math.round(getWidth());
		int h = Math.round(getHeight());
		//Log.i("comp","AQQQUIIIII");

		int rect1_w = Math.round(today_cons*w/max_cons);
		int rect2_w = Math.round(avg_cons*w/max_cons);
		int rect3_w = rect1_w<rect2_w?rect1_w:rect2_w;

		Paint p = new Paint();
		int pos = 0;//(int) Math.round(rect1_h-(rect1_h/3));
		int pic_height = BitmapFactory.decodeResource(getResources(), R.drawable.graph_turn).getHeight();
		int pic_width = BitmapFactory.decodeResource(getResources(), R.drawable.graph_turn).getWidth();
		c.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.graph_turn),0,h-pic_height,p);
		Rect r = new Rect(pic_width-3, h-15,rect1_w,h);
		Rect r2 = new Rect(pic_width-3, (int) (h-18-8),rect2_w,h-8);

		p.setColor(Color.parseColor("#C1C1C1"));
		c.drawRect(r2, p);
		p.setColor(Color.parseColor("#EC5833"));
		c.drawRect(r, p);
		p.setColor(Color.parseColor("#A53E27"));
		Rect r3 = new Rect(pic_width-3, h-15,rect3_w,h-9);
		c.drawRect(r3, p);
	}
	private void drawLegend(Canvas c){
		float x = 0;
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setTextSize(12);
		
		p.setColor(Color.parseColor("#C1C1C1"));
		c.drawRect(getWidth()/2-40,0, getWidth()/2-30, 10, p);
		p.setColor(Color.parseColor("#828282"));
		c.drawText("M�dia", getWidth()/2-25,  10, p);
		
		p.setColor(Color.parseColor("#EC5833"));
		c.drawRect(getWidth()/2+20, 0, getWidth()/2+30, 10, p);
		c.drawText(legend,  getWidth()/2+35,  10, p);
	}
	public void requestRender(){
		System.gc(); // ainda n�o se se � necess�rio mas pronto s� para ter certeza chamamos o GC agora.. aqui � um lugar seguro
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
	//	requestRender();

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		//Log.e("COMP","COMP WIDGET CREATED");
		requestRender();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	public int getToday_cons() {
		return today_cons;
	}

	public void setToday_cons(int today_cons) {
		this.today_cons = today_cons;
	}

	public int getAvg_cons() {
		return avg_cons;
	}

	public void setAvg_cons(int avg_cons) {
		this.avg_cons = avg_cons;
	}
	public int getmax_cons() {
		return max_cons;
	}

	public void setmax_cons(int avg_cons) {
		this.max_cons = avg_cons;
	}

	public String getLegend() {
		return legend;
	}

	public void setLegend(String lgd) {
		legend = lgd;
	}
}
