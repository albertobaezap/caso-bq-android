package app.bq.bibliotecadb;

import com.dropbox.sync.android.DbxAccountManager;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {
	
	// Elementos de conectividad con Dropbox.
	private DbxAccountManager mDbxAcctMgr;
	static final int REQUEST_LINK_TO_DBX = 0;
	// Claves para la sincronizaci�n de la aplicaci�n con Dropbox.
	private static final String APP_KEY = "gr0g9h5f62jzlew";
	private static final String APP_SECRET = "l5rty2j1dfmeqtq";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        //Este m�todo obtendr� una instancia de nuestra cuenta de Dropbox.
        mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), APP_KEY, APP_SECRET);
        //Este m�todo sincroniza la cuenta con Dropbox.
		mDbxAcctMgr.startLink(MainActivity.this, REQUEST_LINK_TO_DBX);
    }
    
    /**
     * La sincronizaci�n se realizar� a trav�s de una actividad externa por lo que el resultado
     * se tratar� en onActivityResult() si la autentificaci�n no da ning�n error.
     */
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_LINK_TO_DBX) {
	        if (resultCode == Activity.RESULT_OK) {
	        	// ... Start using Dropbox files.
	            Log.d("out", "Auth succesful");
	        } else {
	            // ... Link failed or was cancelled by the user.
	        	Log.d("out", "Auth failed");
	        }
	    } else {
	        super.onActivityResult(requestCode, resultCode, data);
	    }
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
