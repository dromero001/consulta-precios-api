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
- OpenAPI Generator 7.25.0

## Decisiones de diseño

### API first

El contrato es la fuente de verdad: [`src/main/resources/openapi/prices-api.yaml`](src/main/resources/openapi/prices-api.yaml).
En cada build, `openapi-generator-maven-plugin` genera a partir de él la interfaz `PricesApi` y el modelo `PriceResponse`
(`interfaceOnly`). El controlador implementa esa interfaz, de modo que si el código se desvía del contrato, no compila.

El esquema `Problem` del contrato se mapea a `org.springframework.http.ProblemDetail` (RFC 9457) en lugar de generar una clase propia.

### Base de datos

H2 en memoria, inicializada al arrancar con [`schema.sql`](src/main/resources/schema.sql) y [`data.sql`](src/main/resources/data.sql).

- `BRANDS`: cadenas del grupo Inditex (`1 = ZARA`, tal y como indica el enunciado). `PRICES.BRAND_ID` es una clave foránea hacia ella.
- `PRICES`: mantiene los nombres de columna del enunciado y añade `ID` como clave primaria técnica.
  - `PRICE` es `DECIMAL(10,2)` y `CURR` es `CHAR(3)` (ISO 4217).
  - `CK_PRICES_START_BEFORE_END`: el inicio de un rango no puede ser posterior a su fin.
  - `CK_PRICES_NO_OVERLAP_WITH_SAME_PRIORITY`: dos tarifas de la misma cadena y el mismo producto cuyos rangos de fechas se solapan
    no pueden tener la misma prioridad. Así nunca se da un empate en el que no se sepa qué tarifa aplicar. Como `UNIQUE (BRAND_ID, PRODUCT_ID, PRIORITY)`
    rechazaría los datos del enunciado (las tarifas 2, 3 y 4 tienen prioridad 1 sin solaparse), la restricción compara rangos.
    H2 no permite que un `CHECK` referencie su propia tabla dentro del `CREATE TABLE`, por eso se añade con `ALTER TABLE`.
  - `IDX_PRICES_LOOKUP` sobre `(BRAND_ID, PRODUCT_ID, START_DATE, END_DATE)` para la consulta de tarifas aplicables.

Limitaciones de la restricción de solape, a tener en cuenta fuera de este ejercicio:

- No es portable: MySQL y PostgreSQL no admiten subconsultas en un `CHECK`. En PostgreSQL se resolvería con
  `EXCLUDE USING gist (... tsrange(START_DATE, END_DATE, '[]') WITH &&)` y en MySQL con un trigger o validando en la capa de escritura.
- Solo se evalúa sobre la fila que se inserta o actualiza. Es suficiente para los datos cargados al arrancar, pero no garantiza el invariante
  con escrituras concurrentes.

### Selección de la tarifa: patrón Strategy

La regla "si varias tarifas cubren la fecha, se aplica la de mayor prioridad" vive en el dominio, no en la SQL:
el repositorio devuelve todas las tarifas candidatas y `PriceSelectionStrategy` elige cuál aplica.
Hoy hay una única implementación, `HighestPriorityPriceSelectionStrategy`. La interfaz es el punto de extensión para
otras reglas de selección (por ejemplo, desempatar por la fecha de inicio más reciente o aplicar promociones)
sin tocar la infraestructura. Esa estrategia no contempla empates porque la base de datos los impide
(ver `CK_PRICES_NO_OVERLAP_WITH_SAME_PRIORITY`).

### Fechas y zona horaria

Las fechas no llevan zona horaria: se modelan como `LocalDateTime` y se comparan con fecha y hora completas
(`START_DATE <= fecha de aplicación <= END_DATE`, rango cerrado por ambos extremos).

Se asume que **no es un sistema multipaís**: todas las fechas están en la hora local de la cadena. Si lo fuera,
habría que añadir a `PRICES` una columna con la zona horaria (o el país/mercado) de la tarifa y resolver la fecha de aplicación contra ella.
