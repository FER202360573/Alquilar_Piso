# Apartamentos API

API REST para gestionar una empresa de **renta de habitaciones, apartamentos y pisos completos**: propiedades, clientes (arrendatarios y propietarios), reservaciones y pagos. Controla que una propiedad no se reserve dos veces en las mismas fechas, calcula el total de cada reservación, lleva el estado de cuenta de cada pago y genera reportes de ocupación, ingresos y morosidad.

> **Sin autenticación.** Esta API no implementa login: todos los endpoints son públicos.

**Autor:** Maria Fernanda Leon Diaz

---

## Tecnologías

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje |
| Spring Boot 4.1.1 | Framework (Web MVC, Data JPA) |
| Hibernate / JPA | Persistencia |
| MySQL 8 | Base de datos (administrada con MySQL Workbench) |
| Maven (wrapper incluido) | Compilación y ejecución |
| Postman | Pruebas de los endpoints |
| Thymeleaf + Bootstrap 5 | Interfaz web opcional en `/vista/...` (extra, no necesaria para usar la API) |

## Requisitos previos

- JDK 21
- MySQL 8 en ejecución
- Postman (o similar) para probar la API
- Conexión a internet para ver la interfaz web (Bootstrap se carga desde un CDN)

## Instalación y ejecución

```bash
# 1. Clonar el repositorio
git clone https://github.com/FER202360573/Alquilar_Piso.git
cd Alquilar_Piso

# 2. Configurar las credenciales de MySQL (ver la sección siguiente)

# 3. Ejecutar
./mvnw spring-boot:run          # Linux / macOS
mvnw.cmd spring-boot:run        # Windows
```

La API queda en `http://localhost:8080`. Para comprobarlo abre `http://localhost:8080/apartamentos` (devuelve `[]` si aún no hay apartamentos).

No hace falta crear las tablas a mano: Hibernate las genera al arrancar (`ddl-auto=update`). La base de datos de la URL se crea sola si no existe (`createDatabaseIfNotExist=true`).

### Configurar la conexión a la base de datos

El proyecto necesita un usuario de MySQL con permiso para crear la base de datos. Hay dos formas de indicar las credenciales; elige una.

#### Opción A: editar `application.properties`

Abre `src/main/resources/application.properties` y cambia los valores de ejemplo por los tuyos:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/libreria?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=usuario
spring.datasource.password=contraseña
```

Si quieres usar otra base de datos, cambia el nombre (`libreria`) en la URL.

> **Importante:** no subas el archivo con tus credenciales reales a GitHub. Deja los valores de ejemplo en el repositorio y rellénalos solo en tu copia local.

#### Opción B: variables de entorno

No hay que tocar ningún archivo. Spring Boot da prioridad a estas variables sobre `application.properties`.

| Variable | Descripción |
|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC de MySQL |
| `SPRING_DATASOURCE_USERNAME` | Usuario de MySQL |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de MySQL |

```bash
# Linux / macOS
export SPRING_DATASOURCE_USERNAME=mi_usuario
export SPRING_DATASOURCE_PASSWORD=mi_password
./mvnw spring-boot:run
```

```powershell
# Windows (PowerShell)
$env:SPRING_DATASOURCE_USERNAME="mi_usuario"
$env:SPRING_DATASOURCE_PASSWORD="mi_password"
.\mvnw.cmd spring-boot:run
```

## Estructura del proyecto

```
src/main/java/com/api/libreria
├── controller/      Controladores REST (/apartamentos, /clientes, ...) y de vistas (/vista/...)
├── exceptions/      Manejo global de errores de la API
├── models/          Entidades JPA (ApartamentoModel, ClienteModel, ReservacionModel, PagoModel)
├── repositories/    Repositorios Spring Data JPA
└── services/        Reglas de negocio y validaciones

