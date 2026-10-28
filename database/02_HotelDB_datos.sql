/* =====================================================================
   Sistema de Gestión de Reservas Hoteleras "Añil" - DWF404
   Ejecutar después de 01_HotelDB_esquema.sql
   ---------------------------------------------------------------------
   Usuarios predefinidos (contraseñas cifradas con BCrypt):
     admin@hotel.com      / Admin123     -> ADMIN
     recepcion@hotel.com  / Recep123     -> RECEPCIONISTA
     carlos@mail.com      / Huesped123   -> HUESPED
     ana@mail.com         / Huesped123   -> HUESPED
   Las fechas de las reservas se calculan a partir de la fecha actual
   para que el panel de control siempre muestre información.
   ===================================================================== */

USE HotelDB;
GO

SET NOCOUNT ON;

DECLARE @hoy DATE = CAST(GETDATE() AS DATE);

/* ---------------- USUARIOS ---------------- */
INSERT INTO usuario (nombre, email, password_hash, rol, activo) VALUES
 (N'Administrador',          'admin@hotel.com',     '$2a$10$8V/oPJSKlm3eueG1tZQ55.fx.LO1w7.eSNL/8laFnGPqzBfBrwCNC', 'ADMIN', 1),
 (N'María Rodríguez',        'recepcion@hotel.com', '$2a$10$zmBqD6Mnnyz4F9YdowCjeuASFw0VhG9PcJcidxL8k6q9gOXxnyzO2', 'RECEPCIONISTA', 1),
 (N'Carlos Pérez',           'carlos@mail.com',     '$2a$10$xxP0tMLgKdJqMW/06ccVp.hgE.UijwldZR1Tea7CoVCvwLEjdX37y', 'HUESPED', 1),
 (N'Ana Gómez',              'ana@mail.com',        '$2a$10$xxP0tMLgKdJqMW/06ccVp.hgE.UijwldZR1Tea7CoVCvwLEjdX37y', 'HUESPED', 1);

/* ---------------- HOTELES Y SUCURSALES ---------------- */
INSERT INTO hotel (nombre, direccion, ciudad, telefono, email, descripcion) VALUES
 (N'Hotel Real Intercontinental', N'Boulevard de Los Héroes y Av. Sisimiles', N'San Salvador', '2211-3333', 'info@realinter.com', N'Hotel de negocios frente a Metrocentro.'),
 (N'Hotel Sheraton Presidente',   N'Av. La Revolución, Colonia San Benito',    N'San Salvador', '2283-4000', 'reservas@sheratonsv.com', N'Hotel de lujo con vista a la ciudad.'),
 (N'Hotel Barceló',               N'Calle El Mirador y 89 Av. Norte',          N'San Salvador', '2268-4545', 'contacto@barcelosv.com', N'Cadena con sedes en la capital y la playa.');

INSERT INTO sucursal (id_hotel, nombre, direccion, ciudad, telefono) VALUES
 (1, N'Sede Los Héroes',    N'Boulevard de Los Héroes #1200',      N'San Salvador', '2211-3334'),
 (2, N'Sede San Benito',    N'Av. La Revolución #11',              N'San Salvador', '2283-4001'),
 (3, N'Sede Escalón',       N'Calle El Mirador #45, Colonia Escalón', N'San Salvador', '2268-4546'),
 (3, N'Sede Playa El Tunco', N'Carretera del Litoral km 42',        N'La Libertad', '2389-7000');

/* ---------------- TIPOS DE HABITACIÓN ---------------- */
INSERT INTO tipo_habitacion (nombre, descripcion, capacidad_maxima, precio_noche) VALUES
 (N'Simple', N'Habitación con una cama individual.',              1,  80.00),
 (N'Doble',  N'Habitación con cama queen o dos camas individuales.', 2, 120.00),
 (N'Suite',  N'Suite amplia con sala de estar y jacuzzi.',         4, 250.00);

