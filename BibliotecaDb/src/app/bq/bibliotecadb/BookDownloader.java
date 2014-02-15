package app.bq.bibliotecadb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

import android.os.Environment;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;


/**
 * Esta clase gestiona la descarga de libro mediante el método getFile de la API de Dropbox.
 * Además se ha incluye la generación del elemento Book para poder tratar los parámetros
 * desde otros métodos.
 *
 */
public class BookDownloader {
	
	private String mPath;
	private DropboxAPI<AndroidAuthSession> mApi;
	
	public BookDownloader(DropboxAPI<AndroidAuthSession> api, String path){
		
		mPath = path;
		mApi = api;		

	}
	
	public Book startDownloader(){
		
		Book book = null;
		
		//Se obtiene el directorio de almacenamiento externo para guardar los archivos temporales
		String filePath =  Environment.getExternalStorageDirectory().toString();
		File file = new File(filePath + "/temp.epub");
		Log.i("out",file.getAbsolutePath());
		
		try {
			
			//Se crea un nuevo archivo para descargar desde la carpeta de Dropbox
			FileOutputStream outputStream = new FileOutputStream(file);
			mApi.getFile(mPath, null, outputStream, null);
			outputStream.close();
			
			//Crear un inputstream hacia el archivo
		 	InputStream epubInputStream =  new FileInputStream(file.getAbsolutePath());

	        //Carga el Book al epubreader para leer los metadatos
	        book = (new EpubReader()).readEpub(epubInputStream);
			
		} catch (DropboxException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Se limpian los archivos temporales y se libera memoria
		file.delete();
		
		return book;
	}

}
