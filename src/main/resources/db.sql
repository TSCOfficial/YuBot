create sequence dynamic_message_name_seq
    as integer;

alter sequence dynamic_message_name_seq owner to yubot;

create sequence ticket_type_control_type_seq
    as integer;

alter sequence ticket_type_control_type_seq owner to yubot;

create table ticket
(
    owner_id            bigint                                       not null,
    assignee_id         bigint,
    channel_id          bigint                                       not null
        constraint ticket_pk
            primary key,
    type                varchar(50)                                  not null,
    last_activity_at    timestamp,
    welcome_message_id  bigint                                       not null,
    is_request_pending  boolean     default false                    not null,
    close_request_count integer     default 0                        not null,
    status              varchar(50) default 'NEW'::character varying not null,
    updated_at          timestamp
);

comment on column ticket.last_activity_at is 'Last activity (message sent) of the ticket owner';

alter table ticket
    owner to yubot;

create table active_mod
(
    moderator_id                   bigint    not null
        constraint closure_pk
            primary key,
    last_activity_at               timestamp not null,
    activity_requested_at          timestamp,
    activity_request_message_id    bigint,
    requested_attention_message_id bigint
);

comment on table active_mod is 'Server closure and active-mod system';

comment on column active_mod.activity_requested_at is 'When the activity request was sent (Activity request is sent when the mod is inactive for a set amount of time)';

comment on column active_mod.requested_attention_message_id is 'If the mod is the last one active, the system requests attention of all mods.';

alter table active_mod
    owner to yubot;

create table dynamic_message
(
    name       varchar(50) not null
        constraint dynamic_message_pk
            primary key,
    channel_id bigint,
    message_id bigint
);

comment on table dynamic_message is 'Dynamic-autoupdating messages';

comment on column dynamic_message.name is 'name of the dynamic message';

alter table dynamic_message
    owner to yubot;

alter sequence dynamic_message_name_seq owned by dynamic_message.name;

create table ticket_type_control
(
    type      varchar(50) not null
        constraint ticket_type_control_pk
            primary key,
    is_locked boolean default false
);

comment on table ticket_type_control is 'Control the ticket types';

alter table ticket_type_control
    owner to yubot;

alter sequence ticket_type_control_type_seq owned by ticket_type_control.type;

create table active_mod_tracking
(
    moderator_id     bigint            not null,
    active_time      integer default 0 not null,
    last_time_active timestamp
);

comment on table active_mod_tracking is 'Track how much time each mod was active for';

comment on column active_mod_tracking.active_time is 'active time in minutes';

comment on column active_mod_tracking.last_time_active is 'The system uses this column to check if one minute has passed or not (else, restarting the application increments the counter every time)';

alter table active_mod_tracking
    owner to yubot;