/* ---------------- SERVICIOS ---------------- */
-- adicional = 0: incluido en la habitación | adicional = 1: se contrata en la reserva
INSERT INTO servicio (nombre, descripcion, costo, adicional) VALUES
 (N'WiFi',                    N'Internet inalámbrico de alta velocidad.',      0.00, 0),
 (N'Aire acondicionado',      N'Climatización individual.',                    0.00, 0),
 (N'Jacuzzi',                 N'Jacuzzi privado en la habitación.',            0.00, 0),
 (N'Desayuno incluido',       N'Desayuno buffet para los huéspedes.',          0.00, 0),
 (N'Traslado al aeropuerto',  N'Transporte desde o hacia el aeropuerto.',     25.00, 1),
 (N'Lavandería',              N'Lavado y planchado de ropa (por servicio).',  10.00, 1),
 (N'Plan de alimentación',    N'Almuerzo y cena (por día).',                  30.00, 1),
 (N'Tour por la ciudad',      N'Recorrido guiado de medio día.',              35.00, 1);

/* ---------------- HABITACIONES ---------------- */
INSERT INTO habitacion (id_sucursal, id_tipo, numero, piso, estado) VALUES
 (1, 2, '101', 1, 'DISPONIBLE'),  -- 1
 (1, 2, '102', 1, 'DISPONIBLE'),  -- 2
 (1, 1, '201', 2, 'DISPONIBLE'),  -- 3
 (2, 3, '501', 5, 'DISPONIBLE'),  -- 4
 (2, 3, '502', 5, 'OCUPADA'),     -- 5
 (2, 2, '301', 3, 'DISPONIBLE'),  -- 6
 (3, 1, '101', 1, 'DISPONIBLE'),  -- 7
 (3, 1, '102', 1, 'MANTENIMIENTO'), -- 8
 (3, 2, '201', 2, 'DISPONIBLE'),  -- 9
 (4, 2, '10',  0, 'DISPONIBLE'),  -- 10
 (4, 3, '20',  1, 'DISPONIBLE');  -- 11

INSERT INTO habitacion_servicio (id_habitacion, id_servicio) VALUES
 (1,1),(1,2),
 (2,1),(2,2),
 (3,1),
 (4,1),(4,2),(4,3),(4,4),
 (5,1),(5,2),(5,3),(5,4),
 (6,1),(6,2),(6,4),
 (7,1),
 (8,1),
 (9,1),(9,2),
 (10,1),(10,2),(10,4),
 (11,1),(11,2),(11,3),(11,4);

/* ---------------- HUÉSPEDES ---------------- */
INSERT INTO huesped (id_usuario, nombre_completo, fecha_nacimiento, tipo_documento, documento, telefono, email) VALUES
 (3,    N'Carlos Pérez',   '1990-05-15', 'DUI',       '01234567-8', '7777-8888', 'carlos@mail.com'),
 (4,    N'Ana Gómez',      '1995-11-02', 'DUI',       '04567891-2', '7123-4567', 'ana@mail.com'),
 (NULL, N'Luis Martínez',  '1987-02-20', 'DUI',       '03456789-1', '6012-3456', 'luis.martinez@mail.com'),
 (NULL, N'John Smith',     '1980-07-09', 'PASAPORTE', 'A12345678',  '2222-1111', 'john.smith@mail.com');

/* ---------------- RESERVAS ---------------- */
-- 1) Carlos - Suite Sheraton, confirmada, llega en 2 días
INSERT INTO reserva (codigo, id_huesped, id_habitacion, id_usuario, fecha_entrada, fecha_salida, numero_noches, numero_huespedes,
                     subtotal_habitacion, subtotal_servicios, precio_total, estado_reserva, solicitudes_especiales, fecha_reserva)
VALUES (CONCAT('RES-', YEAR(@hoy), '-0001'), 1, 4, 3, DATEADD(DAY, 2, @hoy), DATEADD(DAY, 7, @hoy), 5, 2,
        1250.00, 25.00, 1275.00, 'CONFIRMADA', N'Habitación en piso alto', DATEADD(DAY, -10, SYSDATETIME()));

-- 2) Ana - Doble Real Intercontinental, pendiente de pago, llega en 3 días
INSERT INTO reserva (codigo, id_huesped, id_habitacion, id_usuario, fecha_entrada, fecha_salida, numero_noches, numero_huespedes,
                     subtotal_habitacion, subtotal_servicios, precio_total, estado_reserva, fecha_reserva)
VALUES (CONCAT('RES-', YEAR(@hoy), '-0002'), 2, 1, 4, DATEADD(DAY, 3, @hoy), DATEADD(DAY, 5, @hoy), 2, 2,
        240.00, 0.00, 240.00, 'PENDIENTE', DATEADD(DAY, -2, SYSDATETIME()));

