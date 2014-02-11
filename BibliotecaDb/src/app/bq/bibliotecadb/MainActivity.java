package app.bq.bibliotecadb;

import java.util.List;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DeltaEntry;
import com.dropbox.client2.DropboxAPI.DeltaPage;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.ListView;

public class MainActivity extends Activity {

	
	// Claves para la sincronización de la aplicación con Dropbox.
	private static final String APP_KEY = "d7i4j7ctpt5hn1q";
	private static final String APP_SECRET = "zxe1ea2972uyi62";

	//Elementos de conectividad con Dropbox.
	private AppKeyPair mAppKeys;
	private AndroidAuthSession mSession;
	private DropboxAPI<AndroidAuthSession> mApi;
	
	//Elementos de referencia a la interfaz gráfica
	private ListView mListView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Inicio de sesión
        startSession();
        
       
        //Referencias a la interfaz
        mListView = (ListView) findViewById(R.id.file_list);
        
        
    }
    /**
     * Después de una autentificación vuelve a onResume para terminar de cerrar el inicio
     * de sesión correctamente.
     */
    
    @Override
    protected void onResume(){
    		super.onResume();
    		
    		if(mApi.getSession().authenticationSuccessful()){
    			try{
    				//Necesario para completar la autentificación
    				mApi.getSession().finishAuthentication();
    				
    				//Llama a la funcionalidad principal de la aplicación para listar los archivos
    				new ListFiles().execute();
    				Log.i("out","Auth succesful");
    			} catch (IllegalStateException e) {
					e.printStackTrace();
				} 
    		} else Log.i("out", "Auth failed");
    		
    }
    
    /**
     * Este método realiza el inicio de la sesión en Dropbox autentificándose en la red con
     * las claves que otorgan derechos a esta aplicación para utilizar la API.
     */
    private void startSession(){
    	
    	 mAppKeys = new AppKeyPair(APP_KEY, APP_SECRET); //Guarda la clave y secreto de la API
         mSession = new AndroidAuthSession(mAppKeys); //Guarda la sesiónd de autentificación
         mApi = new DropboxAPI<AndroidAuthSession>(mSession); //Nueva instancia de sesión
         mApi.getSession().startOAuth2Authentication(MainActivity.this); //Inicia intento de autentificación
         
    }
    
   /**
    * 
    * Esta clase llevará a cabo la obtención de ficheros desde el sistema de ficheros original
    * para insertarlos en una lista y luego poder trabajar con ellos.
    *
    */
   private class ListFiles extends AsyncTask<Void, Void, Boolean>{
	   
	   @Override
	   protected void onPreExecute(){
		   super.onPreExecute();
		   
	   }
	   
   /**
	 * El método delta obtiene toda la lista de archivos que se encuentran todo el sistema
	 * de ficheros en Dropbox cuando se llama por primera vez. Se recomienda utilizar delta en 
	 * vez de metadata recursivamente porque implica menos llamadas a la red y es más rápido.
	 * TODO La lectura de ficheros es lenta, incluir una barra de progreso.
	 */

	@Override
	protected Boolean doInBackground(Void... params) {
		
		
		try {
			
			DeltaPage<Entry> existingEntry = mApi.delta("");
			
			List<DeltaEntry<Entry>> de = existingEntry.entries; //Se realiza conversión a lista para poder iterarla
			
			//Iteración de la lista que muestra como resultado los nombres de los archivos.
			for (DeltaEntry<Entry> e : de){
				Log.i("out", e.metadata.fileName());
			}
			
		} catch (DropboxException e) {
			e.printStackTrace();
		}

		
		
		return false;
	}
	
	@Override
	public void onPostExecute(Boolean result){
		super.onPostExecute(result);
		//mSession.unlink();
	}
	   
   }
    
}