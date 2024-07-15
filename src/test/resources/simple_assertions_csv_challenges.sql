-- Simple Assertions for testing csv exports, with an emphasis on:
--   - ties for extrema such as margin, difficulty, etc
--   - things that need to be escaped for csv, e.g. ','
-- Note that these assertions don't necessarily make any sense in that they may not imply a consistent
-- unique election outcome - they're just for testing CSV export.
-- In particular, things like difficulty and margin are normally (anti) correlated, but are deliberately
-- messed up here.

-- This contest has lots of (random/unrealistic) ties.
INSERT INTO county (id, name) VALUES (1,'Lots of assertions with ties County');
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) VALUES (1,1,0,'IRV','Lots of assertions with ties Contest',1,5,1);
INSERT INTO generate_assertions_summary (contest_name, error, message, version, warning, winner) VALUES ('Lots of assertions with ties Contest', '','',0,'', 'Alice');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values
('NEB', 'Lots of assertions with ties Contest', 2.1, 0.32, 'Bob', 320, 0.04, 110, 0, 0, 100, 0, 0, 0, 0, 'Alice');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values
('NEB', 'Lots of assertions with ties Contest', 1.1, 0.22, 'Bob', 220, 0.23, 430, 0, 0, 200, 0, 0, 0, 0, 'Chuan');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values
('NEB', 'Lots of assertions with ties Contest', 3.1, 0.32, 'Chuan', 320, 0.23, 50, 0, 0, 110, 0, 0, 0, 0, 'Diego');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values
('NEN', 'Lots of assertions with ties Contest', 1.999999999, 0.42, 'Bob', 420, 0.04, 320, 0, 0, 910, 0, 0, 0, 0, 'Alice');
INSERT INTO assertion_assumed_continuing values (4, 'Alice');
INSERT INTO assertion_assumed_continuing values (4, 'Bob');
INSERT INTO assertion_assumed_continuing values (4, 'Chuan');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values
('NEN', 'Lots of assertions with ties Contest', 1.1, 0.22, 'Diego', 220, 0.07, 430, 0, 0, 210, 0, 0, 0, 0, 'Alice');
INSERT INTO assertion_assumed_continuing values (5, 'Alice');
INSERT INTO assertion_assumed_continuing values (5, 'Diego');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values
('NEN', 'Lots of assertions with ties Contest', 1.2, 0.22, 'Bob', 220, 0.04, 400, 0, 0, 110, 0, 0, 0, 0, 'Alice');
INSERT INTO assertion_assumed_continuing values (6, 'Alice');
INSERT INTO assertion_assumed_continuing values (6, 'Bob');
INSERT INTO assertion_assumed_continuing values (6, 'Diego');

-- This contest has lots of characters that need escaping.
INSERT INTO county (id, name) VALUES (2,'Lots of tricky characters County');
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) VALUES (2,2,0,'IRV','Lots of tricky characters Contest',2,4,1);
INSERT INTO generate_assertions_summary (contest_name, error, message, version, warning, winner) VALUES ('Lots of tricky characters Contest', '','',0,'', 'Annoying, Alice');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values
('NEB', 'Lots of tricky characters Contest', 2.1, 0.32, '"Breaking, Bob"', 320, 0.04, 110, 0, 1, 100, 0, 0, 0, 0, 'Annoying, Alice');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values
('NEN', 'Lots of tricky characters Contest', 3.01, 0.12, 'O''Difficult, Diego', 240, 0.04, 120, 1, 0, 110, 0, 0, 0, 0, 'Challenging, Chuan');
INSERT INTO assertion_assumed_continuing values (8, 'Annoying, Alice');
INSERT INTO assertion_assumed_continuing values (8, 'Challenging, Chuan');
INSERT INTO assertion_assumed_continuing values (8, 'O''Difficult, Diego');
INSERT INTO assertion_assumed_continuing values (8, '"Breaking, Bob"');

-- This contest has sensible correlated values for making a demo csv
INSERT INTO county (id, name) VALUES (3,'CSV Demo County');
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) VALUES (3,3,0,'IRV','CSV Demo Contest',3,4,1);
INSERT INTO generate_assertions_summary (contest_name, error, message, version, warning, winner) VALUES ('CSV Demo Contest', '','',0,'', 'Diego');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values
('NEB', 'CSV Demo Contest', 5.1, 0.112, 'Alice', 112, 0.06, 55, 2, 0, 35, 0, 0, 0, 0, 'Bob');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values
('NEN', 'CSV Demo Contest', 6.1, 0.1, 'Chuan', 100, 0.05, 45, 0, 0, 45, 0, 0, 0, 0, 'Diego');
INSERT INTO assertion_assumed_continuing values (10, 'Alice');
INSERT INTO assertion_assumed_continuing values (10, 'Chuan');
INSERT INTO assertion_assumed_continuing values (10, 'Diego');