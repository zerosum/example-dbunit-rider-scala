create schema test
    create table "user"
    (
        id   integer primary key,
        name varchar(256)
    )
    create table tweet
    (
        id      varchar(64) primary key,
        content varchar(140),
        date    timestamp,
        user_id integer
    )
    create table follower
    (
        id          integer primary key,
        user_id     integer,
        follower_id integer
    );
