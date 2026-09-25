# consulta-precios-api

Servicio REST en Spring Boot que devuelve la tarifa y el precio final que aplican a un producto de una cadena en una fecha determinada.

## Requisitos

Hay dos formas de ejecutar la aplicación y cada una tiene sus propios requisitos:

- **Con Maven**: Java 17. No hace falta instalar Maven, porque el proyecto incluye Maven Wrapper (`mvnw` / `mvnw.cmd`).
- **Con Docker**: solo Docker. La compilación se hace dentro del contenedor, así que no se necesita ni Java ni Maven (ver [Con Docker](#con-docker)).

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

### Con Docker

Solo hace falta Docker: la compilación se hace dentro del contenedor.

```bash
docker build -t consulta-precios-api .
docker run --rm -p 8080:8080 consulta-precios-api
```

La aplicación arranca en `http://localhost:8080`.

- SwaggerUI: http://localhost:8080/swagger-ui.html
- Contrato OpenAPI: http://localhost:8080/openapi/prices-api.yaml

## Uso

```bash
curl "http://localhost:8080/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1"
```

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 2,
  "startDate": "2020-06-14T15:00:00",
  "endDate": "2020-06-14T18:30:00",
  "price": 25.45,
  "currency": "EUR"
}
```

`applicationDate` es una fecha y hora local sin zona horaria, con segundos y sin fracciones (`yyyy-MM-ddTHH:mm:ss`).

La respuesta incluye `currency` aunque el enunciado no la enumera entre los datos de salida: un precio sin moneda es ambiguo
y la columna `CURR` forma parte de la tarifa. Añadir un campo a la respuesta no rompe a los clientes existentes.

## Tests

```bash
./mvnw verify
```

`verify` ejecuta los tests unitarios y de slice con Surefire (`*Test`) y los e2e con Failsafe (`*IT`).
Solo los unitarios: `./mvnw test`.

| Nivel | Clase | Qué prueba |
|---|---|---|
| Unitario | `HighestPriorityPriceSelectionStrategyTest` | Regla de selección por mayor prioridad |
| Unitario | `GetApplicablePriceUseCaseTest` | Orquestación del caso de uso y sus errores (Mockito) |
| Slice web | `PriceControllerTest` | Contrato HTTP, validación de parámetros y `ProblemDetail` (`@WebMvcTest` + `MockMvcTester`) |
| Slice JDBC | `JdbcPriceRepositoryTest`, `JdbcBrandRepositoryTest` | SQL contra H2, incluidos los extremos de los rangos (`@JdbcTest`) |
| Slice JDBC | `DatabaseSchemaTest` | Datos iniciales y restricciones de `PRICES` |
| Arquitectura | `ArchitectureTest` | Reglas de dependencias de la arquitectura hexagonal (ArchUnit) |
| E2E | `PricesE2EIT` + [`prices.feature`](src/test/resources/e2e/prices.feature) | Los 5 casos del enunciado, más 404 y 400, contra la aplicación levantada |
| E2E | `PricesE2EIT` + [`api-docs.feature`](src/test/resources/e2e/api-docs.feature) | Publicación del contrato y SwaggerUI |

### Tests e2e con Karate

`PricesE2EIT` arranca la aplicación completa con `@SpringBootTest(webEnvironment = RANDOM_PORT)`: Tomcat en un puerto real y H2 cargada
con `schema.sql`/`data.sql`. Después ejecuta el `.feature` de Karate, que hace peticiones HTTP reales al endpoint. Los 5 casos del enunciado
son un `Scenario Outline` con una fila por caso:

| Caso del enunciado | Escenario (regla que ejercita) | Petición (producto 35455, cadena 1) | Tarifa | Precio |
|---|---|---|---|---|
| Test 1 | Only the base price list covers the date | 2020-06-14 10:00 | 1 | 35.50 |
| Test 2 | A higher priority price list overrides the base one | 2020-06-14 16:00 | 2 | 25.45 |
| Test 3 | The base price list applies again once the override ends | 2020-06-14 21:00 | 1 | 35.50 |
| Test 4 | A same-day higher priority price list overrides the base one | 2020-06-15 10:00 | 3 | 30.50 |
| Test 5 | A long-running higher priority price list overrides the base | 2020-06-16 21:00 | 4 | 38.95 |

Los tests y las constantes se nombran por la regla que ejercitan, no por los datos del ejemplo. Los valores compartidos
(`A_BRAND_ID`, `A_PRODUCT_ID`, `AN_APPLICATION_DATE`…) están centralizados en `PriceFixtures`.

Karate usa la sintaxis Gherkin de Cucumber, pero trae los pasos HTTP y las aserciones JSON ya hechos, así que no hay que escribir step definitions.
El informe HTML queda en `target/karate-reports/karate-summary.html`.

## Stack

- Java 17
- Spring Boot 4.1.1
- OpenAPI Generator 7.25.0
- springdoc-openapi 3.1.1 (SwaggerUI)
- H2 (en memoria), JDBC con `NamedParameterJdbcTemplate`
- JUnit 5, AssertJ, Mockito, ArchUnit 1.5.0, Karate 1.5.2

## Decisiones de diseño

### Arquitectura hexagonal

Un único módulo Maven con los paquetes separados por puertos y adaptadores:

```
com.ecommerce.prices
├── domain
│   ├── model        Price, PriceQuery, BrandId, ProductId (Java puro)
│   ├── port         PriceRepository, BrandRepository (puertos de salida)
│   ├── exception    NotFoundException (Brand/Price), AmbiguousPriceException
│   ├── strategy     PriceSelectionStrategy + HighestPriorityPriceSelectionStrategy
│   └── usecase      GetApplicablePriceUseCase
├── adapter
│   └── database     JdbcPriceRepository, JdbcBrandRepository (NamedParameterJdbcTemplate)
└── webapp
    └── controller     PriceController (implementa PricesApi generada), GlobalExceptionHandler,
                       InvalidApplicationDateException
```

- Las dependencias apuntan siempre hacia el dominio. `ArchitectureTest` (ArchUnit) hace fallar la build si `domain` depende de `adapter` o `webapp`,
  si `domain.model`, `domain.port` o `domain.exception` dependen de Spring o Jakarta, o si los adaptadores dependen entre sí.
- Se ha preferido un único módulo a un multimódulo Maven para mantener el código mínimo: ArchUnit da la misma garantía con menos ficheros.
- `BrandId` y `ProductId` son value objects: el compilador impide intercambiar la cadena y el producto, que de otro modo serían dos `long`.
- Persistencia con JDBC (`NamedParameterJdbcTemplate`) y SQL explícita, sin JPA: para una consulta de solo lectura, un ORM no aporta nada.
- Las implementaciones de los puertos llevan como prefijo su tecnología (`JdbcPriceRepository`). Así, un adaptador nuevo (otra base de datos,
  una caché, un cliente REST) se añade sin tocar el dominio.

### API first

El contrato es la fuente de verdad: [`src/main/resources/static/openapi/prices-api.yaml`](src/main/resources/static/openapi/prices-api.yaml).
En cada build, `openapi-generator-maven-plugin` genera a partir de él la interfaz `PricesApi` y el modelo `PriceResponse`
(`interfaceOnly`). El controlador implementa esa interfaz, de modo que si el código se desvía del contrato, no compila.

Las fechas (`applicationDate`, `startDate`, `endDate`) se declaran con el esquema `LocalDateTime` del contrato: `type: string` con
`pattern: '^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$'`. No se usa `format: date-time` porque en OpenAPI significa RFC 3339,
que exige offset, y este servicio trabaja con fechas locales. Así el contrato dice exactamente lo que el servidor acepta y devuelve:
el generador añade `@Pattern` al parámetro (400 automático si el formato no encaja) y el controlador convierte el texto a `LocalDateTime`
con un parser estricto que también rechaza fechas imposibles, como `2020-02-30T10:00:00`.

El esquema `Problem` del contrato se mapea a `org.springframework.http.ProblemDetail` (RFC 9457) en lugar de generar una clase propia.

SwaggerUI (springdoc) no genera la documentación a partir del código: muestra el mismo YAML del contrato, publicado como recurso estático
(`springdoc.swagger-ui.url: /openapi/prices-api.yaml`). Así la documentación y el contrato son un único fichero.

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
- Solo compara la fila que se escribe con las versiones ya guardadas de las demás. Un `UPDATE` de varias filas
  (`UPDATE PRICES SET PRIORITY = 7 WHERE PRICE_LIST IN (1, 2)`) o dos transacciones concurrentes pueden crear un empate;
  `DatabaseSchemaTest` lo demuestra. Por eso la estrategia de selección rechaza también los empates.

### Selección de la tarifa: patrón Strategy

La regla "si varias tarifas cubren la fecha, se aplica la de mayor prioridad" vive en el dominio, no en la SQL:
el repositorio devuelve todas las tarifas candidatas y `PriceSelectionStrategy` elige cuál aplica.
Hoy hay una única implementación, `HighestPriorityPriceSelectionStrategy`. La interfaz es el punto de extensión para
otras reglas de selección (por ejemplo, desempatar por la fecha de inicio más reciente o aplicar promociones)
sin tocar la infraestructura.

Los empates se controlan con dos barreras. La primera es la base de datos (`CK_PRICES_NO_OVERLAP_WITH_SAME_PRIORITY`).
La segunda es la estrategia: si varias tarifas comparten la prioridad más alta, lanza `AmbiguousPriceException` y el servicio responde
500 `Ambiguous price` indicando las tarifas empatadas, en lugar de devolver una de ellas al azar. La segunda barrera es necesaria
porque la primera tiene huecos (ver las limitaciones de la restricción).

### Fechas y zona horaria

Las fechas no llevan zona horaria: se modelan como `LocalDateTime` y se comparan con fecha y hora completas
(`START_DATE <= fecha de aplicación <= END_DATE`, rango cerrado por ambos extremos).

Se asume que **no es un sistema multipaís**: todas las fechas están en la hora local de la cadena. Si lo fuera,
habría que añadir a `PRICES` una columna con la zona horaria (o el país/mercado) de la tarifa y resolver la fecha de aplicación contra ella.

### Gestión de errores

Los errores se devuelven como `application/problem+json` con el formato `ProblemDetail` (RFC 9457), desde un único
`GlobalExceptionHandler` (`@RestControllerAdvice`). El dominio lanza sus propias excepciones, que no dependen de Spring: `BrandNotFoundException` y `PriceNotFoundException`
extienden `NotFoundException` y un único manejador las traduce a 404 (título estándar `Not Found`; el `detail` distingue el caso).
La traducción a HTTP se hace solo en la capa web.

| Situación | Estado | `detail` de ejemplo |
|---|---|---|
| La cadena no existe | 404 | `Brand 99 not found` |
| Ninguna tarifa aplica en esa fecha | 404 | `No price applies to product 35455 of brand 1 at 2020-06-13T10:00:00` |
| Falta un parámetro | 400 | `Required parameter 'brandId' is not present.` |
| Identificador no numérico | 400 | `Failed to convert 'productId' with value: 'abc'` |
| Identificador menor que 1 | 400 | `brandId: must be greater than or equal to 1` |
| Fecha sin segundos, con fracciones, con zona horaria o mal formada | 400 | `applicationDate: must match "^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$"` |
| Fecha con el formato correcto pero inexistente | 400 | `applicationDate: '2020-02-30T10:00:00' is not a valid date` |
| Varias tarifas comparten la prioridad más alta | 500 | `Price lists 2 and 3 of product 35455 of brand 1 share the highest priority 1` (título `Ambiguous price`) |
| Cualquier otro error | 500 | `Unexpected error` (se registra en el log, sin exponer detalles al cliente) |

Rechazar las fracciones de segundo evita que un instante como `2020-12-31T23:59:59.5` caiga entre dos tarifas consecutivas
(`...23:59:59]` y `[00:00:00...`), y rechazar el offset evita devolver el precio de otra hora sin avisar.

Los mensajes de validación se fijan en inglés (`spring.web.locale: en`) para que no dependan del idioma de la máquina.

### Docker

`Dockerfile` multi-stage: una etapa con Maven y JDK 17 compila y empaqueta, y la imagen final solo lleva el JRE 17 (Alpine) y el jar,
y se ejecuta con un usuario sin privilegios. No hay `docker-compose`: es un único servicio y H2 va en memoria dentro del propio proceso,
así que un compose no aportaría nada.
