package app.bq.bibliotecadb;

import java.util.ArrayList;

import android.content.Context;
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

	public BookAdapter(Context context, int textViewResourceId, ArrayList<BookElement> objects) {
		super(context, textViewResourceId, objects);
		this.mFileList = objects;
		//La clase GestureListener simplemente devuelve true cuando se reconoce un double-tap
		this.mGestureListener = new GestureDetector(context, new GestureListener());
		
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
						 MainActivity.getCover(book.getId());
					 }
					 
					 return true;
				}
			});
			
		}
		
		
		
		return v;
		
	}
	

}
