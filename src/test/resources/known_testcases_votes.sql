-- Assertion generation data for when we know exactly what the answer should be. This includes mostly
-- small examples for which the correct answer can be established by human inspection.

-- Test Counties
INSERT INTO county (id, name) values (8, 'TiedWinnersCounty');
INSERT INTO county (id, name) values (9, 'GuideToRaireCounty');
INSERT INTO county (id, name) values (10, 'SimpleCounty1');
INSERT INTO county (id, name) values (11, 'SimpleCounty2');
-- leave a space for Byron, 12, from the NSW2021Data directory
INSERT INTO county (id, name) values (13, 'TimeoutCheckingWinnersCounty');


-- A test assertion to allow for some basic sanity checking.
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEB', 'Sanity Check NEB Assertion Contest', 1.1, 0.32, 'Bob', 320, 1, 0, 0, 0, 0, 0, 0, 0, 0, 'Alice');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEN', 'Sanity Check NEN Assertion Contest', 2.5, 0.4, 'Bob', 20, 1, 0, 0, 0, 0, 0, 0, 0, 0, 'Alice');
INSERT INTO assertion_assumed_continuing values (2, 'Alice');
INSERT INTO assertion_assumed_continuing values (2, 'Bob');
INSERT INTO assertion_assumed_continuing values (2, 'Chuan');

-- Further test assertions to allow for some basic sanity checking.
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEB', 'Sanity Check NEB NEN Assertion Contest', 1.1, 0.32, 'Bob', 320, 1, 0, 0, 0, 0, 0, 0, 0, 0, 'Alice');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEN', 'Sanity Check NEB NEN Assertion Contest', 2.5, 0.4, 'Bob', 20, 1, 0, 0, 0, 0, 0, 0, 0, 0, 'Alice');
INSERT INTO assertion_assumed_continuing values (4, 'Alice');
INSERT INTO assertion_assumed_continuing values (4, 'Bob');
INSERT INTO assertion_assumed_continuing values (4, 'Chuan');

INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEB', 'Sanity Check 3 Assertion Contest', 1.1, 0.32, 'Bob', 320, 1, 0, 0, 0, 0, 0, 0, 0, 0, 'Alice');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEB', 'Sanity Check 3 Assertion Contest', 1.5, 0.6, 'Chuan', 320, 1, 0, 0, 0, 0, 0, 0, 0, 0, 'Alice');
INSERT INTO assertion (assertion_type, contest_name, difficulty, diluted_margin, loser, margin, current_risk, estimated_samples_to_audit, one_vote_over_count, one_vote_under_count, optimistic_samples_to_audit, other_count, two_vote_over_count, two_vote_under_count, version, winner) values ('NEN', 'Sanity Check 3 Assertion Contest', 2.5, 0.4, 'Bob', 20, 1, 0, 0, 0, 0, 0, 0, 0, 0, 'Alice');
INSERT INTO assertion_assumed_continuing values (6, 'Alice');
INSERT INTO assertion_assumed_continuing values (6, 'Bob');
INSERT INTO assertion_assumed_continuing values (6, 'Chuan');

