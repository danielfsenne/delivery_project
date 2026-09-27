-- Dados de demonstração. owner_id = 2 corresponde a restaurante@rota.dev (ver DemoUsersSeeder no auth-service).

INSERT INTO restaurants (id, owner_id, name, description, cuisine, phone, image_url, delivery_fee, min_order_value,
                         delivery_time_min, delivery_time_max, street, number, district, city, state, zip_code,
                         latitude, longitude)
VALUES (1, 2, 'Burger House', 'Hambúrgueres artesanais na brasa', 'Hambúrguer', '(16) 3700-1000',
        'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800', 5.99, 20.00, 30, 45,
        'Rua Voluntários da Franca', '1200', 'Centro', 'Franca', 'SP', '14400-490', -20.5386, -47.4009),
       (2, 2, 'Sushi Kento', 'Culinária japonesa tradicional e combinados', 'Japonesa', '(16) 3700-2000',
        'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=800', 7.50, 40.00, 40, 60,
        'Avenida Presidente Vargas', '800', 'Cidade Nova', 'Franca', 'SP', '14401-110', -20.5310, -47.3950),
       (3, 2, 'Forno da Nonna', 'Pizzas em forno a lenha', 'Pizza', '(16) 3700-3000',
        'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800', 0.00, 30.00, 35, 50,
        'Avenida Champagnat', '1500', 'Jardim Petraglia', 'Franca', 'SP', '14409-015', -20.5450, -47.4100);

SELECT setval('restaurants_id_seq', 3);

-- Abertos todos os dias, o dia inteiro, para facilitar a demonstração.
INSERT INTO opening_hours (restaurant_id, day_of_week, opens_at, closes_at)
SELECT r.id, d.day, TIME '00:00', TIME '23:59'
FROM restaurants r
CROSS JOIN (VALUES ('MONDAY'), ('TUESDAY'), ('WEDNESDAY'), ('THURSDAY'), ('FRIDAY'), ('SATURDAY'), ('SUNDAY')) AS d(day);

INSERT INTO categories (id, restaurant_id, name, position)
VALUES (1, 1, 'Hambúrgueres', 0),
       (2, 1, 'Acompanhamentos', 1),
       (3, 1, 'Bebidas', 2),
       (4, 2, 'Combinados', 0),
       (5, 2, 'Temakis', 1),
       (6, 3, 'Pizzas Salgadas', 0),
       (7, 3, 'Pizzas Doces', 1),
       (8, 3, 'Bebidas', 2);

SELECT setval('categories_id_seq', 8);

INSERT INTO products (id, category_id, name, description, price)
VALUES (1, 1, 'X-Bacon', 'Pão brioche, blend 180g, cheddar e bacon crocante', 32.90),
       (2, 1, 'X-Salada', 'Pão brioche, blend 180g, queijo, alface e tomate', 28.90),
       (3, 1, 'X-Tudo', 'Blend 180g, bacon, ovo, presunto, queijo e salada', 38.90),
       (4, 2, 'Batata Frita', 'Porção de 300g', 16.90),
       (5, 2, 'Onion Rings', 'Anéis de cebola empanados', 18.90),
       (6, 3, 'Coca-Cola 350ml', NULL, 6.50),
       (7, 3, 'Guaraná 350ml', NULL, 6.00),
       (8, 4, 'Combinado 20 peças', 'Sashimi, niguiri e uramaki', 69.90),
       (9, 4, 'Combinado 40 peças', 'Seleção do chef', 119.90),
       (10, 5, 'Temaki Salmão', 'Salmão fresco e cebolinha', 29.90),
       (11, 5, 'Temaki Skin', 'Pele de salmão grelhada', 24.90),
       (12, 6, 'Margherita', 'Molho, muçarela de búfala e manjericão', 54.90),
       (13, 6, 'Calabresa', 'Calabresa fatiada e cebola', 49.90),
       (14, 6, 'Quatro Queijos', 'Muçarela, provolone, parmesão e gorgonzola', 59.90),
       (15, 7, 'Chocolate com Morango', NULL, 52.90),
       (16, 8, 'Suco Natural 500ml', 'Laranja ou limão', 9.90);

SELECT setval('products_id_seq', 16);

INSERT INTO product_options (product_id, name, price)
VALUES (1, 'Bacon extra', 5.00),
       (1, 'Cheddar extra', 4.00),
       (2, 'Bacon', 5.00),
       (3, 'Carne extra', 9.00),
       (4, 'Cheddar e bacon', 7.00),
       (10, 'Cream cheese', 3.00),
       (12, 'Borda recheada', 10.00),
       (13, 'Borda recheada', 10.00),
       (14, 'Borda recheada', 10.00);
