BibliotecaDb
===============

Caso de desarrollo de aplicación para bq.

En cada commit se encuentran los comentarios sobre el desarrollo de la aplicación



Resumen de cómo se ha implementado cada punto:
--

1. Exista una pantalla inicial de login, donde el usuario pueda introducir sus credenciales para tener acceso a su cuenta Dropbox. (https://www.dropbox.com/).

	Recursos: Dropbox Core API.
	
  He considerado innecesario el uso de una pantalla de Login puesto que al utilizar la librería de Dropbox, al realizar la autentificación con el servidor, directamente te lleva online al formulario, que ya incluye una pantalla de login. La aplicación realiza la conexión automáticamente y pasa directamente a descargar la lista de ficheros si los datos son correctos.
  
2. Una vez introducidos los credenciales, se mostrarán en pantalla todos los libros que el usuario disponga en su cuenta Dropbox en forma de biblioteca. Esto es, todos los ficheros que tengan extensión .epub . (Para representar cada libro, puede usarse un icono genérico).

	Recursos: Epublib.
	
	La actividad principal de la aplicación es una ListActivity que carga los libros en formato .epub en la lista. Para ello haciendo uso de las librerías de Dropbox, se realiza la descarga de ficheros que coincidan con la extensión y se introducen en la lista principal junto a un icono genérico. Para evitar descargar de nuevo la lista en sucesivas aperturas de la aplicación, se ha creado una base de datos utilizando SQLiteDatabase que almacena todos los datos de los libros ya descargados anteriormente para mostrarlos en la lista y no tener que descargarlos de nuevo.
	
3. Dicha pantalla tendrá un menú desplegable con dos opciones, una de ellas ordenará la lista de libros por el nombre del archivo y la otra por fecha de creación. (Idealmente nos gustaría ordenar por el título del libro en vez del nombre del fichero).

	Para realizar la ordenación se ha creado un menú desplegable en la barra de acción haciendo uso del parámetro showAsAction en el archivo .xml de interfaz del menú. Para realizar la ordenación se han utilizado la clase Comparator que actúa sobre la clase personalizada BookElement y permite ordenarlas según los parámetros necesarios.

4.  Si el usuario clickea dos veces sobre el icono genérico que representa cada libro, la aplicación mostrará la portada del libro clickEado.

	Recursos: Dropbox Core API, Epublib.

	Para mostrar la portada del libro se ha implementado un método en la clase BookAdapter que descarga de nuevo el libro utilizando un Id almacenado (la ruta completa del archivo en Dropbox) y obteniendo la portada desde el libro utilizando el método getCoverImage() de Epublib. Para mostrarla se ha utilizado simplemente un Dialogo que muestra la portada, y se cierra pulsando el botón de Back. (Ver: Captura2)

Finalmente se proponen unas sencillas mejoras para la aplicación que
ayudarían a su funcionamiento y que no se han terminado de implementar
por falta de tiempo:

- Ahora que está abierta a otros usuarios, se debería añadir una página
de opciones para poder desvincular la aplicación de un usuario y poder
acceder desde otra cuenta (mediante el método unlink()). Actualmente se
puede hacer desde la web de Dropbox o desinstalando la aplicación.

- En vez de obtener la lista cada vez que se abre la aplicación, se
podría simplemente añadir un botón para "Sincronizar" los datos, y
mientras tanto trabajar con la base de datos offline (en caso de
necesitar una portada, si se requeriría una conexión). Actualmente
mientras la aplicación esté corriendo, la sesión se encuentra abierta en
todo momento.

- Cuando vaya a descargar la lista por primera vez, primero observar el
número de archivos a descargar, y añadir una barra de progreso indicando
los archivos que faltan para terminar la descarga.

Bugs conocidos o elementos a mejorar:

- Actualmente la descarga de los archivos temporales se realiza en la
raíz del directorio de almacenamiento externo, sería conveniente
utilizar una carpeta apropiada.

- La lista de elementos se recarga cada vez que se llama a onCreate() en
la aplicación, es decir tanto si la aplicación vuelve de Background o
cuando se cambia la orientación del teléfono, sería conveniente guardar
el estado, forzar la no actualización, o en el peor caso, forzar la
orientación Portrait mediante setRequestedOrientation. De momento se ha
decidido dejar tal cual está.

- Se ha trabajado con y sin la muestra de "Autor" en la lista, al final
se ha quitado para trabajar con la orientación y no se ha vuelto a
añadir por lo que en el código se encontrarán referencias a este
parámetro.

- A pesar de que cumple con las funcionalidades pedidas, falta testeo y
prueba en varias cuentas, que no ha sido posible debido a la falta de
tiempo.
  
