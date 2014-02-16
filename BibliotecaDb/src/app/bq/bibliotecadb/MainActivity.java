package app.bq.bibliotecadb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Date;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DeltaEntry;
import com.dropbox.client2.DropboxAPI.DeltaPage;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ListActivity {

	
	// Claves para la sincronización de la aplicación con Dropbox.
	public static final String APP_KEY = "d7i4j7ctpt5hn1q";
	public static final String APP_SECRET = "zxe1ea2972uyi62";

	//Elementos de conectividad con Dropbox.
	private AppKeyPair mAppKeys;
	private AndroidAuthSession mSession;
	private DropboxAPI<AndroidAuthSession> mApi;
	
	//Elementos de almacenamiento de datos
	public static final int MODE_TITLE = 0;
	public static final int MODE_DATE = 1;
	private ArrayList<BookElement> mFileList;
	private BookAdapter mBookAdapter;
	
	//Elementos adicionales
	private SQLiteDatabase mDb;
	private DatabaseHelper mDbHelper;
	
	//Elementos de SharedPreferences
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    public boolean mLoggedIn;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Inicio de sesión
        startSession();
        
        mApi = new DropboxAPI<AndroidAuthSession>(mSession); //Nueva instancia de sesión 
        
        //Si ya estamos logeados no hace falta volver a crear la sesión
        if (mLoggedIn) {
        	//Llama a la funcionalidad principal de la aplicación para listar los archivos
			new ListFiles().execute();

        } else {
        	 Log.i("out", "Logging in");
            	//Si no estamos logeados tenemos que crear una nueva sesión
            	mApi.getSession().startOAuth2Authentication(MainActivity.this); //Inicia intento de autentificación          
        }
        
       
  
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
    				String accessToken = mApi.getSession().getOAuth2AccessToken();    	
    				
    				//Si no tenemos un token de sesión, lo almacenamos en SharedPreferences
    				 if (accessToken != null) {
    			            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
    			            prefs.edit().putString(ACCESS_KEY_NAME, "auth").commit();
    			            prefs.edit().putString(ACCESS_SECRET_NAME, accessToken).commit();     
    				 }
    				 
    				 //Ponemos en login la aplicación
    				 
    				 if (!mLoggedIn){
    					//Llama a la funcionalidad principal de la aplicación para listar los archivos
						new ListFiles().execute();
						mLoggedIn = true;
    				 }
    				 
    				Log.i("out","Auth succesful");
    			} catch (IllegalStateException e) {
					e.printStackTrace();
				} 
    		} else {
    			Log.i("out", "Auth failed");
    			
    		}
    		
    }
    
    //Sobreescribe el menú para utilizar el que he creado
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	getMenuInflater().inflate(R.menu.main, menu);
    	
    	return true;
    }
    
    //Sobreescribe el método para dar funcionalidad a las opciones del menú
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
        switch (item.getItemId()) {
            case R.id.action_order_1:
            	
            	sortList(MODE_TITLE);
            	
                return true;
            case R.id.action_order_2:
            	
            	sortList(MODE_DATE);
            	
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Este método realiza el inicio de la sesión en Dropbox autentificándose en la red con
     * las claves que otorgan derechos a esta aplicación para utilizar la API.
     */
    private void startSession(){
    	
    	 mAppKeys = new AppKeyPair(APP_KEY, APP_SECRET); //Guarda la clave y secreto de la API
         mSession = new AndroidAuthSession(mAppKeys); //Guarda la sesión de autentificación
         
         
         //Buscamos en shared preferences el token de la aplicación
         SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
         String key = prefs.getString(ACCESS_KEY_NAME, null);
         String secret = prefs.getString(ACCESS_SECRET_NAME, null);
         
         if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;
  
         //Si ya estamos logeados no tenemos que crear una nueva essión
         if (key.equals("auth")) {  
        	 Log.i("out", "Already logged in with secret: " + secret);
             mSession.setOAuth2AccessToken(secret);
             mLoggedIn = true;
         }
 	  
    }
    
   /**
    * 
    * Esta clase llevará a cabo la obtención de ficheros desde el sistema de ficheros original
    * para insertarlos en una lista y luego poder trabajar con ellos.
    *
    */
   private class ListFiles extends AsyncTask<Void, Void, Boolean>{
	   
	   private ProgressDialog mProgressDialog;

	   @Override
	   protected void onPreExecute(){
		   super.onPreExecute();
		   
		   createProgressDialog();	  
	        
	        //Crea la base de datos en pre-execute para poder acceder a ella al volver
	        createDB();
		   
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
			
			List<DeltaEntry<Entry>> deltaEntryList = existingEntry.entries; //Se realiza conversión a lista para poder iterarla
			
			mFileList = new ArrayList<BookElement>(); //Se crea la lista donde se almacenarán los datos
						
			//Iteración de la lista que muestra como resultado los nombres de los archivos.
			for (DeltaEntry<Entry> e : deltaEntryList){
				
				//Comprueba que no sea un directorio y que sea .epub y añade a la lista cada libro
				if ((!e.metadata.isDir) && (isEpub(e.metadata.fileName()))) {

					//Esta consulta comprobará si el título ya está en la base de datos
				    String sql = "SELECT * FROM Book WHERE Id = '" + e.metadata.parentPath()+e.metadata.fileName() + "'";
				    Log.i("out", "Searching for " + e.metadata.fileName());
				   
				   //Obtiene el cursor al primer elemento, si existe es que se encuentra en la bd
				    Cursor c = mDb.rawQuery(sql, null);
					if (c.moveToFirst()){
						Log.i("out", "Record exists: " + c.getString(0) + "  " + c.getString(1) + "  " + c.getString(2));
						BookElement book_element = new BookElement(c.getString(0), c.getString(1), "Author", c.getString(2));
						insertElement(book_element);
					//Si no se encuentra lo añadimos	
					}else {	
											
						BookDownloader bd = new BookDownloader(mApi, e.metadata.parentPath()+e.metadata.fileName());
						
						Book book = bd.startDownloader();
						readEpub(book, e.metadata.parentPath()+e.metadata.fileName());
						
						

						
					}
				}			
				//Log.i("out", book.getTitle() + " " + book.getDate() + " " + book.getId());
			}
			
		} catch (DropboxException e) {
			e.printStackTrace();
		}

		return false;
	}
	
	/**
	 * Cuando termine de listar todos los archivos, se llama a onPostExecute donde se va a 
	 * añadir el adaptador para la lista, que incluirá todos los datos incluidos en el Array.
	 */
	@Override
	public void onPostExecute(Boolean result){
		super.onPostExecute(result);
		
		mProgressDialog.dismiss();
		
		//Hay cerrar la base de datos cuando acabemos de usarla
		mDb.close();
		mDbHelper.close();
		
		//Construimos un nuevo adaptador para introducir los datos en la lista
		mBookAdapter = new BookAdapter(MainActivity.this, R.layout.activity_main, mFileList, mApi);
		setListAdapter(mBookAdapter);
				
		
	}
	
	/**
	 * Crea el popup con la barra de progreso mientras espera a que se obtengan los archivos.
	 */
	public void createProgressDialog(){
		
		mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage(getString(R.string.progress_dialog_text));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        
        mProgressDialog.show();
		
	}
		
	/**
	 * Esta función comprobará cada archivo individualmente antes de descargarlo para
	 * asegurarse de que es un archivo .epub
	 * 
	 * @param filename
	 * @return true si es epub, falso en caso contrario
	 */
	public boolean isEpub(String filename){
		
		//Los 5 últimos caracteres tienen que ser .epub
		//TODO Comprobar que el archivo tiene más de 5 caracteres
		if (filename.substring(filename.length()-5, filename.length())
				.equals(".epub")){
			Log.i("out", filename + " is .epub");
			return true;		
		}
		
		Log.i("out", filename + " is not .epub");
		return false;
	}
	
	/**
	 * Esta función leerá los metadatos del archivo .epub para incluirlos a la lista de libros
	 * que se mostrará por pantalla. Se puede extender para incluir más parámetros.
	 * 
	 */
	public void readEpub(Book book, String id){
		
        //Obtiene los metadatos necesarios
        Log.i("author", "Author: " + book.getMetadata().getAuthors());
        Log.i("title", "Title: " + book.getTitle());
        Log.i("date", "Date: " + book.getMetadata().getDates());
        Log.i("hash", "Id: " + id);
        
		
	    //Introduce los valores en la base de datos
	    ContentValues values = new ContentValues();
	    values.put("Title", book.getTitle());
	    values.put("Date", parseDate(book.getMetadata().getDates()));
	    values.put("Id", id);
	    mDb.insert("Book", null, values);
	    Log.i("out", "Record doesn't exist yet but was inserted.");
        
        //Se introducen los datos necesarios en la nueva clase BookElement para hacer más sencillo su gestión
        BookElement book_element = new BookElement(book.getTitle(), parseDate(book.getMetadata().getDates()), book.getMetadata().getAuthors().toString(),  id);
        insertElement(book_element);

        
	}
	   
   }
   
   /**
    * Esta función crea la base de datos inicial y la obtiene en modo escritura
    */
   public void createDB(){
	   
	   mDbHelper = new DatabaseHelper(this);
	  	   
	   mDb = mDbHelper.getWritableDatabase();	    
	}
   
   /**
    * ESta función inserta un elemento en la lista
    * @param book_element
    */
   public void insertElement(BookElement book_element){
		mFileList.add(book_element);
   }
   
   /**
    * Esta función formatea la fecha a un valor constante y legible
    * @param list
    * @return La fecha formateada yy-MM-dd
    */
   public String parseDate(List<Date> list){

	   //Si no se consigue convertir, se pone desconocida por defecto
	   String d = "Fecha desconocida";
	   
	   try{
		   //Formato de conversión
		   SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
		   
		   for (Date e : list){
			   			   
				if (e!=null){
					 //La fecha se tiene que convertir en un formato Date normal
					 java.util.Date dt = sdf.parse(e.getValue());
					 
					 d = sdf.format(dt);
				} 	
			}	
    
	   }catch (IllegalArgumentException e){
		 e.printStackTrace();  
	   } catch (ParseException e) {
		e.printStackTrace();
	}
	   
		
	   return d;
   }
   
   
   
   
   /**
    * Esta función ordena los elementos de la lista según el modo seleccionado, ya sea
    * por título o por fecha. Para ello se hace uso de la función sort sobre un Comparador
    * personalizado que toma los elementos necesarios de la clase BookElement.
    * @param mode
    * @param adapter
    */
   public void sortList(int mode){
	   
	   switch(mode){
	   
		   case MODE_TITLE:{
			   Log.i("out","Sorting by Title");
			   mBookAdapter.sort(new Comparator<BookElement>() {
				    public int compare(BookElement arg0, BookElement arg1) {
				        return arg0.getTitle().compareTo(arg1.getTitle());
				    }
				});
			   
		   }break;
		   case MODE_DATE:{
			   Log.i("out","Sorting by Date");
			   mBookAdapter.sort(new Comparator<BookElement>() {
				    public int compare(BookElement arg0, BookElement arg1) {
				        return arg0.getDate().compareTo(arg1.getDate());
				    }
				});
			   
		   }break;
	   }
	   
	   
	   
		mBookAdapter.notifyDataSetChanged();
		
		
			   
   }
    
}