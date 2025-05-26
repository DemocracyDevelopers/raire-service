-- This file is intended to be run after known_testcases_votes.sql.
-- It substitutes County 9's votes with an edited version of the Guide To Raire Example 1, in which
-- Alice and Bob are flipped. This allows for testing of proper replacement of assertions.

-- This is what happens when County 9 deletes its CSV export file. Should cascade to cvr_contest_info.
DELETE FROM cvr_contest_info WHERE county_id = 9;
DELETE FROM cast_vote_record WHERE county_id = 9;

-- Replace the CVRs.
-- The Guide To Raire Part 2 Example 1, divided by 500
-- Chuan and Alice are flipped compared with the example in The Guide and known_testcases_votes.
-- We expect the assertions to be the same, but to have Chuan and Alice flipped.

-- 10 (C,B,A)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (1, 1, 'Type 1', 1, 9, '1-1-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (1, 9, '["Alice","Bob","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (2, 2, 'Type 1', 1, 9, '1-1-2', 2, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (2, 9, '["Alice","Bob","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (3, 3, 'Type 1', 1, 9, '1-1-3', 3, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (3, 9, '["Alice","Bob","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (4, 4, 'Type 1', 1, 9, '1-1-4', 4, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (4, 9, '["Alice","Bob","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (5, 5, 'Type 1', 1, 9, '1-1-5', 5, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (5, 9, '["Alice","Bob","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (6, 6, 'Type 1', 1, 9, '1-1-6', 6, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (6, 9, '["Alice","Bob","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (7, 7, 'Type 1', 1, 9, '1-1-7', 7, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (7, 9, '["Alice","Bob","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (8, 8, 'Type 1', 1, 9, '1-1-8', 8, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (8, 9, '["Alice","Bob","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (9, 9, 'Type 1', 1, 9, '1-1-9', 9, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (9, 9, '["Alice","Bob","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10, 10, 'Type 1', 1, 9, '1-1-10', 10, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10, 9, '["Alice","Bob","Chuan"]', 999991, 0);

-- 2 (B,C,D)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (11, 11, 'Type 1', 2, 9, '1-2-1', 11, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (11, 9, '["Bob","Alice","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (12, 12, 'Type 1', 2, 9, '1-2-2', 12, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (12, 9, '["Bob","Alice","Diego"]', 999991, 0);

-- 3 (D,A)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (13, 13, 'Type 1', 3, 9, '1-3-1', 13, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (13, 9, '["Diego","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (14, 14, 'Type 1', 3, 9, '1-3-2', 14, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (14, 9, '["Diego","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (15, 15, 'Type 1', 3, 9, '1-3-3', 15, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (15, 9, '["Diego","Chuan"]', 999991, 0);

-- 8 (A,D)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (16, 16, 'Type 1', 4, 9, '1-4-1', 16, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (16, 9, '["Chuan","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (17, 17, 'Type 1', 4, 9, '1-4-2', 17, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (17, 9, '["Chuan","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (18, 18, 'Type 1', 4, 9, '1-4-3', 18, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (18, 9, '["Chuan","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (19, 19, 'Type 1', 4, 9, '1-4-4', 19, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (19, 9, '["Chuan","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20, 20, 'Type 1', 4, 9, '1-4-5', 20, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20, 9, '["Chuan","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (21, 21, 'Type 1', 4, 9, '1-4-6', 21, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (21, 9, '["Chuan","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (22, 22, 'Type 1', 4, 9, '1-4-7', 22, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (22, 9, '["Chuan","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (23, 23, 'Type 1', 4, 9, '1-4-8', 23, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (23, 9, '["Chuan","Diego"]', 999991, 0);

-- 4 (D)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (24, 24, 'Type 1', 5, 9, '1-5-1', 24, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (24, 9, '["Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (25, 25, 'Type 1', 5, 9, '1-5-2', 25, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (25, 9, '["Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (26, 26, 'Type 1', 5, 9, '1-5-3', 26, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (26, 9, '["Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (27, 27, 'Type 1', 5, 9, '1-5-4', 27, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (27, 9, '["Diego"]', 999991, 0);