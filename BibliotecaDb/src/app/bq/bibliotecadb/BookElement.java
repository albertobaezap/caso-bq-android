package app.bq.bibliotecadb;

/**
 * Esta clase almacena los datos necesarios para cada elemento de la lista que se va a mostrar
 *
 */

public class BookElement {
	
	//Datos necesarios de cada libro
	private String mTitle;
	private String mDate;
	private String mAuthor;
	private String mId;
		
	public BookElement(String t, String d, String a, String i){
		
		mTitle = t;
		mDate = d;
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
	
	public String getId(){
		return mId;
	}
	
	public String getAuthor(){
		return mAuthor;
	}	
	

}
