-- Datos de ejemplo. Idempotente: se ejecuta en cada arranque sin duplicar nada.
--   * restaurantes y productos: ON CONFLICT DO NOTHING sobre sus UNIQUE
--   * pedidos, líneas y eventos: NOT EXISTS por (cliente_id, fecha_creacion) y claves naturales
-- Los ids son autogenerados, por eso todo se enlaza por nombre y no por id.

-- ---------------------------------------------------------------- restaurantes
INSERT INTO restaurante (nombre, categoria, activo) VALUES
    ('Burger Lab',        'HAMBURGUESERIA', TRUE),
    ('Sakura Sushi',      'SUSHI',          TRUE),
    ('Pizzeria Napoli',   'PIZZERIA',       TRUE),
    ('Smash House',       'HAMBURGUESERIA', TRUE),
    -- inactivo: sirve para probar el 422 "restaurante inactivo" al crear un pedido
    ('Trattoria Cerrada', 'PIZZERIA',       FALSE)
ON CONFLICT (nombre) DO NOTHING;

-- ------------------------------------------------------------------- menú
INSERT INTO producto_menu (restaurante_id, nombre, precio, disponible)
SELECT r.id, v.nombre, v.precio, v.disponible
FROM (VALUES
    ('Burger Lab',        'Classic Burger',          9.50, TRUE),
    ('Burger Lab',        'Doble Bacon',            11.90, TRUE),
    ('Burger Lab',        'Patatas fritas',          3.50, TRUE),
    ('Burger Lab',        'Refresco cola',           2.20, TRUE),
    -- no disponible: sirve para probar el 422 "producto no disponible"
    ('Burger Lab',        'Burger Vegana',          10.50, FALSE),
    ('Sakura Sushi',      'Sushi Mix 12 piezas',    18.90, TRUE),
    ('Sakura Sushi',      'Nigiri salmón (2 uds)',   5.50, TRUE),
    ('Sakura Sushi',      'Maki California (8 uds)', 8.90, TRUE),
    ('Sakura Sushi',      'Edamame',                 4.50, TRUE),
    ('Pizzeria Napoli',   'Margarita',               8.50, TRUE),
    ('Pizzeria Napoli',   'Cuatro quesos',          10.50, TRUE),
    ('Pizzeria Napoli',   'Diavola',                10.90, TRUE),
    ('Pizzeria Napoli',   'Tiramisú',                4.50, TRUE),
    ('Pizzeria Napoli',   'Calzone',                 9.90, FALSE),
    ('Smash House',       'Smash Simple',            8.50, TRUE),
    ('Smash House',       'Smash Doble',            10.90, TRUE),
    ('Smash House',       'Aros de cebolla',         3.90, TRUE),
    ('Trattoria Cerrada', 'Lasaña',                 11.50, TRUE),
    ('Trattoria Cerrada', 'Spaghetti carbonara',    10.50, TRUE)
) AS v (restaurante, nombre, precio, disponible)
JOIN restaurante r ON r.nombre = v.restaurante
-- WHERE TRUE evita la ambigüedad del parser entre el ON del JOIN y el ON CONFLICT
WHERE TRUE
ON CONFLICT (restaurante_id, nombre) DO NOTHING;

-- ---------------------------------------------------------------- pedidos
-- cliente 1: un pedido ENTREGADO (cancelar da 409) y uno EN_REPARTO (para el seguimiento SSE)
-- cliente 2: un pedido PEDIDO_RECIBIDO (cancelable)
-- cliente 3: un pedido ya CANCELADO (cancelar da 409)
INSERT INTO pedido (restaurante_id, cliente_id, estado, fecha_creacion, total, tiempo_estimado_minutos)
SELECT r.id, v.cliente_id, v.estado, v.fecha_creacion, v.total, v.tiempo
FROM (VALUES
    ('Burger Lab',      1, 'ENTREGADO',       TIMESTAMP '2026-10-05 20:15:00', 22.50, 30),
    ('Sakura Sushi',    1, 'EN_REPARTO',      TIMESTAMP '2026-10-06 13:05:00', 27.90, 40),
    ('Pizzeria Napoli', 2, 'PEDIDO_RECIBIDO', TIMESTAMP '2026-10-06 13:20:00', 21.50, 35),
    ('Burger Lab',      3, 'CANCELADO',       TIMESTAMP '2026-10-04 21:00:00', 11.90, 30)
) AS v (restaurante, cliente_id, estado, fecha_creacion, total, tiempo)
JOIN restaurante r ON r.nombre = v.restaurante
WHERE NOT EXISTS (
    SELECT 1 FROM pedido p
    WHERE p.cliente_id = v.cliente_id AND p.fecha_creacion = v.fecha_creacion
);

