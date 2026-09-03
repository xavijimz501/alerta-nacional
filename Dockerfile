# Etapa de construcción (Build Stage)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar el archivo de configuración de Maven y descargar dependencias para aprovechar el caché de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar y empaquetar la aplicación omitiendo pruebas unitarias
RUN mvn clean package -DskipTests

# Renombrar el JAR generado para estandarizar el nombre
RUN mv target/*.jar target/app.jar

# Etapa de ejecución (Run Stage)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiar el JAR construido en la etapa anterior
COPY --from=build /app/target/app.jar app.jar

# Exponer el puerto por defecto de Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
