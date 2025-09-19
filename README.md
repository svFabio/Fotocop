# Problema: Crear un JAR ejecutable con un solo clic para una aplicación JavaFX

Al empaquetar una aplicación JavaFX con Maven, el archivo JAR generado (`fotocop-1.0-SNAPSHOT.jar`) no se ejecuta al hacer doble clic.

### Causas Principales

* **Dependencias de JavaFX:** Las librerías de JavaFX no son parte del JDK estándar. Un JAR simple no las incluye, lo que impide que la máquina virtual de Java (JVM) encuentre y cargue la aplicación.
* **Modularidad (JPMS):** Si intentas crear un ejecutable nativo con plugins como **jlink**, necesitas que el proyecto sea modular (usando `module-info.java`). Si las dependencias como PDFBox no son compatibles con este sistema, el proceso de construcción falla con errores como `Error: automatic module cannot be used with jlink`.
* **Clase Principal de JavaFX:** Por las limitaciones de un JAR, la clase que extiende `Application` (el punto de entrada de JavaFX) no puede ser la `Main-Class` en el manifiesto del JAR cuando se utiliza el **Maven Shade Plugin**.

---

### Solución: Crear un "Uber-JAR" con una clase de lanzamiento

La solución más simple es crear un **"Uber-JAR"** (un único archivo JAR que contiene todas las dependencias) usando el **Maven Shade Plugin** junto con una clase de lanzamiento. Esto evita los problemas de modularidad y permite un solo archivo ejecutable que funciona con un clic.

### Pasos de Implementación

1. **Crea la clase de lanzamiento (`AppLauncher.java`)**

   Esta clase actúa como el punto de entrada del JAR y simplemente llama al método `main` de la clase principal de JavaFX. Crea este archivo en la misma ruta que tu clase principal de la aplicación (`FotocopiadoraApp`).

   ```java
   package com.onebit.fasv.fotocop;

   public class AppLauncher {
       public static void main(String[] args) {
           FotocopiadoraApp.main(args);
       }
   }

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.onebit.fasv</groupId>
    <artifactId>fotocop</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <javafx.version>21</javafx.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.45.3.0</version>
        </dependency>

        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>2.0.31</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>com.onebit.fasv.fotocop.AppLauncher</mainClass>
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>


Proceso de Compilación

Para compilar el proyecto y generar el JAR ejecutable, ejecuta el siguiente comando en la terminal:

```java
mvn clean package


Una vez que el proceso finalice con éxito, encontrarás el JAR en la carpeta target de tu proyecto. Este archivo contiene todas las dependencias necesarias y se puede ejecutar con un simple doble clic.




