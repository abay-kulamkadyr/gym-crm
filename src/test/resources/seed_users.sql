INSERT INTO users (user_id, first_name, last_name, username, password, is_active, role) VALUES
(1,  'John',    'Smith',    'John.Smith',    '$2a$12$VwP2hOmpi3R/5Ya9Jgocz.zMOrLiLcpmHKFj.tWLm7f..e8n3cijO', true,  'TRAINER'),
(2,  'Emma',    'Johnson',  'Emma.Johnson',  '$2a$12$VwP2hOmpi3R/5Ya9Jgocz.zMOrLiLcpmHKFj.tWLm7f..e8n3cijO', true,  'TRAINER'),
(3,  'Michael', 'Williams', 'Michael.Williams','$2a$12$VwP2hOmpi3R/5Ya9Jgocz.zMOrLiLcpmHKFj.tWLm7f..e8n3cijO', true, 'TRAINER'),
(4,  'Sarah',   'Brown',    'Sarah.Brown',   '$2a$12$VwP2hOmpi3R/5Ya9Jgocz.zMOrLiLcpmHKFj.tWLm7f..e8n3cijO', true,  'TRAINER'),
(5,  'David',   'Davis',    'David.Davis',   '$2a$12$VwP2hOmpi3R/5Ya9Jgocz.zMOrLiLcpmHKFj.tWLm7f..e8n3cijO', true,  'TRAINEE'),
(6,  'Lisa',    'Miller',   'Lisa.Miller',   '$2a$12$VwP2hOmpi3R/5Ya9Jgocz.zMOrLiLcpmHKFj.tWLm7f..e8n3cijO', true,  'TRAINEE'),
(7,  'James',   'Wilson',   'James.Wilson',  '$2a$12$VwP2hOmpi3R/5Ya9Jgocz.zMOrLiLcpmHKFj.tWLm7f..e8n3cijO', true,  'TRAINEE'),
(8,  'Emily',   'Moore',    'Emily.Moore',   '$2a$12$VwP2hOmpi3R/5Ya9Jgocz.zMOrLiLcpmHKFj.tWLm7f..e8n3cijO', false, 'TRAINEE');

INSERT INTO trainers (trainer_id, training_type_id, user_id) VALUES
(1, 1, 1),
(2, 2, 2),
(3, 3, 3),
(4, 4, 4);

INSERT INTO trainees (trainee_id, date_of_birth, address, user_id) VALUES
(1, '1990-05-15', '123 Main Street, New York, NY 10001',   5),
(2, '1992-08-22', '456 Oak Avenue, Los Angeles, CA 90001', 6),
(3, '1988-03-10', '789 Pine Road, Chicago, IL 60601',      7),
(4, '1995-11-30', '321 Elm Street, Houston, TX 77001',     8);

INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES
(1, 1), (1, 2),
(2, 3),
(3, 4), (3, 1);

INSERT INTO trainings (training_id, training_name, training_date, training_duration, trainee_id, trainer_id, training_type_id) VALUES
(1,  'Morning Cardio Session',     '2024-01-15 09:00:00', 60,  1, 1, 1),
(2,  'Strength Building Workout',  '2024-01-15 14:00:00', 90,  1, 2, 2),
(3,  'Evening Yoga Class',         '2024-01-16 18:00:00', 75,  2, 3, 3),
(4,  'CrossFit Intensive',         '2024-01-17 10:00:00', 120, 3, 4, 4),
(5,  'Cardio Bootcamp',            '2024-01-17 16:00:00', 45,  3, 1, 1),
(6,  'Advanced Strength Training', '2024-01-18 11:00:00', 90,  1, 2, 2),
(7,  'Beginner Yoga Flow',         '2024-01-18 19:00:00', 60,  2, 3, 3),
(8,  'HIIT Cardio',                '2024-01-19 08:00:00', 45,  1, 1, 1),
(9,  'CrossFit Fundamentals',      '2024-01-19 15:00:00', 90,  3, 4, 4),
(10, 'Power Yoga',                 '2024-01-20 17:00:00', 75,  2, 3, 3);
