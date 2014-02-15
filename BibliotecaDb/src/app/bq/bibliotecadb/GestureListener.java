package app.bq.bibliotecadb;

import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;


/**
 * Esta clase se ha extendido de SimpleOnGestureListener para reconocer
 * el evento Double-Tap que reconoce el doble click en los elementos de la lista.
 *
 */
public class GestureListener extends SimpleOnGestureListener {
				
	@Override
	public boolean onDown(MotionEvent e) {
	    return false;
	}
	
	@Override
	public boolean onDoubleTap(MotionEvent e){
	    return true;
	}
	

}
