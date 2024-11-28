package logicadenegocios;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;

/**
 * Ventana de propiedades para mostrar información detallada sobre un archivo o directorio.
 * La información mostrada incluye nombre, extensión, tamaño, ubicación, fecha de creación, creador y atributos.
 * 
 * @author Owen y Elian
 */
public class Propiedades extends JDialog {

  /**
   * Constructor para abrir la ventana de propiedades de un archivo.
   * 
   * @param pOwner El dueño de la ventana (el frame principal).
   * @param pArchivo El archivo sobre el que se mostrarán las propiedades.
   */
  public Propiedades(Frame pOwner, File pArchivo) {
    super(pOwner, "Propiedades de " + pArchivo.getName(), true);
    setSize(600, 400);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(pOwner);
    crearPropiedades(pArchivo);
  }

  /**
   * Constructor para abrir la ventana de propiedades a partir de un nodo seleccionado en el árbol.
   * 
   * @param pOwner El dueño de la ventana (el frame principal).
   * @param pNodo El nodo de árbol que representa el archivo o directorio sobre el que se mostrarán las propiedades.
   */
  public Propiedades(Frame pOwner, DefaultMutableTreeNode pNodo) {
    super(pOwner, "Propiedades del nodo seleccionado", true);
    setSize(600, 400);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(pOwner);

    File archivo = obtenerRutaDesdeNodo(pNodo);

    if (archivo == null || !archivo.exists()) {
      JOptionPane.showMessageDialog(pOwner,
        "El archivo o directorio representado por el nodo no existe.",
        "Error", JOptionPane.ERROR_MESSAGE);
      dispose();
    } else {
      crearPropiedades(archivo);
    }
  }

  /**
   * Método para crear la interfaz de usuario con las propiedades del archivo o directorio.
   * 
   * @param pArchivo El archivo o directorio del que se mostrarán las propiedades.
   */
  private void crearPropiedades(File pArchivo) {
    JPanel panelTitulos = new JPanel(new GridLayout(7, 1, 5, 5));
    panelTitulos.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    panelTitulos.add(new JLabel("Nombre:"));
    panelTitulos.add(new JLabel("Extensión:"));
    panelTitulos.add(new JLabel("Tamaño:"));
    panelTitulos.add(new JLabel("Ubicación:"));
    panelTitulos.add(new JLabel("Fecha de creación:"));
    panelTitulos.add(new JLabel("Creador:"));
    panelTitulos.add(new JLabel("Atributos:"));

    JPanel panelValores = new JPanel(new GridLayout(7, 1, 5, 5));
    panelValores.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

    String nombreArchivo = pArchivo.getName();
    if (nombreArchivo.isEmpty()) {
      nombreArchivo = pArchivo.getAbsolutePath();
    }
    panelValores.add(new JLabel(nombreArchivo));

    String extension = pArchivo.isFile() && pArchivo.getName().contains(".")
      ? pArchivo.getName().substring(pArchivo.getName().lastIndexOf('.') + 1)
      : "N/A";
    panelValores.add(new JLabel(extension));

    if (esUnidadDeAlmacenamiento(pArchivo)) {
      panelValores.add(crearBarraCapacidad(pArchivo));
    } else {
      panelValores.add(new JLabel(convertirTamaño(pArchivo)));
    }

    JTextArea areaRuta = new JTextArea(pArchivo.getAbsolutePath());
    areaRuta.setWrapStyleWord(true);
    areaRuta.setLineWrap(true);
    areaRuta.setEditable(false);
    JScrollPane scrollRuta = new JScrollPane(areaRuta, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    panelValores.add(scrollRuta);

    panelValores.add(new JLabel(obtenerFechaCreacion(pArchivo)));
    panelValores.add(new JLabel(obtenerCreador(pArchivo)));
    panelValores.add(new JLabel(obtenerAtributos(pArchivo)));

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelTitulos, panelValores);
    splitPane.setResizeWeight(0.2);
    splitPane.setDividerSize(15);
    add(splitPane, BorderLayout.CENTER);

    JButton botonCerrar = new JButton("Cerrar");
    botonCerrar.addActionListener(e -> dispose());
    JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panelBoton.add(botonCerrar);
    add(panelBoton, BorderLayout.SOUTH);

    setVisible(true);
  }

  /**
   * Crea una barra de progreso que muestra el uso de la capacidad de una unidad de almacenamiento.
   * 
   * @param pUnidad La unidad de almacenamiento (por ejemplo, un disco duro o memoria).
   * @return Un panel que contiene la barra de progreso.
   */
  private JPanel crearBarraCapacidad(File pUnidad) {
    long totalSpace = pUnidad.getTotalSpace();
    long freeSpace = pUnidad.getFreeSpace();
    long usedSpace = totalSpace - freeSpace;

    int porcentajeUsado = (int) ((usedSpace * 100) / totalSpace);

    JProgressBar barra = new JProgressBar(0, 100);
    barra.setValue(porcentajeUsado);
    barra.setStringPainted(true);
    barra.setString(String.format("Usado: %s / Total: %s",
      convertirTamañoBytes(usedSpace), convertirTamañoBytes(totalSpace)));

    JPanel panelBarra = new JPanel(new BorderLayout());
    panelBarra.add(new JLabel("Espacio de almacenamiento:"), BorderLayout.NORTH);
    panelBarra.add(barra, BorderLayout.CENTER);

    return panelBarra;
  }

