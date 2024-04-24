-- Assertion generation data for when we know exactly what the answer should be. This includes mostly
-- small examples for which the correct answer can be established by human inspection.

-- Test Counties
INSERT INTO county (id, name) values (8, 'TiedWinnersCounty');
INSERT INTO county (id, name) values (9, 'GuideToRaireCounty');

-- Contest
-- Simple contests to test basic functioning.
-- INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (8, 999990, 0, 'IRV', 'Multi-County Contest 1', 0, 7, 1);
-- INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (9, 999991, 0, 'IRV', 'Multi-County Contest 1', 1, 7, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (8, 999990, 0, 'IRV', 'Tied Winners Contest', 2, 7, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (9, 999991, 0, 'IRV', 'Guide To Raire Example 2', 3, 3, 1);

-- CVRs
-- Tied Winners Contest
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (1, 1, 'Type 1', 1, 8, '1-1-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (1, 8, '["Alice","Bob","Chuan"]', 999990, 0);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (2, 2, 'Type 1', 1, 8, '1-1-2', 2, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (2, 8, '["Bob","Alice","Chuan"]', 999990, 0);

-- The Guide To Raire Example 2, divided by 1000
-- Note that this will _not_ have the same margins as raire-java, because of the divide by 1000,
-- but it should have the same difficulties and assertions.

-- 5 (A,B,C)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10001, 1, 'Type 1', 1, 9, '1-1-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10001, 9, '["Alice","Bob","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10002, 2, 'Type 1', 1, 9, '1-1-2', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10002, 9, '["Alice","Bob","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10003, 3, 'Type 1', 1, 9, '1-1-3', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10003, 9, '["Alice","Bob","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10004, 4, 'Type 1', 1, 9, '1-1-4', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10004, 9, '["Alice","Bob","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10005, 5, 'Type 1', 1, 9, '1-1-5', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10005, 9, '["Alice","Bob","Chuan"]', 999991, 0);

-- 5 (A,C)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10006, 6, 'Type 1', 2, 9, '1-2-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10006, 9, '["Alice","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10007, 7, 'Type 1', 2, 9, '1-2-2', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10007, 9, '["Alice","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10008, 8, 'Type 1', 2, 9, '1-2-3', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10008, 9, '["Alice","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10009, 9, 'Type 1', 2, 9, '1-2-4', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10009, 9, '["Alice","Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10010, 10, 'Type 1', 2, 9, '1-2-5', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10010, 9, '["Alice","Chuan"]', 999991, 0);

-- 5 (B,C,A)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10011, 11, 'Type 1', 3, 9, '1-3-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10011, 9, '["Bob","Chuan","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10012, 12, 'Type 1', 3, 9, '1-3-2', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10012, 9, '["Bob","Chuan","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10013, 13, 'Type 1', 3, 9, '1-3-3', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10013, 9, '["Bob","Chuan","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10014, 14, 'Type 1', 3, 9, '1-3-4', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10014, 9, '["Bob","Chuan","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10015, 1, 'Type 1', 3, 9, '1-3-5', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10015, 9, '["Bob","Chuan","Alice"]', 999991, 0);

-- 6 (B)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10016, 16, 'Type 1', 4, 9, '1-4-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10016, 9, '["Bob"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10017, 17, 'Type 1', 4, 9, '1-4-2', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10017, 9, '["Bob"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10018, 18, 'Type 1', 4, 9, '1-4-3', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10018, 9, '["Bob"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10019, 19, 'Type 1', 4, 9, '1-4-4', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10019, 9, '["Bob"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10020, 20, 'Type 1', 4, 9, '1-4-5', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10020, 9, '["Bob"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10021, 21, 'Type 1', 4, 9, '1-4-6', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10021, 9, '["Bob"]', 999991, 0);

-- 10 (C,B)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10022, 22, 'Type 1', 5, 9, '1-5-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10022, 9, '["Chuan","Bob"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10023, 23, 'Type 1', 5, 9, '1-5-2', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10023, 9, '["Chuan","Bob"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10024, 24, 'Type 1', 5, 9, '1-5-3', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10024, 9, '["Chuan","Bob"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10025, 25, 'Type 1', 5, 9, '1-5-4', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10025, 9, '["Chuan","Bob"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10026, 26, 'Type 1', 5, 9, '1-5-5', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10026, 9, '["Chuan","Bob"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10027, 27, 'Type 1', 5, 9, '1-5-6', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10027, 9, '["Chuan","Bob"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10028, 28, 'Type 1', 5, 9, '1-5-7', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10028, 9, '["Chuan","Bob"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10029, 29, 'Type 1', 5, 9, '1-5-8', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10029, 9, '["Chuan","Bob"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10030, 30, 'Type 1', 5, 9, '1-5-9', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10030, 9, '["Chuan","Bob"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10031, 31, 'Type 1', 5, 9, '1-5-10', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10031, 9, '["Chuan","Bob"]', 999991, 0);

-- 10 (C)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10032, 1, 'Type 1', 6, 9, '1-6-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10032, 9, '["Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10033, 1, 'Type 1', 6, 9, '1-6-2', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10033, 9, '["Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10034, 1, 'Type 1', 6, 9, '1-6-3', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10034, 9, '["Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10035, 1, 'Type 1', 6, 9, '1-6-4', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10035, 9, '["Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10036, 1, 'Type 1', 6, 9, '1-6-5', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10036, 9, '["Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10037, 1, 'Type 1', 6, 9, '1-6-6', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10037, 9, '["Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10038, 1, 'Type 1', 6, 9, '1-6-7', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10038, 9, '["Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10039, 1, 'Type 1', 6, 9, '1-6-8', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10039, 9, '["Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10040, 1, 'Type 1', 6, 9, '1-6-9', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10040, 9, '["Chuan"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10041, 1, 'Type 1', 6, 9, '1-6-10', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10041, 9, '["Chuan"]', 999991, 0);