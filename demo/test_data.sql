-- Insert test data into the music_record table
INSERT INTO music_record (title, artist, genre) VALUES
    ('Bohemian Rhapsody', 'Queen', 'POP'),
    ('Smells Like Teen Spirit', 'Nirvana', 'ROCK'),
    ('Stairway to Heaven', 'Led Zeppelin', 'ROCK');

-- Insert test data into the store_item table
INSERT INTO store_item (name, price, version) VALUES
  ('Guitar', 500, 1),
  ('Drum Set', 800, 2),
  ('Microphone', 200, 3);

-- Insert test data into the item_comment table
INSERT INTO item_comment (comment, item_id) VALUES
('Best song ever!', 1),
('Love this band!', 2),
('Great for recording', 3);
