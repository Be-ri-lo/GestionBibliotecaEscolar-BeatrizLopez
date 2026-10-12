# Gestión de Biblioteca Escolar

Aplicación de escritorio en Java para registrar libros, estudiantes, préstamos y devoluciones de una biblioteca escolar. Forma parte de la Evaluación Final Transversal de Desarrollo Orientado a Objetos II.

## Qué hace

- Ingreso con rol bibliotecario o estudiante.
- El bibliotecario administra libros, categorías, estudiantes y el acceso de cada estudiante.
- Préstamos con vencimiento a 14 días, tope de 3 libros activos y aviso de atraso.
- La devolución atrasada avisa los días de atraso y deja el préstamo como devuelto.
- Reportes: libros más prestados, historial por estudiante y libros que siguen prestados.

## Cómo dejar lista la base de datos

1. Abre MySQL.
2. Ejecuta `sql/Script_crea_tablas_biblioteca.sql`.
3. Ejecuta `sql/Script_poblado_tablas_biblioteca.sql`.
4. Copia `src/main/resources/database.properties.example` como `database.properties` y escribe tu usuario y clave de MySQL. En local se usó `root` / `1234`.

Los archivos de `sql/` son los scripts de la guía. Crean la base `biblioteca`.

## Cómo ejecutarla

En IntelliJ IDEA abre el proyecto, espera a que Maven descargue MySQL Connector/J y ejecuta `cl.duoc.biblioteca.Main`.

También puedes generar el ejecutable:

```bash
mvn package
java -jar target/GestionBibliotecaEscolar.jar
```

## Cuentas de prueba

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Bibliotecario | antonia@correo.cl | clave123 |
| Estudiante | carlos@correo.cl | clave123 |
| Estudiante | maria@correo.cl | clave123 |

Esas cuentas vienen del script de poblado de la guía. La contraseña de todos los usuarios cargados es `clave123`.

## Cómo está organizado

- `modelo`: entidades. `Usuario` y `Estudiante` heredan de `Persona`. `Libro` hereda de `RecursoBibliografico`.
- `vista`: ventanas y paneles con JFrame. No ejecutan SQL.
- `controlador`: une la vista con el modelo.
- `dao`: un DAO por tabla, con la interfaz `IDao`.
- `conexion.DatabaseConnection`: Singleton. `getInstance()` devuelve la única conexión creada con `DriverManager.getConnection`.
- `servicio`: el préstamo y la devolución. `descontarStock` y `reponerStock` son `synchronized`.
- `HiloRegistroPrestamo` y `HiloRegistroDevolucion`: hacen el trabajo fuera de la interfaz para que la ventana no se congele.

## Guion para el video (3 a 5 minutos)

1. Muestra tu rostro y presenta el sistema en una frase.
2. Entra como Antonia (`antonia@correo.cl` / `clave123`) y recorre libros, estudiantes, un préstamo y la devolución atrasada de Carlos (Dune).
3. Abre Reportes y muestra los tres informes.
4. Cierra sesión, entra como Carlos (`carlos@correo.cl` / `clave123`) y pide un libro desde el catálogo.
5. En el IDE muestra `DatabaseConnection` (Singleton), un DAO, `ServicioPrestamo` (synchronized) y `HiloRegistroPrestamo`.
6. Explica por qué separaste vista, controlador y modelo, y cómo el hilo más el método sincronizado cuidan el stock.
