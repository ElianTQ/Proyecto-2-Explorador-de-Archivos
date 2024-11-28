// Ruta: GUI/MenuPrincipal.java
package Controlador;

import GUI.*;
import logicadenegocios.*;


import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList; 
import java.util.Arrays;
import javax.swing.table.DefaultTableCellRenderer;
import java.util.Stack;

/**
 * Clase principal para el explorador de archivos.
 * Proporciona una interfaz gráfica con un árbol de directorios y una tabla para visualizar archivos.
 */
  

public class MenuPrincipal extends JFrame {

  private JTree arbolDirectorios;
  private JTable tablaArchivos;
  private DefaultTreeModel modeloArbol;
  private ModeloTablaArchivos modeloTablaArchivos;
  private Stack<File> historialAtras = new Stack<>();
  private Stack<File> historialAdelante = new Stack<>();
  private File directorioActual;  // El directorio actual


 /**
  * Constructor de la clase MenuPrincipal.
  * Configura los componentes gráficos de la ventana principal y la funcionalidad básica del explorador.
  */
  public MenuPrincipal() {
    setTitle("Explorador de Archivos");
    setSize(800, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    
    directorioActual = new File("C:\\");  // Guardamos el directorio inicial en el historial
    historialAtras.push(directorioActual);
    
    NodoArchivo nodoRaizArchivo = new NodoArchivo(new File("/"));
    DefaultMutableTreeNode raiz = new DefaultMutableTreeNode(nodoRaizArchivo);
    modeloArbol = new DefaultTreeModel(raiz);
    arbolDirectorios = new JTree(modeloArbol);
    arbolDirectorios.setShowsRootHandles(true);
    arbolDirectorios.setCellRenderer(new IconoRenderer());

    for (File unidad : File.listRoots()) {
      DefaultMutableTreeNode nodoUnidad = new DefaultMutableTreeNode(new NodoArchivo(unidad));
      raiz.add(nodoUnidad);
      nodoUnidad.add(new DefaultMutableTreeNode("Cargando..."));
    }

    arbolDirectorios.addTreeWillExpandListener(new TreeWillExpandListener() {
      /**
      * Evento que se dispara antes de que un nodo del árbol se expanda.
      *
      * @param event Evento de expansión del árbol.
      * @throws ExpandVetoException si ocurre un error al expandir el nodo.
      */     
      @Override
      public void treeWillExpand(javax.swing.event.TreeExpansionEvent event) throws ExpandVetoException {
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
        NodoArchivo nodoArchivo = (NodoArchivo) nodo.getUserObject();
        cargarSubdirectorios(nodo, nodoArchivo.getArchivo());
      }
      
      /**
      * Evento que se dispara antes de que un nodo del árbol se colapse. no hace nada
      * porque no es necesario a la hora de colapsarlo.
      *
      * @param event Evento de colapso del árbol.
      * @throws ExpandVetoException si ocurre un error al colapsar el nodo.
      */
      @Override
      public void treeWillCollapse(javax.swing.event.TreeExpansionEvent event) throws ExpandVetoException {
      }
    });

    arbolDirectorios.addTreeSelectionListener(new TreeSelectionListener() {
      /**
      * Evento que se dispara al seleccionar un nodo en el árbol.
      *
      * @param e Evento de selección del árbol.
      */
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) arbolDirectorios.getLastSelectedPathComponent();
        if (nodoSeleccionado != null) {
          NodoArchivo infoNodo = (NodoArchivo) nodoSeleccionado.getUserObject();
          mostrarContenidoDirectorio(infoNodo.getArchivo());
        }
      }
    });

    modeloTablaArchivos  = new ModeloTablaArchivos();
    tablaArchivos = new JTable(modeloTablaArchivos);
    tablaArchivos.setFillsViewportHeight(true);
    modeloTablaArchivos.setIconosEnTabla(tablaArchivos);

