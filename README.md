# Sistema de Gestión de Reservas Hoteleras "Añil"

Proyecto de cátedra **DWF404 – Desarrollo de Aplicaciones con Web Frameworks** · Universidad Don Bosco.

API REST en **Java 21 + Spring Boot 3.5** (Spring Security con JWT, Spring Data JPA, validaciones y Swagger) con base de datos **SQL Server** y frontend en **React 18 + Vite** consumiendo la API con Axios.

**Integrantes**
- Francisco Manuel Mazariego Arteaga – Coordinador / Backend
- Christopher Adiel Alberto Guido – Backend / Base de datos
- Rodrigo Adrián Flores Pérez – Frontend / Documentación

---

## 1. Estructura del repositorio

```
├── database/
│   ├── 01_HotelDB_esquema.sql      # DDL: base de datos, tablas, PK, FK, CHECK
│   └── 02_HotelDB_datos.sql        # DML: datos y usuarios de prueba
├── docs/
│   ├── modelo-relacional.md        # Modelo relacional (tablas, PK, FK, reglas)
│   ├── modelo-relacional.png       # Diagrama del modelo relacional
│   └── diccionario-datos.md        # Diccionario de datos completo
├── hotel-api/                      # Backend Spring Boot (Maven)
└── hotel-frontend/                 # Frontend React (Vite)
```

### Paquetes del backend (`hotel-api/src/main/java/sv/edu/udb/hotel`)

| Paquete | Contenido |
|---|---|
| `config` | Configuración de OpenAPI/Swagger y carga inicial de usuarios |
| `controller` | Controladores REST (endpoints) |
| `dto.request` / `dto.response` | Objetos de transferencia de datos (patrón DTO) con validaciones |
| `exception` | Excepciones propias y manejador global (`@RestControllerAdvice`) |
| `model` / `model.enums` | Entidades JPA y enumeraciones (roles y estados) |
| `repository` | Repositorios Spring Data JPA |
| `security` | JWT (`JwtService`, `JwtAuthenticationFilter`), `SecurityConfig`, `UserDetailsServiceImpl` |
| `service` | Lógica de negocio (reservas, pagos, cancelaciones, estadísticas...) |
| `validation` | Validaciones personalizadas (`@SoloLetras`, `@MayorDeEdad`, `@FechasReserva`, `@DocumentoValido`) |

## 2. Herramientas y versiones

| Herramienta | Versión |
|---|---|
| Java (JDK) | 21 |
| Spring Boot | 3.5.6 |
| Maven | 3.9+ (incluido en IntelliJ IDEA) |
| jjwt (JWT) | 0.12.6 |
| springdoc-openapi (Swagger UI) | 2.8.13 |
| Lombok | administrado por Spring Boot |
| Driver SQL Server (mssql-jdbc) | administrado por Spring Boot |
| SQL Server / SSMS | SQL Server 2019+ / SSMS 22 |
| Node.js | 18 o superior (probado con 24) |
| React / React Router / Axios / Vite | 18.3 / 6.28 / 1.16 / 6.3 |
| IDE | IntelliJ IDEA (backend) y Visual Studio Code (frontend) |

## 3. Configurar SQL Server (una sola vez)

La API se conecta por TCP con un usuario de SQL Server (`hotel_app`). Para eso:

1. **Habilitar la autenticación mixta.** En SSMS: clic derecho sobre el servidor › **Propiedades** › **Seguridad** › seleccionar **"Modo de autenticación de SQL Server y de Windows"** › Aceptar.
2. **Habilitar TCP/IP.** Abrir **SQL Server Configuration Manager** › Configuración de red de SQL Server › Protocolos de MSSQLSERVER › **TCP/IP: Habilitado**. En *Propiedades › Direcciones IP › IPAll* verificar **Puerto TCP = 1433**.
3. **Reiniciar el servicio** *SQL Server (MSSQLSERVER)* desde Configuration Manager o desde `services.msc`.
4. En SSMS abrir y ejecutar (F5), en este orden:
   - `database/01_HotelDB_esquema.sql` (crea `HotelDB` y el usuario `hotel_app` / `Hotel2026*`)
   - `database/02_HotelDB_datos.sql` (datos de prueba)

> Si se usa otra instancia, puerto o usuario, modificar `hotel-api/src/main/resources/application.properties`.

## 4. Ejecutar el backend

**IntelliJ IDEA:** File › Open › carpeta `hotel-api` › esperar a que Maven descargue las dependencias › ejecutar `HotelApiApplication`.

**Consola:**
```bash
cd hotel-api
mvn spring-boot:run
```

- API: http://localhost:8080/api
- Swagger UI: **http://localhost:8080/swagger-ui.html**
- Especificación OpenAPI: http://localhost:8080/api-docs

Para probar en Swagger: ejecutar `POST /api/auth/login`, copiar el `token` y pegarlo en el botón **Authorize**.

## 5. Ejecutar el frontend

```bash
cd hotel-frontend
npm install
npm run dev
```

Abrir **http://localhost:5173**. Por defecto el frontend usa `http://localhost:8080/api`. Para cambiarla, copiar `hotel-frontend/.env.example` como `.env` y editar `VITE_API_URL`.

## 6. Usuarios de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@hotel.com | Admin123 |
| Recepcionista | recepcion@hotel.com | Recep123 |
| Huésped | carlos@mail.com | Huesped123 |
| Huésped | ana@mail.com | Huesped123 |

## 7. Roles y permisos

