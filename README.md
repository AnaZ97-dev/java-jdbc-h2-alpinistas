# Acceso a Base de Datos con JDBC y H2 (Gestión de Alpinistas)

Proyecto de programación en Java que implementa el acceso a una base de datos relacional utilizando **JDBC** y el motor de base de datos **H2**. La aplicación permite gestionar picos, alpinistas y ascensiones mediante un menú interactivo por consola.

## 🛠️ Tecnologías y Herramientas
*   **Lenguaje:** Java (JDK 11+)
*   **Base de Datos:** H2 Database (v1.4.200)
*   **Conectividad:** JDBC (Java Database Connectivity)
*   **IDE:** Apache NetBeans

## 🎯 Funcionalidades y Conceptos Aplicados

1.  **Conexión a Base de Datos (JDBC):**
    *   Carga del driver (`org.h2.Driver`).
    *   Establecimiento de conexión con `DriverManager.getConnection()`.
    *   Uso de `try-with-resources` para asegurar el cierre de la conexión.

2.  **Ejecución de Scripts SQL:**
    *   Lectura de archivos `.sql` desde los recursos del proyecto.
    *   Ejecución de scripts de creación de tablas (`creaBD.sql`) e inserción de datos (`cargaBD.sql`) con `Statement`.

3.  **Operaciones CRUD:**
    *   **Consulta (SELECT):** Mostrar picos, alpinistas y ascensiones.
    *   **Actualización (UPDATE):** Modificar la nacionalidad de un alpinista usando `PreparedStatement`.
    *   **Borrado (DELETE):** Eliminar un alpinista por su ID usando `PreparedStatement`.

4.  **Menú Interactivo:**
    *   Uso de la clase `ES` (utilidades de entrada/salida) para leer opciones y datos por teclado.

## 🚀 Cómo ejecutar el proyecto
1. Abre el proyecto en Apache NetBeans.
2. Asegúrate de que la librería `h2-1.4.200.jar` está en la carpeta `lib` del proyecto.
3. Ejecuta la clase `Aplicacion.java`.
4. La aplicación creará automáticamente la base de datos `proyectobase.h2db` y las tablas si no existen.
5. Sigue las instrucciones del menú para interactuar con la base de datos.
