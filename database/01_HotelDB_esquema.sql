/* =====================================================================
   Sistema de Gestión de Reservas Hoteleras "Añil" - DWF404
   ===================================================================== */

USE master;
GO

IF DB_ID('HotelDB') IS NOT NULL
BEGIN
    ALTER DATABASE HotelDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE HotelDB;
END
GO

CREATE DATABASE HotelDB;
GO

/* Usuario de SQL Server que usará la API (Spring Boot) para conectarse.
   Requiere que el servidor tenga habilitada la autenticación mixta */
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'hotel_app')
    CREATE LOGIN hotel_app WITH PASSWORD = 'Hotel2026*', CHECK_POLICY = OFF, DEFAULT_DATABASE = HotelDB;
GO

USE HotelDB;
GO

CREATE USER hotel_app FOR LOGIN hotel_app;
ALTER ROLE db_owner ADD MEMBER hotel_app;
GO

/* ---------------------------------------------------------------------
   1. USUARIO: cuentas de acceso al sistema
   --------------------------------------------------------------------- */
CREATE TABLE usuario (
    id_usuario      INT IDENTITY(1,1) NOT NULL,
    nombre          NVARCHAR(100)     NOT NULL,
    email           VARCHAR(80)       NOT NULL,
    password_hash   VARCHAR(100)      NOT NULL,
    rol             VARCHAR(20)       NOT NULL CONSTRAINT DF_usuario_rol DEFAULT 'HUESPED',
    activo          BIT               NOT NULL CONSTRAINT DF_usuario_activo DEFAULT 1,
    fecha_creacion  DATETIME2         NOT NULL CONSTRAINT DF_usuario_fecha DEFAULT SYSDATETIME(),
    CONSTRAINT PK_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT UQ_usuario_email UNIQUE (email),
    CONSTRAINT CK_usuario_rol CHECK (rol IN ('ADMIN', 'RECEPCIONISTA', 'HUESPED'))
);
GO

/* ---------------------------------------------------------------------
   2. HOTEL: cadena u hotel principal
   --------------------------------------------------------------------- */
CREATE TABLE hotel (
    id_hotel        INT IDENTITY(1,1) NOT NULL,
    nombre          NVARCHAR(100)     NOT NULL,
    direccion       NVARCHAR(200)     NOT NULL,
    ciudad          NVARCHAR(60)      NOT NULL,
    telefono        VARCHAR(15)       NOT NULL,
    email           VARCHAR(80)       NOT NULL,
    descripcion     NVARCHAR(300)     NULL,
    CONSTRAINT PK_hotel PRIMARY KEY (id_hotel),
    CONSTRAINT UQ_hotel_nombre UNIQUE (nombre)
);
GO

/* ---------------------------------------------------------------------
   3. SUCURSAL: sede física de un hotel
   --------------------------------------------------------------------- */
CREATE TABLE sucursal (
    id_sucursal     INT IDENTITY(1,1) NOT NULL,
    id_hotel        INT               NOT NULL,
    nombre          NVARCHAR(100)     NOT NULL,
    direccion       NVARCHAR(200)     NOT NULL,
    ciudad          NVARCHAR(60)      NOT NULL,
    telefono        VARCHAR(15)       NOT NULL,
    CONSTRAINT PK_sucursal PRIMARY KEY (id_sucursal),
    CONSTRAINT FK_sucursal_hotel FOREIGN KEY (id_hotel) REFERENCES hotel (id_hotel),
    CONSTRAINT UQ_sucursal_hotel_nombre UNIQUE (id_hotel, nombre)
);
GO

/* ---------------------------------------------------------------------
   4. TIPO_HABITACION: categoría y tarifa por noche
   --------------------------------------------------------------------- */
CREATE TABLE tipo_habitacion (
    id_tipo           INT IDENTITY(1,1) NOT NULL,
    nombre            NVARCHAR(50)      NOT NULL,
    descripcion       NVARCHAR(250)     NULL,
    capacidad_maxima  INT               NOT NULL,
    precio_noche      DECIMAL(10,2)     NOT NULL,
    CONSTRAINT PK_tipo_habitacion PRIMARY KEY (id_tipo),
    CONSTRAINT UQ_tipo_habitacion_nombre UNIQUE (nombre),
    CONSTRAINT CK_tipo_capacidad CHECK (capacidad_maxima BETWEEN 1 AND 10),
    CONSTRAINT CK_tipo_precio CHECK (precio_noche > 0)
);
GO

