package logicadenegocios;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Clase encargada de crear directorios en el sistema de archivos y en un árbol de directorios en la interfaz de usuario.
 * 
 * @author Owen y Elian
 * @version 1.0
 */
public class Creador {

  /**
   * Solicita al usuario un nombre para el nuevo directorio y lo valida.
   * 
   * Este método verifica que el nombre del directorio cumpla con los requisitos: no estar vacío, no exceder
   * los 64 caracteres, y solo contener letras, números y guiones bajos. Luego intenta crear el directorio en
   * la ruta especificada.
   * 
   * @param pRutaDestino El directorio de destino donde se creará el nuevo directorio.
   * @return El nuevo directorio creado, o null si no se pudo crear.
   */
  public static File crearDirectorio(File pRutaDestino) {
    String nombreDirectorio = JOptionPane.showInputDialog("Introduce el nombre del nuevo directorio (máximo 64 caracteres, sin caracteres especiales):");

    if (nombreDirectorio == null || nombreDirectorio.isEmpty()) {
      JOptionPane.showMessageDialog(null, "El nombre no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
      return null;
    }

    if (nombreDirectorio.length() > 64) {
      JOptionPane.showMessageDialog(null, "El nombre del directorio no puede exceder los 64 caracteres.", "Error", JOptionPane.ERROR_MESSAGE);
      return null;
    }

    if (!nombreDirectorio.matches("[a-zA-Z0-9_]+")) {
      JOptionPane.showMessageDialog(null, "El nombre solo puede contener letras, números y guiones bajos.", "Error", JOptionPane.ERROR_MESSAGE);
      return null;
    }

    File nuevoDirectorio = new File(pRutaDestino, nombreDirectorio);

    if (nuevoDirectorio.exists()) {
      JOptionPane.showMessageDialog(null, "El directorio ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
      return null;
    }

    if (!pRutaDestino.canWrite()) {
      JOptionPane.showMessageDialog(null, "No tienes permisos de escritura en el directorio de destino.", "Error", JOptionPane.ERROR_MESSAGE);
      return null;
    }

    try {
      boolean creado = nuevoDirectorio.mkdirs();  // Usamos mkdirs() para crear directorios y subdirectorios si es necesario

      if (creado) {
        JOptionPane.showMessageDialog(null, "Directorio creado con éxito en: " + nuevoDirectorio.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
        return nuevoDirectorio;
      } else {
        JOptionPane.showMessageDialog(null, "No se pudo crear el directorio.", "Error", JOptionPane.ERROR_MESSAGE);
        return null;
      }

    } catch (SecurityException ex) {
      JOptionPane.showMessageDialog(null, "Error de permisos al intentar crear el directorio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      return null;
    }
  }

  /**
   * Crea un nuevo directorio desde un nodo seleccionado en un árbol de directorios.
   * 
   * Solicita al usuario un nombre para el nuevo directorio, lo valida y lo agrega al árbol de directorios en
   * la interfaz de usuario. El nuevo directorio se añade como un nodo hijo del nodo seleccionado.
   * 
   * @param pRutaSeleccionada La ruta del nodo seleccionado en el árbol de directorios.
   * @param pModeloArbol El modelo del árbol que se actualizará después de crear el nuevo directorio.
   */
  public static void crearDirectorioDesdeArbol(TreePath pRutaSeleccionada, DefaultTreeModel pModeloArbol) {
    if (pRutaSeleccionada != null) {
      DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) pRutaSeleccionada.getLastPathComponent();
      String nuevoNombre = JOptionPane.showInputDialog(null, "Nombre del nuevo directorio:", "Crear Directorio", JOptionPane.PLAIN_MESSAGE);

      if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
        DefaultMutableTreeNode nuevoNodo = new DefaultMutableTreeNode(nuevoNombre.trim());
        nodoSeleccionado.add(nuevoNodo);
        pModeloArbol.reload(nodoSeleccionado);
      }
    } else {
      JOptionPane.showMessageDialog(null, "Por favor, selecciona un nodo para crear un nuevo directorio.", "Error", JOptionPane.WARNING_MESSAGE);
    }
  }
}