-- Contest
-- Simple contests to test basic functioning.
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (8, 999990, 0, 'IRV', 'Tied Winners Contest', 2, 7, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (9, 999991, 0, 'IRV', 'Guide To Raire Example 1', 3, 4, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (9, 999992, 0, 'IRV', 'Guide To Raire Example 2', 4, 3, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (10, 999993, 0, 'IRV', 'Simple Contest', 5, 3, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (10, 999994, 0, 'IRV', 'Cross-county Simple Contest', 7, 3, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (11, 999995, 0, 'IRV', 'Cross-county Simple Contest', 8, 3, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (13, 999996, 0, 'IRV', 'Time out checking winners contest', 9, 20, 1);

-- CVRs

-- The Guide To Raire Part 2 Example 1, divided by 500
-- Note that this will _not_ have the same margins as raire-java, because of the divide by 500,
-- but it should have the same difficulties and assertions.

-- 10 (C,B,A)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (1, 1, 'Type 1', 1, 9, '1-1-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (1, 9, '["Chuan","Bob","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (2, 2, 'Type 1', 1, 9, '1-1-2', 2, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (2, 9, '["Chuan","Bob","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (3, 3, 'Type 1', 1, 9, '1-1-3', 3, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (3, 9, '["Chuan","Bob","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (4, 4, 'Type 1', 1, 9, '1-1-4', 4, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (4, 9, '["Chuan","Bob","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (5, 5, 'Type 1', 1, 9, '1-1-5', 5, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (5, 9, '["Chuan","Bob","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (6, 6, 'Type 1', 1, 9, '1-1-6', 6, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (6, 9, '["Chuan","Bob","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (7, 7, 'Type 1', 1, 9, '1-1-7', 7, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (7, 9, '["Chuan","Bob","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (8, 8, 'Type 1', 1, 9, '1-1-8', 8, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (8, 9, '["Chuan","Bob","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (9, 9, 'Type 1', 1, 9, '1-1-9', 9, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (9, 9, '["Chuan","Bob","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10, 10, 'Type 1', 1, 9, '1-1-10', 10, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10, 9, '["Chuan","Bob","Alice"]', 999991, 0);

-- 2 (B,C,D)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (11, 11, 'Type 1', 2, 9, '1-2-1', 11, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (11, 9, '["Bob","Chuan","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (12, 12, 'Type 1', 2, 9, '1-2-2', 12, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (12, 9, '["Bob","Chuan","Diego"]', 999991, 0);

-- 3 (D,A)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (13, 13, 'Type 1', 3, 9, '1-3-1', 13, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (13, 9, '["Diego","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (14, 14, 'Type 1', 3, 9, '1-3-2', 14, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (14, 9, '["Diego","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (15, 15, 'Type 1', 3, 9, '1-3-3', 15, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (15, 9, '["Diego","Alice"]', 999991, 0);

-- 8 (A,D)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (16, 16, 'Type 1', 4, 9, '1-4-1', 16, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (16, 9, '["Alice","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (17, 17, 'Type 1', 4, 9, '1-4-2', 17, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (17, 9, '["Alice","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (18, 18, 'Type 1', 4, 9, '1-4-3', 18, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (18, 9, '["Alice","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (19, 19, 'Type 1', 4, 9, '1-4-4', 19, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (19, 9, '["Alice","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20, 20, 'Type 1', 4, 9, '1-4-5', 20, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20, 9, '["Alice","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (21, 21, 'Type 1', 4, 9, '1-4-6', 21, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (21, 9, '["Alice","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (22, 22, 'Type 1', 4, 9, '1-4-7', 22, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (22, 9, '["Alice","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (23, 23, 'Type 1', 4, 9, '1-4-8', 23, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (23, 9, '["Alice","Diego"]', 999991, 0);

-- 4 (D)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (24, 24, 'Type 1', 5, 9, '1-5-1', 24, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (24, 9, '["Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (25, 25, 'Type 1', 5, 9, '1-5-2', 25, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (25, 9, '["Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (26, 26, 'Type 1', 5, 9, '1-5-3', 26, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (26, 9, '["Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (27, 27, 'Type 1', 5, 9, '1-5-4', 27, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (27, 9, '["Diego"]', 999991, 0);

-- The Guide To Raire Example 2, divided by 1000
-- Note that this will _not_ have the same margins as raire-java, because of the divide by 1000,
-- but it should have the same difficulties and assertions.

-- 5 (A,B,C)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20001, 1, 'Type 1', 1, 9, '2-1-1', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20001, 9, '["Alice","Bob","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20002, 2, 'Type 1', 1, 9, '2-1-2', 2, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20002, 9, '["Alice","Bob","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20003, 3, 'Type 1', 1, 9, '2-1-3', 3, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20003, 9, '["Alice","Bob","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20004, 4, 'Type 1', 1, 9, '2-1-4', 4, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20004, 9, '["Alice","Bob","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20005, 5, 'Type 1', 1, 9, '2-1-5', 5, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20005, 9, '["Alice","Bob","Chuan"]', 999992, 0);

-- 5 (A,C)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20006, 6, 'Type 1', 2, 9, '2-2-1', 6, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20006, 9, '["Alice","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20007, 7, 'Type 1', 2, 9, '2-2-2', 7, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20007, 9, '["Alice","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20008, 8, 'Type 1', 2, 9, '2-2-3', 8, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20008, 9, '["Alice","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20009, 9, 'Type 1', 2, 9, '2-2-4', 9, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20009, 9, '["Alice","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20010, 10, 'Type 1', 2, 9, '2-2-5', 10, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20010, 9, '["Alice","Chuan"]', 999992, 0);

-- 5 (B,C,A)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20011, 11, 'Type 1', 3, 9, '2-3-1', 11, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20011, 9, '["Bob","Chuan","Alice"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20012, 12, 'Type 1', 3, 9, '2-3-2', 12, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20012, 9, '["Bob","Chuan","Alice"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20013, 13, 'Type 1', 3, 9, '2-3-3', 13, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20013, 9, '["Bob","Chuan","Alice"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20014, 14, 'Type 1', 3, 9, '2-3-4', 14, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20014, 9, '["Bob","Chuan","Alice"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20015, 15, 'Type 1', 3, 9, '2-3-5', 15, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20015, 9, '["Bob","Chuan","Alice"]', 999992, 0);

-- 6 (B)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20016, 16, 'Type 1', 4, 9, '2-4-1', 16, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20016, 9, '["Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20017, 17, 'Type 1', 4, 9, '2-4-2', 17, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20017, 9, '["Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20018, 18, 'Type 1', 4, 9, '2-4-3', 18, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20018, 9, '["Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20019, 19, 'Type 1', 4, 9, '2-4-4', 19, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20019, 9, '["Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20020, 20, 'Type 1', 4, 9, '2-4-5', 20, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20020, 9, '["Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20021, 21, 'Type 1', 4, 9, '2-4-6', 21, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20021, 9, '["Bob"]', 999992, 0);

-- 10 (C,B)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20022, 22, 'Type 1', 5, 9, '2-5-1', 22, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20022, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20023, 23, 'Type 1', 5, 9, '2-5-2', 23, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20023, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20024, 24, 'Type 1', 5, 9, '2-5-3', 24, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20024, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20025, 25, 'Type 1', 5, 9, '2-5-4', 25, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20025, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20026, 26, 'Type 1', 5, 9, '2-5-5', 26, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20026, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20027, 27, 'Type 1', 5, 9, '2-5-6', 27, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20027, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20028, 28, 'Type 1', 5, 9, '2-5-7', 28, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20028, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20029, 29, 'Type 1', 5, 9, '2-5-8', 29, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20029, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20030, 30, 'Type 1', 5, 9, '2-5-9', 30, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20030, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20031, 31, 'Type 1', 5, 9, '2-5-10', 31, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20031, 9, '["Chuan","Bob"]', 999992, 0);

-- 10 (C)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20032, 32, 'Type 1', 6, 9, '2-6-1', 32, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20032, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20033, 33, 'Type 1', 6, 9, '2-6-2', 33, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20033, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20034, 34, 'Type 1', 6, 9, '2-6-3', 34, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20034, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20035, 35, 'Type 1', 6, 9, '2-6-4', 35, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20035, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20036, 36, 'Type 1', 6, 9, '2-6-5', 36, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20036, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20037, 37, 'Type 1', 6, 9, '2-6-6', 37, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20037, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20038, 38, 'Type 1', 6, 9, '2-6-7', 38, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20038, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20039, 39, 'Type 1', 6, 9, '2-6-8', 39, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20039, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20040, 40, 'Type 1', 6, 9, '2-6-9', 40, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20040, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20041, 41, 'Type 1', 6, 9, '2-6-10', 41, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20041, 9, '["Chuan"]', 999992, 0);

-- Tied Winners Contest
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (30001, 1, 'Type 1', 1, 8, '3-1-1', 1, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (30001, 8, '["Alice","Bob","Chuan"]', 999990, 0);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (30002, 2, 'Type 1', 1, 8, '3-1-2', 2, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (30002, 8, '["Bob","Alice","Chuan"]', 999990, 0);

-- Simple Contest, single county.
-- This should produce "A NEB C" and "A NEN B | {A,B} continuing". Note that "A NEB B" is _not_ true.
-- 2 (A,B,C)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (30003, 3, 'Type 1', 1, 10, '3-1-3', 3, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (30003, 10, '["Alice","Bob","Chuan"]', 999993, 0);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (30004, 4, 'Type 1', 1, 10, '3-1-4', 4, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (30004, 10, '["Alice","Bob","Chuan"]', 999993, 0);

-- 2 (B)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (30005, 5, 'Type 1', 1, 10, '3-1-5', 5, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (30005, 10, '["Bob"]', 999993, 0);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (30006, 6, 'Type 1', 1, 10, '3-1-6', 6, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (30006, 10, '["Bob"]', 999993, 0);

-- 1 (C,A)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (30007, 7, 'Type 1', 1, 10, '3-1-7', 7, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (30007, 10, '["Chuan","Alice"]', 999993, 0);

-- The same Simple Contest, but divided between two counties.
-- This should produce the same assertions: "A NEB C" and "A NEN B | {A,B} continuing".
-- 2 (A,B,C) in county 10.
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (30008, 8, 'Type 1', 1, 10, '3-1-8', 8, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (30008, 10, '["Alice","Bob","Chuan"]', 999994, 0);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (30009, 9, 'Type 1', 1, 10, '3-1-9', 9, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (30009, 10, '["Alice","Bob","Chuan"]', 999994, 0);

-- 2 (B) in county 11.
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (30010, 10, 'Type 1', 1, 10, '3-1-10', 10, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (30010, 11, '["Bob"]', 999995, 0);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (30011, 11, 'Type 1', 1, 10, '3-1-11', 11, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (30011, 11, '["Bob"]', 999995, 0);

-- 1 (C,A) in county 11.
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (30012, 12, 'Type 1', 1, 10, '3-1-12', 12, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (30012, 11, '["Chuan","Alice"]', 999995, 0);

-- A contest designed to make it time out checking winners. There are 20 candidates and 20 votes,
-- all the same except for a cyclical offset.
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40001, 1, 'Type 1', 1, 13, '1-1-1', 1, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40001, 13, '["A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40002, 2, 'Type 1', 1, 13, '1-1-2', 2, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40002, 13, '["B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","A"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40003, 3, 'Type 1', 1, 13, '1-1-3', 3, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40003, 13, '["C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","A","B"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40004, 4, 'Type 1', 1, 13, '1-1-4', 4, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40004, 13, '["D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","A","B","C"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40005, 5, 'Type 1', 1, 13, '1-1-5', 5, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40005, 13, '["E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","A","B","C","D"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40006, 6, 'Type 1', 1, 13, '1-1-6', 6, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40006, 13, '["F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","A","B","C","D","E"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40007, 7, 'Type 1', 1, 13, '1-1-7', 7, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40007, 13, '["G","H","I","J","K","L","M","N","O","P","Q","R","S","T","A","B","C","D","E","F"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40008, 8, 'Type 1', 1, 13, '1-1-8', 8, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40008, 13, '["H","I","J","K","L","M","N","O","P","Q","R","S","T","A","B","C","D","E","F","G"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40009, 9, 'Type 1', 1, 13, '1-1-9', 9, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40009, 13, '["I","J","K","L","M","N","O","P","Q","R","S","T","A","B","C","D","E","F","G","H"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40010, 10, 'Type 1', 1, 13, '1-1-10', 10, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40010, 13, '["J","K","L","M","N","O","P","Q","R","S","T","A","B","C","D","E","F","G","H","I"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40011, 11, 'Type 1', 1, 13, '1-1-11', 11, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40011, 13, '["K","L","M","N","O","P","Q","R","S","T","A","B","C","D","E","F","G","H","I","J"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40012, 12, 'Type 1', 1, 13, '1-1-12', 12, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40012, 13, '["L","M","N","O","P","Q","R","S","T","A","B","C","D","E","F","G","H","I","J","K"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40013, 13, 'Type 1', 1, 13, '1-1-13', 13, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40013, 13, '["M","N","O","P","Q","R","S","T","A","B","C","D","E","F","G","H","I","J","K","L"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40014, 14, 'Type 1', 1, 13, '1-1-14', 14, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40014, 13, '["N","O","P","Q","R","S","T","A","B","C","D","E","F","G","H","I","J","K","L","M"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40015, 15, 'Type 1', 1, 13, '1-1-14', 15, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40015, 13, '["O","P","Q","R","S","T","A","B","C","D","E","F","G","H","I","J","K","L","M","N"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40016, 16, 'Type 1', 1, 13, '1-1-16', 16, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40016, 13, '["P","Q","R","S","T","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40017, 17, 'Type 1', 1, 13, '1-1-17', 17, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40017, 13, '["Q","R","S","T","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40018, 18, 'Type 1', 1, 13, '1-1-18', 18, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40018, 13, '["R","S","T","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40019, 19, 'Type 1', 1, 13, '1-1-19', 19, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40019, 13, '["S","T","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R"]', 999996, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (40020, 20, 'Type 1', 1, 13, '1-1-20', 20, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (40020, 13, '["T","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S"]', 999996, 0);
