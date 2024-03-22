-- Test schema for contests. Note this includes all the fields in corla, including the ones we never read.
create table contest(
  id SERIAL,
  description varchar(100) not null,
  name varchar(1000) not null,
  -- sequence_number int not null,
  county_id int not null,
  version int not null,
  -- votes_allowed int not null,
  -- winners_allowed int not null,
  primary key(id)
);