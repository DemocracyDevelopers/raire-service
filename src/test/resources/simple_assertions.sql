-- Simple Assertions for testing Retrieval/Deletion
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEB', 'One NEB Assertion Contest', 1.1, 0.32, 'Bob', 320, 1, 0, 0, 0, 0, 0, 0, 0, 0, 'Alice');

INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEN', 'One NEN Assertion Contest', 3.01, 0.12, 'Charlie', 240, 1, 0, 0, 0, 0, 0, 0, 0, 0, 'Alice');
INSERT INTO assertion_assumed_continuing values (2, 'Alice');
INSERT INTO assertion_assumed_continuing values (2, 'Charlie');
INSERT INTO assertion_assumed_continuing values (2, 'Diego');
INSERT INTO assertion_assumed_continuing values (2, 'Bob');

INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEB', 'One NEN NEB Assertion Contest', 0.1, 0.1, 'Liesl', 112, 1, 0, 0, 0, 0, 0, 0, 0, 0, 'Amanda');

INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEN', 'One NEN NEB Assertion Contest', 3.17, 0.5, 'Wendell', 560, 1, 0, 0, 0, 0, 0, 0, 0, 0, 'Amanda');
INSERT INTO assertion_assumed_continuing values (4, 'Liesl');
INSERT INTO assertion_assumed_continuing values (4, 'Wendell');
INSERT INTO assertion_assumed_continuing values (4, 'Amanda');


INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEB', 'Multi-County Contest 1', 2.1, 0.01, 'Alice P. Mangrove', 310, 1, 0, 0, 0, 0, 0, 0, 0, 0, 'Charlie C. Chaplin');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEB', 'Multi-County Contest 1', 0.9, 0.07, 'Al (Bob) Jones', 2170, 1, 0, 0, 0, 0, 0, 0, 0, 0, 'Alice P. Mangrove');

INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEN', 'Multi-County Contest 1', 5.0, 0.001, 'West W. Westerson', 31, 1, 0, 0, 0, 0, 0, 0, 0, 0, 'Alice P. Mangrove');
INSERT INTO assertion_assumed_continuing values (7, 'West W. Westerson');
INSERT INTO assertion_assumed_continuing values (7, 'Alice P. Mangrove');