src/main/resources
├── templates/       Vistas Thymeleaf (apartamentos, clientes, reservaciones, pagos, reportes)
└── application.properties
```

Cada recurso tiene dos controladores: uno REST (`ApartamentoController`, que devuelve JSON) y uno de vistas (`ApartamentoVistaController`, que muestra la página HTML y usa al primero).

## Modelo de datos

| Entidad | Tabla | Descripción |
|---|---|---|
| `ApartamentoModel` | `apartamentos` | Propiedad en renta: habitación, apartamento o piso completo |
| `ClienteModel` | `clientes` | Arrendatario (quien renta) o propietario (dueño de la propiedad) |
| `ReservacionModel` | `reservaciones` | Reserva de un apartamento por un arrendatario en un rango de fechas |
| `PagoModel` | `pagos` | Pago aplicado a una reservación |

Relaciones: un apartamento pertenece a **un cliente propietario**; una reservación une **un apartamento** con **un cliente arrendatario**; una reservación puede tener **muchos pagos**.

Valores permitidos: `tipo` del apartamento (`HABITACION`, `APARTAMENTO`, `PISO`), `estado` del apartamento (`ACTIVO`, `INACTIVO`), `tipo` del cliente (`ARRENDATARIO`, `PROPIETARIO`), `estado` de la reservación (`CONFIRMADA`, `CANCELADA`) y `metodo` del pago (`EFECTIVO`, `TARJETA`, `TRANSFERENCIA`).

Los `id` de todos los recursos los genera la base de datos. El `total` de una reservación se calcula solo (días × `precioPorDia`, donde días = `fechaFin` − `fechaInicio`) y la `fechaFin` es el día de salida. El `saldo` de una reservación es su total menos la suma de sus pagos.

Formato de fecha: `yyyy-MM-dd` (por ejemplo `2026-12-20`).

## Endpoints

Base URL: `http://localhost:8080`. Todos los cuerpos van en JSON (`Content-Type: application/json`).

### Apartamentos — `/apartamentos`

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/apartamentos` | Listar apartamentos | 200 |
| GET | `/apartamentos/{id}` | Obtener un apartamento | 200 / 404 |
| GET | `/apartamentos/disponibles?inicio=yyyy-MM-dd&fin=yyyy-MM-dd` | Apartamentos activos libres en ese rango | 200 / 400 |
| POST | `/apartamentos` | Crear apartamento | 201 / 400 / 404 |
| PUT | `/apartamentos/{id}` | Actualizar apartamento | 200 / 400 / 404 |
| DELETE | `/apartamentos/{id}` | Eliminar apartamento (sin reservaciones) | 204 / 404 / 409 |

### Clientes — `/clientes`

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/clientes` | Listar clientes (`?tipo=PROPIETARIO` o `?tipo=ARRENDATARIO` para filtrar) | 200 |
| GET | `/clientes/{id}` | Obtener un cliente | 200 / 404 |
| POST | `/clientes` | Crear cliente | 201 / 400 / 409 |
| PUT | `/clientes/{id}` | Actualizar cliente | 200 / 400 / 404 / 409 |
| DELETE | `/clientes/{id}` | Eliminar cliente (sin apartamentos ni reservaciones) | 204 / 404 / 409 |

### Reservaciones — `/reservaciones`

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/reservaciones` | Listar reservaciones | 200 |
| GET | `/reservaciones/{id}` | Obtener una reservación | 200 / 404 |
| GET | `/reservaciones/cliente/{clienteId}` | Reservaciones de un cliente | 200 / 404 |
| GET | `/reservaciones/{id}/estado-cuenta` | Total, pagado, saldo y lista de pagos | 200 / 404 |
| POST | `/reservaciones` | Crear reservación | 201 / 400 / 404 / 409 |
| PUT | `/reservaciones/{id}/cancelar` | Cancelar reservación | 200 / 404 / 409 |

### Pagos — `/pagos`

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/pagos` | Listar pagos | 200 |
| GET | `/pagos/{id}` | Obtener un pago | 200 / 404 |
| GET | `/pagos/reservacion/{reservacionId}` | Pagos de una reservación | 200 / 404 |
| POST | `/pagos` | Registrar un pago | 201 / 400 / 404 / 409 |