  /**
   * Verifica si un archivo es una unidad de almacenamiento.
   * 
   * @param pArchivo El archivo a verificar.
   * @return Verdadero si el archivo es una unidad de almacenamiento, falso en caso contrario.
   */
  private boolean esUnidadDeAlmacenamiento(File pArchivo) {
    return pArchivo.isDirectory() && pArchivo.getParent() == null;
  }

  /**
   * Obtiene la ruta del archivo a partir de un nodo en el árbol.
   * 
   * @param pNodo El nodo del árbol.
   * @return El archivo correspondiente al nodo del árbol.
   */
  private File obtenerRutaDesdeNodo(DefaultMutableTreeNode pNodo) {
    if (pNodo == null) return null;

    StringBuilder ruta = new StringBuilder();
    Object[] camino = pNodo.getUserObjectPath();
    for (Object componente : camino) {
      ruta.append(componente.toString()).append(File.separator);
    }
    return new File(ruta.toString());
  }

  /**
   * Convierte el tamaño de un archivo o directorio en una cadena legible (B, KB, MB, etc.).
   * 
   * @param pArchivo El archivo o directorio cuyo tamaño se desea obtener.
   * @return El tamaño del archivo o directorio como una cadena legible.
   */
  private String convertirTamaño(File pArchivo) {
    if (pArchivo.isDirectory()) {
      return calcularTamañoDirectorio(pArchivo);
    }
    return convertirTamañoBytes(pArchivo.length());
  }

  /**
   * Calcula el tamaño total de un directorio de manera recursiva.
   * 
   * @param pDirectorio El directorio cuyo tamaño se desea calcular.
   * @return El tamaño total del directorio en una cadena legible.
   */
  private String calcularTamañoDirectorio(File pDirectorio) {
    long tamañoTotal = calcularTamañoRecursivo(pDirectorio);
    return convertirTamañoBytes(tamañoTotal);
  }

  /**
   * Calcula el tamaño total de un archivo o directorio de manera recursiva.
   * 
   * @param pArchivo El archivo o directorio cuyo tamaño se desea calcular.
   * @return El tamaño total en bytes.
   */
  private long calcularTamañoRecursivo(File pArchivo) {
    if (pArchivo == null || !pArchivo.exists()) return 0;

    if (pArchivo.isFile()) return pArchivo.length();

    File[] archivos = pArchivo.listFiles();
    long tamaño = 0;
    if (archivos != null) {
      for (File f : archivos) {
        tamaño += calcularTamañoRecursivo(f);
      }
    }
    return tamaño;
  }

  /**
   * Convierte el tamaño de un archivo en bytes a una cadena legible (B, KB, MB, etc.).
   * 
   * @param pTamañoBytes El tamaño en bytes.
   * @return El tamaño en una cadena legible.
   */
  private String convertirTamañoBytes(long pTamañoBytes) {
    if (pTamañoBytes < 1024) return pTamañoBytes + " B";
    int exp = (int) (Math.log(pTamañoBytes) / Math.log(1024));
    String pre = "KMGTPE".charAt(exp - 1) + "B";
    return String.format("%.1f %s", pTamañoBytes / Math.pow(1024, exp), pre);
  }

  /**
   * Obtiene la fecha de creación de un archivo.
   * 
   * @param pArchivo El archivo cuyo fecha de creación se desea obtener.
   * @return La fecha de creación en formato "dd/MM/yyyy HH:mm:ss", o "N/A" si no se puede obtener.
   */
  private String obtenerFechaCreacion(File pArchivo) {
    try {
      BasicFileAttributes atributos = Files.readAttributes(pArchivo.toPath(), BasicFileAttributes.class);
      return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(atributos.creationTime().toMillis());
    } catch (Exception e) {
      return "N/A";
    }
  }

  /**
   * Obtiene el creador de un archivo (en sistemas Windows).
   * 
   * @param pArchivo El archivo cuyo creador se desea obtener.
   * @return El creador del archivo o "N/A" si no se puede obtener.
   */
  private String obtenerCreador(File pArchivo) {
    try {
      ProcessBuilder builder = new ProcessBuilder("powershell",
        "-command", "(Get-Acl '" + pArchivo.getAbsolutePath() + "').Owner");
      Process process = builder.start();
      process.waitFor();
      String output = new String(process.getInputStream().readAllBytes()).trim();
      return output.isEmpty() ? "N/A" : output;
    } catch (Exception e) {
      return "N/A";
    }
  }

  /**
   * Obtiene los atributos de un archivo (lectura, escritura, ejecución).
   * 
   * @param pArchivo El archivo cuyo atributos se desean obtener.
   * @return Los atributos como una cadena.
   */
  private String obtenerAtributos(File pArchivo) {
    StringBuilder atributos = new StringBuilder();
    if (pArchivo.canRead()) atributos.append("Lectura, ");
    if (pArchivo.canWrite()) atributos.append("Escritura, ");
    if (!pArchivo.isDirectory() && pArchivo.canExecute()) {
      atributos.append("Ejecución, ");
    }
    if (atributos.length() > 2) {
      atributos.setLength(atributos.length() - 2);
    }
    return atributos.isEmpty() ? "Ninguno" : atributos.toString();
  }
}
