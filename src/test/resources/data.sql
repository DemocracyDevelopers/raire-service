-- Test Counties
INSERT INTO county (id, name) values (8, 'Ballina');
INSERT INTO county (id, name) values (9, 'Byron');
INSERT INTO county (id, name) values (10, 'Westgarth');
-- Contest
-- Simple contests to test basic functioning.
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (8, 999990, 0, 'IRV', 'Multi-County Contest 1', 0, 7, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (9, 999991, 0, 'IRV', 'Multi-County Contest 1', 1, 7, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (8, 999992, 0, 'IRV', 'Ballina Mayoral', 2, 7, 1);
-- Deliberately mixed IRV/Plurality contest (which should not happen) to check that this is rejected.
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (9, 999993, 0, 'IRV', 'Invalid Mixed Contest', 3, 7, 1);
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (10, 999994, 0, 'Plurality', 'Invalid Mixed Contest', 4, 7, 1);
-- Plurality contest
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (10, 999995, 0, 'Plurality', 'Valid Plurality Contest', 5, 7, 1);
-- IRV contest designed to have no matching CVRs
INSERT INTO contest (county_id, id, version, description, name, sequence_number, votes_allowed, winners_allowed) values (10, 999996, 0, 'IRV', 'No CVR Mayoral', 6, 7, 1);


