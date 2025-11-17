INSERT INTO categories (name)
VALUES ('Elettronica'),
       ('Abbigliamento'),
       ('Libri'),
       ('Cucina'),
       ('Giocattoli');

INSERT INTO products (name, price, description, category_id)
VALUES ('Smartphone Samsung Galaxy S23', 799.99,
        'Smartphone top di gamma con display AMOLED da 6.1", 256GB di memoria interna e fotocamera da 50MP.', 1),
       ('Cuffie Bose QuietComfort 45', 329.00,
        'Cuffie wireless con cancellazione del rumore, perfette per viaggi e ufficio.', 1),
       ('Maglietta Uomo Nike Dri-FIT', 29.99, 'Maglietta sportiva traspirante, disponibile in diverse taglie e colori.',
        2),
       ('Jeans Donna Levi''s 721', 69.90, 'Jeans skinny da donna, vita alta, tessuto stretch comodo e resistente.', 2),
       ('Il Signore degli Anelli', 24.50, 'Classico romanzo fantasy di J.R.R. Tolkien, edizione illustrata.', 3),
       ('Harry Potter e la Pietra Filosofale', 19.99,
        'Primo libro della celebre saga di J.K. Rowling, adatto a tutte le età.', 3),
       ('Set Coltelli Professionali Zwilling', 149.90,
        'Set completo di coltelli da cucina in acciaio inossidabile di alta qualità.', 4),
       ('Robot da Cucina Kenwood', 299.00,
        'Robot multifunzione con 1000W di potenza, ideale per impastare, tritare e frullare.', 4),
       ('LEGO Technic Auto da Corsa', 129.99,
        'Set LEGO Technic con dettagli realistici, ingranaggi funzionanti e auto da corsa.', 5),
       ('Puzzle 1000 Pezzi Ravensburger', 19.50, 'Puzzle panoramico di alta qualità, ideale per adulti e bambini.', 5);