| Funcionalidad | ADMIN | RECEPCIONISTA | HUESPED |
|---|:-:|:-:|:-:|
| Iniciar sesión / ver perfil | ✔ | ✔ | ✔ |
| Registrarse en línea | – | – | ✔ (público) |
| Usuarios: crear, editar, eliminar, activar/desactivar | ✔ | – | – |
| Hoteles, sucursales, tipos/tarifas, servicios (CRUD) | ✔ | consulta | consulta |
| Habitaciones (CRUD) | ✔ | consulta y cambio de estado | – |
| Buscar disponibilidad y reservar | ✔ | ✔ | ✔ |
| Ver reservas | todas | todas | solo las suyas |
| Pagar (tarjeta / transferencia) | ✔ | ✔ | solo las suyas |
| Registrar pago en efectivo | ✔ | ✔ | – (paga en recepción) |
| Cancelar reserva | ✔ | ✔ | solo las suyas |
| Check-in / check-out | ✔ | ✔ | – |
| Eliminar reservas | ✔ | – | – |
| Huéspedes (CRUD) | ✔ | ✔ (sin eliminar) | su propio perfil |
| Reclamos | todos + eliminar | todos + responder | crear y ver los suyos |
| Estadísticas (panel de control) | ✔ | ✔ | resumen propio |

## 8. Endpoints principales

| Módulo | Endpoints |
|---|---|
| Autenticación | `POST /api/auth/login`, `POST /api/auth/registro`, `GET /api/auth/perfil` |
| Usuarios | `GET/POST /api/usuarios`, `GET/PUT/DELETE /api/usuarios/{id}`, `PATCH /api/usuarios/{id}/estado` |
| Hoteles | `GET/POST /api/hoteles`, `GET/PUT/DELETE /api/hoteles/{id}` |
| Sucursales | `GET/POST /api/sucursales?hotelId=`, `GET/PUT/DELETE /api/sucursales/{id}` |
| Tipos de habitación | `GET/POST /api/tipos-habitacion`, `GET/PUT/DELETE /api/tipos-habitacion/{id}` |
| Servicios | `GET/POST /api/servicios?adicional=`, `GET/PUT/DELETE /api/servicios/{id}` |
| Habitaciones | `GET/POST /api/habitaciones`, `GET/PUT/DELETE /api/habitaciones/{id}`, `PATCH /api/habitaciones/{id}/estado`, `GET /api/habitaciones/disponibles?ciudad=&fechaEntrada=&fechaSalida=&huespedes=&tipoId=` |
| Huéspedes | `GET/POST /api/huespedes`, `GET/PUT/DELETE /api/huespedes/{id}`, `GET/PUT /api/huespedes/mi-perfil` |
| Reservas | `GET/POST /api/reservas`, `GET/PUT/DELETE /api/reservas/{id}`, `GET /api/reservas/{id}/cancelacion/simular`, `POST /api/reservas/{id}/cancelar`, `PATCH /api/reservas/{id}/check-in`, `PATCH /api/reservas/{id}/check-out`, `GET /api/reservas/cancelaciones` |
| Pagos | `POST /api/pagos`, `GET /api/pagos`, `GET /api/pagos/reserva/{reservaId}` |
| Reclamos | `GET/POST /api/reclamos`, `GET/PUT/DELETE /api/reclamos/{id}`, `PATCH /api/reclamos/{id}/respuesta` |
| Estadísticas | `GET /api/estadisticas/dashboard` |

## 9. Reglas de negocio

- **Flujo de reserva:** búsqueda › habitación › datos del huésped › resumen con servicios adicionales › pago › confirmación.
- **Estados de la reserva:** `PENDIENTE` (sin pago) › `CONFIRMADA` (pagada) › `EN_CURSO` (check-in) › `FINALIZADA` (check-out). También `CANCELADA`.
- **Disponibilidad:** una habitación no puede tener dos reservas activas que se crucen en fechas; las habitaciones en `MANTENIMIENTO` no aparecen en la búsqueda.
- **Precio:** el total lo calcula siempre el servidor: noches × precio por noche + servicios adicionales.
- **Política de cancelación:** gratuita hasta 48 horas antes del check-in (2:00 p.m.); con menos anticipación se retiene el 50 % del monto pagado.
- **Pagos simulados:** la tarjeta se valida con el algoritmo de Luhn y su fecha de vencimiento (tarjeta de prueba `4242 4242 4242 4242`).

## 10. Validaciones del lado del servidor

- **Campos vacíos:** `@NotBlank` / `@NotNull` con mensajes en español.
- **Tipo de dato y formato (expresiones regulares):** correo, teléfono `7777-8888`, DUI `00000000-0`, pasaporte, número de habitación, contraseña con letras y números, tarjeta, CVV y vencimiento `MM/AA`. Si se envía texto en un campo numérico o una fecha inválida, se responde 400 indicando el campo y el tipo esperado.
- **Datos congruentes:** la fecha de entrada debe ser hoy o posterior, la salida debe ser posterior a la entrada (máximo 30 noches), el huésped titular debe ser mayor de edad, los huéspedes no pueden superar la capacidad y la tarjeta no puede estar vencida.

Todas las respuestas de error tienen el mismo formato:

```json
{
  "fecha": "2026-10-28T10:15:30",
  "estado": 400,
  "error": "Datos inválidos",
  "mensaje": "Revisa los campos marcados: hay datos vacíos o con formato incorrecto.",
  "ruta": "/api/reservas",
  "errores": { "fechaEntrada": "La fecha de entrada no puede ser anterior a hoy" }
}
```
