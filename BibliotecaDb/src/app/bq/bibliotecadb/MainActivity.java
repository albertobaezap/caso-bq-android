package app.bq.bibliotecadb;

import java.io.IOException;
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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ListActivity {

	
	// Claves para la sincronizaci�n de la aplicaci�n con Dropbox.
	public static final String APP_KEY = "d7i4j7ctpt5hn1q";
	public static final String APP_SECRET = "zxe1ea2972uyi62";

	//Elementos de conectividad con Dropbox.
	private AppKeyPair mAppKeys;
	private AndroidAuthSession mSession;
	private static DropboxAPI<AndroidAuthSession> mApi;
	
	//Elementos de almacenamiento de datos
	public static final int MODE_TITLE = 0;
	public static final int MODE_DATE = 1;
	private ArrayList<BookElement> mFileList;
	private BookAdapter mBookAdapter;
	
	//Elementos adicionales
	private SQLiteDatabase mDB;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Inicio de sesi�n
        startSession();
        
        //Crea la base de datos
        createDB();
  
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
    				//Necesario para completar la autentificaci�n
    				mApi.getSession().finishAuthentication();
    				
    				//Llama a la funcionalidad principal de la aplicaci�n para listar los archivos
    				new ListFiles().execute();
    				Log.i("out","Auth succesful");
    			} catch (IllegalStateException e) {
					e.printStackTrace();
				} 
    		} else Log.i("out", "Auth failed");
    		
    }
    
    //Sobreescribe el men� para utilizar el que he creado
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	getMenuInflater().inflate(R.menu.main, menu);
    	
    	return true;
    }
    
    //Sobreescribe el m�todo para dar funcionalidad a las opciones del men�
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
     * Este m�todo realiza el inicio de la sesi�n en Dropbox autentific�ndose en la red con
     * las claves que otorgan derechos a esta aplicaci�n para utilizar la API.
     */
    private void startSession(){
    	
    	 mAppKeys = new AppKeyPair(APP_KEY, APP_SECRET); //Guarda la clave y secreto de la API
         mSession = new AndroidAuthSession(mAppKeys); //Guarda la sesi�n de autentificaci�n
         mApi = new DropboxAPI<AndroidAuthSession>(mSession); //Nueva instancia de sesi�n
         mApi.getSession().startOAuth2Authentication(MainActivity.this); //Inicia intento de autentificaci�n
         
    }
    
   /**
    * 
    * Esta clase llevar� a cabo la obtenci�n de ficheros desde el sistema de ficheros original
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
	 * El m�todo delta obtiene toda la lista de archivos que se encuentran todo el sistema
	 * de ficheros en Dropbox cuando se llama por primera vez. Se recomienda utilizar delta en 
	 * vez de metadata recursivamente porque implica menos llamadas a la red y es m�s r�pido.
	 * TODO La lectura de ficheros es lenta, incluir una barra de progreso.
	 */

	@Override
	protected Boolean doInBackground(Void... params) {
		
		
		try {
			
			DeltaPage<Entry> existingEntry = mApi.delta("");
			
			List<DeltaEntry<Entry>> deltaEntryList = existingEntry.entries; //Se realiza conversi�n a lista para poder iterarla
			
			mFileList = new ArrayList<BookElement>(); //Se crea la lista donde se almacenar�n los datos
						
			//Iteraci�n de la lista que muestra como resultado los nombres de los archivos.
			for (DeltaEntry<Entry> e : deltaEntryList){
				
				//Comprueba que no sea un directorio y que sea .epub y a�ade a la lista cada libro
				if ((!e.metadata.isDir) && (isEpub(e.metadata.fileName()))) {

					//Esta consulta comprobar� si el t�tulo ya est� en la base de datos
				    String sql = "SELECT * FROM Book WHERE Id = '" + e.metadata.parentPath()+e.metadata.fileName() + "'";
				    Log.i("out", "Searching for " + e.metadata.fileName());
				   
				   //Obtiene el cursor al primer elemento, si existe es que se encuentra en la bd
				    Cursor c = mDB.rawQuery(sql, null);
					if (c.moveToFirst()){
						Log.i("out", "Record exists: " + c.getString(0) + "  " + c.getString(1) + "  " + c.getString(2));
						BookElement book_element = new BookElement(c.getString(0), c.getString(1), "Author", c.getString(2));
						insertElement(book_element);
					//Si no se encuentra lo a�adimos	
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
	 * a�adir el adaptador para la lista, que incluir� todos los datos incluidos en el Array.
	 */
	@Override
	public void onPostExecute(Boolean result){
		super.onPostExecute(result);
		
		mProgressDialog.dismiss();
		mDB.close();
		//setListAdapter(new ArrayAdapter<BookElement>(MainActivity.this, R.layout.activity_main, R.id.list_label, mFileList));
		
		//Construimos un nuevo adaptador para introducir los datos en la lista
		mBookAdapter = new BookAdapter(MainActivity.this, R.layout.activity_main, mFileList);
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
	 * Esta funci�n comprobar� cada archivo individualmente antes de descargarlo para
	 * asegurarse de que es un archivo .epub
	 * 
	 * @param filename
	 * @return true si es epub, falso en caso contrario
	 */
	public boolean isEpub(String filename){
		
		//Los 5 �ltimos caracteres tienen que ser .epub
		//TODO Comprobar que el archivo tiene m�s de 5 caracteres
		if (filename.substring(filename.length()-5, filename.length())
				.equals(".epub")){
			Log.i("out", filename + " is .epub");
			return true;		
		}
		
		Log.i("out", filename + " is not .epub");
		return false;
	}
	
	/**
	 * Esta funci�n leer� los metadatos del archivo .epub para incluirlos a la lista de libros
	 * que se mostrar� por pantalla. Se puede extender para incluir m�s par�metros.
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
	    mDB.insert("Book", null, values);
	    Log.i("out", "Record doesn't exist yet but was inserted.");
        
        //Se introducen los datos necesarios en la nueva clase BookElement para hacer m�s sencillo su gesti�n
        BookElement book_element = new BookElement(book.getTitle(), parseDate(book.getMetadata().getDates()), book.getMetadata().getAuthors().toString(),  id);
        insertElement(book_element);

        
	}
	   
   }
   
   /**
    * Esta funci�n crea la base de datos inicial y la obtiene en modo escritura
    */
   public void createDB(){
	   
	   DatabaseHelper mDbHelper = new DatabaseHelper(this);
	   
	   mDB = mDbHelper.getWritableDatabase();

	    
	}
   
   /**
    * ESta funci�n inserta un elemento en la lista
    * @param book_element
    */
   public void insertElement(BookElement book_element){
		mFileList.add(book_element);
   }
   
   /**
    * Esta funci�n formatea la fecha a un valor constante y legible
    * @param list
    * @return La fecha formateada yy-MM-dd
    */
   public String parseDate(List<Date> list){

	   //Si no se consigue convertir, se pone desconocida por defecto
	   String d = "Fecha desconocida";
	   
	   try{
		   //Formato de conversi�n
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
    * M�todo para descargar la portada del libro. Se realiza de nuevo la descarga del libro seg�n
    * el par�metro Id que es la ruta completa del archivo y se obtiene la portada mediante los
    * m�todos de epublib de getCoverImage()
    * Se ha declarado el m�todo est�tico para poder accederlo desde la clase BookAdapter.   
    * @param path
    */
   public static void getCover(String path){
	   
	   final String mPath = path;
	
	   //Es necesario crear un AsyncTask para poder realizar la descarga.
	   new AsyncTask<String, Void, Boolean>(){
	   	   
		@Override
		protected Boolean doInBackground(String... arg0) {
			
			BookDownloader bd = new BookDownloader(mApi, mPath);
			
			Book book = bd.startDownloader();
			
			//Se utiliza el m�todo getCoverImage de epublib para obtener la portada del epub
	        Bitmap coverImage;
			try {
				coverImage = BitmapFactory.decodeStream(book.getCoverImage()
				.getInputStream());
		        Log.i("epublib", "Coverimage is " + coverImage.getWidth() +
		                " by " +
		                coverImage.getHeight() + " pixels");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	   }.execute();
   }
   
   
   
   
   
   /**
    * Esta funci�n ordena los elementos de la lista seg�n el modo seleccionado, ya sea
    * por t�tulo o por fecha. Para ello se hace uso de la funci�n sort sobre un Comparador
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