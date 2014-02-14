package app.bq.bibliotecadb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Date;
import nl.siegmann.epublib.epub.EpubReader;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DeltaEntry;
import com.dropbox.client2.DropboxAPI.DeltaPage;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MainActivity extends ListActivity {

	
	// Claves para la sincronización de la aplicación con Dropbox.
	public static final String APP_KEY = "d7i4j7ctpt5hn1q";
	public static final String APP_SECRET = "zxe1ea2972uyi62";

	//Elementos de conectividad con Dropbox.
	private AppKeyPair mAppKeys;
	private AndroidAuthSession mSession;
	private DropboxAPI<AndroidAuthSession> mApi;
	
	//Elementos de almacenamiento de datos
	private ArrayList<BookElement> mFileList;
	
	//Elementos adicionales
	SQLiteDatabase mDB;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Inicio de sesión
        startSession();
        
        //Crea la base de datos
        createDB();
  
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
         mSession = new AndroidAuthSession(mAppKeys); //Guarda la sesión de autentificación
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
	   
	   private ProgressDialog mProgressDialog;

	   @Override
	   protected void onPreExecute(){
		   super.onPreExecute();
		   
		   createProgressDialog();	  
		   
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
				    String sql = "SELECT * FROM Book WHERE Title = '" + e.metadata.fileName() + "'";	
				   
				   //Obtiene el cursor al primer elemento, si existe es que se encuentra en la bd
				    Cursor c = mDB.rawQuery(sql, null);
					if (c.moveToFirst()){
						//TODO Implementar correcto parseo de fecha
						List<Date> ld = null ;
						Log.i("out", "Record exists");
						BookElement book_element = new BookElement(c.getString(0), ld, c.getString(1), 0);
						insertElement(book_element);
					//Si no se encuentra lo añadimos	
					}else {
						
					    // Create a new map of values, where column names are the keys
					    ContentValues values = new ContentValues();
					    values.put("Title", e.metadata.fileName());
					    values.put("Date", e.metadata.modified);
					    mDB.insert("Book", null, values);
					    Log.i("out", "Record doesn't exist yet but was inserted.");
			
					
						//Se obtiene el directorio de almacenamiento externo para guardar los archivos temporales
						String filePath =  Environment.getExternalStorageDirectory().toString();
						File file = new File(filePath + "/temp.epub");
						Log.i("out",file.getAbsolutePath());
						
						//Se crea un nuevo archivo para descargar desde la carpeta de Dropbox
						FileOutputStream outputStream = new FileOutputStream(file);
						DropboxFileInfo info = mApi.getFile(e.metadata.parentPath()+e.metadata.fileName(), null, outputStream, null);
						Log.i("out", info.getMetadata().parentPath() + "  " + info.getMetadata().fileName());
						
						//Se llama a la función que lee los metadatos del archivo
						readEpub(file.getAbsolutePath());
						
						//Se limpian los archivos temporales y se libera memoria
						file.delete();
						outputStream.close();
					}
				}
				
				//Log.i("out", book.getTitle() + " " + book.getDate() + " " + book.getId());
			}
			
		} catch (DropboxException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
		mDB.close();
		//setListAdapter(new ArrayAdapter<BookElement>(MainActivity.this, R.layout.activity_main, R.id.list_label, mFileList));
		
		//Construimos un nuevo adaptador para introducir los datos en la lista
		BookAdapter bookAdapter = new BookAdapter(MainActivity.this, R.layout.activity_main, mFileList);
		setListAdapter(bookAdapter);
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
	 * @param filename
	 */
	public void readEpub(String filename){
		
		 try {
	            //Crear un inputstream hacia el archivo
			 	InputStream epubInputStream =  new FileInputStream(filename);

	            //Carga el Book al epubreader para leer los metadatos
	            Book book = (new EpubReader()).readEpub(epubInputStream);
	           

	            //Obtiene los metadatos necesarios
	            Log.i("author", "Author: " + book.getMetadata().getAuthors());
	            Log.i("title", "Title: " + book.getTitle());
	            Log.i("date", "Date: " + book.getMetadata().getDates());
	            
	            //Se introducen los datos necesarios en la nueva clase BookElement para hacer más sencillo su gestión
	            BookElement book_element = new BookElement(book.getTitle(), book.getMetadata().getDates(), book.getMetadata().getAuthors().toString(),  0);
	            insertElement(book_element);

	            /* Log the book's coverimage property */
	            // Bitmap coverImage =
	            // BitmapFactory.decodeStream(book.getCoverImage()
	            // .getInputStream());
	            // Log.i("epublib", "Coverimage is " + coverImage.getWidth() +
	            // " by "
	            // + coverImage.getHeight() + " pixels");

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}
	   
   }
   
   /**
    * Esta función crea la base de datos inicial y la obtiene en modo escritura
    */
   public void createDB(){
	   
	   DatabaseHelper mDbHelper = new DatabaseHelper(this);
	   
	   mDB = mDbHelper.getWritableDatabase();

	    
	}
   
   public void insertElement(BookElement book_element){
		mFileList.add(book_element);
   }
    
}