### Reportes — `/reportes`

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/reportes/ocupacion?inicio=yyyy-MM-dd&fin=yyyy-MM-dd` | Días ocupados y porcentaje de ocupación por apartamento (ambos días incluidos) | 200 / 400 |
| GET | `/reportes/ingresos?inicio=yyyy-MM-dd&fin=yyyy-MM-dd` | Pagos recibidos en el periodo, total y por método | 200 / 400 |
| GET | `/reportes/morosidad` | Reservaciones confirmadas que ya iniciaron y tienen saldo pendiente | 200 |

### Vistas web (extra)

| Ruta | Descripción |
|---|---|
| `/` | Redirige a `/vista/apartamentos` |
| `/vista/apartamentos` | Alta, edición y baja de apartamentos |
| `/vista/clientes` | Alta, edición y baja de clientes |
| `/vista/reservaciones` | Crear y cancelar reservaciones, con total y saldo |
| `/vista/pagos` | Registrar pagos e historial |
| `/vista/reportes` | Reportes de ocupación, ingresos y morosidad |

## Manejo de errores

Los errores de la API devuelven un JSON con un único campo `error`:

```json
{ "error": "No existe un apartamento con id 99" }
```

| Código | Cuándo |
|---|---|
| 400 Bad Request | Datos faltantes o inválidos, valores no permitidos, cliente de tipo equivocado, JSON o fechas con formato incorrecto |
| 404 Not Found | El apartamento, cliente, reservación o pago buscado no existe |
| 409 Conflict | Fechas ya ocupadas, apartamento inactivo, correo duplicado, pago mayor al saldo, reservación ya cancelada, o se intenta eliminar un registro con datos relacionados |

## Pruebas con Postman

Todos los endpoints se probaron con Postman. Para repetir las pruebas:

1. Crea una variable `baseUrl` con el valor `http://localhost:8080` y usa `{{baseUrl}}/apartamentos` en las URL (o escribe la URL completa).
2. En los `POST` y `PUT` ve a **Body → raw → JSON** y pega el cuerpo de los ejemplos. Postman agrega solo el encabezado `Content-Type: application/json`.
3. Sigue este orden: crear un propietario, crear un arrendatario, crear un apartamento, crear la reservación, registrar pagos y consultar el estado de cuenta y los reportes.

## Ejemplos de peticiones y respuestas

Flujo completo de uso. Los IDs dependen de tu base de datos.

### 1. Crear un propietario y un arrendatario

`POST /clientes`

```json
{
  "nombre": "Laura",
  "apellidos": "Gómez Ruiz",
  "correo": "laura.gomez@correo.com",
  "telefono": "2291112233",
  "tipo": "PROPIETARIO"
}
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "nombre": "Laura",
  "apellidos": "Gómez Ruiz",
  "correo": "laura.gomez@correo.com",
  "telefono": "2291112233",
  "tipo": "PROPIETARIO"
}
```

`POST /clientes` con `"tipo": "ARRENDATARIO"` (por ejemplo Ana Pérez López, `ana.perez@correo.com`) crea al cliente que va a rentar; en este flujo recibe el `id` 2.

### 2. Crear un apartamento

`POST /apartamentos`

```json
{
  "tipo": "APARTAMENTO",
  "titulo": "Apartamento frente al mar",
  "direccion": "Blvd. Ávila Camacho 220, Boca del Río",
  "descripcion": "Con aire acondicionado y WiFi",
  "habitaciones": 2,
  "banos": 1,
  "metrosCuadrados": 80,
  "precioPorDia": 900.00,
  "propietario": { "id": 1 }
}
```

Respuesta `201 Created` (si no se manda `estado`, queda `ACTIVO`):

```json
{
  "id": 1,
  "tipo": "APARTAMENTO",
  "titulo": "Apartamento frente al mar",
  "direccion": "Blvd. Ávila Camacho 220, Boca del Río",
  "descripcion": "Con aire acondicionado y WiFi",
  "habitaciones": 2,
  "banos": 1,
  "metrosCuadrados": 80,
  "precioPorDia": 900.00,
  "estado": "ACTIVO",
  "propietario": {
    "id": 1,
    "nombre": "Laura",
    "apellidos": "Gómez Ruiz",
    "correo": "laura.gomez@correo.com",
    "telefono": "2291112233",
    "tipo": "PROPIETARIO"
  }
}
```

### 3. Crear una reservación

Solo hace falta indicar el `id` del apartamento, el `id` del arrendatario y las fechas. El total se calcula solo (3 días × 900 = 2700).

`POST /reservaciones`

```json
{
  "apartamento": { "id": 1 },
  "cliente": { "id": 2 },
  "fechaInicio": "2026-12-20",
  "fechaFin": "2026-12-23"
}
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "apartamento": { "id": 1, "tipo": "APARTAMENTO", "titulo": "Apartamento frente al mar", ... },
  "cliente": { "id": 2, "nombre": "Ana", "apellidos": "Pérez López", ... },
  "fechaInicio": "2026-12-20",
  "fechaFin": "2026-12-23",
  "total": 2700.00,
  "estado": "CONFIRMADA"
}
```

