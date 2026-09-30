--liquibase formatted sql

--changeset interview-team:007-seed context:dev
INSERT INTO candidate (name, email, phone,skill) VALUES
  ('Candidate1',  'mahapolakam@gmail.com',  '9000000001','Java,Spring,Microservices'),
  ('Candidate2',  'candidate2@gmail.com',  '9000000002','Web development,Java script,React');

INSERT INTO interviewer (name, email, department) VALUES
  ('Interviewer-1', 'interviewer1@company.com', 'Engineering'),
  ('Interviewer-2',  'interviewer2@company.com',   'Engineering'),
  ('Interviewer-3',  'interviewer3@company.com',    'HR');

  INSERT INTO interviewer_availability (interviewer_id, day_of_week, start_time, end_time)
  SELECT i.id, d.day_name, TIME '09:00:00', TIME '17:00:00'
  FROM interviewer i
  CROSS JOIN (VALUES ('MONDAY'),('TUESDAY'),('WEDNESDAY'),('THURSDAY'),('FRIDAY')) AS d(day_name);
--rollback DELETE FROM interviewer; DELETE FROM candidate;