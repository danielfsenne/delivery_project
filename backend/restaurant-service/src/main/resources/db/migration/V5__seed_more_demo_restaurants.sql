-- Mais restaurantes de demonstração, todos do restaurante@rota.dev (owner_id = 2).
-- Sem ids fixos: quem já cadastrou restaurantes pelo painel não entra em conflito de chave.
-- Categorias, produtos e adicionais são ligados pelo nome do restaurante.
-- As notas (rating_sum / rating_count) são fictícias, para a vitrine não começar toda como "Novo".

INSERT INTO restaurants (owner_id, name, description, cuisine, phone, image_url, delivery_fee, min_order_value,
                         delivery_time_min, delivery_time_max, street, number, district, city, state, zip_code,
                         latitude, longitude, rating_sum, rating_count)
VALUES (2, 'Cantina Bella Massa', 'Massas frescas e molhos feitos na casa', 'Italiana', '(16) 3700-4000',
        'https://images.unsplash.com/photo-1563379926898-05f4575a45d8?w=1200&auto=format', 6.90, 35.00, 35, 50,
        'Rua Major Claudiano', '1450', 'Centro', 'Franca', 'SP', '14400-690', -20.5372, -47.4031, 1006, 214),
       (2, 'Brasa Viva', 'Picanha, costela e espetinhos na brasa', 'Churrasco', '(16) 3700-5000',
        'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=1200&auto=format', 8.90, 40.00, 40, 55,
        'Avenida Alonso y Alonso', '2100', 'Jardim Consolação', 'Franca', 'SP', '14403-000', -20.518, -47.3905, 1709, 356),
       (2, 'Taquería El Sol', 'Tacos, burritos e nachos com receita mexicana', 'Mexicana', '(16) 3700-6000',
        'https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=1200&auto=format', 4.99, 25.00, 30, 45,
        'Rua Saldanha Marinho', '980', 'Estação', 'Franca', 'SP', '14405-040', -20.5335, -47.4085, 441, 98),
       (2, 'Verde Bowl', 'Pokes, saladas e wraps para comer leve', 'Saudável', '(16) 3700-7000',
        'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=1200&auto=format', 0.00, 25.00, 20, 35,
        'Avenida Dr. Hélio Palermo', '3300', 'Jardim Petraglia', 'Franca', 'SP', '14409-155', -20.5502, -47.4142, 916, 187),
       (2, 'Doce Encanto', 'Bolos, donuts e potes da felicidade', 'Doces', '(16) 3700-8000',
        'https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=1200&auto=format', 3.99, 15.00, 20, 30,
        'Rua General Telles', '1210', 'Centro', 'Franca', 'SP', '14400-650', -20.5368, -47.4002, 1970, 402),
       (2, 'Gelato & Cia', 'Sorvetes artesanais e milk-shakes', 'Sorvetes', '(16) 3700-8100',
        'https://images.unsplash.com/photo-1497034825429-c343d7c6a68f?w=1200&auto=format', 0.00, 20.00, 15, 25,
        'Avenida São Vicente', '500', 'Cidade Nova', 'Franca', 'SP', '14401-260', -20.529, -47.3931, 658, 143),
       (2, 'Café Grão Fino', 'Cafés especiais, brunch e panquecas', 'Café', '(16) 3700-8200',
        'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=1200&auto=format', 4.50, 15.00, 20, 35,
        'Rua Campos Salles', '1890', 'Centro', 'Franca', 'SP', '14400-760', -20.5395, -47.4048, 569, 121),
       (2, 'Masala House', 'Curries, samosas e pães indianos. Abre só no jantar', 'Indiana', '(16) 3700-8300',
        'https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=1200&auto=format', 7.90, 40.00, 45, 60,
        'Rua Voluntários da Franca', '2450', 'Vila Aparecida', 'Franca', 'SP', '14401-080', -20.5302, -47.398, 282, 64),
       (2, 'Lámen Kaze', 'Lámen artesanal e sushis', 'Japonesa', '(16) 3700-8400',
        'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=1200&auto=format', 6.50, 35.00, 35, 50,
        'Avenida Presidente Vargas', '1720', 'Cidade Nova', 'Franca', 'SP', '14401-115', -20.5275, -47.3962, 1118, 233),
       (2, 'Frango Crocante', 'Frango frito, tiras e baldes para dividir', 'Frango', '(16) 3700-8500',
        'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=1200&auto=format', 5.50, 25.00, 25, 40,
        'Avenida Brasil', '640', 'São José', 'Franca', 'SP', '14403-090', -20.5228, -47.4122, 757, 176),
       (2, 'Smash Bros Burger', 'Smash burgers prensados na chapa e batatas', 'Hambúrguer', '(16) 3700-8600',
        'https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=1200&auto=format', 0.00, 30.00, 25, 40,
        'Rua do Comércio', '455', 'Centro', 'Franca', 'SP', '14400-260', -20.5381, -47.4018, 1329, 289),
       (2, 'Pizzaria Vesúvio', 'Pizzas napolitanas de longa fermentação', 'Pizza', '(16) 3700-8700',
        'https://images.unsplash.com/photo-1590947132387-155cc02f3212?w=1200&auto=format', 5.00, 40.00, 40, 55,
        'Avenida Champagnat', '2600', 'Jardim Paulista', 'Franca', 'SP', '14409-030', -20.5478, -47.4166, 756, 168),
       (2, 'Sabor da Tailândia', 'Pad thai, curries e sopas tailandesas', 'Tailandesa', '(16) 3700-8800',
        'https://images.unsplash.com/photo-1617093727343-374698b1b08d?w=1200&auto=format', 8.50, 45.00, 40, 55,
        'Rua Ouvidor Freire', '1330', 'Centro', 'Franca', 'SP', '14400-500', -20.5355, -47.4062, 239, 52),
       (2, 'Empório Árabe', 'Shawarma, esfihas e kibes', 'Árabe', '(16) 3700-8900',
        'https://images.unsplash.com/photo-1529006557810-274b9b2fc783?w=1200&auto=format', 3.99, 20.00, 25, 40,
        'Avenida Major Nicácio', '1880', 'Jardim Petraglia', 'Franca', 'SP', '14409-010', -20.5455, -47.411, 964, 205);

