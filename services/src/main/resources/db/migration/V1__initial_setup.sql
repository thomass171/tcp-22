/**
 *
 */

create table maze (
    id bigint not null,
    name varchar(30) not null,
    grid varchar not null,
    secret varchar,
    description varchar not null,
    type varchar,
    created_at timestamp with time zone not null,
    created_by varchar(30) not null,
    modified_at timestamp with time zone not null,
    modified_by varchar(30) not null,

    primary key(id),
    unique (name)
);

create sequence maze_seq start with 1;