### 4. Registrar un pago

`POST /pagos`

```json
{
  "reservacion": { "id": 1 },
  "monto": 1000.00,
  "metodo": "TARJETA"
}
```

Respuesta `201 Created` (si no se manda `fechaPago`, se usa la fecha de hoy):

```json
{
  "id": 1,
  "reservacion": { "id": 1, "total": 2700.00, "estado": "CONFIRMADA", ... },
  "monto": 1000.00,
  "fechaPago": "2026-12-18",
  "metodo": "TARJETA"
}
```

### 5. Consultar el estado de cuenta

`GET /reservaciones/1/estado-cuenta`

```json
{
  "reservacionId": 1,
  "cliente": "Ana Pérez López",
  "apartamento": "Apartamento frente al mar",
  "estadoReservacion": "CONFIRMADA",
  "total": 2700.00,
  "totalPagado": 1000.00,
  "saldo": 1700.00,
  "estadoCuenta": "PENDIENTE",
  "pagos": [
    { "id": 1, "monto": 1000.00, "fechaPago": "2026-12-18", "metodo": "TARJETA", ... }
  ]
}
```

### 6. Consultar disponibilidad

`GET /apartamentos/disponibles?inicio=2026-12-21&fin=2026-12-22` → lista los apartamentos activos sin reservaciones en esas fechas (el del ejemplo no aparece, porque está reservado del 20 al 23).

### 7. Reportes

`GET /reportes/ocupacion?inicio=2026-12-01&fin=2026-12-31`

```json
{
  "inicio": "2026-12-01",
  "fin": "2026-12-31",
  "ocupacionGeneralPorcentaje": 9.7,
  "apartamentos": [
    {
      "apartamentoId": 1,
      "titulo": "Apartamento frente al mar",
      "diasOcupados": 3,
      "diasEnRango": 31,
      "ocupacionPorcentaje": 9.7
    }
  ]
}
```

`GET /reportes/ingresos?inicio=2026-12-01&fin=2026-12-31`

```json
{
  "inicio": "2026-12-01",
  "fin": "2026-12-31",
  "numeroPagos": 1,
  "totalIngresos": 1000.00,
  "ingresosPorMetodo": { "TARJETA": 1000.00 }
}
```

`GET /reportes/morosidad` (consultado el 21 de diciembre, cuando la estancia ya inició):

```json
{
  "fechaConsulta": "2026-12-21",
  "totalAdeudado": 1700.00,
  "deudores": [
    {
      "reservacionId": 1,
      "clienteId": 2,
      "cliente": "Ana Pérez López",
      "correo": "ana.perez@correo.com",
      "telefono": "2297778899",
      "apartamento": "Apartamento frente al mar",
      "fechaInicio": "2026-12-20",
      "total": 2700.00,
      "totalPagado": 1000.00,
      "saldo": 1700.00
    }
  ]
}
```

### Ejemplos de error

`POST /apartamentos` sin título → `400 Bad Request`:

```json
{ "error": "El campo 'titulo' es obligatorio." }
```

`POST /apartamentos` con un propietario que no es de tipo `PROPIETARIO` → `400 Bad Request`:

```json
{ "error": "El cliente indicado no es de tipo PROPIETARIO." }
```

`POST /clientes` con un correo ya registrado → `409 Conflict`:

```json
{ "error": "Ya existe un cliente con el correo laura.gomez@correo.com" }
```

`POST /reservaciones` en fechas ya ocupadas → `409 Conflict`:

```json
{ "error": "El apartamento no está disponible en esas fechas." }
```

`POST /pagos` con un monto mayor al saldo → `409 Conflict`:

```json
{ "error": "El monto excede el saldo pendiente (1700.00)." }
```

`GET /apartamentos/99` → `404 Not Found`:

```json
{ "error": "No existe un apartamento con id 99" }
```

`DELETE /apartamentos/1` cuando tiene reservaciones → `409 Conflict`:

```json
{ "error": "No se puede eliminar el apartamento porque tiene reservaciones. Puedes marcarlo como INACTIVO." }
```

## Interfaz web (extra)

Además de la API, el proyecto incluye pantallas Thymeleaf para administrar todo desde el navegador, sin necesidad de Postman:

`http://localhost:8080/vista/apartamentos`, `/vista/clientes`, `/vista/reservaciones`, `/vista/pagos` y `/vista/reportes` (la raíz `/` abre la de apartamentos).