    tablaArchivos.setShowHorizontalLines(true);
    tablaArchivos.setShowVerticalLines(false);
    tablaArchivos.setIntercellSpacing(new Dimension(0, 1));

    tablaArchivos.setFont(new Font("SansSerif", Font.PLAIN, 14));
    tablaArchivos.setRowHeight(25);
    tablaArchivos.setSelectionBackground(new Color(200, 200, 255));
    tablaArchivos.setSelectionForeground(Color.BLACK);
    tablaArchivos.setBackground(Color.WHITE);  //

  // Ajustar anchos de columna (opcional)
    tablaArchivos.getColumnModel().getColumn(0).setPreferredWidth(60);  // Ícono
    tablaArchivos.getColumnModel().getColumn(1).setPreferredWidth(300); // Nombre
    tablaArchivos.getColumnModel().getColumn(2).setPreferredWidth(100); // Tamaño
    tablaArchivos.getColumnModel().getColumn(3).setPreferredWidth(150); // Tipo
    tablaArchivos.getColumnModel().getColumn(4).setPreferredWidth(150); // Fecha de creación


    // Integración para centrar el contenido de todas las celdas
    DefaultTableCellRenderer renderizadorCentrado = new DefaultTableCellRenderer();
    renderizadorCentrado.setHorizontalAlignment(SwingConstants.CENTER);

    for (int i = 1; i < tablaArchivos.getColumnCount(); i++) { // Empieza desde 1 para omitir la columna 0
        tablaArchivos.getColumnModel().getColumn(i).setCellRenderer(renderizadorCentrado);
    }

