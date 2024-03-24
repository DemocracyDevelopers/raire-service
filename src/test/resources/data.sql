-- schema is (countyID, id, version, description, name).
-- IDs are deliberately chosen to avoid clashes, in case someone wants to write tests that save contests.
-- Note that this is _not_ the same schema as the real corla database, which includes other fields.
INSERT INTO contest values(8, 999990, 0, 'IRV', 'Byron');
INSERT INTO contest values(8, 999991, 0, 'IRV', 'Ballina Mayoral');