-- Abertos o dia inteiro, menos o Masala House, que só abre no jantar e aparece em "Fechados agora" de dia.
INSERT INTO opening_hours (restaurant_id, day_of_week, opens_at, closes_at)
SELECT r.id, d.day,
       CASE WHEN r.name = 'Masala House' THEN TIME '18:00' ELSE TIME '00:00' END,
       CASE WHEN r.name = 'Masala House' THEN TIME '23:30' ELSE TIME '23:59' END
FROM restaurants r
CROSS JOIN (VALUES ('MONDAY'), ('TUESDAY'), ('WEDNESDAY'), ('THURSDAY'), ('FRIDAY'), ('SATURDAY'), ('SUNDAY')) AS d(day)
WHERE r.owner_id = 2
  AND r.name IN ('Cantina Bella Massa', 'Brasa Viva', 'Taquería El Sol', 'Verde Bowl', 'Doce Encanto', 'Gelato & Cia', 'Café Grão Fino', 'Masala House', 'Lámen Kaze', 'Frango Crocante', 'Smash Bros Burger', 'Pizzaria Vesúvio', 'Sabor da Tailândia', 'Empório Árabe');

INSERT INTO categories (restaurant_id, name, position)
SELECT r.id, c.name, c.position
FROM (VALUES
        ('Cantina Bella Massa', 'Massas', 0),
        ('Cantina Bella Massa', 'Entradas', 1),
        ('Cantina Bella Massa', 'Sobremesas', 2),
        ('Brasa Viva', 'Carnes', 0),
        ('Brasa Viva', 'Espetinhos', 1),
        ('Brasa Viva', 'Bebidas', 2),
        ('Taquería El Sol', 'Tacos', 0),
        ('Taquería El Sol', 'Burritos', 1),
        ('Taquería El Sol', 'Acompanhamentos', 2),
        ('Verde Bowl', 'Pokes', 0),
        ('Verde Bowl', 'Saladas', 1),
        ('Verde Bowl', 'Wraps', 2),
        ('Doce Encanto', 'Bolos', 0),
        ('Doce Encanto', 'Donuts', 1),
        ('Doce Encanto', 'Potes', 2),
        ('Gelato & Cia', 'Sorvetes', 0),
        ('Gelato & Cia', 'Milk-shakes', 1),
        ('Café Grão Fino', 'Cafés', 0),
        ('Café Grão Fino', 'Brunch', 1),
        ('Masala House', 'Curries', 0),
        ('Masala House', 'Entradas', 1),
        ('Masala House', 'Pães', 2),
        ('Lámen Kaze', 'Lámen', 0),
        ('Lámen Kaze', 'Sushis', 1),
        ('Frango Crocante', 'Frango', 0),
        ('Frango Crocante', 'Acompanhamentos', 1),
        ('Smash Bros Burger', 'Burgers', 0),
        ('Smash Bros Burger', 'Acompanhamentos', 1),
        ('Pizzaria Vesúvio', 'Pizzas', 0),
        ('Pizzaria Vesúvio', 'Bebidas', 1),
        ('Sabor da Tailândia', 'Pratos', 0),
        ('Empório Árabe', 'Shawarma', 0),
        ('Empório Árabe', 'Esfihas', 1),
        ('Empório Árabe', 'Kibes', 2)
     ) AS c(restaurant, name, position)
