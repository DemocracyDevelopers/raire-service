-- Contest
-- schema is (countyID, id, version, description, name).
-- IDs are deliberately chosen to avoid clashes, in case someone wants to write tests that save contests.
-- This is _not_ the same schema as the real corla database, which includes other fields.
--
-- Simple contests to test basic functioning.
INSERT INTO contest values(8, 999990, 0, 'IRV', 'Byron');
INSERT INTO contest values(9, 999991, 0, 'IRV', 'Byron');
INSERT INTO contest values(8, 999992, 0, 'IRV', 'Ballina Mayoral');
-- Deliberately mixed IRV/Plurality contest (which should not happen) to check that this is rejected.
INSERT INTO contest values(9, 999993, 0, 'IRV', 'Invalid Mixed Contest');
INSERT INTO contest values(10, 999994, 0, 'Plurality', 'Invalid Mixed Contest');
-- Plurality contest
INSERT INTO contest values(10, 999995, 0, 'Plurality', 'Valid Plurality Contest');