-- 3) Luis - Suite Sheraton 502, en curso (llegó hace 1 día)
INSERT INTO reserva (codigo, id_huesped, id_habitacion, id_usuario, fecha_entrada, fecha_salida, numero_noches, numero_huespedes,
                     subtotal_habitacion, subtotal_servicios, precio_total, estado_reserva, fecha_reserva)
VALUES (CONCAT('RES-', YEAR(@hoy), '-0003'), 3, 5, 2, DATEADD(DAY, -1, @hoy), DATEADD(DAY, 3, @hoy), 4, 3,
        1000.00, 60.00, 1060.00, 'EN_CURSO', DATEADD(DAY, -15, SYSDATETIME()));

-- 4) John - Doble Escalón, finalizada la semana pasada
INSERT INTO reserva (codigo, id_huesped, id_habitacion, id_usuario, fecha_entrada, fecha_salida, numero_noches, numero_huespedes,
                     subtotal_habitacion, subtotal_servicios, precio_total, estado_reserva, fecha_reserva)
VALUES (CONCAT('RES-', YEAR(@hoy), '-0004'), 4, 9, 2, DATEADD(DAY, -6, @hoy), DATEADD(DAY, -3, @hoy), 3, 1,
        360.00, 0.00, 360.00, 'FINALIZADA', DATEADD(DAY, -20, SYSDATETIME()));

-- 5) Carlos - Simple Los Héroes, cancelada con reembolso total
INSERT INTO reserva (codigo, id_huesped, id_habitacion, id_usuario, fecha_entrada, fecha_salida, numero_noches, numero_huespedes,
                     subtotal_habitacion, subtotal_servicios, precio_total, estado_reserva, fecha_reserva)
VALUES (CONCAT('RES-', YEAR(@hoy), '-0005'), 1, 3, 3, DATEADD(DAY, 12, @hoy), DATEADD(DAY, 14, @hoy), 2, 1,
        160.00, 0.00, 160.00, 'CANCELADA', DATEADD(DAY, -5, SYSDATETIME()));

INSERT INTO reserva_servicio (id_reserva, id_servicio, cantidad, precio_unitario) VALUES
 (1, 5, 1, 25.00),
 (3, 6, 1, 10.00),
 (3, 5, 2, 25.00);

/* ---------------- PAGOS ---------------- */
INSERT INTO pago (id_reserva, metodo_pago, monto, fecha_pago, estado_pago, referencia) VALUES
 (1, 'TARJETA',       1275.00, DATEADD(DAY, -10, SYSDATETIME()), 'COMPLETADO',  '**** 4242'),
 (3, 'EFECTIVO',      1060.00, DATEADD(DAY, -1,  SYSDATETIME()), 'COMPLETADO',  N'Recepción'),
 (4, 'TRANSFERENCIA',  360.00, DATEADD(DAY, -20, SYSDATETIME()), 'COMPLETADO',  'TRF-778812'),
 (5, 'TARJETA',        160.00, DATEADD(DAY, -5,  SYSDATETIME()), 'REEMBOLSADO', '**** 1881');

/* ---------------- CANCELACIONES ---------------- */
INSERT INTO cancelacion (id_reserva, id_usuario, fecha_cancelacion, motivo, horas_anticipacion,
                         porcentaje_penalizacion, monto_penalizacion, monto_reembolso)
VALUES (5, 3, DATEADD(DAY, -4, SYSDATETIME()), N'Cambio de planes de viaje', 384, 0.00, 0.00, 160.00);

/* ---------------- RECLAMOS ---------------- */
INSERT INTO reclamo (id_huesped, id_reserva, asunto, descripcion, estado, respuesta, fecha_creacion, fecha_respuesta) VALUES
 (4, 4, N'Ruido en la habitación', N'Durante la segunda noche hubo mucho ruido en el pasillo hasta tarde.', 'RESUELTO',
  N'Lamentamos lo ocurrido. Aplicamos un descuento del 10% en su próxima visita.', DATEADD(DAY, -3, SYSDATETIME()), DATEADD(DAY, -2, SYSDATETIME())),
 (1, 1, N'Solicitud de cuna', N'Quisiera saber si es posible agregar una cuna para bebé en la suite.', 'ABIERTO',
  NULL, DATEADD(DAY, -1, SYSDATETIME()), NULL);

PRINT 'Datos de prueba insertados correctamente.';
GO
