package GUI;

import java.io.File;

/**
 * Representa un nodo que contiene un archivo en el sistema de archivos.
 * Proporciona métodos para acceder al archivo y mostrar su nombre en formato legible.
 */
public class NodoArchivo {

  private final File archivo;

  /**
   * Constructor para crear un nodo con el archivo proporcionado.
   * 
   * @param pArchivo El archivo que representa este nodo.
   */
  public NodoArchivo(File pArchivo) {
    this.archivo = pArchivo;
  }

  /**
   * Obtiene el archivo asociado con este nodo.
   * 
   * @return El archivo asociado a este nodo.
   */
  public File getArchivo() {
    return archivo;
  }

  @Override
  public String toString() {
    return archivo.getName().isEmpty() ? archivo.getAbsolutePath() : archivo.getName();
  }
}
