-- Test Counties
INSERT INTO county (id, name) values (8, 'Ballina');
INSERT INTO county (id, name) values (9, 'Byron');
INSERT INTO county (id, name) values (10, 'Westgarth');
INSERT INTO county (id, name) values (11, 'Malformed');
-- Contest
-- Simple contests to test basic functioning.
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (8, 999990, 0, 'IRV', 'Multi-County Contest 1', 0, 7, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (9, 999991, 0, 'IRV', 'Multi-County Contest 1', 1, 7, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (8, 999992, 0, 'IRV', 'Ballina Mayoral', 2, 7, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (8, 999998, 0, 'IRV', 'Ballina One Vote Contest', 3, 7, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (8, 999997, 0, 'IRV', 'Ballina Board of Parks', 4, 7, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (10, 999999, 0, 'IRV', 'Larger Contest', 9, 4, 1);
-- Deliberately mixed IRV/Plurality contest (which should not happen) to check that this is rejected.
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (9, 999993, 0, 'IRV', 'Invalid Mixed Contest', 5, 7, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (10, 999994, 0, 'Plurality', 'Invalid Mixed Contest', 6, 7, 1);
-- Plurality contest
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (10, 999995, 0, 'Plurality', 'Valid Plurality Contest', 7, 7, 1);
-- IRV contest designed to have no matching CVRs
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (10, 999996, 0, 'IRV', 'No CVR Mayoral', 8, 7, 1);
-- Single CVR Contest
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (10, 999988, 0, 'IRV', 'Multi-County Contest 1', 10, 4, 1);
-- Contest with malformed CVR contest infos.
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (11, 999987, 0, 'IRV', 'Malformed Contest 1', 11, 4, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (11, 999986, 0, 'IRV', 'Malformed Contest 2', 12, 4, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (11, 999985, 0, 'IRV', 'Malformed Contest 3', 13, 4, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (11, 999984, 0, 'IRV', 'Malformed Contest 4', 14, 4, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (11, 999983, 0, 'IRV', 'Malformed Contest 5', 15, 4, 1);
--CVRs
INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (1, 1, 'Type 1', 1, 8, '1-1-1', 1, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (1, 8, '["Alice","Bob","Charlie"]', 999998, 0);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (2, 2, 'Type 2', 2, 9, '2-2-2', 2, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (2, 9, '["Alice P. Mangrove"]', 999991, 1);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (3, 3, 'Type 2', 3, 8, '3-3-3', 3, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (3, 8, '["Charlie C. Chaplin","West W. Westerson"]', 999990, 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (3, 8, '["Laurie M.","Bonny Smith","Thomas D''Angelo"]', 999997, 3);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (4, 4, 'Type 2', 4, 8, '4-4-4', 4, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (4, 8, '["West W. Westerson"]', 999990, 4);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (5, 5, 'Type 2', 5, 8, '5-5-5', 5, 'UPLOADED', 2);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (5, 8, '["Al (Bob) Jones","West W. Westerson","Charlie C. Chaplin"]', 999990, 5);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (6, 6, 'Type 2', 6, 9, '6-6-6', 6, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (6, 9, '["Charlie C. Chaplin"]', 999991, 6);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (7, 7, 'Type 2', 7, 9, '7-7-7', 7, 'UPLOADED', 3);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (7, 9, '["West W. Westerson","Al (Bob) Jones"]', 999991, 7);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (8, 8, 'Type 1', 8, 10, '8-8-8', 8, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (8, 10, '["A","B","CC"]', 999999, 8);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (9, 9, 'Type 1', 9, 10, '9-9-9', 9, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (9, 10, '["B","CC"]', 999999, 9);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (10, 10, 'Type 1', 10, 10, '10-10-10', 10, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10, 10, '["CC"]', 999999, 10);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (10, 10, '["Harold Holt","Wendy Squires","(B)(C)(D)"]', 999988, 11);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (11, 11, 'Type 1', 11, 10, '11-11-11', 11, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (11, 10, '["CC","A","B"]', 999999, 12);

INSERT INTO cast_vote_record (id, cvr_number, ballot_type, batch_id, county_id, imprinted_id, record_id, record_type, scanner_id) values (12, 12, 'Type 1', 12, 11, '12-12-12', 12, 'UPLOADED', 1);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (12, 11, null, 999987, 13);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (12, 11, NULL, 999986, 14);
-- This entry is not an error, just a blank vote.
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (12, 11, '[]', 999985, 15);
-- A vote where the choice string is not a list.
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (12, 11, 'NotAList', 999984, 16);
INSERT INTO cvr_contest_info (cvr_id, county_id, choices, contest_id, index) values (12, 11, '', 999983, 17);