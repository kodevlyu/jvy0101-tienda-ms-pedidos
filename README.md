# ms-pedidos

Microservicio REST de la tienda para registrar y consultar los **pedidos** y sus **detalles**. Forma parte de la Evaluación Parcial N°2 de JVY0101. Al crear un pedido, ms-pedidos llama por REST a ms-pagos (`POST /api/pagos`) para registrar el pago. Si ms-pagos no responde, el pedido igual se guarda con el estado `PAGO_NO_REGISTRADO`. Cada detalle guarda el `productoId` como referencia: los productos viven en otro microservicio con su propia base de datos y se comunican por REST.

## Tecnologías
- Java 17
- Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA, Validation)
- PostgreSQL 16 (Docker)
- Maven (Maven Wrapper `mvnw`)
- RestClient para la llamada a ms-pagos

## Requisitos
- JDK 17
- Docker Desktop
- Git
- Postman
- ms-pagos corriendo en `http://localhost:8084` para que se registre el pago al crear un pedido

## Clonar el repositorio
```bash
git clone https://github.com/kodevlyu/jvy0101-tienda-ms-pedidos.git
cd jvy0101-tienda-ms-pedidos
```

## Configurar la base de datos
```bash
docker run --name pg-ms -e POSTGRES_USER=ms_user -e POSTGRES_PASSWORD=ms_pass123 -e POSTGRES_DB=pedidos_db -p 5432:5432 -d postgres:16
```
Si el contenedor `pg-ms` ya existe, basta con `docker start pg-ms`. Las tablas `pedidos` y `detalle_pedido` las crea Hibernate al iniciar.

## Configuración (`src/main/resources/application.properties`)
```properties
spring.application.name=ms-pedidos
server.port=8085
spring.datasource.url=jdbc:postgresql://localhost:5432/pedidos_db
spring.datasource.username=ms_user
spring.datasource.password=ms_pass123
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
ms-pagos.url=http://localhost:8084
```

## Compilar y ejecutar
```powershell
.\mvnw clean
.\mvnw install
.\mvnw package
java -jar target\ms-pedidos-0.0.1-SNAPSHOT.jar
```
El servicio queda en `http://localhost:8085`. La base debe estar corriendo antes de compilar. Para que el pago se registre al crear un pedido, ms-pagos debe estar corriendo en el puerto 8084.

## Endpoints (`/api/pedidos`)
| Método | Ruta | Descripción | Respuesta |
|--------|------|-------------|-----------|
| POST | `/api/pedidos` | Crear un pedido (estado PENDIENTE por defecto) y registrar su pago en ms-pagos | 201, 400 |
| GET | `/api/pedidos` | Listar pedidos | 200 |
| GET | `/api/pedidos/{id}` | Buscar pedido por id | 200, 404 |
| GET | `/api/pedidos/estado/{estado}` | Listar pedidos por estado | 200 |
| PUT | `/api/pedidos/{id}` | Actualizar total y estado de un pedido | 200, 400, 404 |
| DELETE | `/api/pedidos/{id}` | Eliminar un pedido (y sus detalles) | 204, 404 |
| POST | `/api/pedidos/{id}/detalles` | Agregar un detalle a un pedido | 201, 400, 404 |
| GET | `/api/pedidos/{id}/detalles` | Listar los detalles de un pedido | 200, 404 |

Ejemplo `POST /api/pedidos`:
```json
{
  "total": 25980
}
```

Ejemplo `POST /api/pedidos/1/detalles`:
```json
{
  "productoId": 1,
  "cantidad": 2,
  "precioUnitario": 9990
}
```

Estados de un pedido: `PENDIENTE` (pago registrado en ms-pagos), `PAGO_NO_REGISTRADO` (ms-pagos no respondió) y `CONFIRMADO` (por ejemplo, al actualizarlo con `PUT`).

Ejemplo de error 400:
```json
{
  "mensaje": "Error de validación",
  "errores": [
    "total: El total debe ser mayor a 0"
  ],
  "status": 400
}
```

## Estructura
```
cl.duoc.mspedidos
├── client       (PagosClient)
├── config       (RestClientConfig)
├── controller   (PedidoController)
├── service      (PedidoService)
├── repository   (PedidoRepository, DetallePedidoRepository)
├── model        (Pedido, DetallePedido)
└── exception    (RecursoNoEncontradoException, GlobalExceptionHandler)
```

## Autora
kodevlyu