/* ---------------------------------------------------------------------
   5. HABITACION
   --------------------------------------------------------------------- */
CREATE TABLE habitacion (
    id_habitacion   INT IDENTITY(1,1) NOT NULL,
    id_sucursal     INT               NOT NULL,
    id_tipo         INT               NOT NULL,
    numero          VARCHAR(10)       NOT NULL,
    piso            INT               NOT NULL,
    estado          VARCHAR(20)       NOT NULL CONSTRAINT DF_habitacion_estado DEFAULT 'DISPONIBLE',
    CONSTRAINT PK_habitacion PRIMARY KEY (id_habitacion),
    CONSTRAINT FK_habitacion_sucursal FOREIGN KEY (id_sucursal) REFERENCES sucursal (id_sucursal),
    CONSTRAINT FK_habitacion_tipo FOREIGN KEY (id_tipo) REFERENCES tipo_habitacion (id_tipo),
    CONSTRAINT UQ_habitacion_sucursal_numero UNIQUE (id_sucursal, numero),
    CONSTRAINT CK_habitacion_piso CHECK (piso BETWEEN 0 AND 60),
    CONSTRAINT CK_habitacion_estado CHECK (estado IN ('DISPONIBLE', 'OCUPADA', 'MANTENIMIENTO'))
);
GO

/* ---------------------------------------------------------------------
   6. SERVICIO: incluido en la habitación (adicional = 0)
                o contratable en la reserva (adicional = 1)
   --------------------------------------------------------------------- */
CREATE TABLE servicio (
    id_servicio     INT IDENTITY(1,1) NOT NULL,
    nombre          NVARCHAR(80)      NOT NULL,
    descripcion     NVARCHAR(250)     NULL,
    costo           DECIMAL(10,2)     NOT NULL CONSTRAINT DF_servicio_costo DEFAULT 0,
    adicional       BIT               NOT NULL CONSTRAINT DF_servicio_adicional DEFAULT 0,
    CONSTRAINT PK_servicio PRIMARY KEY (id_servicio),
    CONSTRAINT UQ_servicio_nombre UNIQUE (nombre),
    CONSTRAINT CK_servicio_costo CHECK (costo >= 0)
);
GO

/* ---------------------------------------------------------------------
   7. HABITACION_SERVICIO: relación N:M (servicios incluidos)
   --------------------------------------------------------------------- */
CREATE TABLE habitacion_servicio (
    id_habitacion   INT NOT NULL,
    id_servicio     INT NOT NULL,
    CONSTRAINT PK_habitacion_servicio PRIMARY KEY (id_habitacion, id_servicio),
    CONSTRAINT FK_hs_habitacion FOREIGN KEY (id_habitacion) REFERENCES habitacion (id_habitacion) ON DELETE CASCADE,
    CONSTRAINT FK_hs_servicio FOREIGN KEY (id_servicio) REFERENCES servicio (id_servicio) ON DELETE CASCADE
);
GO

/* ---------------------------------------------------------------------
   8. HUESPED: persona que se hospeda. Puede estar vinculada a una
      cuenta de usuario (rol HUESPED) o haber sido registrada en recepción.
   --------------------------------------------------------------------- */
CREATE TABLE huesped (
    id_huesped        INT IDENTITY(1,1) NOT NULL,
    id_usuario        INT               NULL,
    nombre_completo   NVARCHAR(100)     NOT NULL,
    fecha_nacimiento  DATE              NOT NULL,
    tipo_documento    VARCHAR(10)       NOT NULL CONSTRAINT DF_huesped_tipo_doc DEFAULT 'DUI',
    documento         VARCHAR(20)       NOT NULL,
    telefono          VARCHAR(15)       NOT NULL,
    email             VARCHAR(80)       NOT NULL,
    fecha_registro    DATETIME2         NOT NULL CONSTRAINT DF_huesped_fecha DEFAULT SYSDATETIME(),
    CONSTRAINT PK_huesped PRIMARY KEY (id_huesped),
    CONSTRAINT FK_huesped_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT UQ_huesped_documento UNIQUE (documento),
    CONSTRAINT CK_huesped_tipo_doc CHECK (tipo_documento IN ('DUI', 'PASAPORTE'))
);
GO

