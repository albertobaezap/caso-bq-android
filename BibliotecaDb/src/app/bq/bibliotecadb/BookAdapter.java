package app.bq.bibliotecadb;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


/**
 * Clase personalizada para utilizar un nuevo adaptador para rellenar la lista, ya que
 * esta contiene m�ltiples elementos que hay que rellenar, y no es posible hacerlo
 * mediante un ArrayAdapter normal.
 * 
 */
public class BookAdapter extends ArrayAdapter<BookElement> {
	
	private ArrayList<BookElement> mFileList;

	public BookAdapter(Context context, int textViewResourceId, ArrayList<BookElement> objects) {
		super(context, textViewResourceId, objects);
		
		this.mFileList = objects;
		
	}
	
	/**
	 * Sobreescribimos el m�todo para enlazar cada TextView al texto que le corresponde y
	 * poder personalizar la lista. Se podr�a extender para incluir iconos y dem�s par�metros.
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
		BookElement book = mFileList.get(position);
		
		//Introducimos los datos en el TextView correspondiente para que aparezca en la lista
		if (book != null){
			
			TextView label = (TextView) v.findViewById(R.id.list_label);
			TextView date = (TextView) v.findViewById(R.id.list_date);
			
			if (label!=null) label.setText(book.getTitle());
			if (date!=null) date.setText(book.getDate());
			
		}
		
		return v;
		
	}
	

}