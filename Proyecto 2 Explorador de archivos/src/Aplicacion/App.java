package Aplicacion;

import Controlador.MenuPrincipal;
import javax.swing.SwingUtilities;

/**
 * Clase principal de la aplicación. 
 * Se encarga de ejecutar la interfaz gráfica de la aplicación.
 */
public class App {
  
  /**
   * Método principal que inicia la aplicación.
   * Este método es el punto de entrada de la aplicación, y ejecuta la clase
   * link MenuPrincipal en el hilo de despacho de eventos de Swing.
   * 
   * @param args Argumentos de la línea de comandos, si los hay.
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        new MenuPrincipal().setVisible(true);
      }
    });
  }
}
