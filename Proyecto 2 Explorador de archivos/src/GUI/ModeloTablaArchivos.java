package GUI;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Modelo de tabla para mostrar archivos en un JTable.
 * Este modelo gestiona una lista de archivos, proporcionando el acceso a la información 
 * relacionada con cada archivo o directorio.
 */
public class ModeloTablaArchivos extends AbstractTableModel {

  private List<File> pArchivos = new ArrayList<>();
  private final String[] pNombresColumnas = {"Icono", "Nombre", "Tamaño", "Tipo", "Fecha de creación"};
  private final Icon pIconoCarpeta = UIManager.getIcon("FileView.directoryIcon");
  private final Icon pIconoArchivo = UIManager.getIcon("FileView.fileIcon");

  /**
   * Establece la lista de archivos a mostrar en la tabla.
   * 
   * @param pArchivos Lista de archivos a mostrar en la tabla.
   */
  public void setArchivos(List<File> pArchivos) {
    this.pArchivos = pArchivos;
    ordenarArchivos();
    fireTableDataChanged();
  }

  /**
   * Obtiene el archivo en una fila específica.
   * 
   * @param pFila La fila de la tabla.
   * @return El archivo en la fila indicada.
   */
  public File getArchivoEn(int pFila) {
    if (pArchivos == null || pFila < 0 || pFila >= pArchivos.size()) {
      return null;
    }
    return pArchivos.get(pFila);
  }

  @Override
  public int getRowCount() {
    return pArchivos == null ? 0 : pArchivos.size();
  }

  @Override
  public int getColumnCount() {
    return pNombresColumnas.length;
  }

  /**
   * Obtiene el valor de una celda específica de la tabla.
   * 
   * @param pFila La fila de la tabla.
   * @param pColumna La columna de la tabla.
   * @return El valor de la celda.
   */
  @Override
  public Object getValueAt(int pFila, int pColumna) {
    if (pArchivos == null || pFila < 0 || pFila >= pArchivos.size()) {
      return null;
    }

    File pArchivo = pArchivos.get(pFila);
    if (pArchivo == null) {
      return null;
    }

    switch (pColumna) {
      case 0:
        return pArchivo.isDirectory() ? pIconoCarpeta : pIconoArchivo;
      case 1:
        return pArchivo.getName();
      case 2:
        return pArchivo.isDirectory() ? "" : convertirTamaño(pArchivo.length());
      case 3:
        return obtenerTipo(pArchivo);
      case 4:
        return obtenerFechaCreacion(pArchivo);
      default:
        return null;
    }
  }

  /**
   * Convierte el tamaño de un archivo de bytes a una unidad más legible.
   * 
   * @param pTamañoBytes El tamaño del archivo en bytes.
   * @return El tamaño convertido a una unidad legible.
   */
  private String convertirTamaño(long pTamañoBytes) {
    if (pTamañoBytes < 1024) return pTamañoBytes + " B";
    int exp = (int) (Math.log(pTamañoBytes) / Math.log(1024));
    String pre = "KMGTPE".charAt(exp - 1) + "B";
    return String.format("%.1f %s", pTamañoBytes / Math.pow(1024, exp), pre);
  }

  /**
   * Obtiene el tipo de archivo (Carpeta o extensión de archivo).
   * 
   * @param pArchivo El archivo cuyo tipo se desea conocer.
   * @return El tipo de archivo (Carpeta o extensión de archivo).
   */
  private String obtenerTipo(File pArchivo) {
    if (pArchivo.isDirectory()) {
      return "Carpeta";
    } else {
      String nombreArchivo = pArchivo.getName();
      int indicePunto = nombreArchivo.lastIndexOf('.');
      if (indicePunto > 0 && indicePunto < nombreArchivo.length() - 1) {
        return nombreArchivo.substring(indicePunto + 1).toUpperCase();
      } else {
        return "Archivo";
      }
    }
  }

  /**
   * Ordena los archivos, colocando primero los directorios y luego los archivos,
   * ordenados alfabéticamente.
   */
  private void ordenarArchivos() {
    if (pArchivos != null) {
      pArchivos.sort((a, b) -> {
        if (a.isDirectory() && !b.isDirectory()) {
          return -1;
        } else if (!a.isDirectory() && b.isDirectory()) {
          return 1;
        } else {
          return a.getName().compareToIgnoreCase(b.getName());
        }
      });
    }
  }

  @Override
  public String getColumnName(int pColumna) {
    return pNombresColumnas[pColumna];
  }

  /**
   * Obtiene la fecha de creación de un archivo.
   * 
   * @param pArchivo El archivo cuya fecha de creación se desea conocer.
   * @return La fecha de creación del archivo en formato "dd/MM/yyyy HH:mm:ss".
   */
  private String obtenerFechaCreacion(File pArchivo) {
    try {
      BasicFileAttributes pAtributos = Files.readAttributes(pArchivo.toPath(), BasicFileAttributes.class);
      Date pFechaCreacion = new Date(pAtributos.creationTime().toMillis());
      return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(pFechaCreacion);
    } catch (Exception e) {
      return "N/A";
    }
  }

  /**
   * Establece los íconos en la columna correspondiente de la tabla.
   * Este método ajusta la visualización de los íconos en la primera columna de la tabla.
   * 
   * @param pTabla La tabla en la que se deben establecer los íconos.
   */
  public void setIconosEnTabla(JTable pTabla) {
    pTabla.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
      @Override
      public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = new JLabel();

        if (value instanceof Icon) {
          label.setIcon((Icon) value);
        } else {
          label.setIcon(null);
        }

        label.setHorizontalAlignment(SwingConstants.CENTER);
        if (isSelected) {
          label.setBackground(table.getSelectionBackground());
          label.setForeground(table.getSelectionForeground());
          label.setOpaque(true);
        } else {
          label.setBackground(table.getBackground());
          label.setForeground(table.getForeground());
        }

        return label;
      }
    });
  }
}
