package logicadenegocios;

import GUI.ModeloTablaArchivos;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase encargada de buscar archivos en un directorio y actualizar una tabla con los resultados de la búsqueda.
 * 
 * Esta clase proporciona funcionalidades para buscar archivos por nombre dentro de un directorio y sus 
 * subdirectorios, y actualizar un modelo de tabla con los archivos encontrados.
 * 
 * @author Owen y Elian
 * @version 1.0
 */
public class Buscador {

  /**
   * Método para buscar archivos por nombre y actualizar la tabla con los resultados encontrados.
   * 
   * Este método busca recursivamente en el directorio base y sus subdirectorios, archivos que
   * coincidan con el criterio de búsqueda proporcionado. Los resultados se pasan al modelo de
   * la tabla para actualizar su visualización.
   *
   * @param pCriterioBusqueda El criterio de búsqueda proporcionado, generalmente una cadena de texto
   *                          que se comparará con los nombres de los archivos.
   * @param pDirectorioBase El directorio base donde se iniciará la búsqueda. Debe ser un directorio
   *                        válido.
   * @param pModeloTabla El modelo de tabla que contiene los archivos que serán mostrados en la UI.
   */
  public static void buscarArchivosPorNombre(String pCriterioBusqueda, File pDirectorioBase, ModeloTablaArchivos pModeloTabla) {
    if (pDirectorioBase == null || !pDirectorioBase.isDirectory()) {
      JOptionPane.showMessageDialog(null, "El directorio seleccionado no es válido.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    List<File> resultados = buscarRecursivamente(pDirectorioBase, pCriterioBusqueda);

    if (resultados.isEmpty()) {
      JOptionPane.showMessageDialog(null, "No se encontraron archivos que coincidan con el criterio de búsqueda.");
      pModeloTabla.setArchivos(new ArrayList<>()); // Limpiar la tabla
    } else {
      pModeloTabla.setArchivos(resultados); // Actualiza la tabla con los resultados encontrados
    }
  }

  /**
   * Método recursivo para buscar archivos en un directorio y sus subdirectorios.
   * 
   * Este método recorre recursivamente todos los directorios dentro del directorio base dado, 
   * y agrega a la lista los archivos cuyo nombre contiene el criterio de búsqueda proporcionado.
   *
   * @param pDirectorio El directorio base en el que se iniciará la búsqueda.
   * @param pCriterio El nombre o parte del nombre del archivo a buscar. La búsqueda no distingue
   *                  entre mayúsculas y minúsculas.
   * @return Una lista de archivos que coinciden con el criterio de búsqueda.
   */
  private static List<File> buscarRecursivamente(File pDirectorio, String pCriterio) {
    List<File> resultados = new ArrayList<>();

    File[] archivos = pDirectorio.listFiles();
    if (archivos != null) {
      for (File archivo : archivos) {
        if (archivo.isDirectory()) {
          resultados.addAll(buscarRecursivamente(archivo, pCriterio));
        } else {
          if (archivo.getName().toLowerCase().contains(pCriterio.toLowerCase())) {
            resultados.add(archivo); // Agrega el archivo a los resultados
          }
        }
      }
    }

    return resultados; // Devuelve la lista de archivos encontrados
  }
}
