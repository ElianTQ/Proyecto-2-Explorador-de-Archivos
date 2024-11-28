package logicadenegocios;

import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.io.IOException;

/**
 * Clase que proporciona funcionalidad para mover archivos o directorios 
 * dentro del sistema de archivos, incluyendo opciones de selección 
 * de destino y nombre para los elementos movidos.
 * 
 * @author Owen y Elian
 */
public class Movedor {

  /**
   * Mueve un archivo o directorio de la ruta de origen a la ruta de destino.
   * 
   * El usuario puede proporcionar un nuevo nombre para el archivo o directorio
   * en el destino, y el método asegura que no se sobrescriba un archivo o directorio
   * existente en el destino.
   * 
   * @param pOrigen El archivo o directorio de origen.
   * @param pDestino El directorio de destino donde se moverá el archivo o directorio.
   */
  public static void moverArchivo(File pOrigen, File pDestino) {
    if (pOrigen == null || !pOrigen.exists()) {
      JOptionPane.showMessageDialog(null, "El archivo o directorio de origen no existe.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    if (pDestino == null) {
      JOptionPane.showMessageDialog(null, "No se seleccionó un destino válido.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    String nombreArchivo = pOrigen.getName();
    String extension = "";
    int index = nombreArchivo.lastIndexOf(".");
    if (index > 0) {
      extension = nombreArchivo.substring(index);
      nombreArchivo = nombreArchivo.substring(0, index);
    }

    String nuevoNombre = JOptionPane.showInputDialog(null, "Introduce el nuevo nombre del archivo (mantendrá su extensión):", nombreArchivo);

    if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
      nuevoNombre = nombreArchivo;
    }

    String nombreFinal = nuevoNombre.trim() + extension;
    File destinoFinal = new File(pDestino, nombreFinal);

    if (destinoFinal.exists()) {
      JOptionPane.showMessageDialog(null, "Ya existe un archivo o directorio con este nombre en el destino.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      boolean exito = pOrigen.renameTo(destinoFinal);
      if (exito) {
        JOptionPane.showMessageDialog(null, "El archivo o directorio ha sido movido con éxito.");
      } else {
        throw new IOException("No se pudo mover el archivo o directorio. Verifica permisos o el estado del sistema.");
      }
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(null, "Error al mover el archivo o directorio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Mueve un archivo o directorio desde un nodo seleccionado en un árbol de directorios
   * a una ubicación de destino elegida por el usuario. Actualiza el árbol con el nuevo
   * directorio si el objeto movido es un directorio.
   * 
   * @param pNodoSeleccionado El nodo seleccionado en el árbol de directorios que contiene el archivo o directorio a mover.
   * @param pModeloArbol El modelo de árbol que se actualizará después de mover el archivo o directorio.
   */
  public static void moverDesdeNodo(DefaultMutableTreeNode pNodoSeleccionado, DefaultTreeModel pModeloArbol) {
    if (pNodoSeleccionado == null) {
      JOptionPane.showMessageDialog(null, "No se seleccionó ningún nodo.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    File archivoOrigen = obtenerRutaDesdeNodo(pNodoSeleccionado);

    if (archivoOrigen == null || !archivoOrigen.exists()) {
      JOptionPane.showMessageDialog(null, "El archivo o directorio no existe.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    JFileChooser selectorDestino = new JFileChooser();
    selectorDestino.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int resultado = selectorDestino.showSaveDialog(null);

    if (resultado == JFileChooser.APPROVE_OPTION) {
      File directorioDestino = selectorDestino.getSelectedFile();
      moverArchivo(archivoOrigen, directorioDestino);

      if (archivoOrigen.isDirectory()) {
        DefaultMutableTreeNode nodoDestino = new DefaultMutableTreeNode(directorioDestino.getName());
        DefaultMutableTreeNode nodoPadreDestino = (DefaultMutableTreeNode) pNodoSeleccionado.getParent();
        if (nodoPadreDestino != null) {
          nodoPadreDestino.add(nodoDestino);
          pModeloArbol.reload(nodoPadreDestino);
        }
      }
    } else {
      JOptionPane.showMessageDialog(null, "No se seleccionó un directorio de destino.", "Advertencia", JOptionPane.WARNING_MESSAGE);
    }
  }

  /**
   * Método auxiliar para obtener un objeto File a partir de un nodo del árbol.
   * 
   * @param pNodo El nodo del árbol.
   * @return El archivo representado por el nodo en el árbol, o {@code null} si la ruta no es válida.
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