-- ----------------------------------------------------------------- líneas
-- precio_unitario se copia del producto, igual que hace CreatePedidoService
INSERT INTO linea_pedido (pedido_id, producto_id, cantidad, precio_unitario)
SELECT p.id, pm.id, v.cantidad, pm.precio
FROM (VALUES
    (1, TIMESTAMP '2026-10-05 20:15:00', 'Classic Burger',          2),
    (1, TIMESTAMP '2026-10-05 20:15:00', 'Patatas fritas',          1),
    (1, TIMESTAMP '2026-10-06 13:05:00', 'Sushi Mix 12 piezas',     1),
    (1, TIMESTAMP '2026-10-06 13:05:00', 'Edamame',                 2),
    (2, TIMESTAMP '2026-10-06 13:20:00', 'Margarita',               2),
    (2, TIMESTAMP '2026-10-06 13:20:00', 'Tiramisú',                1),
    (3, TIMESTAMP '2026-10-04 21:00:00', 'Doble Bacon',             1)
) AS v (cliente_id, fecha_creacion, producto, cantidad)
JOIN pedido p ON p.cliente_id = v.cliente_id AND p.fecha_creacion = v.fecha_creacion
JOIN producto_menu pm ON pm.restaurante_id = p.restaurante_id AND pm.nombre = v.producto
WHERE NOT EXISTS (
    SELECT 1 FROM linea_pedido l WHERE l.pedido_id = p.id AND l.producto_id = pm.id
);

-- ----------------------------------------------------------------- eventos
-- minutos = desplazamiento sobre fecha_creacion del pedido
INSERT INTO evento_seguimiento (pedido_id, tipo, "timestamp", metadata)
SELECT p.id, v.tipo, p.fecha_creacion + v.minutos * INTERVAL '1 minute', v.metadata
FROM (VALUES
    (1, TIMESTAMP '2026-10-05 20:15:00', 'PEDIDO_RECIBIDO',      0, CAST(NULL AS TEXT)),
    (1, TIMESTAMP '2026-10-05 20:15:00', 'EN_PREPARACION',       3, NULL),
    (1, TIMESTAMP '2026-10-05 20:15:00', 'REPARTIDOR_ASIGNADO', 15, NULL),
    (1, TIMESTAMP '2026-10-05 20:15:00', 'EN_REPARTO',          18, '{"lat":40.4168,"lon":-3.7038}'),
    (1, TIMESTAMP '2026-10-05 20:15:00', 'ENTREGADO',           32, NULL),
    (1, TIMESTAMP '2026-10-06 13:05:00', 'PEDIDO_RECIBIDO',      0, NULL),
    (1, TIMESTAMP '2026-10-06 13:05:00', 'EN_PREPARACION',       4, NULL),
    (1, TIMESTAMP '2026-10-06 13:05:00', 'REPARTIDOR_ASIGNADO', 20, NULL),
    (1, TIMESTAMP '2026-10-06 13:05:00', 'EN_REPARTO',          25, '{"lat":40.4200,"lon":-3.7050}'),
    (2, TIMESTAMP '2026-10-06 13:20:00', 'PEDIDO_RECIBIDO',      0, NULL),
    (3, TIMESTAMP '2026-10-04 21:00:00', 'PEDIDO_RECIBIDO',      0, NULL),
    (3, TIMESTAMP '2026-10-04 21:00:00', 'CANCELADO',            2, NULL)
) AS v (cliente_id, fecha_creacion, tipo, minutos, metadata)
JOIN pedido p ON p.cliente_id = v.cliente_id AND p.fecha_creacion = v.fecha_creacion
WHERE NOT EXISTS (
    SELECT 1 FROM evento_seguimiento e WHERE e.pedido_id = p.id AND e.tipo = v.tipo
);
