package logicadenegocios;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Clase encargada de eliminar archivos y directorios en el sistema de archivos y en un árbol de directorios en la interfaz de usuario.
 * 
 * @author Owen y Elian
 * @version 1.0
 */
public class Eliminador {

  /**
   * Muestra un cuadro de confirmación antes de eliminar un archivo o directorio.
   * 
   * @param pArchivo El archivo o directorio a eliminar.
   * @return true si el usuario confirma la eliminación, false si la cancela.
   */
  public static boolean confirmarEliminacion(File pArchivo) {
    int confirmacion = JOptionPane.showConfirmDialog(
      null,
      "¿Estás seguro de que deseas eliminar " + pArchivo.getName() + "?",
      "Confirmación de eliminación",
      JOptionPane.YES_NO_OPTION,
      JOptionPane.WARNING_MESSAGE
    );
    return confirmacion == JOptionPane.YES_OPTION;
  }

  /**
   * Elimina un archivo o directorio, lanzando una excepción si no se puede eliminar.
   * 
   * @param pArchivo El archivo o directorio a eliminar.
   * @throws IOException Si ocurre un error durante la eliminación.
   */
  public static void eliminarArchivo(File pArchivo) throws IOException {
    if (pArchivo.isDirectory()) {
      eliminarDirectorio(pArchivo);
    } else if (!pArchivo.delete()) {
      throw new IOException("No se pudo eliminar el archivo: " + pArchivo.getPath());
    }
  }

  /**
   * Elimina un directorio completo, incluyendo sus subdirectorios y archivos.
   * 
   * @param pDirectorio El directorio a eliminar.
   * @throws IOException Si ocurre un error durante la eliminación de un archivo o directorio.
   */
  private static void eliminarDirectorio(File pDirectorio) throws IOException {
    File[] archivos = pDirectorio.listFiles();
    if (archivos != null) {
      for (File archivo : archivos) {
        if (archivo.isDirectory()) {
          eliminarDirectorio(archivo);
        } else if (!archivo.delete()) {
          throw new IOException("No se pudo eliminar el archivo: " + archivo.getPath());
        }
      }
    }
    if (!pDirectorio.delete()) {
      throw new IOException("No se pudo eliminar el directorio: " + pDirectorio.getPath());
    }
  }

  /**
   * Elimina un archivo o directorio desde un nodo seleccionado en el árbol de directorios.
   * 
   * @param pNodoSeleccionado El nodo seleccionado que representa el archivo o directorio a eliminar.
   * @param pModeloArbol El modelo del árbol de directorios que se actualizará después de la eliminación.
   */
  public static void eliminarDesdeNodo(DefaultMutableTreeNode pNodoSeleccionado, DefaultTreeModel pModeloArbol) {
    if (pNodoSeleccionado == null || pNodoSeleccionado.getParent() == null) {
      JOptionPane.showMessageDialog(null, "No se puede eliminar el nodo raíz.", "Advertencia", JOptionPane.WARNING_MESSAGE);
      return;
    }

    File archivoEliminar = obtenerRutaDesdeNodo(pNodoSeleccionado);

    if (archivoEliminar == null || !archivoEliminar.exists()) {
      JOptionPane.showMessageDialog(null, "El archivo o directorio no existe o no es válido.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    int confirmacion = JOptionPane.showConfirmDialog(
      null,
      "¿Estás seguro de que deseas eliminar " + archivoEliminar.getName() + "?",
      "Confirmación de eliminación",
      JOptionPane.YES_NO_OPTION,
      JOptionPane.WARNING_MESSAGE
    );

    if (confirmacion == JOptionPane.YES_OPTION) {
      try {
        eliminarArchivo(archivoEliminar);
        DefaultMutableTreeNode nodoPadre = (DefaultMutableTreeNode) pNodoSeleccionado.getParent();
        nodoPadre.remove(pNodoSeleccionado);
        pModeloArbol.reload(nodoPadre);

        JOptionPane.showMessageDialog(null, "El archivo o directorio ha sido eliminado correctamente.");
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, "Error al eliminar el archivo o directorio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    } else {
      JOptionPane.showMessageDialog(null, "Operación de eliminación cancelada.");
    }
  }

  /**
   * Obtiene un objeto File representando la ruta completa desde un nodo del árbol.
   * 
   * @param pNodo El nodo del árbol que representa un archivo o directorio.
   * @return El objeto File que representa la ruta completa del archivo o directorio.
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
