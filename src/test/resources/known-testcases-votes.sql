-- Assertion generation data for when we know exactly what the answer should be. This includes mostly
-- small examples for which the correct answer can be established by human inspection.

-- Test Counties
INSERT INTO county (id, name) values (8, 'TiedWinnersCounty');
INSERT INTO county (id, name) values (9, 'GuideToRaireCounty');
INSERT INTO county (id, name) values (10, 'SimpleCounty');

-- Contest
-- Simple contests to test basic functioning.
-- INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (8, 999990, 0, 'IRV', 'Multi-County Contest 1', 0, 7, 1);
-- INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (9, 999991, 0, 'IRV', 'Multi-County Contest 1', 1, 7, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (8, 999990, 0, 'IRV', 'Tied Winners Contest', 2, 7, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (9, 999991, 0, 'IRV', 'Guide To Raire Example 1', 3, 4, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (9, 999992, 0, 'IRV', 'Guide To Raire Example 2', 4, 3, 1);

-- CVRs
-- Tied Winners Contest
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (1, 1, 'Type 1', 1, 8, '1-1-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (1, 8, '["Alice","Bob","Chuan"]', 999990, 0);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (2, 2, 'Type 1', 1, 8, '1-1-2', 2, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (2, 8, '["Bob","Alice","Chuan"]', 999990, 0);

-- The Guide To Raire Example 1, divided by 500
-- Note that this will _not_ have the same margins as raire-java, because of the divide by 500,
-- but it should have the same difficulties and assertions.

-- 10 (C,B,A)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20001, 1, 'Type 1', 1, 9, '1-1-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20001, 9, '[Chuan","Bob","Alice]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20002, 2, 'Type 1', 1, 9, '1-1-2', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20002, 9, '[Chuan","Bob","Alice]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20003, 3, 'Type 1', 1, 9, '1-1-3', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20003, 9, '[Chuan","Bob","Alice]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20004, 4, 'Type 1', 1, 9, '1-1-4', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20004, 9, '[Chuan","Bob","Alice]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20005, 5, 'Type 1', 1, 9, '1-1-5', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20005, 9, '[Chuan","Bob","Alice]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20006, 6, 'Type 1', 1, 9, '1-1-6', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20006, 9, '[Chuan","Bob","Alice]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20007, 7, 'Type 1', 1, 9, '1-1-7', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20007, 9, '[Chuan","Bob","Alice]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20008, 8, 'Type 1', 1, 9, '1-1-8', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20008, 9, '[Chuan","Bob","Alice]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20009, 9, 'Type 1', 1, 9, '1-1-9', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20009, 9, '[Chuan","Bob","Alice]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20010, 10, 'Type 1', 1, 9, '1-1-10', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20010, 9, '[Chuan","Bob","Alice]', 999991, 0);

-- 2 (B,C,D)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20011, 11, 'Type 1', 2, 9, '1-2-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20011, 9, '["Bob","Chuan","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20012, 12, 'Type 1', 2, 9, '1-2-2', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20012, 9, '["Bob","Chuan","Diego"]', 999991, 0);

-- 3 (D,A)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20013, 13, 'Type 1', 3, 9, '1-3-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20013, 9, '["Diego","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20014, 14, 'Type 1', 3, 9, '1-3-2', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20014, 9, '["Diego","Alice"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20015, 15, 'Type 1', 3, 9, '1-3-3', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20015, 9, '["Diego","Alice"]', 999991, 0);

-- 8 (A,D)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20016, 16, 'Type 1', 4, 9, '1-4-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20016, 9, '["Alice","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20017, 17, 'Type 1', 4, 9, '1-4-2', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20017, 9, '["Alice","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20018, 18, 'Type 1', 4, 9, '1-4-3', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20018, 9, '["Alice","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20019, 19, 'Type 1', 4, 9, '1-4-4', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20019, 9, '["Alice","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20020, 20, 'Type 1', 4, 9, '1-4-5', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20020, 9, '["Alice","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20021, 21, 'Type 1', 4, 9, '1-4-6', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20021, 9, '["Alice","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20022, 22, 'Type 1', 4, 9, '1-4-7', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20022, 9, '["Alice","Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20023, 23, 'Type 1', 4, 9, '1-4-8', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20023, 9, '["Alice","Diego"]', 999991, 0);

-- 4 (D)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20024, 24, 'Type 1', 5, 9, '1-5-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20024, 9, '["Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20025, 25, 'Type 1', 5, 9, '1-5-2', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20025, 9, '["Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20026, 26, 'Type 1', 5, 9, '1-5-3', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20026, 9, '["Diego"]', 999991, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20027, 27, 'Type 1', 5, 9, '1-5-4', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20027, 9, '["Diego"]', 999991, 0);

-- The Guide To Raire Example 2, divided by 1000
-- Note that this will _not_ have the same margins as raire-java, because of the divide by 1000,
-- but it should have the same difficulties and assertions.

-- 5 (A,B,C)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20001, 1, 'Type 1', 1, 9, '2-1-1', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20001, 9, '["Alice","Bob","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20002, 2, 'Type 1', 1, 9, '2-1-2', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20002, 9, '["Alice","Bob","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20003, 3, 'Type 1', 1, 9, '2-1-3', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20003, 9, '["Alice","Bob","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20004, 4, 'Type 1', 1, 9, '2-1-4', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20004, 9, '["Alice","Bob","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20005, 5, 'Type 1', 1, 9, '2-1-5', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20005, 9, '["Alice","Bob","Chuan"]', 999992, 0);

-- 5 (A,C)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20006, 6, 'Type 1', 2, 9, '2-2-1', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20006, 9, '["Alice","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20007, 7, 'Type 1', 2, 9, '2-2-2', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20007, 9, '["Alice","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20008, 8, 'Type 1', 2, 9, '2-2-3', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20008, 9, '["Alice","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20009, 9, 'Type 1', 2, 9, '2-2-4', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20009, 9, '["Alice","Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20010, 10, 'Type 1', 2, 9, '2-2-5', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20010, 9, '["Alice","Chuan"]', 999992, 0);

-- 5 (B,C,A)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20011, 11, 'Type 1', 3, 9, '2-3-1', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20011, 9, '["Bob","Chuan","Alice"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20012, 12, 'Type 1', 3, 9, '2-3-2', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20012, 9, '["Bob","Chuan","Alice"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20013, 13, 'Type 1', 3, 9, '2-3-3', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20013, 9, '["Bob","Chuan","Alice"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20014, 14, 'Type 1', 3, 9, '2-3-4', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20014, 9, '["Bob","Chuan","Alice"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20015, 1, 'Type 1', 3, 9, '2-3-5', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20015, 9, '["Bob","Chuan","Alice"]', 999992, 0);

-- 6 (B)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20016, 16, 'Type 1', 4, 9, '2-4-1', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20016, 9, '["Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20017, 17, 'Type 1', 4, 9, '2-4-2', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20017, 9, '["Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20018, 18, 'Type 1', 4, 9, '2-4-3', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20018, 9, '["Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20019, 19, 'Type 1', 4, 9, '2-4-4', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20019, 9, '["Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20020, 20, 'Type 1', 4, 9, '2-4-5', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20020, 9, '["Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20021, 21, 'Type 1', 4, 9, '2-4-6', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20021, 9, '["Bob"]', 999992, 0);

-- 10 (C,B)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20022, 22, 'Type 1', 5, 9, '2-5-1', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20022, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20023, 23, 'Type 1', 5, 9, '2-5-2', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20023, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20024, 24, 'Type 1', 5, 9, '2-5-3', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20024, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20025, 25, 'Type 1', 5, 9, '2-5-4', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20025, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20026, 26, 'Type 1', 5, 9, '2-5-5', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20026, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20027, 27, 'Type 1', 5, 9, '2-5-6', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20027, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20028, 28, 'Type 1', 5, 9, '2-5-7', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20028, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20029, 29, 'Type 1', 5, 9, '2-5-8', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20029, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20030, 30, 'Type 1', 5, 9, '2-5-9', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20030, 9, '["Chuan","Bob"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20031, 31, 'Type 1', 5, 9, '2-5-10', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20031, 9, '["Chuan","Bob"]', 999992, 0);

-- 10 (C)
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20032, 1, 'Type 1', 6, 9, '2-6-1', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20032, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20033, 1, 'Type 1', 6, 9, '2-6-2', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20033, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20034, 1, 'Type 1', 6, 9, '2-6-3', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20034, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20035, 1, 'Type 1', 6, 9, '2-6-4', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20035, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20036, 1, 'Type 1', 6, 9, '2-6-5', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20036, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20037, 1, 'Type 1', 6, 9, '2-6-6', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20037, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20038, 1, 'Type 1', 6, 9, '2-6-7', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20038, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20039, 1, 'Type 1', 6, 9, '2-6-8', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20039, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20040, 1, 'Type 1', 6, 9, '2-6-9', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20040, 9, '["Chuan"]', 999992, 0);
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (20041, 1, 'Type 1', 6, 9, '2-6-10', 1, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (20041, 9, '["Chuan"]', 999992, 0);