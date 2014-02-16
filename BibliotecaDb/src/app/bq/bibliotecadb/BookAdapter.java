package app.bq.bibliotecadb;

import java.io.IOException;
import java.util.ArrayList;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

import nl.siegmann.epublib.domain.Book;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Clase personalizada para utilizar un nuevo adaptador para rellenar la lista, ya que
 * esta contiene múltiples elementos que hay que rellenar, y no es posible hacerlo
 * mediante un ArrayAdapter normal.
 * 
 */
public class BookAdapter extends ArrayAdapter<BookElement> {
	
	private ArrayList<BookElement> mFileList;
	private GestureDetector mGestureListener;
	private static DropboxAPI<AndroidAuthSession> mApi;
	private Context mContext;

	public BookAdapter(Context context, int textViewResourceId, ArrayList<BookElement> objects,
			DropboxAPI<AndroidAuthSession> api) {
		super(context, textViewResourceId, objects);
		this.mFileList = objects;
		//La clase GestureListener simplemente devuelve true cuando se reconoce un double-tap
		this.mGestureListener = new GestureDetector(context, new GestureListener());
		
		mContext = context;
		mApi = api;
		
	}
	
	/**
	 * Sobreescribimos el método para enlazar cada TextView al texto que le corresponde y
	 * poder personalizar la lista. Se podría extender para incluir iconos y demás parámetros.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		
		View v = convertView;
		
		//Hay que comprobar que la View existe, y si no, la inflamos
		if (v==null){
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.activity_main, null);
		}
		
		//El elemento del que vamos a obtener los datos
		final BookElement book = mFileList.get(position);
		
		//Introducimos los datos en el TextView correspondiente para que aparezca en la lista
		if (book != null){
			
			TextView label = (TextView) v.findViewById(R.id.list_label);
			TextView date = (TextView) v.findViewById(R.id.list_date);
			ImageView image = (ImageView) v.findViewById(R.id.list_icon);
			
			if (label!=null) label.setText(book.getTitle());
			if (date!=null) date.setText(book.getDate());
			
			//Se ha creado un listener que reconoce double-tap para gestionar el doble click.
			image.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					 if (mGestureListener.onTouchEvent(arg1)){
						 //Llama al método estático para obtener la portada del libro.
						getCover(book.getId());
					 }
					 
					 return true;
				}
			});
			
		}
		
		
		
		return v;
		
	}
	
		/**
	    * Método para descargar la portada del libro. Se realiza de nuevo la descarga del libro según
	    * el parámetro Id que es la ruta completa del archivo y se obtiene la portada mediante los
	    * métodos de epublib de getCoverImage()  
	    * @param path
	    */
	   private void getCover(String path){
		   
		   final String mPath = path;
		   
		
		   //Es necesario crear un AsyncTask para poder realizar la descarga.
		   new AsyncTask<String, Void, Boolean>(){
			   
			AlertDialog.Builder dialog = null;
		   	   
			@Override
			protected Boolean doInBackground(String... arg0) {
				
				BookDownloader bd = new BookDownloader(mApi, mPath);
				
				Book book = bd.startDownloader();
				
				//Se utiliza el método getCoverImage de epublib para obtener la portada del epub
		        Bitmap coverImage;
				try {
					coverImage = BitmapFactory.decodeStream(book.getCoverImage()
					.getInputStream());
					
					//Se crea un nuevo diálogo para mostrar la portada en el hilo original
					dialog = new AlertDialog.Builder(mContext);
					ImageView iv = new ImageView(mContext);
					iv.setImageBitmap(coverImage);
					dialog.setView(iv);
			        
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			public void onPostExecute(Boolean result){
				super.onPostExecute(result);
				
				dialog.show();
							
			}
		   }.execute();
		   
		   
	   }
	

}
