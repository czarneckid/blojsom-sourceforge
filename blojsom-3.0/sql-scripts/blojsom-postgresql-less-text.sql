-- There is no conditional drop in PostgreSQL.
-- Commented out for now in case the error from a failed DROP is not handled.

-- Alter tables to drop foreign key constraints
-- ALTER TABLE ONLY TrackbackMetadata DROP CONSTRAINT trackbackmetadata_trackback_id_fkey;
-- ALTER TABLE ONLY Trackback DROP CONSTRAINT trackback_blog_id_fkey;
-- ALTER TABLE ONLY Template DROP CONSTRAINT template_blog_id_fkey;
-- ALTER TABLE ONLY Properties DROP CONSTRAINT properties_blog_id_fkey;
-- ALTER TABLE ONLY Plugin DROP CONSTRAINT plugin_blog_id_fkey;
-- ALTER TABLE ONLY PingbackMetadata DROP CONSTRAINT pingbackmetadata_pingback_id_fkey;
-- ALTER TABLE ONLY Pingback DROP CONSTRAINT pingback_blog_id_fkey;
-- ALTER TABLE ONLY EntryMetadata DROP CONSTRAINT entrymetadata_entry_id_fkey;
-- ALTER TABLE ONLY Entry DROP CONSTRAINT entry_blog_id_fkey;
-- ALTER TABLE ONLY Entry DROP CONSTRAINT entry_blog_category_id_fkey;
-- ALTER TABLE ONLY DBUserMetadata DROP CONSTRAINT dbusermetadata_user_id_fkey;
-- ALTER TABLE ONLY DBUser DROP CONSTRAINT dbuser_blog_id_fkey;
-- ALTER TABLE ONLY CommentMetadata DROP CONSTRAINT commentmetadata_comment_id_fkey;
-- ALTER TABLE ONLY Comment DROP CONSTRAINT comment_blog_id_fkey;
-- ALTER TABLE ONLY CategoryMetadata DROP CONSTRAINT categorymetadata_category_id_fkey;
-- ALTER TABLE ONLY Category DROP CONSTRAINT category_blog_id_fkey;

-- Alter tables to drop primary key constraints
-- ALTER TABLE ONLY Trackback DROP CONSTRAINT trackback_pkey;
-- ALTER TABLE ONLY Pingback DROP CONSTRAINT pingback_pkey;
-- ALTER TABLE ONLY Entry DROP CONSTRAINT entry_pkey;
-- ALTER TABLE ONLY DBUserMetadata DROP CONSTRAINT dbusermetadata_pkey;
-- ALTER TABLE ONLY DBUser DROP CONSTRAINT dbuser_pkey;
-- ALTER TABLE ONLY Comment DROP CONSTRAINT comment_pkey;
-- ALTER TABLE ONLY Category DROP CONSTRAINT category_pkey;
-- ALTER TABLE ONLY Blog DROP CONSTRAINT blog_pkey;

--
-- Table structure for table Blog
--

-- DROP TABLE Blog;
CREATE TABLE Blog (
  id SERIAL PRIMARY KEY,
  blog_id varchar(50)
);

--
-- Table structure for table Category
--

-- DROP TABLE Category;
CREATE TABLE Category (
  category_id SERIAL PRIMARY KEY,
  blog_id int NOT NULL REFERENCES Blog (id) ON DELETE CASCADE,
  parent_category_id int default NULL,
  name text NOT NULL,
  description text
);

--
-- Table structure for table CategoryMetadata
--

-- DROP TABLE CategoryMetadata;
CREATE TABLE CategoryMetadata (
  category_id int NOT NULL REFERENCES Category (category_id) ON DELETE CASCADE,
  metadata_key varchar(255) NOT NULL,
  metadata_value text
);

--
-- Table structure for table Comment
--

-- DROP TABLE Comment;
CREATE TABLE Comment (
  comment_id SERIAL PRIMARY KEY,
  entry_id int NOT NULL,
  author text,
  author_url text,
  author_email text,
  comment text,
  date timestamp NOT NULL,
  ip varchar(100) default NULL,
  status varchar(255) default NULL,
  comment_parent int default NULL,
  blog_id int NOT NULL REFERENCES Blog (id) ON DELETE CASCADE
);

--
-- Table structure for table CommentMetadata
--

-- DROP TABLE CommentMetadata;
CREATE TABLE CommentMetadata (
  comment_id int NOT NULL REFERENCES Comment (comment_id) ON DELETE CASCADE,
  metadata_key varchar(255) NOT NULL,
  metadata_value text
);