-- Un usuario solo puede tener un perfil de huésped (se permiten varios NULL)
CREATE UNIQUE INDEX UX_huesped_usuario ON huesped (id_usuario) WHERE id_usuario IS NOT NULL;
GO

/* ---------------------------------------------------------------------
   9. RESERVA
   --------------------------------------------------------------------- */
CREATE TABLE reserva (
    id_reserva              INT IDENTITY(1,1) NOT NULL,
    codigo                  VARCHAR(20)       NOT NULL,
    id_huesped              INT               NOT NULL,
    id_habitacion           INT               NOT NULL,
    id_usuario              INT               NULL,      -- usuario que registró la reserva
    fecha_entrada           DATE              NOT NULL,
    fecha_salida            DATE              NOT NULL,
    numero_noches           INT               NOT NULL,
    numero_huespedes        INT               NOT NULL,
    subtotal_habitacion     DECIMAL(10,2)     NOT NULL,
    subtotal_servicios      DECIMAL(10,2)     NOT NULL CONSTRAINT DF_reserva_sub_serv DEFAULT 0,
    precio_total            DECIMAL(10,2)     NOT NULL,
    estado_reserva          VARCHAR(20)       NOT NULL CONSTRAINT DF_reserva_estado DEFAULT 'PENDIENTE',
    solicitudes_especiales  NVARCHAR(250)     NULL,
    fecha_reserva           DATETIME2         NOT NULL CONSTRAINT DF_reserva_fecha DEFAULT SYSDATETIME(),
    CONSTRAINT PK_reserva PRIMARY KEY (id_reserva),
    CONSTRAINT UQ_reserva_codigo UNIQUE (codigo),
    CONSTRAINT FK_reserva_huesped FOREIGN KEY (id_huesped) REFERENCES huesped (id_huesped),
    CONSTRAINT FK_reserva_habitacion FOREIGN KEY (id_habitacion) REFERENCES habitacion (id_habitacion),
    CONSTRAINT FK_reserva_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT CK_reserva_fechas CHECK (fecha_salida > fecha_entrada),
    CONSTRAINT CK_reserva_noches CHECK (numero_noches > 0),
    CONSTRAINT CK_reserva_huespedes CHECK (numero_huespedes > 0),
    CONSTRAINT CK_reserva_total CHECK (precio_total > 0),
    CONSTRAINT CK_reserva_estado CHECK (estado_reserva IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO', 'FINALIZADA', 'CANCELADA'))
);
GO

CREATE INDEX IX_reserva_habitacion_fechas ON reserva (id_habitacion, fecha_entrada, fecha_salida);
GO

/* ---------------------------------------------------------------------
   10. RESERVA_SERVICIO: servicios adicionales contratados en una reserva
   --------------------------------------------------------------------- */
CREATE TABLE reserva_servicio (
    id_reserva_servicio  INT IDENTITY(1,1) NOT NULL,
    id_reserva           INT               NOT NULL,
    id_servicio          INT               NOT NULL,
    cantidad             INT               NOT NULL CONSTRAINT DF_rs_cantidad DEFAULT 1,
    precio_unitario      DECIMAL(10,2)     NOT NULL,
    CONSTRAINT PK_reserva_servicio PRIMARY KEY (id_reserva_servicio),
    CONSTRAINT FK_rs_reserva FOREIGN KEY (id_reserva) REFERENCES reserva (id_reserva) ON DELETE CASCADE,
    CONSTRAINT FK_rs_servicio FOREIGN KEY (id_servicio) REFERENCES servicio (id_servicio),
    CONSTRAINT UQ_rs_reserva_servicio UNIQUE (id_reserva, id_servicio),
    CONSTRAINT CK_rs_cantidad CHECK (cantidad BETWEEN 1 AND 10),
    CONSTRAINT CK_rs_precio CHECK (precio_unitario >= 0)
);
GO

/* ---------------------------------------------------------------------
   11. PAGO (1:1 con reserva)
   --------------------------------------------------------------------- */
CREATE TABLE pago (
    id_pago         INT IDENTITY(1,1) NOT NULL,
    id_reserva      INT               NOT NULL,
    metodo_pago     VARCHAR(20)       NOT NULL,
    monto           DECIMAL(10,2)     NOT NULL,
    fecha_pago      DATETIME2         NOT NULL CONSTRAINT DF_pago_fecha DEFAULT SYSDATETIME(),
    estado_pago     VARCHAR(20)       NOT NULL CONSTRAINT DF_pago_estado DEFAULT 'COMPLETADO',
    referencia      VARCHAR(40)       NULL,
    CONSTRAINT PK_pago PRIMARY KEY (id_pago),
    CONSTRAINT FK_pago_reserva FOREIGN KEY (id_reserva) REFERENCES reserva (id_reserva) ON DELETE CASCADE,
    CONSTRAINT UQ_pago_reserva UNIQUE (id_reserva),
    CONSTRAINT CK_pago_metodo CHECK (metodo_pago IN ('TARJETA', 'EFECTIVO', 'TRANSFERENCIA')),
    CONSTRAINT CK_pago_monto CHECK (monto > 0),
    CONSTRAINT CK_pago_estado CHECK (estado_pago IN ('COMPLETADO', 'REEMBOLSADO', 'REEMBOLSO_PARCIAL'))
);
GO

/* ---------------------------------------------------------------------
   12. CANCELACION (1:1 con reserva)
   --------------------------------------------------------------------- */
CREATE TABLE cancelacion (
    id_cancelacion           INT IDENTITY(1,1) NOT NULL,
    id_reserva               INT               NOT NULL,
    id_usuario               INT               NULL,     -- quién canceló
    fecha_cancelacion        DATETIME2         NOT NULL CONSTRAINT DF_cancelacion_fecha DEFAULT SYSDATETIME(),
    motivo                   NVARCHAR(250)     NOT NULL,
    horas_anticipacion       INT               NOT NULL,
    porcentaje_penalizacion  DECIMAL(5,2)      NOT NULL,
    monto_penalizacion       DECIMAL(10,2)     NOT NULL CONSTRAINT DF_cancelacion_pen DEFAULT 0,
    monto_reembolso          DECIMAL(10,2)     NOT NULL CONSTRAINT DF_cancelacion_reem DEFAULT 0,
    CONSTRAINT PK_cancelacion PRIMARY KEY (id_cancelacion),
    CONSTRAINT FK_cancelacion_reserva FOREIGN KEY (id_reserva) REFERENCES reserva (id_reserva) ON DELETE CASCADE,
    CONSTRAINT FK_cancelacion_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT UQ_cancelacion_reserva UNIQUE (id_reserva),
    CONSTRAINT CK_cancelacion_porcentaje CHECK (porcentaje_penalizacion BETWEEN 0 AND 100),
    CONSTRAINT CK_cancelacion_montos CHECK (monto_penalizacion >= 0 AND monto_reembolso >= 0)
);
GO

/* ---------------------------------------------------------------------
   13. RECLAMO: quejas o solicitudes de los huéspedes
   --------------------------------------------------------------------- */
CREATE TABLE reclamo (
    id_reclamo       INT IDENTITY(1,1) NOT NULL,
    id_huesped       INT               NOT NULL,
    id_reserva       INT               NULL,
    asunto           NVARCHAR(100)     NOT NULL,
    descripcion      NVARCHAR(500)     NOT NULL,
    estado           VARCHAR(20)       NOT NULL CONSTRAINT DF_reclamo_estado DEFAULT 'ABIERTO',
    respuesta        NVARCHAR(500)     NULL,
    fecha_creacion   DATETIME2         NOT NULL CONSTRAINT DF_reclamo_fecha DEFAULT SYSDATETIME(),
    fecha_respuesta  DATETIME2         NULL,
    CONSTRAINT PK_reclamo PRIMARY KEY (id_reclamo),
    CONSTRAINT FK_reclamo_huesped FOREIGN KEY (id_huesped) REFERENCES huesped (id_huesped),
    CONSTRAINT FK_reclamo_reserva FOREIGN KEY (id_reserva) REFERENCES reserva (id_reserva),
    CONSTRAINT CK_reclamo_estado CHECK (estado IN ('ABIERTO', 'EN_PROCESO', 'RESUELTO'))
);
GO

PRINT 'Base de datos HotelDB creada correctamente. Ejecute ahora 02_HotelDB_datos.sql';
GO
