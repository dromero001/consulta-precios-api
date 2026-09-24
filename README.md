# consulta-precios-api

Servicio REST en Spring Boot que devuelve la tarifa y el precio final que aplican a un producto de una cadena en una fecha determinada.

## Requisitos

- Java 17
- No hace falta instalar Maven: el proyecto incluye Maven Wrapper (`mvnw` / `mvnw.cmd`).

## Ejecución

```bash
./mvnw spring-boot:run
```

En Windows:

```bash
mvnw.cmd spring-boot:run
```

También se puede empaquetar y arrancar el jar:

```bash
./mvnw package
java -jar target/consulta-precios-api-0.0.1-SNAPSHOT.jar
```

La aplicación arranca en `http://localhost:8080`.

## Tests

```bash
./mvnw verify
```

## Stack

- Java 17
- Spring Boot 4.1.1
