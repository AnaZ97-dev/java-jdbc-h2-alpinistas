package aplicacion;

// jdbc:h2:./proyectobase.h2db
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import org.h2.tools.Server;
import static java.lang.System.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.stream.Collectors;
import utilidades.ES;

/**
 * Programa principal que usa el acceso a base de datos configurada en H2
 *
 * @author Zafra Vallejo Ana
 * @version mayo/2026
 */
public class Aplicacion {

    /**
     * Nombre del archivo de base de datos local.
     */
    private static final String DB_NOMBRE = "proyectobase.h2db";
    /**
     * URL para la conexión a la base de datos.
     */
    private static final String URL_CONEXION = "jdbc:h2:./" + DB_NOMBRE;
    /**
     * Driver a utilizar para conectarse a la base de datos.
     */
    private static final String DRIVER = "org.h2.Driver";
    /**
     * Opciones de conexión.
     */
    private static final String PARAMS = ";MODE=MySQL;AUTO_RECONNECT=TRUE;AUTO_SERVER=TRUE";

    /**
     * Path al archivo que contiene la estructura de la base de datos.
     */
    public final static String ESTRUCTURA_DB = "/resources/creaBD.sql";

    /**
     * Path al archivo que contiene la estructura de la base de datos.
     */
    public final static String INSERTA_DB = "/resources/cargaBD.sql";

