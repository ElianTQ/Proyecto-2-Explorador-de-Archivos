package GUI;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * Renderizador personalizado para los nodos de un JTree que asigna un icono adecuado
 * según si el nodo representa un archivo o un directorio.
 * 
 * @author Owen y Elian
 */
public class IconoRenderer extends DefaultTreeCellRenderer {

  private final Icon iconoCarpeta = UIManager.getIcon("FileView.directoryIcon");
  private final Icon iconoArchivo = UIManager.getIcon("FileView.fileIcon");

  /**
   * Asigna un icono adecuado a cada nodo del árbol dependiendo de si es un archivo
   * o un directorio.
   * 
   * @param tree El JTree que contiene los nodos.
   * @param value El valor del nodo que se está renderizando.
   * @param sel Indica si el nodo está seleccionado.
   * @param expanded Indica si el nodo está expandido.
   * @param leaf Indica si el nodo es una hoja (no tiene hijos).
   * @param row La fila en la que el nodo se encuentra.
   * @param hasFocus Indica si el nodo tiene el foco.
   * @return El componente que se usará para renderizar el nodo en el JTree.
   */
  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component componente = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
    if (nodo.getUserObject() instanceof NodoArchivo) {
      NodoArchivo nodoArchivo = (NodoArchivo) nodo.getUserObject();
      setIcon(nodoArchivo.getArchivo().isDirectory() ? iconoCarpeta : iconoArchivo);
    }
    return componente;
  }
}
