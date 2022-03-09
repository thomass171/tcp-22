/**
 *
 */

create table maze (
    id bigint not null,
    grid varchar not null,
    description varchar not null,
    created_at timestamp with time zone not null,
    created_by varchar(30) not null,
    modified_at timestamp with time zone not null,
    modified_by varchar(30) not null,

    primary key(id)
);

create sequence maze_seq start with 1;