    /**
     * Método principal de la aplicación. En él se realiza la preparación del
     * entorno antes de empezar. A destacar:
     *
     * - Se carga el driver (Class.forName). - Se establece una conexión con la
     * base de datos (DriverManager.getConnection) - Se crean las tablas, si no
     * están creadas, invocando el método createTables. - Se ejecuta una
     * consulta de prueba
     *
     * @param args
     */
    public static void main(String[] args) {
        boolean driverCargado = false;

        //Carga del driver de la base de datos.
        try {
            Class.forName(DRIVER).getDeclaredConstructor().newInstance();
            driverCargado = true;
        } catch (ClassNotFoundException e) {
            err.printf("No se encuentra el driver de la base de datos (%s)\n", DRIVER);
        } catch (InstantiationException | IllegalAccessException ex) {
            err.printf("No se ha podido iniciar el driver de la base de datos (%s)\n", DRIVER);
        } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            java.util.logging.Logger.getLogger(Aplicacion.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Si el driver está cargado, aseguramos que podremos conectar.
        if (driverCargado) {
            //Conectamos con la base de datos.
            //El try-with-resources asegura que se cerrará la conexión al salir.
            String[] wsArgs = {"-baseDir", System.getProperty("user.dir"), "-browser"};
            try ( Connection con = DriverManager.getConnection(URL_CONEXION + PARAMS, "", "")) {

                // Iniciamos el servidor web interno (consola H2 para depuraciones)
                Server sr = Server.createWebServer(wsArgs);
                sr.start();

                // Presentamos información inicial por consola
                out.println("¡¡Atención!!");
                out.println();
                out.println("Mientras tu aplicación se esté ejecutando \n"
                        + "puedes acceder a la consola de la base de datos \n"
                        + "a través del navegador web.");
                out.println();
                out.println("Página local: " + sr.getURL());
                out.println();
                out.println("Datos de acceso");
                out.println("---------------");
                out.println("Controlador: " + DRIVER);
                out.println("URL JDBC: " + URL_CONEXION);
                out.println("Usuario: (no indicar nada)");
                out.println("Password: (no indicar nada)");

                // Creamos las tablas y algunos datos de prueba si no existen
                // y continuamos
                if (crearTablas(con)) {

                    // Insertar los datos en las tablas de la BD
                    insertarDatosTablas(con);

                    boolean continuar = true;

                    do {
                        System.out.println("\n\n");
                        System.out.println("\n --------------------------------------------------------------------------------");
                        System.out.println("| MENÚ DE LA APLICACIÓN                                                          |");
                        System.out.println(" --------------------------------------------------------------------------------");
                        System.out.println("  1 - Mostrar picos más altos del mundo");
                        System.out.println("  2 - Borrar alpinista");
                        System.out.println("  3 - Modificar nacionalidad alpinista");
                        System.out.println("  4 - Mostrar ascensiones de cada pico");
                        System.out.println("  0 - Salir");
                        System.out.println(" --------------------------------------------------------------------------------\n\n");

                        // Leer la opción correspondiente a ejecutar.
                        int opcion = ES.leeEntero("Escriba opción: ", 0, 4);
                        switch (opcion) {
                            case 0:
                                continuar = false;
                                break;
                            case 1:
                                mostrarPicos(con);
                                break;
                            case 2:
                                borrarAlpinista(con);
                                break;
                            case 3:
                                modificarNacionalidadAlpinista(con);
                                break;
                            case 4:                                
                                mostrarAscensionesPicos(con);
                                break;
                        }
                    } while (continuar);

                    // Esperar tecla
                    ES.leeCadena("Antes de terminar, puedes acceder a la "
                            + "consola de H2 para ver y modificar la base de "
                            + "datos. Pulsa cualquier tecla para salir.");

                } else {
                    System.out.printf("Problema creando las tablas: ");
                }

                sr.stop();
                sr.shutdown();

            } catch (SQLException ex) {
                System.out.printf("No se pudo conectar a la base de datos (%s): %s\n", DB_NOMBRE, ex.getMessage());
            }
        }

    }

    /**
     * Dada una conexión válida, lleva a cabo la creación de la estructura de la
     * base de datos, usando como SQL para la creación el contenido en la
     * constante ESTRUCTURA_DB
     *
     * @param con conexión a la base de datos.
     * @see ESTRUCTURA_DB
     * @return true si se creo la estructura y false en caso contrario.
     */
    public static boolean crearTablas(Connection con) {
        boolean todoBien = false;

        try ( Statement st = con.createStatement()) {

            String sqlScript = loadResourceAsString(ESTRUCTURA_DB);
            if (sqlScript != null) {
                st.execute(sqlScript);
                todoBien = true;
            } else {
                System.out.printf("Problema cargando el archivo: %s \n", ESTRUCTURA_DB);
                System.out.printf("Para ejecutar este proyecto no puede usarse 'Run File'\n");
            }

        } catch (SQLException ex) {
            System.out.printf("Problema creando la estructura de la base de datos: %s\n", ex.getMessage());
        }
        return todoBien;
    }

    /**
     * Dada una conexión válida, lleva a cabo la inserción de datos de la base
     * de datos, usando como SQL para la creación el contenido en la constante
     * INSERTA_DB
     *
     * @param con conexión a la base de datos.
     * @see INSERTA_DB
     * @return true si se creo la estructura y false en caso contrario.
     */
    private static boolean insertarDatosTablas(Connection con) {
        boolean todoBien = false;

        try ( Statement st = con.createStatement()) {

            String sqlScript = loadResourceAsString(INSERTA_DB);
            if (sqlScript != null) {
                st.execute(sqlScript);
                todoBien = true;
            } else {
                System.out.printf("Problema cargando el archivo: %s \n", INSERTA_DB);
                System.out.printf("Para ejecutar este proyecto no puede usarse 'Run File'\n");
            }

        } catch (SQLException ex) {
            System.out.printf("Problema insertando datos en la base de datos: %s\n", ex.getMessage());
        }
        return todoBien;
    }

    /**
     * Carga un recurso que estará dentro del JAR como cadena de texto.
     *
     * @param resourceName Nombre del recurso dentro del JAR.
     * @return Cadena que contiene el contenido del archivo.
     */
    public static String loadResourceAsString(String resourceName) {
        String resource = null;
        InputStream is = Aplicacion.class.getResourceAsStream(resourceName);
        if (is != null) {
            try ( InputStreamReader isr = new InputStreamReader(is);  BufferedReader br = new BufferedReader(isr);) {
                resource = br.lines().collect(Collectors.joining("\n"));
            } catch (IOException ex) {
                System.out.printf("Problema leyendo el recurso como cadena: %S\n ", resourceName);
            }
        }
        return resource;
    }

    /**
     * Muestra por consola la información de los picos de la BD.
     *
     * @param con Conexión a la BD
     */
    private static void mostrarPicos(Connection con) {
        // Ejecutamos la consulta sobre la tabla picos y mostramos su contenido.
        
        String query = "SELECT * FROM Picos";
        try (Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
        
            System.out.println("\n--------------- Listado de Picos más altos del mundo ----------------");
            System.out.printf("%-4s %-15s %-15s %-20s %-12s\n", "Id", "Nombre", "Cordillera", "Localización", "Altitud (m)");
            System.out.println("---------------------------------------------------------------------");
        
            while (rs.next()) {
                System.out.printf("%-4d %-15s %-15s %-20s %-12d\n", 
                    rs.getInt("idPico"), 
                    rs.getString("nombre"), 
                    rs.getString("cordillera"), 
                    rs.getString("localizacion"), 
                    rs.getInt("altitud"));
            }
        } catch (SQLException e) {
            System.err.println("Error al mostrar picos: " + e.getMessage());
        }
    }

    /**
     * Modificar la nacionalidad de un alpinista de la BD
     *
     * @param con Conexión a la BD
     */
    private static void modificarNacionalidadAlpinista(Connection con) {
        // Consultamos y mostramos los distintos alplinistas insertados en la bd.
        String listQuery = "SELECT idAlpinista, nombre, apellidos, nacionalidad FROM Alpinistas";
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(listQuery)) {
            System.out.println("\n------------- Listado de alpinistas ---------------");
            System.out.printf("%-4s %-15s %-15s %-15s\n", "Id", "Nombre", "Apellidos", "Nacionalidad");
            System.out.println("---------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-4d %-15s %-15s %-15s\n", rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        
        // Petición del identificador y de la nueva nacionalidad del alpinista a modificar.
        int id = ES.leeEntero("\nEscriba el identificador del alpinista a modificar: ");
        String nuevaNac = ES.leeCadena("Introduzca la nueva nacionalidad del alpinista: ");
        
        // Ejecutamos la sentencia SQL de actualización.
        String updateSql = "UPDATE Alpinistas SET nacionalidad = ? WHERE idAlpinista = ?";
        try (PreparedStatement pstmt = con.prepareStatement(updateSql)) {
            pstmt.setString(1, nuevaNac);
            pstmt.setInt(2, id);
            int filasAfec = pstmt.executeUpdate();
            if (filasAfec > 0) {
                System.out.println("Modificación correctamente el alpinista con identificador " + id + ".");
            } else {
                System.out.println("No se realizó ninguna modificación en los datos de los alpinistas.");
            }
        } catch (SQLException e) {
            System.err.println("Error al modificar: " + e.getMessage());
        }
    }

    /**
     * Borrar el registro de un alpinista de la BD
     *
     * @param con Conexión a la BD
     */
    private static void borrarAlpinista(Connection con) {
        // Consultamos y mostramos los distintos alpinistas insertados en la bd.
        String listQuery = "SELECT idAlpinista, nombre, apellidos FROM Alpinistas";
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(listQuery)) {
            System.out.println("\n------ Listado de alpinistas -------");
            System.out.printf("%-4s %-15s %-15s\n", "Id", "Nombre", "Apellidos");
            System.out.println("------------------------------------");
            while (rs.next()) {
                System.out.printf("%-4d %-15s %-15s\n", rs.getInt(1), rs.getString(2), rs.getString(3));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        
        // Petición del identificador del alpinista a eliminar.
        int id = ES.leeEntero("\nEscriba el identificador del alpinista a borrar: ");
        String deleteSql = "DELETE FROM Alpinistas WHERE idAlpinista = ?";
    
        // Ejecutamos la sentencia SQL de borrado.
        try (PreparedStatement pstmt = con.prepareStatement(deleteSql)) {
            pstmt.setInt(1, id);
            int filasAfec = pstmt.executeUpdate();
            if (filasAfec > 0) {
                System.out.println("Borrado correctamente el alpinista con identificador " + id + ".");
            } else {
                System.out.println("No se realizó ninguna modificación en la base de datos.");
            }
        } catch (SQLException e) {
            System.err.println("Error al borrar: " + e.getMessage());
        }    
    }

    /**
     * Muestra el nombre de todos los picos existentes junto a los alpinistas que los han ascendido.
     *
     * @param con Conexión a la BD
     */
    private static void mostrarAscensionesPicos(Connection con) {
        // Consultamos todos los picos existentes en la bd.
        System.out.println("\n --------------------------------------------------------------------------");
        System.out.println("| ASCENSIONES A PICOS                                                      |");
        System.out.println(" --------------------------------------------------------------------------");
        
        String picosSql = "SELECT idPico, nombre FROM Picos";
        String ascensionesSql = "SELECT A.nombre, A.apellidos, ASCE.fecha, ASCE.ruta " +
                                "FROM Ascensiones ASCE " +
                                "JOIN Alpinistas A ON ASCE.idAlpinista = A.idAlpinista " +
                                "WHERE ASCE.idPico = ?";
        
        // Almacenamos el identificador y nombre para cada pico devuelto en la consulta.        
        try (Statement stmtPicos = con.createStatement();
             ResultSet rsPicos = stmtPicos.executeQuery(picosSql);
             PreparedStatement pstmtAsce = con.prepareStatement(ascensionesSql)) {
            
        // Consultamos las  ascensiones a cada pico.
        while (rsPicos.next()) {
            int idPico = rsPicos.getInt("idPico");
            String nombrePico = rsPicos.getString("nombre");
            
            // Cálculo de cabecera dinámica
            int anchoTotal = 60;
            int guionesRestantes = (anchoTotal - nombrePico.length() - 2) / 2;
            String guiones = "-".repeat(guionesRestantes);
            
            System.out.println("\n\n" + guiones + " " + nombrePico + " " + guiones);
            System.out.printf("%-28s %-12s %-20s\n", "Nombre alpinista", "Fecha", "Ruta ascenso");
            System.out.println("------------------------------------------------------------");                 
       
        // Consultamos los datos para cada alpinista involucrado en la ascensión al pico.
        pstmtAsce.setInt(1, idPico);
            try (ResultSet rsAsce = pstmtAsce.executeQuery()) {
                while (rsAsce.next()) {
                    System.out.printf("%-12s %-15s %-12s %-20s\n", 
                    rsAsce.getString(1), 
                    rsAsce.getString(2), 
                    rsAsce.getDate(3), 
                    rsAsce.getString(4));
                }
            }
        }
    } catch (SQLException e) {
        System.err.println("Error al mostrar ascensiones: " + e.getMessage());
    }                   
    }
}