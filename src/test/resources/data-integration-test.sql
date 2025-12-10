-- Test data for Integration Tests
-- Insert initial test data

-- Test Users
INSERT INTO users (id, name, email) VALUES
                                        (1, 'Test User 1', 'test1@example.com'),
                                        (2, 'Test User 2', 'test2@example.com'),
                                        (3, 'Test User 3', 'test3@example.com'),
                                        (4, 'Item Owner', 'owner@example.com'),
                                        (5, 'Booker User', 'booker@example.com');

-- Test Requests
INSERT INTO requests (id, description, requestor_id, created) VALUES
                                                                  (1, 'Need a drill for home renovation', 2, '2024-01-15 10:00:00'),
                                                                  (2, 'Looking for a camping tent', 3, '2024-01-16 14:30:00');

-- Test Items
INSERT INTO items (id, name, description, is_available, owner_id, request_id) VALUES
                                                                                  (1, 'Electric Drill', 'Powerful cordless drill with various bits', true, 4, 1),
                                                                                  (2, 'Camping Tent', '4-person waterproof tent', true, 4, 2),
                                                                                  (3, 'Laptop', 'Gaming laptop with RTX 3080', false, 1, NULL),
                                                                                  (4, 'Camera', 'DSLR camera with lens kit', true, 2, NULL),
                                                                                  (5, 'Bicycle', 'Mountain bike in good condition', true, 3, NULL);

-- Test Bookings
INSERT INTO bookings (id, start_date, end_date, item_id, booker_id, status) VALUES
                                                                                (1, '2024-01-20 10:00:00', '2024-01-22 18:00:00', 1, 5, 'APPROVED'),
                                                                                (2, '2024-01-25 09:00:00', '2024-01-27 20:00:00', 2, 1, 'WAITING'),
                                                                                (3, '2024-02-01 14:00:00', '2024-02-03 16:00:00', 4, 3, 'REJECTED'),
                                                                                (4, '2023-12-10 08:00:00', '2023-12-12 19:00:00', 1, 2, 'APPROVED'),
                                                                                (5, '2024-02-10 11:00:00', '2024-02-12 17:00:00', 5, 4, 'WAITING');

-- Test Comments
INSERT INTO comments (id, text, item_id, author_id, created) VALUES
                                                                 (1, 'Great drill, worked perfectly for my project!', 1, 2, '2023-12-13 10:30:00'),
                                                                 (2, 'Very reliable and easy to use', 1, 5, '2024-01-23 15:45:00'),
                                                                 (3, 'Camera was in excellent condition', 4, 3, '2024-01-28 12:15:00');

-- Reset sequences to continue from appropriate values
ALTER SEQUENCE users_id_seq RESTART WITH 100;
ALTER SEQUENCE items_id_seq RESTART WITH 100;
ALTER SEQUENCE bookings_id_seq RESTART WITH 100;
ALTER SEQUENCE comments_id_seq RESTART WITH 100;
ALTER SEQUENCE requests_id_seq RESTART WITH 100;
