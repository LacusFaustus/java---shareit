-- Test data for Integration Tests
-- Insert initial test data

-- Test Users
INSERT INTO users (name, email) VALUES
                                    ('Test User 1', 'test1@example.com'),
                                    ('Test User 2', 'test2@example.com'),
                                    ('Test User 3', 'test3@example.com'),
                                    ('Item Owner', 'owner@example.com'),
                                    ('Booker User', 'booker@example.com');

-- Test Requests
INSERT INTO requests (description, requestor_id, created) VALUES
                                                              ('Need a drill for home renovation', 2, '2024-01-15 10:00:00'),
                                                              ('Looking for a camping tent', 3, '2024-01-16 14:30:00');

-- Test Items
INSERT INTO items (name, description, is_available, owner_id, request_id) VALUES
                                                                              ('Electric Drill', 'Powerful cordless drill with various bits', true, 4, 1),
                                                                              ('Camping Tent', '4-person waterproof tent', true, 4, 2),
                                                                              ('Laptop', 'Gaming laptop with RTX 3080', false, 1, NULL),
                                                                              ('Camera', 'DSLR camera with lens kit', true, 2, NULL),
                                                                              ('Bicycle', 'Mountain bike in good condition', true, 3, NULL);

-- Test Bookings
INSERT INTO bookings (start_date, end_date, item_id, booker_id, status) VALUES
                                                                            ('2024-01-20 10:00:00', '2024-01-22 18:00:00', 1, 5, 'APPROVED'),
                                                                            ('2024-01-25 09:00:00', '2024-01-27 20:00:00', 2, 1, 'WAITING'),
                                                                            ('2024-02-01 14:00:00', '2024-02-03 16:00:00', 4, 3, 'REJECTED'),
                                                                            ('2023-12-10 08:00:00', '2023-12-12 19:00:00', 1, 2, 'APPROVED'),
                                                                            ('2024-02-10 11:00:00', '2024-02-12 17:00:00', 5, 4, 'WAITING');

-- Test Comments
INSERT INTO comments (text, item_id, author_id, created) VALUES
                                                             ('Great drill, worked perfectly for my project!', 1, 2, '2023-12-13 10:30:00'),
                                                             ('Very reliable and easy to use', 1, 5, '2024-01-23 15:45:00'),
                                                             ('Camera was in excellent condition', 4, 3, '2024-01-28 12:15:00');
