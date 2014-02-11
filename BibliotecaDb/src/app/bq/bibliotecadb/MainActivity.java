package app.bq.bibliotecadb;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	
	// Elementos de conectividad con Dropbox.
	//private DbxAccountManager mDbxAcctMgr;
	static final int REQUEST_LINK_TO_DBX = 0;
	// Claves para la sincronizaci�n de la aplicaci�n con Dropbox.
	private static final String APP_KEY = "d7i4j7ctpt5hn1q";
	private static final String APP_SECRET = "zxe1ea2972uyi62";

	//Elementos de conectividad con Dropbox.
	private AppKeyPair mAppKeys;
	private AndroidAuthSession mSession;
	private DropboxAPI<AndroidAuthSession> mApi;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /*
        
        //Este m�todo obtendr� una instancia de nuestra cuenta de Dropbox.
        mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), APP_KEY, APP_SECRET);
        //Este m�todo sincroniza la cuenta con Dropbox.
		mDbxAcctMgr.startLink(MainActivity.this, REQUEST_LINK_TO_DBX);
		
		*/
        
        mAppKeys = new AppKeyPair(APP_KEY, APP_SECRET); //Guarda la clave y secreto de la API
        mSession = new AndroidAuthSession(mAppKeys); //Guarda la sesi�nd de autentificaci�n
        mApi = new DropboxAPI<AndroidAuthSession>(mSession); //Nueva instancia de sesi�n
        mApi.getSession().startOAuth2Authentication(MainActivity.this); //Inicia intento de autentificaci�n
        
        
    }
    /**
     * Despu�s de una autentificaci�n vuelve a onResume para terminar de cerrar el inicio
     * de sesi�n correctamente.
     */
    
    @Override
    protected void onResume(){
    		super.onResume();
    		if(mApi.getSession().authenticationSuccessful()){
    			try{
    				mApi.getSession().finishAuthentication();
    				Log.d("out"," Auth succesful");
    				mSession.unlink();
    			} catch (IllegalStateException e) {
					e.printStackTrace();
				}
    		} else Log.d("out", "Auth failed");
    		
    }
    
    /*
    
    /**
     * La sincronizaci�n se realizar� a trav�s de una actividad externa por lo que el resultado
     * se tratar� en onActivityResult() si la autentificaci�n no da ning�n error.
     *
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
    */


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}