-- Esquema de FastTrack Delivery (PostgreSQL).
-- Idempotente: se ejecuta en cada arranque (spring.sql.init.mode=always), por eso todo es IF NOT EXISTS.
-- Los nombres van en minúsculas: Spring Data R2DBC emite PEDIDO, RESTAURANTE_ID... sin comillas y
-- Postgres los pliega a minúsculas, así que coinciden con las anotaciones @Table/@Column.

CREATE TABLE IF NOT EXISTS restaurante (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre    VARCHAR(120) NOT NULL,
    categoria VARCHAR(30)  NOT NULL,
    activo    BOOLEAN      NOT NULL DEFAULT TRUE,
    -- Esta constraint es la que dispara el DataIntegrityViolationException -> RestauranteConflictException
    CONSTRAINT uk_restaurante_nombre UNIQUE (nombre),
    CONSTRAINT ck_restaurante_categoria CHECK (categoria IN ('HAMBURGUESERIA', 'SUSHI', 'PIZZERIA'))
);

CREATE TABLE IF NOT EXISTS producto_menu (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    restaurante_id BIGINT         NOT NULL REFERENCES restaurante (id),
    nombre         VARCHAR(120)   NOT NULL,
    precio         NUMERIC(10, 2) NOT NULL,
    disponible     BOOLEAN        NOT NULL DEFAULT TRUE,
    -- Dispara el DataIntegrityViolationException -> ProductoMenuConflictException.
    -- Su índice también sirve a findByRestauranteId (restaurante_id es la primera columna).
    CONSTRAINT uk_producto_menu_restaurante_nombre UNIQUE (restaurante_id, nombre),
    CONSTRAINT ck_producto_menu_precio CHECK (precio > 0)
);

CREATE TABLE IF NOT EXISTS pedido (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    restaurante_id          BIGINT         NOT NULL REFERENCES restaurante (id),
    -- cliente_id sin FK: los clientes no son de este microservicio
    cliente_id              BIGINT         NOT NULL,
    estado                  VARCHAR(30)    NOT NULL,
    fecha_creacion          TIMESTAMP      NOT NULL,
    total                   NUMERIC(10, 2) NOT NULL,
    tiempo_estimado_minutos INTEGER        NOT NULL,
    CONSTRAINT ck_pedido_estado CHECK (estado IN
        ('PEDIDO_RECIBIDO', 'EN_PREPARACION', 'REPARTIDOR_ASIGNADO', 'EN_REPARTO', 'ENTREGADO', 'CANCELADO')),
    CONSTRAINT ck_pedido_total CHECK (total >= 0)
);
CREATE INDEX IF NOT EXISTS ix_pedido_cliente_id ON pedido (cliente_id);
CREATE INDEX IF NOT EXISTS ix_pedido_restaurante_id ON pedido (restaurante_id);

CREATE TABLE IF NOT EXISTS linea_pedido (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_id       BIGINT         NOT NULL REFERENCES pedido (id),
    producto_id     BIGINT         NOT NULL REFERENCES producto_menu (id),
    cantidad        INTEGER        NOT NULL,
    -- Foto del precio del producto en el momento de crear el pedido
    precio_unitario NUMERIC(10, 2) NOT NULL,
    CONSTRAINT ck_linea_pedido_cantidad CHECK (cantidad >= 1)
);
-- Lo usan findByPedidoId y findByPedidoIdIn (carga de líneas de los pedidos)
CREATE INDEX IF NOT EXISTS ix_linea_pedido_pedido_id ON linea_pedido (pedido_id);

CREATE TABLE IF NOT EXISTS evento_seguimiento (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_id   BIGINT        NOT NULL REFERENCES pedido (id),
    tipo        VARCHAR(30)   NOT NULL,
    "timestamp" TIMESTAMP     NOT NULL,
    -- JSON serializado como texto: EventoSeguimientoEntity.metadata es un String y
    -- r2dbc-postgresql no convierte String -> JSONB (ver MetadataMapper)
    metadata    TEXT,
    CONSTRAINT ck_evento_seguimiento_tipo CHECK (tipo IN
        ('PEDIDO_RECIBIDO', 'EN_PREPARACION', 'REPARTIDOR_ASIGNADO', 'EN_REPARTO', 'ENTREGADO', 'CANCELADO'))
);
CREATE INDEX IF NOT EXISTS ix_evento_seguimiento_pedido ON evento_seguimiento (pedido_id, "timestamp");
