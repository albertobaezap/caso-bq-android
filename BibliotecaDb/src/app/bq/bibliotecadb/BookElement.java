package app.bq.bibliotecadb;

import java.util.List;

import android.util.Log;

import nl.siegmann.epublib.domain.Date;

/**
 * Esta clase almacena los datos necesarios para cada elemento de la lista que se va a mostrar
 *
 */

public class BookElement {
	
	//Datos necesarios de cada libro
	private String mTitle;
	private String mDate;
	private String mAuthor;
	private int mId;
	
	//Datos en crudo para parsear
	private List<Date> mDateRaw;
	
	public BookElement(String t, List<Date> d, String a, int i){
		
		mTitle = t;
		mDateRaw = d;
		mAuthor = a;
		mId = i;
		
		//format();
		
	}
	
	public String getTitle(){
		return mTitle;
	}
	
	public String getDate(){
		return mDate;
	}
	
	public int getId(){
		return mId;
	}
	
	public String getAuthor(){
		return mAuthor;
	}
	
	/**
	 * Esta función formatea los datos para poder mostrarlos por pantalla.
	 * En este caso sólo actúa sobre las fechas, para obtener la fecha correcta de publicación.
	 */
	private void format(){
				
		for (Date d : mDateRaw){
			if (d!=null){
				Log.i("out", d.toString());
				mDate = d.toString();
			} else mDate = "Fecha desconocida";
				
		}		
		
	}
	
	

}
