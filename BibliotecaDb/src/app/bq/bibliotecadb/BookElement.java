package app.bq.bibliotecadb;

public class BookElement {
	
	private String mTitle;
	private String mDate;
	private int mId;
	
	public BookElement(String t, String d, int i){
		
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
