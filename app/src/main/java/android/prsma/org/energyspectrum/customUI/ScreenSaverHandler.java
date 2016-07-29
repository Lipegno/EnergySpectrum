package android.prsma.org.energyspectrum.customUI;

import android.util.Log;

import java.util.Observable;

public class ScreenSaverHandler extends  Observable {

	private long _updatePeriod;
	private boolean _isRunning = false;
	private final HandlerThread _handler = new HandlerThread();
	private static final int BEAT = 1;
	private static final String MODULE = "Screen Saver Thread";
	
	public ScreenSaverHandler(long updatePeriod){
		_updatePeriod=updatePeriod;
		_isRunning=true;
		Log.i(MODULE,	"handler created");
	}
	
	public void finish(){
		_isRunning=false;
	}
	
	public void start(){
		Log.i(MODULE,	"thread started");
		_handler.start();
	}
	
	public void heartBeat(){
		setChanged();
		notifyObservers(BEAT);
	}
	
	public boolean isRunning(){
		return _handler.isAlive();
	}
	
	
	private class HandlerThread extends Thread {
		
		@Override
		public void run(){
			while(_isRunning){
				try {
					Thread.sleep(_updatePeriod);
					heartBeat();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
