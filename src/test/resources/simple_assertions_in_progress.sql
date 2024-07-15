-- Simple Assertions for testing retrieval when audit is in progress
INSERT INTO county (id, name) VALUES (1,'One NEB Assertion County');
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) VALUES (1,1,0,'IRV','One NEB Assertion Contest',1,5,1);
INSERT INTO generate_assertions_summary (contest_name, error, message, version, warning, winner) VALUES ('One NEB Assertion Contest', '','',0,'', 'Alice');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEB', 'One NEB Assertion Contest', 1.1, 0.32, 'Bob', 320, 0.5, 111, 0, 0, 111, 0, 0, 0, 0, 'Alice');

INSERT INTO county (id, name) VALUES (2,'One NEN Assertion County');
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) VALUES (2,2,0,'IRV','One NEN Assertion Contest',2,4,1);
INSERT INTO generate_assertions_summary (contest_name, error, message, version, warning, winner) VALUES ('One NEN Assertion Contest', '','',0,'', 'Alice');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEN', 'One NEN Assertion Contest', 3.01, 0.12, 'Charlie', 240, 0.2, 245, 1, 0, 201, 2, 0, 0, 0, 'Alice');
INSERT INTO assertion_assumed_continuing values (2, 'Alice');
INSERT INTO assertion_assumed_continuing values (2, 'Charlie');
INSERT INTO assertion_assumed_continuing values (2, 'Diego');
INSERT INTO assertion_assumed_continuing values (2, 'Bob');

INSERT INTO assertion_discrepancies values (2, 1, 13);
INSERT INTO assertion_discrepancies values (2, 0, 14);
INSERT INTO assertion_discrepancies values (2, 0, 15);

INSERT INTO county (id, name) VALUES (3,'One NEN NEB Assertion County');
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) VALUES (3,3,0,'IRV','One NEN NEB Assertion Contest',3,4,1);
INSERT INTO generate_assertions_summary (contest_name, error, message, version, warning, winner) VALUES ('One NEN NEB Assertion Contest', '','',0,'', 'Amanda');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEB', 'One NEN NEB Assertion Contest', 0.1, 0.1, 'Liesl', 112, 0.08, 27, 0, 1, 20, 0, 2, 0, 0, 'Amanda');
INSERT INTO assertion_discrepancies values (3, -1, 13);
INSERT INTO assertion_discrepancies values (3, 2, 14);
INSERT INTO assertion_discrepancies values (3, 2, 15);

INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEN', 'One NEN NEB Assertion Contest', 3.17, 0.5, 'Wendell', 560, 0.7, 300, 2, 0, 200, 0, 0, 1, 0, 'Amanda');
INSERT INTO assertion_assumed_continuing values (4, 'Liesl');
INSERT INTO assertion_assumed_continuing values (4, 'Wendell');
INSERT INTO assertion_assumed_continuing values (4, 'Amanda');

INSERT INTO assertion_discrepancies values (4, 1, 13);
INSERT INTO assertion_discrepancies values (4, 1, 14);
INSERT INTO assertion_discrepancies values (4, -2, 15);