JOIN restaurants r ON r.owner_id = 2 AND r.name = c.restaurant;

INSERT INTO products (category_id, name, description, price, image_url)
SELECT c.id, p.name, p.description, p.price, p.image_url
FROM (VALUES
        ('Cantina Bella Massa', 'Massas', 'Penne all''arrabbiata', 'Molho de tomate apimentado, alho e manjericão'::varchar, 42.90, 'https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Cantina Bella Massa', 'Massas', 'Espaguete ao camarão', 'Camarões salteados no azeite, tomate-cereja e salsinha'::varchar, 64.90, 'https://images.unsplash.com/photo-1563379926898-05f4575a45d8?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Cantina Bella Massa', 'Massas', 'Lasanha à bolonhesa', 'Massa fresca, ragu de carne e molho branco gratinado'::varchar, 49.90, NULL::varchar),
        ('Cantina Bella Massa', 'Entradas', 'Bruschetta', 'Pão italiano tostado com tomate, alho e azeite'::varchar, 24.90, 'https://images.unsplash.com/photo-1625938145744-e380515399bf?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Cantina Bella Massa', 'Sobremesas', 'Tiramisù', 'Mascarpone, café e cacau'::varchar, 22.90, NULL::varchar),
        ('Brasa Viva', 'Carnes', 'Picanha fatiada 400g', 'Acompanha farofa e vinagrete'::varchar, 89.90, 'https://images.unsplash.com/photo-1558030006-450675393462?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Brasa Viva', 'Carnes', 'Costela no bafo', 'Costela bovina assada por 8 horas'::varchar, 74.90, 'https://images.unsplash.com/photo-1544025162-d76694265947?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Brasa Viva', 'Carnes', 'Bife ancho com fritas', 'Ancho grelhado e batatas fritas'::varchar, 69.90, 'https://images.unsplash.com/photo-1600891964092-4316c288032e?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Brasa Viva', 'Espetinhos', 'Espetinho misto', 'Carne, frango, linguiça e legumes'::varchar, 14.90, 'https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Brasa Viva', 'Espetinhos', 'Espetinho de frango', 'Coxa desossada temperada'::varchar, 11.90, NULL::varchar),
        ('Brasa Viva', 'Bebidas', 'Refrigerante lata', NULL::varchar, 6.50, NULL::varchar),
        ('Taquería El Sol', 'Tacos', 'Tacos de carnitas', '3 unidades com porco desfiado, cebola roxa e coentro'::varchar, 34.90, 'https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Taquería El Sol', 'Tacos', 'Tacos de frango', '3 unidades com frango chipotle e pico de gallo'::varchar, 31.90, NULL::varchar),
        ('Taquería El Sol', 'Burritos', 'Burrito de carne', 'Tortilha recheada com carne, feijão, arroz e queijo'::varchar, 36.90, 'https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Taquería El Sol', 'Acompanhamentos', 'Nachos com guacamole', 'Tortilhas crocantes, guacamole e sour cream'::varchar, 28.90, NULL::varchar),
        ('Verde Bowl', 'Pokes', 'Poke de salmão', 'Arroz, salmão, edamame, manga e gergelim'::varchar, 44.90, 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Verde Bowl', 'Pokes', 'Poke vegano', 'Arroz integral, tofu grelhado, abacate e legumes'::varchar, 36.90, NULL::varchar),
        ('Verde Bowl', 'Saladas', 'Salada completa', 'Folhas, grão-de-bico, abacate, tomate e ovos'::varchar, 32.90, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Verde Bowl', 'Saladas', 'Salada de folhas', 'Mix de folhas, cebola roxa e molho de mostarda'::varchar, 26.90, 'https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Verde Bowl', 'Wraps', 'Wrap de frango', 'Frango grelhado, repolho roxo e molho de iogurte'::varchar, 29.90, 'https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Doce Encanto', 'Bolos', 'Fatia de bolo de frutas vermelhas', 'Massa branca, creme e framboesas'::varchar, 18.90, 'https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Doce Encanto', 'Bolos', 'Bolo no pote de chocolate', 'Camadas de brownie, creme e calda'::varchar, 16.90, 'https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Doce Encanto', 'Donuts', 'Donut confeitado', 'Cobertura de chocolate e granulado colorido'::varchar, 9.90, 'https://images.unsplash.com/photo-1551024601-bec78aea704b?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Doce Encanto', 'Potes', 'Morango com creme', 'Morangos frescos e creme de leite ninho'::varchar, 15.90, 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Gelato & Cia', 'Sorvetes', 'Casquinha', 'Uma bola do sabor do dia'::varchar, 9.90, 'https://images.unsplash.com/photo-1497034825429-c343d7c6a68f?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Gelato & Cia', 'Sorvetes', 'Taça 3 bolas', 'Escolha três sabores e uma calda'::varchar, 22.90, NULL::varchar),
        ('Gelato & Cia', 'Milk-shakes', 'Milk-shake de chocolate', '500ml com chantilly'::varchar, 19.90, NULL::varchar),
        ('Café Grão Fino', 'Cafés', 'Cappuccino', 'Espresso, leite vaporizado e cacau'::varchar, 12.90, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Café Grão Fino', 'Cafés', 'Latte', 'Espresso duplo com leite cremoso'::varchar, 11.90, 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Café Grão Fino', 'Brunch', 'Ovos benedict', 'Pão brioche, ovos pochê, bacon e molho holandês'::varchar, 34.90, 'https://images.unsplash.com/photo-1608039829572-78524f79c4c7?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Café Grão Fino', 'Brunch', 'Rabanada com frutas', 'Brioche, frutas vermelhas e banana'::varchar, 29.90, 'https://images.unsplash.com/photo-1484723091739-30a097e8f929?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Café Grão Fino', 'Brunch', 'Panquecas com mel', 'Pilha de panquecas americanas'::varchar, 27.90, 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Café Grão Fino', 'Brunch', 'Torrada com ovo', 'Pão de fermentação natural e ovo caipira'::varchar, 19.90, 'https://images.unsplash.com/photo-1525351484163-7529414344d8?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Masala House', 'Curries', 'Chicken tikka masala', 'Frango marinado em molho de tomate e especiarias, com arroz basmati'::varchar, 52.90, 'https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Masala House', 'Curries', 'Curry de grão-de-bico', 'Chana masala com arroz basmati'::varchar, 42.90, NULL::varchar),
        ('Masala House', 'Entradas', 'Samosas', '3 unidades recheadas com batata e ervilha'::varchar, 22.90, 'https://images.unsplash.com/photo-1601050690597-df0568f70950?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Masala House', 'Pães', 'Pão naan', 'Pão indiano na manteiga de alho'::varchar, 9.90, NULL::varchar),
        ('Lámen Kaze', 'Lámen', 'Tonkotsu lámen', 'Caldo de porco, chashu, ovo marinado e cebolinha'::varchar, 46.90, 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Lámen Kaze', 'Lámen', 'Lámen picante', 'Caldo de missô apimentado e carne moída'::varchar, 48.90, NULL::varchar),
        ('Lámen Kaze', 'Sushis', 'Combinado 16 peças', 'Niguiris, uramakis e hossomakis'::varchar, 54.90, 'https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Lámen Kaze', 'Sushis', 'Uramaki de salmão', '8 unidades com cream cheese'::varchar, 32.90, 'https://images.unsplash.com/photo-1580822184713-fc5400e7fe10?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Lámen Kaze', 'Sushis', 'Barca 30 peças', 'Seleção do sushiman'::varchar, 99.90, 'https://images.unsplash.com/photo-1611143669185-af224c5e3252?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Frango Crocante', 'Frango', 'Balde 8 pedaços', 'Coxas e sobrecoxas empanadas e crocantes'::varchar, 64.90, 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Frango Crocante', 'Frango', 'Filé empanado', 'Peito de frango empanado com molho da casa'::varchar, 34.90, 'https://images.unsplash.com/photo-1562967914-608f82629710?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Frango Crocante', 'Frango', 'Asinhas 10 unidades', 'Com molho barbecue'::varchar, 36.90, NULL::varchar),
        ('Frango Crocante', 'Acompanhamentos', 'Batata frita', 'Porção de 300g'::varchar, 15.90, 'https://images.unsplash.com/photo-1576107232684-1279f390859f?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Smash Bros Burger', 'Burgers', 'Smash duplo', 'Dois smash de 90g, cheddar e cebola caramelizada'::varchar, 33.90, 'https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Smash Bros Burger', 'Burgers', 'Smash bacon', 'Smash de 90g, bacon crocante e maionese da casa'::varchar, 36.90, 'https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Smash Bros Burger', 'Burgers', 'Combo smash e fritas', 'Smash duplo, fritas e refrigerante'::varchar, 42.90, 'https://images.unsplash.com/photo-1594212699903-ec8a3eca50f5?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Smash Bros Burger', 'Acompanhamentos', 'Fritas rústicas', 'Com alecrim e páprica'::varchar, 16.90, 'https://images.unsplash.com/photo-1576107232684-1279f390859f?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Pizzaria Vesúvio', 'Pizzas', 'Margherita napolitana', 'Tomate San Marzano, fior di latte e manjericão'::varchar, 56.90, 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Pizzaria Vesúvio', 'Pizzas', 'Pepperoni', 'Muçarela e pepperoni fatiado'::varchar, 62.90, 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Pizzaria Vesúvio', 'Pizzas', 'Rúcula com tomate seco', 'Muçarela de búfala, rúcula e tomate seco'::varchar, 64.90, 'https://images.unsplash.com/photo-1590947132387-155cc02f3212?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Pizzaria Vesúvio', 'Bebidas', 'Refrigerante 2L', NULL::varchar, 14.90, NULL::varchar),
        ('Sabor da Tailândia', 'Pratos', 'Tom yum', 'Sopa picante de camarão com capim-limão'::varchar, 48.90, 'https://images.unsplash.com/photo-1617093727343-374698b1b08d?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Sabor da Tailândia', 'Pratos', 'Curry vermelho com camarão', 'Leite de coco, pimenta e arroz jasmim'::varchar, 62.90, 'https://images.unsplash.com/photo-1559847844-5315695dadae?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Sabor da Tailândia', 'Pratos', 'Pad thai', 'Talharim de arroz, frango, amendoim e broto de feijão'::varchar, 52.90, NULL::varchar),
        ('Empório Árabe', 'Shawarma', 'Shawarma de carne', 'Pão sírio, carne, tahine e salada'::varchar, 32.90, 'https://images.unsplash.com/photo-1529006557810-274b9b2fc783?w=400&h=400&fit=crop&auto=format'::varchar),
        ('Empório Árabe', 'Shawarma', 'Shawarma de frango', 'Pão sírio, frango, coalhada e picles'::varchar, 29.90, NULL::varchar),
        ('Empório Árabe', 'Esfihas', 'Esfiha de carne', NULL::varchar, 7.90, NULL::varchar),
        ('Empório Árabe', 'Esfihas', 'Esfiha de queijo', NULL::varchar, 7.50, NULL::varchar),
        ('Empório Árabe', 'Kibes', 'Kibe frito', '4 unidades'::varchar, 19.90, NULL::varchar)
     ) AS p(restaurant, category, name, description, price, image_url)
JOIN restaurants r ON r.owner_id = 2 AND r.name = p.restaurant
JOIN categories c ON c.restaurant_id = r.id AND c.name = p.category;

INSERT INTO product_options (product_id, name, price)
SELECT p.id, o.name, o.price
FROM (VALUES
        ('Smash Bros Burger', 'Smash duplo', 'Bacon extra', 5.00),
        ('Smash Bros Burger', 'Smash duplo', 'Cheddar extra', 4.00),
        ('Verde Bowl', 'Poke de salmão', 'Cream cheese', 4.00),
        ('Verde Bowl', 'Poke de salmão', 'Porção extra de salmão', 12.00),
        ('Pizzaria Vesúvio', 'Margherita napolitana', 'Borda recheada', 10.00),
        ('Pizzaria Vesúvio', 'Pepperoni', 'Borda recheada', 10.00),
        ('Lámen Kaze', 'Tonkotsu lámen', 'Ovo marinado extra', 5.00),
        ('Lámen Kaze', 'Tonkotsu lámen', 'Chashu extra', 9.00),
        ('Gelato & Cia', 'Casquinha', 'Cobertura de chocolate', 3.00),
        ('Taquería El Sol', 'Tacos de carnitas', 'Guacamole extra', 6.00),
        ('Café Grão Fino', 'Cappuccino', 'Leite vegetal', 3.00),
        ('Brasa Viva', 'Picanha fatiada 400g', 'Arroz e feijão tropeiro', 14.00)
     ) AS o(restaurant, product, name, price)
JOIN restaurants r ON r.owner_id = 2 AND r.name = o.restaurant
JOIN categories c ON c.restaurant_id = r.id
JOIN products p ON p.category_id = c.id AND p.name = o.product;

-- Fotos para os produtos do seed original (V2), só onde ainda não há imagem.
UPDATE products p
SET image_url = u.image_url
FROM (VALUES
        ('Burger House', 'X-Bacon', 'https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=400&h=400&fit=crop&auto=format'),
        ('Burger House', 'X-Salada', 'https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=400&h=400&fit=crop&auto=format'),
        ('Burger House', 'X-Tudo', 'https://images.unsplash.com/photo-1551782450-a2132b4ba21d?w=400&h=400&fit=crop&auto=format'),
        ('Burger House', 'Batata Frita', 'https://images.unsplash.com/photo-1576107232684-1279f390859f?w=400&h=400&fit=crop&auto=format'),
        ('Sushi Kento', 'Combinado 20 peças', 'https://images.unsplash.com/photo-1611143669185-af224c5e3252?w=400&h=400&fit=crop&auto=format'),
        ('Sushi Kento', 'Combinado 40 peças', 'https://images.unsplash.com/photo-1553621042-f6e147245754?w=400&h=400&fit=crop&auto=format'),
        ('Forno da Nonna', 'Margherita', 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400&h=400&fit=crop&auto=format'),
        ('Forno da Nonna', 'Calabresa', 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&h=400&fit=crop&auto=format')
     ) AS u(restaurant, product, image_url),
     categories c,
     restaurants r
WHERE p.category_id = c.id
  AND c.restaurant_id = r.id
  AND r.name = u.restaurant
  AND p.name = u.product
  AND p.image_url IS NULL;
