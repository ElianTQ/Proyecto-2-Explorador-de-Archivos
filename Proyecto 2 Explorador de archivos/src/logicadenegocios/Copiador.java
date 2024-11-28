package logicadenegocios;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.*;
import javax.swing.tree.TreePath;

/**
 * Clase encargada de realizar copias de archivos o directorios.
 * 
 * Esta clase proporciona funcionalidades para copiar archivos o directorios desde una ubicación
 * de origen a una ubicación de destino, y actualizar un árbol de directorios en la interfaz de usuario.
 * 
 * @author Owen y Elian
 * @version 1.0
 */
public class Copiador {

  /**
   * Copia un archivo o directorio de origen a destino.
   * 
   * Si el archivo de origen es un directorio, se realiza una copia recursiva de todo su contenido.
   * Si es un archivo, se copia directamente.
   * 
   * @param pArchivoOrigen El archivo o directorio de origen.
   * @param pArchivoDestino El archivo o directorio de destino.
   * @throws IOException Si ocurre un error durante la copia.
   */
  public static void copiarArchivo(File pArchivoOrigen, File pArchivoDestino) throws IOException {
    if (pArchivoOrigen.isDirectory()) {
      copiarDirectorio(pArchivoOrigen, pArchivoDestino);
    } else {
      Files.copy(pArchivoOrigen.toPath(), pArchivoDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Copia un directorio completo, incluyendo subdirectorios y archivos.
   * 
   * Si el directorio de destino no existe, se crea. Luego, se copian los archivos y subdirectorios
   * de manera recursiva desde el directorio de origen.
   * 
   * @param pDirectorioOrigen El directorio de origen.
   * @param pDirectorioDestino El directorio de destino.
   * @throws IOException Si ocurre un error durante la copia.
   */
  public static void copiarDirectorio(File pDirectorioOrigen, File pDirectorioDestino) throws IOException {
    if (!pDirectorioDestino.exists()) {
      pDirectorioDestino.mkdirs();
    }

    for (File archivo : pDirectorioOrigen.listFiles()) {
      File archivoDestino = new File(pDirectorioDestino, archivo.getName());
      if (archivo.isDirectory()) {
        copiarDirectorio(archivo, archivoDestino);  // Llamada recursiva para directorios
      } else {
        copiarArchivo(archivo, archivoDestino);  // Copiar el archivo
      }
    }
  }

  /**
   * Copia un directorio seleccionado desde el árbol de directorios a un nuevo destino.
   * 
   * Este método permite seleccionar un directorio desde un árbol en la interfaz de usuario, y luego
   * copiarlo a una nueva ubicación.
   * 
   * @param pNodoSeleccionado El nodo seleccionado en el árbol de directorios.
   * @param pModeloArbol El modelo del árbol que se actualizará después de realizar la copia.
   */
  public static void copiarDesdeNodo(DefaultMutableTreeNode pNodoSeleccionado, DefaultTreeModel pModeloArbol) {
    if (pNodoSeleccionado == null) {
      JOptionPane.showMessageDialog(null, "No se seleccionó ningún nodo.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    File archivoOrigen = obtenerRutaDesdeNodo(pNodoSeleccionado);

    if (archivoOrigen == null || !archivoOrigen.exists()) {
      JOptionPane.showMessageDialog(null, "El archivo o directorio no existe.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    if (!archivoOrigen.isDirectory()) {
      JOptionPane.showMessageDialog(null, "Solo se pueden copiar directorios.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    JFileChooser selectorDestino = new JFileChooser();
    selectorDestino.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int resultado = selectorDestino.showSaveDialog(null); // Mostrar el diálogo para seleccionar el destino

    if (resultado == JFileChooser.APPROVE_OPTION) {
      File directorioDestino = selectorDestino.getSelectedFile();

      String nombreCopia = JOptionPane.showInputDialog("Introduce el nombre para la copia del directorio:");

      if (nombreCopia == null || nombreCopia.trim().isEmpty()) {
        JOptionPane.showMessageDialog(null, "El nombre del directorio no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      File directorioDestinoFinal = new File(directorioDestino, nombreCopia.trim());

      if (directorioDestinoFinal.exists()) {
        JOptionPane.showMessageDialog(null, "Ya existe un directorio con este nombre en el destino.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      try {
        copiarDirectorio(archivoOrigen, directorioDestinoFinal); // Función para copiar el directorio

        DefaultMutableTreeNode nuevoNodo = new DefaultMutableTreeNode(directorioDestinoFinal.getName());

        DefaultMutableTreeNode nodoPadreDestino = (DefaultMutableTreeNode) pNodoSeleccionado.getParent();
        if (nodoPadreDestino != null) {
          nodoPadreDestino.add(nuevoNodo);
          pModeloArbol.reload(nodoPadreDestino);
        }

        JOptionPane.showMessageDialog(null, "El directorio ha sido copiado con éxito.");
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, "Error al copiar el directorio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    } else {
      JOptionPane.showMessageDialog(null, "No se seleccionó un directorio de destino.", "Advertencia", JOptionPane.WARNING_MESSAGE);
    }
  }

  /**
   * Obtiene la ruta de un archivo o directorio a partir de un nodo del árbol.
   * 
   * Este método convierte el camino del nodo en una ruta de archivo que puede ser utilizada para
   * acceder al archivo o directorio en el sistema de archivos.
   * 
   * @param pNodo El nodo del árbol que representa el archivo o directorio.
   * @return La ruta del archivo o directorio correspondiente al nodo.
   */
  private static File obtenerRutaDesdeNodo(DefaultMutableTreeNode pNodo) {
    StringBuilder ruta = new StringBuilder();
    Object[] camino = pNodo.getUserObjectPath();
    for (Object componente : camino) {
      ruta.append(componente.toString()).append(File.separator);
    }
    return new File(ruta.toString());
  }
}
