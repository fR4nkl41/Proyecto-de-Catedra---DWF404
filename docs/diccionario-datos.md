# Diccionario de datos – HotelDB

Base de datos: **HotelDB** (SQL Server). Script: `database/01_HotelDB_esquema.sql`.

Convenciones: **PK** = clave primaria, **FK** = clave foránea, **UQ** = único, **NN** = NOT NULL, **AI** = autoincremental (IDENTITY), **DF** = valor por defecto, **CK** = restricción CHECK.

## 1. usuario

Cuentas de acceso al sistema y su rol.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_usuario | INT | PK, AI, NN | Identificador del usuario |
| nombre | NVARCHAR(100) | NN | Nombre completo |
| email | VARCHAR(80) | UQ, NN | Correo para iniciar sesión |
| password_hash | VARCHAR(100) | NN | Contraseña cifrada con BCrypt |
| rol | VARCHAR(20) | NN, DF 'HUESPED', CK: rol IN ('ADMIN', 'RECEPCIONISTA', 'HUESPED') | Rol que define los permisos |
| activo | BIT | NN, DF 1 | 1 = puede iniciar sesión, 0 = desactivado |
| fecha_creacion | DATETIME2 | NN, DF SYSDATETIME() | Fecha de registro de la cuenta |

## 2. hotel

Hotel o cadena hotelera.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_hotel | INT | PK, AI, NN | Identificador del hotel |
| nombre | NVARCHAR(100) | UQ, NN | Nombre comercial |
| direccion | NVARCHAR(200) | NN | Dirección de la oficina principal |
| ciudad | NVARCHAR(60) | NN | Ciudad principal |
| telefono | VARCHAR(15) | NN | Teléfono de contacto |
| email | VARCHAR(80) | NN | Correo de contacto |
| descripcion | NVARCHAR(300) | NULL | Descripción breve |

## 3. sucursal

Sede física de un hotel donde están las habitaciones.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_sucursal | INT | PK, AI, NN | Identificador de la sucursal |
| id_hotel | INT | FK → hotel.id_hotel, UQ (id_hotel, nombre), NN | Hotel al que pertenece |
| nombre | NVARCHAR(100) | UQ (id_hotel, nombre), NN | Nombre de la sede |
| direccion | NVARCHAR(200) | NN | Dirección de la sede |
| ciudad | NVARCHAR(60) | NN | Ciudad (se usa en la búsqueda) |
| telefono | VARCHAR(15) | NN | Teléfono de la sede |

## 4. tipo_habitacion

Categoría de habitación y su tarifa por noche.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_tipo | INT | PK, AI, NN | Identificador del tipo |
| nombre | NVARCHAR(50) | UQ, NN | Simple, Doble, Suite... |
| descripcion | NVARCHAR(250) | NULL | Descripción del tipo |
| capacidad_maxima | INT | NN, CK: capacidad_maxima BETWEEN 1 AND 10 | Máximo de personas |
| precio_noche | DECIMAL(10,2) | NN, CK: precio_noche > 0 | Tarifa por noche en USD |

## 5. habitacion

Habitación de una sucursal.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_habitacion | INT | PK, AI, NN | Identificador de la habitación |
| id_sucursal | INT | FK → sucursal.id_sucursal, UQ (id_sucursal, numero), NN | Sucursal donde se ubica |
| id_tipo | INT | FK → tipo_habitacion.id_tipo, NN | Tipo (define capacidad y precio) |
| numero | VARCHAR(10) | UQ (id_sucursal, numero), NN | Número visible (101, 12B) |
| piso | INT | NN, CK: piso BETWEEN 0 AND 60 | Piso |
| estado | VARCHAR(20) | NN, DF 'DISPONIBLE', CK: estado IN ('DISPONIBLE', 'OCUPADA', 'MANTENIMIENTO') | Estado operativo actual |

## 6. servicio

Servicios incluidos en habitaciones o adicionales que se cobran.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_servicio | INT | PK, AI, NN | Identificador del servicio |
| nombre | NVARCHAR(80) | UQ, NN | Nombre del servicio |
| descripcion | NVARCHAR(250) | NULL | Descripción |
| costo | DECIMAL(10,2) | NN, DF 0, CK: costo >= 0 | Costo unitario (0 si es incluido) |
| adicional | BIT | NN, DF 0 | 1 = se contrata en la reserva, 0 = incluido en la habitación |

## 7. habitacion_servicio

Relación N:M entre habitaciones y servicios incluidos.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_habitacion | INT | PK, FK → habitacion.id_habitacion (ON DELETE CASCADE), NN | Habitación |
| id_servicio | INT | PK, FK → servicio.id_servicio (ON DELETE CASCADE), NN | Servicio incluido |

## 8. huesped

Persona que se hospeda.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_huesped | INT | PK, AI, NN | Identificador del huésped |
| id_usuario | INT | FK → usuario.id_usuario, NULL | Cuenta vinculada (solo si se registró en línea) |
| nombre_completo | NVARCHAR(100) | NN | Nombre completo |
| fecha_nacimiento | DATE | NN | Fecha de nacimiento (mayor de 18 años) |
| tipo_documento | VARCHAR(10) | NN, DF 'DUI', CK: tipo_documento IN ('DUI', 'PASAPORTE') | Tipo de documento |
| documento | VARCHAR(20) | UQ, NN | Número de DUI o pasaporte |
| telefono | VARCHAR(15) | NN | Teléfono |
| email | VARCHAR(80) | NN | Correo electrónico |
| fecha_registro | DATETIME2 | NN, DF SYSDATETIME() | Fecha de registro |

## 9. reserva

