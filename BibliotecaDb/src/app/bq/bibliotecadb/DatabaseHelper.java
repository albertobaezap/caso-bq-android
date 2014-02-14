package app.bq.bibliotecadb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Esta clase va a ayudar a generar la base de datos para almacenar los ficheros.
 * @author Alberto
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	//Valores globales
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Library.db";
    
    //Constantes de nombres de columna
    public static final String TABLE_NAME = "Book";
    public static final String COLUMN_1 = "Title";
    public static final String COLUMN_2 = "Date";
    
    //Constantes de texto para gestionar las consultas SQL
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    
    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + TABLE_NAME + " (" +
        "COLUMN_1" + TEXT_TYPE + COMMA_SEP +
        "COLUMN_2" + TEXT_TYPE + 
        " )";

    private static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + TABLE_NAME;
    
    
	
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
