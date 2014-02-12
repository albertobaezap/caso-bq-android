package app.bq.bibliotecadb;

public class Book {
	
	private String mTitle;
	private String mDate;
	private int mId;
	
	public Book(String t, String d, int i){
		
		mTitle = t;
		mDate = d;
		mId = i;
		
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
	
	

}