Reserva de una habitación por un huésped.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_reserva | INT | PK, AI, NN | Identificador de la reserva |
| codigo | VARCHAR(20) | UQ, NN | Código visible (RES-2026-0001) |
| id_huesped | INT | FK → huesped.id_huesped, NN | Huésped titular |
| id_habitacion | INT | FK → habitacion.id_habitacion, NN | Habitación reservada |
| id_usuario | INT | FK → usuario.id_usuario, NULL | Usuario que registró la reserva |
| fecha_entrada | DATE | NN, CK: fecha_salida > fecha_entrada | Fecha de llegada |
| fecha_salida | DATE | NN, CK: fecha_salida > fecha_entrada | Fecha de salida |
| numero_noches | INT | NN, CK: numero_noches > 0 | Noches calculadas por el servidor |
| numero_huespedes | INT | NN, CK: numero_huespedes > 0 | Personas que se hospedan |
| subtotal_habitacion | DECIMAL(10,2) | NN | Noches x precio por noche |
| subtotal_servicios | DECIMAL(10,2) | NN, DF 0 | Suma de servicios adicionales |
| precio_total | DECIMAL(10,2) | NN, CK: precio_total > 0 | Subtotal habitación + servicios |
| estado_reserva | VARCHAR(20) | NN, DF 'PENDIENTE', CK: estado_reserva IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO', 'FINALIZADA', 'CANCELADA') | Estado del ciclo de la reserva |
| solicitudes_especiales | NVARCHAR(250) | NULL | Peticiones del huésped |
| fecha_reserva | DATETIME2 | NN, DF SYSDATETIME() | Fecha y hora en que se creó |

## 10. reserva_servicio

Servicios adicionales contratados en una reserva.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_reserva_servicio | INT | PK, AI, NN | Identificador |
| id_reserva | INT | FK → reserva.id_reserva (ON DELETE CASCADE), UQ (id_reserva, id_servicio), NN | Reserva |
| id_servicio | INT | FK → servicio.id_servicio, UQ (id_reserva, id_servicio), NN | Servicio adicional |
| cantidad | INT | NN, DF 1, CK: cantidad BETWEEN 1 AND 10 | Cantidad contratada |
| precio_unitario | DECIMAL(10,2) | NN, CK: precio_unitario >= 0 | Precio al momento de reservar |

## 11. pago

Pago (simulado) de una reserva. Relación 1:1.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_pago | INT | PK, AI, NN | Identificador del pago |
| id_reserva | INT | FK → reserva.id_reserva (ON DELETE CASCADE), UQ, NN | Reserva pagada |
| metodo_pago | VARCHAR(20) | NN, CK: metodo_pago IN ('TARJETA', 'EFECTIVO', 'TRANSFERENCIA') | Forma de pago |
| monto | DECIMAL(10,2) | NN, CK: monto > 0 | Monto pagado (= precio_total) |
| fecha_pago | DATETIME2 | NN, DF SYSDATETIME() | Fecha y hora del pago |
| estado_pago | VARCHAR(20) | NN, DF 'COMPLETADO', CK: estado_pago IN ('COMPLETADO', 'REEMBOLSADO', 'REEMBOLSO_PARCIAL') | Estado del pago |
| referencia | VARCHAR(40) | NULL | Últimos 4 dígitos, n.º de transferencia o recepción |

## 12. cancelacion

Cancelación de una reserva con su penalización. Relación 1:1.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_cancelacion | INT | PK, AI, NN | Identificador |
| id_reserva | INT | FK → reserva.id_reserva (ON DELETE CASCADE), UQ, NN | Reserva cancelada |
| id_usuario | INT | FK → usuario.id_usuario, NULL | Usuario que canceló |
| fecha_cancelacion | DATETIME2 | NN, DF SYSDATETIME() | Fecha y hora de cancelación |
| motivo | NVARCHAR(250) | NN | Motivo indicado |
| horas_anticipacion | INT | NN | Horas que faltaban para el check-in |
| porcentaje_penalizacion | DECIMAL(5,2) | NN, CK: porcentaje_penalizacion BETWEEN 0 AND 100 | 0% (>= 48 h) o 50% (< 48 h) |
| monto_penalizacion | DECIMAL(10,2) | NN, DF 0, CK: monto_penalizacion >= 0 AND monto_reembolso >= 0 | Monto retenido |
| monto_reembolso | DECIMAL(10,2) | NN, DF 0, CK: monto_penalizacion >= 0 AND monto_reembolso >= 0 | Monto devuelto al huésped |

## 13. reclamo

Quejas o solicitudes de los huéspedes.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| id_reclamo | INT | PK, AI, NN | Identificador |
| id_huesped | INT | FK → huesped.id_huesped, NN | Huésped que reclama |
| id_reserva | INT | FK → reserva.id_reserva, NULL | Reserva relacionada (opcional) |
| asunto | NVARCHAR(100) | NN | Asunto |
| descripcion | NVARCHAR(500) | NN | Detalle del reclamo |
| estado | VARCHAR(20) | NN, DF 'ABIERTO', CK: estado IN ('ABIERTO', 'EN_PROCESO', 'RESUELTO') | Estado de atención |
| respuesta | NVARCHAR(500) | NULL | Respuesta del personal |
| fecha_creacion | DATETIME2 | NN, DF SYSDATETIME() | Fecha del reclamo |
| fecha_respuesta | DATETIME2 | NULL | Fecha de la respuesta |

Índices adicionales: `IX_reserva_habitacion_fechas` (búsqueda de disponibilidad) y `UX_huesped_usuario` (índice único filtrado: un usuario solo tiene un perfil de huésped, se permiten varios NULL).
