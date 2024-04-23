-- Assertion generation data for when we know exactly what the answer should be. This includes mostly
-- small examples for which the correct answer can be established by human inspection.

-- Test Counties
INSERT INTO county (id, name) values (8, 'TiedWinnersCounty');

-- Contest
-- Simple contests to test basic functioning.
-- INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (8, 999990, 0, 'IRV', 'Multi-County Contest 1', 0, 7, 1);
-- INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (9, 999991, 0, 'IRV', 'Multi-County Contest 1', 1, 7, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (8, 999990, 0, 'IRV', 'Tied Winners Contest', 2, 7, 1);

--CVRs
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (1, 1, 'Type 1', 1, 8, '1-1-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (1, 8, '["Alice","Bob","Charlie"]', 999990, 0);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (2, 2, 'Type 1', 1, 8, '1-1-2', 2, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (2, 8, '["Bob","Alice","Charlie"]', 999990, 0);

