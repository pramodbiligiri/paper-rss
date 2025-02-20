create table arxiv_oai (
  id bigserial NOT NULL UNIQUE,
  oai_xml text,
  src jsonb,
  batch_id bigint,
  resp_date varchar(30), -- responseDate field in XML

  PRIMARY KEY (id)
);
create index idx_batch_id on arxiv_oai(batch_id);

create table paper_data (
  id bigserial NOT NULL UNIQUE,
  arxiv_id varchar (30),
  title text,
  abstract text,
  link varchar(100),
  authors text,

  -- the date associated with the max version for the paper in the arxiv feed
  pubdate timestamp with time zone,

-- TSVECTOR column type for search index:
-- See: https://www.postgresql.org/docs/8.3/textsearch-tables.html
-- and https://blog.crunchydata.com/blog/postgres-full-text-search-a-search-engine-in-a-database
-- and https://www.postgresql.org/docs/9.1/textsearch-indexes.html
-- Giving up on pubdate indexing for now, running to lack of way of "immutable" extraction of date parts
-- Tried this: setweight(to_tsvector('english', concat(coalesce(cast(extract(year from pubdate ) as varchar)
--   , ''), coalesce(cast(extract(month from pubdate) as varchar), ''))), 'E')
  search TSVECTOR GENERATED ALWAYS AS
     (
      setweight(to_tsvector('english', coalesce(title, '')), 'A') ||
      setweight(to_tsvector('english', coalesce(authors, '')), 'B') ||
      setweight(to_tsvector('english', coalesce(arxiv_id, '')), 'C') ||
      setweight(to_tsvector('english', coalesce(abstract, '')), 'D')
     ) STORED,

  PRIMARY KEY (id)
);
create index idx_pubdate on paper_data (pubdate);
create index idx_paper_data_arxiv_id on paper_data(arxiv_id);
create index pd_search_idx ON paper_data USING GIN (search);

create table paper_category (
  id bigserial NOT NULL UNIQUE,
  paper_id bigint,
  category varchar(50),

  PRIMARY KEY (id),
  FOREIGN KEY (paper_id) REFERENCES paper_data(id)
);
create index idx_category_paper on paper_category (paper_id);
create index idx_category on paper_category (category);

create table paper_audio (
  id bigserial NOT NULL UNIQUE,
  paper_id bigint,
  audio varchar(45), -- currently UUID of object (36 chars) + file format extension
  duration int, -- seconds
  create_time timestamp with time zone,

  PRIMARY KEY (id),
  FOREIGN KEY (paper_id) REFERENCES paper_data(id)
);
create index idx_paper_audio_paper on paper_audio (paper_id);

create table paper_tts_task (
  id bigserial NOT NULL UNIQUE,

  paper_id bigint,
  paper_audio_id bigint,
  status smallint,

  start_time timestamp,
  end_time timestamp,

  PRIMARY KEY (id)

  -- Not actually adding these foreign keys, as this is just an
  -- operations-related table
  -- FOREIGN KEY (paper_id) REFERENCES paper_data(id),
  -- FOREIGN KEY (paper_audio_id) REFERENCES paper_audio(id)
);

create table email_sub (
  id bigserial NOT NULL UNIQUE,

  email_id VARCHAR(100),
  create_time timestamp with time zone,

  PRIMARY KEY (id)
);

create table feedback (
  id bigserial NOT NULL UNIQUE,

  data VARCHAR(5000),
  create_time timestamp with time zone,

  PRIMARY KEY (id)
);

-- START: Spring Session related (used only by the webapp) :START
CREATE TABLE SPRING_SESSION (
  PRIMARY_ID CHAR(36) NOT NULL,
  SESSION_ID CHAR(36) NOT NULL,
  CREATION_TIME BIGINT NOT NULL,
  LAST_ACCESS_TIME BIGINT NOT NULL,
  MAX_INACTIVE_INTERVAL INT NOT NULL,
  EXPIRY_TIME BIGINT NOT NULL,
  PRINCIPAL_NAME VARCHAR(100),
  CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
  SESSION_PRIMARY_ID CHAR(36) NOT NULL,
  ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
  ATTRIBUTE_BYTES BYTEA NOT NULL,
  CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
  CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);

create table listen_stat (
  id bigserial NOT NULL UNIQUE,

  session_id char(36) NOT NULL,
  paper_id bigint,
  src VARCHAR(30), -- which UI element is responsible
  current int, -- current location in paper, in seconds
  total int, -- total duration of paper, in seconds

  PRIMARY KEY (id)
);
create index idx_session on listen_stat(session_id);

create table rss_feed (
  id bigserial NOT NULL UNIQUE,

  create_time timestamp with time zone,
  selector varchar(100),
  content text,

  PRIMARY KEY (id)
);
create index idx_rss_selector on rss_feed(selector, create_time);

-- END: Spring Session related :END

create sequence seq_listen_stat START 1;
create sequence seq_arxiv_oai START 1;
create sequence seq_paper_data START 1;
create sequence seq_paper_category START 1;
create sequence seq_paper_audio START 1;
create sequence seq_paper_tts_task START 1;
create sequence seq_email_sub START 1;
create sequence seq_feedback START 1;
create sequence seq_rss_feed START 1;
create sequence hibernate_sequence START 1;