--
-- Table structure for table DBUser
--

-- DROP TABLE DBUser;
CREATE TABLE DBUser (
  user_id SERIAL PRIMARY KEY,
  user_login varchar(50) NOT NULL,
  user_password varchar(64) NOT NULL,
  user_name varchar(250) NOT NULL,
  user_email varchar(100) NOT NULL,
  user_registered timestamp NOT NULL,
  user_status varchar(64) NOT NULL,
  blog_id int NOT NULL REFERENCES Blog (id) ON DELETE CASCADE
);

--
-- Table structure for table DBUserMetadata
--

-- DROP TABLE DBUserMetadata;
CREATE TABLE DBUserMetadata (
  user_metadata_id SERIAL PRIMARY KEY,
  user_id int NOT NULL REFERENCES DBUser (user_id) ON DELETE CASCADE,
  metadata_key varchar(255) NOT NULL,
  metadata_value text
);

--
-- Table structure for table Entry
--

-- DROP TABLE Entry;
CREATE TABLE Entry (
  entry_id SERIAL PRIMARY KEY,
  blog_id int NOT NULL REFERENCES Blog (id) ON DELETE CASCADE,
  title text,
  description text,
  entry_date timestamp NOT NULL,
  blog_category_id int NOT NULL REFERENCES Category (category_id) ON DELETE CASCADE,
  status text,
  author text,
  allow_comments int default '1',
  allow_trackbacks int default '1',
  allow_pingbacks int default '1',
  post_slug text NOT NULL,
  modified_date timestamp NOT NULL
);

--
-- Table structure for table EntryMetadata
--

-- DROP TABLE EntryMetadata;
CREATE TABLE EntryMetadata (
  entry_id int NOT NULL REFERENCES Entry (entry_id) ON DELETE CASCADE,
  metadata_key varchar(255) NOT NULL,
  metadata_value text
);

--
-- Table structure for table Pingback
--

-- DROP TABLE Pingback;
CREATE TABLE Pingback (
  pingback_id SERIAL PRIMARY KEY,
  entry_id int NOT NULL,
  title text,
  excerpt text,
  url text,
  blog_name text,
  trackback_date timestamp NOT NULL,
  blog_id int NOT NULL REFERENCES Blog (id) ON DELETE CASCADE,
  ip varchar(100) default NULL,
  status varchar(255) default NULL,
  source_uri text NOT NULL,
  target_uri text NOT NULL
);

--
-- Table structure for table PingbackMetadata
--

-- DROP TABLE PingbackMetadata;
CREATE TABLE PingbackMetadata (
  pingback_id int NOT NULL REFERENCES Pingback (pingback_id) ON DELETE CASCADE,
  metadata_key varchar(255) NOT NULL,
  metadata_value text
);

--
-- Table structure for table Plugin
--

-- DROP TABLE Plugin;
CREATE TABLE Plugin (
  blog_id int NOT NULL REFERENCES Blog (id) ON DELETE CASCADE,
  plugin_flavor varchar(50) NOT NULL,
  plugin_value varchar(4096) default NULL
);

--
-- Table structure for table Properties
--

-- DROP TABLE Properties;
CREATE TABLE Properties (
  blog_id int NOT NULL REFERENCES Blog (id) ON DELETE CASCADE,
  property_name varchar(255) NOT NULL,
  property_value longtext
);

--
-- Table structure for table Template
--

-- DROP TABLE Template;
CREATE TABLE Template (
  blog_id int NOT NULL REFERENCES Blog (id) ON DELETE CASCADE,
  template_flavor varchar(50) NOT NULL,
  template_value varchar(255) default NULL
);

--
-- Table structure for table Trackback
--

-- DROP TABLE Trackback;
CREATE TABLE Trackback (
  trackback_id SERIAL PRIMARY KEY,
  entry_id int NOT NULL,
  title text,
  excerpt text,
  url text,
  blog_name text,
  trackback_date timestamp NOT NULL,
  blog_id int NOT NULL REFERENCES Blog (id) ON DELETE CASCADE,
  ip varchar(100) default NULL,
  status varchar(255) default NULL
);

--
-- Table structure for table TrackbackMetadata
--

-- DROP TABLE TrackbackMetadata;
CREATE TABLE TrackbackMetadata (
  trackback_id int NOT NULL REFERENCES Trackback (trackback_id) ON DELETE CASCADE,
  metadata_key varchar(255) NOT NULL,
  metadata_value text
);