    tablaArchivos.addMouseListener(new MouseAdapter() {
      /**
      * Evento que se dispara al hacer clic en la tabla de archivos.
      * Si el usuario hace doble clic sobre un directorio, lo expande en el árbol.
      *
      * @param evt Evento de clic del ratón.
      */
      public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
          int fila = tablaArchivos.getSelectedRow();
          File archivo = modeloTablaArchivos.getArchivoEn(fila);
          if (archivo.isDirectory()) {
            TreePath ruta = encontrarRutaEnArbol(archivo);
            if (ruta != null) {
              arbolDirectorios.setSelectionPath(ruta);
              arbolDirectorios.expandPath(ruta);
            }
          }
        }
      }
    });

    JTextField campoBusqueda = new JTextField(20);

    JButton botonBuscarArchivos = new JButton("Buscar Archivos");
    botonBuscarArchivos.addActionListener(e -> {
      String criterio = campoBusqueda.getText().trim();
      if (criterio.isEmpty()) {
        JOptionPane.showMessageDialog(this, "El criterio de búsqueda no puede estar vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
      }

      DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) arbolDirectorios.getLastSelectedPathComponent();
      File directorioBase;

      if (nodoSeleccionado == null) {
        directorioBase = new File("C:\\");
      } else {
        NodoArchivo nodoArchivo = (NodoArchivo) nodoSeleccionado.getUserObject();
        directorioBase = nodoArchivo.getArchivo();
      }

      Buscador.buscarArchivosPorNombre(criterio, directorioBase, modeloTablaArchivos);
    });

    JSplitPane panelDividido = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(arbolDirectorios), new JScrollPane(tablaArchivos));
    panelDividido.setDividerLocation(300);

    JPanel panelBotones = new JPanel(new BorderLayout());

    JPanel panelBusqueda = new JPanel();
    panelBusqueda.add(new JLabel("Archivo a buscar:"));
    panelBusqueda.add(campoBusqueda);
    panelBusqueda.add(botonBuscarArchivos);
    
    JButton btnAtras = new JButton("Atrás");
      btnAtras.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        cargarDirectorioAnterior();  // Cargar el directorio anterior desde el historial de "Atrás"
      }
    });

    JButton btnAdelante = new JButton("Adelante");
    btnAdelante.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        cargarDirectorioAdelante();  // Cargar el siguiente directorio desde el historial de "Adelante"
      }
    });
    JPanel panelBotonesNavegacion = new JPanel();
    panelBotonesNavegacion.add(btnAtras);
    panelBotonesNavegacion.add(btnAdelante);
    panelBotones.add(panelBotonesNavegacion, BorderLayout.WEST);
    panelBotones.add(panelBusqueda, BorderLayout.EAST);

    JPanel panelPrincipal = new JPanel();
    panelPrincipal.setLayout(new BorderLayout());
    panelPrincipal.add(panelBotones, BorderLayout.NORTH);
    panelPrincipal.add(panelDividido, BorderLayout.CENTER);

    add(panelPrincipal);

    mostrarContenidoDirectorio(new File("C:\\"));

    agregarMenuContextual();
    inicializarArbol(arbolDirectorios, modeloArbol);
    agregarMenuContextualArbol(arbolDirectorios);
  }

  /**
   * Método para agregar un menú contextual a la tabla de archivos.
   * Este menú permite realizar varias acciones sobre los archivos seleccionados.
   */
  private void agregarMenuContextual() {
    JPopupMenu menuContextual = new JPopupMenu();

    JMenuItem abrirItem = new JMenuItem("Abrir");
    abrirItem.addActionListener(e -> abrirArchivoConAplicacionPadre());
    menuContextual.add(abrirItem);

    JMenuItem crearDirectorioItem = new JMenuItem("Crear Directorio");
    crearDirectorioItem.addActionListener(e -> crearDirectorio());
    menuContextual.add(crearDirectorioItem);

    JMenuItem copiarItem = new JMenuItem("Copiar");
    copiarItem.addActionListener(e -> copiarArchivoSeleccionado());
    menuContextual.add(copiarItem);

    JMenuItem eliminarItem = new JMenuItem("Eliminar");
    eliminarItem.addActionListener(e -> eliminarArchivoSeleccionado());
    menuContextual.add(eliminarItem);

    JMenuItem moverItem = new JMenuItem("Mover");
    moverItem.addActionListener(e -> moverArchivoSeleccionado());
    menuContextual.add(moverItem);

    JMenuItem propiedadesItem = new JMenuItem("Propiedades");
    propiedadesItem.addActionListener(e -> mostrarPropiedadesDelArchivo());
    menuContextual.add(propiedadesItem);

    tablaArchivos.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          mostrarMenuContextual(e, menuContextual, "tabla");
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          mostrarMenuContextual(e, menuContextual, "tabla");
        }
      }
    });
  }

    /**
   * Agrega un menú contextual a un JTree, permitiendo realizar operaciones como 
   * crear, copiar, eliminar, mover y ver las propiedades de directorios.
   *
   * @param pArbol El JTree sobre el que se agregará el menú contextual.
   */
  private void agregarMenuContextualArbol(JTree pArbol) {
    JPopupMenu menuContextualArbol = new JPopupMenu();

    JMenuItem crearDirectorioItem = new JMenuItem("Crear Directorio");
    crearDirectorioItem.addActionListener(e -> {
      TreePath rutaSeleccionada = pArbol.getSelectionPath();
      if (rutaSeleccionada != null) {
        DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) rutaSeleccionada.getLastPathComponent();
        NodoArchivo nodoArchivo = (NodoArchivo) nodoSeleccionado.getUserObject();
        File directorio = nodoArchivo.getArchivo();

        if (directorio.isDirectory()) {
          File nuevoDirectorio = Creador.crearDirectorio(directorio);
          if (nuevoDirectorio != null) {
            cargarSubdirectorios(nodoSeleccionado, directorio);
          }
        }
      }
    });
    menuContextualArbol.add(crearDirectorioItem);

    JMenuItem copiarItem = new JMenuItem("Copiar Directorio");
    copiarItem.addActionListener(e -> {
      TreePath rutaSeleccionada = pArbol.getSelectionPath();
      if (rutaSeleccionada != null) {
        DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) rutaSeleccionada.getLastPathComponent();
        Copiador.copiarDesdeNodo(nodoSeleccionado, modeloArbol);
      }
    });
    menuContextualArbol.add(copiarItem);

    JMenuItem eliminarItem = new JMenuItem("Eliminar Directorio");
    eliminarItem.addActionListener(e -> {
      TreePath rutaSeleccionada = pArbol.getSelectionPath();
      if (rutaSeleccionada != null) {
        DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) rutaSeleccionada.getLastPathComponent();
        Eliminador.eliminarDesdeNodo(nodoSeleccionado, modeloArbol);
      }
    });
    menuContextualArbol.add(eliminarItem);

    JMenuItem moverItem = new JMenuItem("Mover Directorio");
    moverItem.addActionListener(e -> {
      TreePath rutaSeleccionada = pArbol.getSelectionPath();
      if (rutaSeleccionada != null) {
        DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) rutaSeleccionada.getLastPathComponent();
        Movedor.moverDesdeNodo(nodoSeleccionado, modeloArbol);
      }
    });
    menuContextualArbol.add(moverItem);

    JMenuItem propiedadesItem = new JMenuItem("Propiedades de  elemento");
    propiedadesItem.addActionListener(e -> {
      TreePath rutaSeleccionada = pArbol.getSelectionPath();
      if (rutaSeleccionada != null) {
        DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) rutaSeleccionada.getLastPathComponent();
        new Propiedades(this, nodoSeleccionado);
      }
    });
    menuContextualArbol.add(propiedadesItem);

    pArbol.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          mostrarMenuContextual(e, menuContextualArbol, "arbol");
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          mostrarMenuContextual(e, menuContextualArbol, "arbol");
        }
      }
    });
  }
  
  /**
   * Muestra un menú contextual en función de la ubicación del clic y el origen 
   * (ya sea la tabla de archivos o el árbol de directorios).
   *
   * @param pE El evento de ratón que contiene la información sobre la ubicación 
   *          del clic.
   * @param pMenu El menú contextual que se desea mostrar.
   * @param pOrigen El origen de la acción ("tabla" o "arbol") que determina
   *               el componente en el que se debe mostrar el menú contextual.
   */
  private void mostrarMenuContextual(MouseEvent pE, JPopupMenu pMenu, String pOrigen) {
    if (pOrigen.equals("tabla")) {
      int filaSeleccionada = tablaArchivos.rowAtPoint(pE.getPoint());
      if (filaSeleccionada != -1) {
        tablaArchivos.setRowSelectionInterval(filaSeleccionada, filaSeleccionada);
        pMenu.show(tablaArchivos, pE.getX(), pE.getY());
      }
    } else if (pOrigen.equals("arbol")) {
      TreePath ruta = arbolDirectorios.getPathForLocation(pE.getX(), pE.getY());
      if (ruta != null) {
        arbolDirectorios.setSelectionPath(ruta);
        pMenu.show(arbolDirectorios, pE.getX(), pE.getY());
      }
    }
  }

  
  /**
   * Inicializa un árbol de directorios con el modelo proporcionado y selecciona 
   * la raíz del árbol si está disponible.
   *
   * @param pArbol El JTree que se va a inicializar con el modelo dado.
   * @param pModelo El modelo de árbol (DefaultTreeModel) que se asignará al árbol.
   */
  private void inicializarArbol(JTree pArbol, DefaultTreeModel pModelo) {
    pArbol.setModel(pModelo);

    DefaultMutableTreeNode raiz = (DefaultMutableTreeNode) pModelo.getRoot();
    if (raiz != null) {
      TreePath path = new TreePath(raiz.getPath());
      pArbol.setSelectionPath(path);
      pArbol.scrollPathToVisible(path);
    }
  }


  /**
   * Copia el archivo o directorio seleccionado en la tabla a un directorio de destino.
   * El usuario selecciona el destino y puede especificar un nuevo nombre para la copia.
   */
  private void copiarArchivoSeleccionado() {
    int filaSeleccionada = tablaArchivos.getSelectedRow();
    if (filaSeleccionada == -1) {
      JOptionPane.showMessageDialog(this, "Por favor, selecciona un archivo o directorio para copiar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
      return;
    }

    File archivoSeleccionado = modeloTablaArchivos.getArchivoEn(filaSeleccionada);

    if (archivoSeleccionado == null || !archivoSeleccionado.exists()) {
      JOptionPane.showMessageDialog(this, "El archivo o directorio seleccionado no existe.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    JFileChooser selectorDestino = new JFileChooser();
    selectorDestino.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int resultado = selectorDestino.showSaveDialog(this); // Mostrar el diálogo para seleccionar el destino

    if (resultado != JFileChooser.APPROVE_OPTION) {
      JOptionPane.showMessageDialog(this, "No se seleccionó un directorio de destino.", "Advertencia", JOptionPane.WARNING_MESSAGE);
      return;
    }

    File directorioDestino = selectorDestino.getSelectedFile();

    String nombreArchivo = archivoSeleccionado.getName();
    String extension = "";
    int index = nombreArchivo.lastIndexOf(".");
    if (index > 0) {
      extension = nombreArchivo.substring(index);
      nombreArchivo = nombreArchivo.substring(0, index);
    }

    String nombreCopia = JOptionPane.showInputDialog(this, "Introduce el nombre para la copia (mantendrá su extensión):", nombreArchivo);

    if (nombreCopia == null || nombreCopia.trim().isEmpty()) {
      nombreCopia = nombreArchivo; // Mantener el nombre original si no se introduce nada
    }

    String nombreFinal = nombreCopia.trim() + extension;

    File destinoFinal = new File(directorioDestino, nombreFinal.trim());

    if (destinoFinal.exists()) {
      JOptionPane.showMessageDialog(this, "Ya existe un archivo o directorio con este nombre en el destino.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      if (archivoSeleccionado.isDirectory()) {
        // Copiar el directorio
        Copiador.copiarDirectorio(archivoSeleccionado, destinoFinal);
        JOptionPane.showMessageDialog(this, "El directorio ha sido copiado con éxito.");
      } else {
        // Copiar el archivo
        Copiador.copiarArchivo(archivoSeleccionado, destinoFinal);
        JOptionPane.showMessageDialog(this, "El archivo ha sido copiado con éxito.");
      }
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this, "Error al copiar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Muestra las propiedades del archivo seleccionado en la tabla.
   * Si no se ha seleccionado un archivo, no se hace nada.
   */
  private void mostrarPropiedadesDelArchivo() {
    int filaSeleccionada = tablaArchivos.getSelectedRow();
    if (filaSeleccionada != -1) {
      File archivoSeleccionado = modeloTablaArchivos.getArchivoEn(filaSeleccionada);

      // Crear e invocar el JDialog con la información del archivo
      new Propiedades(this, archivoSeleccionado);
    }
  }



  /**
   * Abre el archivo o directorio seleccionado. Si el archivo es un directorio, 
   * se navega a ese directorio y se muestra su contenido. Si es un archivo, 
   * se intenta abrir con la aplicación predeterminada del sistema.
   */
  private void abrirArchivoConAplicacionPadre() {
    int filaSeleccionada = tablaArchivos.getSelectedRow();

    if (filaSeleccionada != -1) {
      File archivoSeleccionado = modeloTablaArchivos.getArchivoEn(filaSeleccionada);

      if (archivoSeleccionado != null) {
        if (archivoSeleccionado.isDirectory()) {
          if (!archivoSeleccionado.equals(directorioActual)) {
            historialAtras.push(directorioActual);
          }
          directorioActual = archivoSeleccionado;
          historialAdelante.clear(); // Limpiar el historial de adelante cuando navegamos a un nuevo directorio.
          mostrarContenidoDirectorio(archivoSeleccionado);
          System.out.println("Directorio actual: " + directorioActual);
          System.out.println("Historial atrás: " + historialAtras);
          System.out.println("Historial adelante: " + historialAdelante);
        } else {
          try {
            // Si es un archivo, lo abrimos
            Desktop.getDesktop().open(archivoSeleccionado);
          } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          }
        }
      } else {
        JOptionPane.showMessageDialog(this, "Por favor, selecciona un archivo válido para abrir.", "Advertencia", JOptionPane.WARNING_MESSAGE);
      }
    } else {
      JOptionPane.showMessageDialog(this, "Por favor, selecciona un archivo para abrir.", "Advertencia", JOptionPane.WARNING_MESSAGE);
    }
  }


  /**
   * Carga el directorio anterior desde el historial de atrás y muestra su contenido.
   * Si hay un directorio anterior en el historial, se mueve al historial de adelante 
   * y se actualiza el directorio actual.
   */
  private void cargarDirectorioAnterior() {
    if (!historialAtras.isEmpty()) {
        // Mueve el directorio actual al historialAdelante solo si no es el mismo que el último en historialAtras
        if (!directorioActual.equals(historialAtras.peek())) {
            historialAdelante.push(directorioActual);
        }
        // Obtén el directorio anterior del historial de atrás
        File directorioAnterior = historialAtras.pop();

        // Actualiza el directorio actual y muestra el contenido
        directorioActual = directorioAnterior;
        mostrarContenidoDirectorio(directorioAnterior);
        System.out.println("Directorio actual: " + directorioActual);
        System.out.println("Historial atrás: " + historialAtras);
        System.out.println("Historial adelante: " + historialAdelante);
    }
  }

  /**
   * Carga el directorio siguiente desde el historial de adelante y muestra su contenido.
   * Si hay un directorio siguiente en el historial, se mueve al historial de atrás 
   * y se actualiza el directorio actual.
   */
  private void cargarDirectorioAdelante() {
    if (!historialAdelante.isEmpty()) {
        // Obtiene el directorio siguiente del historial de adelante
        File directorioSiguiente = historialAdelante.pop();

        // Verifica que el directorio siguiente no sea el mismo que el directorio actual
        if (!directorioSiguiente.equals(directorioActual)) {
            // Mueve el directorio actual al historial de atrás
            historialAtras.push(directorioActual);
            historialAdelante.clear(); // Limpiar el historial de adelante

            // Actualiza el directorio actual y muestra el contenido
            directorioActual = directorioSiguiente;
            mostrarContenidoDirectorio(directorioSiguiente);
            System.out.println("Directorio actual: " + directorioActual);
            System.out.println("Historial atrás: " + historialAtras);
            System.out.println("Historial adelante: " + historialAdelante);
        }
    }
  }



  /**
   * Elimina el archivo o directorio seleccionado en la tabla. Si se trata de un directorio, 
   * se pide confirmación antes de proceder con la eliminación. En ambos casos, se maneja la eliminación 
   * y se muestra un mensaje de éxito o error según el resultado.
   */
  private void eliminarArchivoSeleccionado() {
    int filaSeleccionada = tablaArchivos.getSelectedRow();
    if (filaSeleccionada != -1) {
      File archivoSeleccionado = modeloTablaArchivos.getArchivoEn(filaSeleccionada);
      if (archivoSeleccionado != null) {
        if (archivoSeleccionado.isDirectory()) {
          int confirmacion = JOptionPane.showConfirmDialog(this,
            "Estás a punto de eliminar un directorio. ¿Estás seguro de que deseas eliminarlo?",
            "Confirmar eliminación de directorio",
            JOptionPane.YES_NO_OPTION);
          if (confirmacion == JOptionPane.YES_OPTION) {
            try {
              // Llamar a ArchivoEliminador para eliminar el directorio
              Eliminador.eliminarArchivo(archivoSeleccionado);
              String mensaje = archivoSeleccionado.isDirectory() ? "Directorio eliminado con éxito." : "Archivo eliminado con éxito.";
              JOptionPane.showMessageDialog(this, mensaje);
            } catch (IOException ex) {
              JOptionPane.showMessageDialog(this, "Error al eliminar el directorio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
          } else {
            JOptionPane.showMessageDialog(this, "Eliminación cancelada.");
          }
          return;
        }

        if (Eliminador.confirmarEliminacion(archivoSeleccionado)) {
          try {
            Eliminador.eliminarArchivo(archivoSeleccionado);
            String mensaje = archivoSeleccionado.isDirectory() ? "Directorio eliminado con éxito." : "Archivo eliminado con éxito.";
            JOptionPane.showMessageDialog(this, mensaje);
          } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          }
        } else {
          JOptionPane.showMessageDialog(this, "Eliminación cancelada.");
        }
      }
    } else {
      JOptionPane.showMessageDialog(this, "Por favor, selecciona un archivo o directorio para eliminar.");
    }
  }

    
  /**
   * Crea un nuevo directorio dentro del directorio seleccionado en la tabla de archivos.
   * Si no se ha seleccionado un directorio válido o la fila seleccionada no es un directorio,
   * se muestra un mensaje de advertencia. Si se crea con éxito, se actualiza la vista del directorio 
   * y el árbol de directorios.
   */
  private void crearDirectorio() {
    int filaSeleccionada = tablaArchivos.getSelectedRow();

    if (filaSeleccionada != -1) {
      File archivoSeleccionado = modeloTablaArchivos.getArchivoEn(filaSeleccionada);

      if (archivoSeleccionado != null && archivoSeleccionado.isDirectory()) {
        File nuevoDirectorio = Creador.crearDirectorio(archivoSeleccionado);

        if (nuevoDirectorio != null) {
          mostrarContenidoDirectorio(archivoSeleccionado);
          TreePath rutaEnArbol = encontrarRutaEnArbol(archivoSeleccionado);

          if (rutaEnArbol != null) {
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) rutaEnArbol.getLastPathComponent();
            cargarSubdirectorios(nodo, archivoSeleccionado);
          }
        }
      } else {
        JOptionPane.showMessageDialog(this, "Por favor, selecciona un directorio para crear un nuevo directorio.", "Advertencia", JOptionPane.WARNING_MESSAGE);
      }
    } else {
      JOptionPane.showMessageDialog(this, "Por favor, selecciona una carpeta en la que crear el nuevo directorio.");
    }
  }


  /**
   * Mueve el archivo o directorio seleccionado a un nuevo directorio. Si no se ha seleccionado un archivo
   * o directorio, o si el archivo no existe, se muestra un mensaje de advertencia. Se abre un selector para 
   * elegir el destino del archivo, y luego se mueve el archivo o directorio al destino seleccionado.
   */
  private void moverArchivoSeleccionado() {
    int filaSeleccionada = tablaArchivos.getSelectedRow();
    if (filaSeleccionada == -1) {
      JOptionPane.showMessageDialog(this, "Por favor, selecciona un archivo o directorio para mover.", "Advertencia", JOptionPane.WARNING_MESSAGE);
      return;
    }

    File archivoSeleccionado = modeloTablaArchivos.getArchivoEn(filaSeleccionada);

    if (archivoSeleccionado == null || !archivoSeleccionado.exists()) {
      JOptionPane.showMessageDialog(this, "El archivo o directorio seleccionado no existe.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    JFileChooser selectorDestino = new JFileChooser();
    selectorDestino.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int resultado = selectorDestino.showSaveDialog(this); // Mostrar el diálogo para seleccionar el destino

    if (resultado != JFileChooser.APPROVE_OPTION) {
      JOptionPane.showMessageDialog(this, "No se seleccionó un directorio de destino.", "Advertencia", JOptionPane.WARNING_MESSAGE);
      return;
    }

    File directorioDestino = selectorDestino.getSelectedFile();

    Movedor.moverArchivo(archivoSeleccionado, directorioDestino);
  }


  /**
   * Carga los subdirectorios de un directorio en el árbol de directorios. El método elimina los hijos
   * del nodo actual, lista los archivos en el directorio proporcionado y agrega los subdirectorios al nodo.
   * Si encuentra subdirectorios, se agrega un nodo con el texto "Cargando..." mientras se procesan los archivos.
   * Luego, actualiza el modelo del árbol.
   *
   * @param pNodo El nodo del árbol que representa el directorio actual.
   * @param pDirectorio El directorio cuyo contenido (subdirectorios) se va a cargar en el árbol.
   */
  private void cargarSubdirectorios(DefaultMutableTreeNode pNodo, File pDirectorio) {
    pNodo.removeAllChildren();
    File[] archivos = pDirectorio.listFiles();
    if (archivos != null) {
      for (File archivo : archivos) {
        if (archivo.isDirectory()) {
          DefaultMutableTreeNode subNodo = new DefaultMutableTreeNode(new NodoArchivo(archivo));
          pNodo.add(subNodo);
          subNodo.add(new DefaultMutableTreeNode("Cargando..."));
        }
      }
    }
    modeloArbol.reload(pNodo);
  }

  /**
   * Muestra el contenido de un directorio en la tabla de archivos. Si el directorio es válido, 
   * obtiene todos los archivos y los muestra en la tabla. Si no es válido o está vacío, 
   * se muestra un mensaje de advertencia.
   *
   * @param pDirectorio El directorio cuyo contenido se va a mostrar en la tabla.
   */
  private void mostrarContenidoDirectorio(File pDirectorio) {
    if (pDirectorio == null || !pDirectorio.isDirectory()) {
      modeloTablaArchivos.setArchivos(new ArrayList<>());
      JOptionPane.showMessageDialog(null, "El directorio no es válido.", "Advertencia", JOptionPane.WARNING_MESSAGE);
      return;
    }

    File[] archivos = pDirectorio.listFiles();

    if (archivos == null) {
      modeloTablaArchivos.setArchivos(new ArrayList<>());
      JOptionPane.showMessageDialog(null, "No se pueden acceder a algunos archivos debido a problemas de permisos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
    } else {
      if (archivos.length == 0) {
        modeloTablaArchivos.setArchivos(new ArrayList<>());
        JOptionPane.showMessageDialog(null, "El directorio está vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
      } else {
        modeloTablaArchivos.setArchivos(Arrays.asList(archivos));
      }
    }
  }

  /**
   * Encuentra la ruta en el árbol para el archivo proporcionado.
   * 
   * @param pArchivo El archivo cuyo nodo en el árbol se busca.
   * @return La ruta del nodo correspondiente al archivo en el árbol, o null si no se encuentra.
   */
  private TreePath encontrarRutaEnArbol(File pArchivo) {
    DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) modeloArbol.getRoot();
    return buscarNodo(nodo, pArchivo);
  }

  /**
   * Busca un nodo en el árbol que corresponda al archivo proporcionado.
   * 
   * @param pPadre El nodo actual desde donde se inicia la búsqueda.
   * @param pArchivo El archivo cuyo nodo se busca.
   * @return La ruta del nodo correspondiente al archivo en el árbol, o null si no se encuentra.
   */
  private TreePath buscarNodo(DefaultMutableTreeNode pPadre, File pArchivo) {
    if (((NodoArchivo) pPadre.getUserObject()).getArchivo().equals(pArchivo)) {
      return new TreePath(pPadre.getPath());
    }
    for (int i = 0; i < pPadre.getChildCount(); i++) {
      TreePath ruta = buscarNodo((DefaultMutableTreeNode) pPadre.getChildAt(i), pArchivo);
      if (ruta != null) {
        return ruta;
      }
    }
    return null;
  }

}
