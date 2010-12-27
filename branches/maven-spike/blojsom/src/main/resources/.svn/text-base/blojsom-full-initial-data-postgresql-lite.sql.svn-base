--
-- Initial database creation
--

-- Create the database blojsom and set the owner to the blojsom user prior to
-- running this script.
-- DROP DATABASE blojsom;
-- CREATE DATABASE blojsom OWNER blojsom ENCODING 'UTF8';
-- USE blojsom;

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
-- Dumping data for table Blog
--

INSERT INTO Blog VALUES (1,'default');

--
-- Table structure for table Category
--

-- DROP TABLE Category;
CREATE TABLE Category (
  category_id SERIAL PRIMARY KEY,
  blog_id int NOT NULL REFERENCES Blog (id) ON DELETE CASCADE,
  parent_category_id int default NULL,
  name varchar(255) NOT NULL,
  description text
);

--
-- Dumping data for table Category
--

INSERT INTO Category VALUES (1,1,NULL,'/uncategorised/','Uncategorised');

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
-- Dumping data for table CategoryMetadata
--


--
-- Table structure for table Comment
--

-- DROP TABLE Comment;
CREATE TABLE Comment (
  comment_id SERIAL PRIMARY KEY,
  entry_id int NOT NULL,
  author varchar(250),
  author_url varchar(250),
  author_email varchar(100),
  comment text,
  date timestamp NOT NULL,
  ip varchar(100) default NULL,
  status varchar(255) default NULL,
  comment_parent int default NULL,
  blog_id int NOT NULL REFERENCES Blog (id) ON DELETE CASCADE
);

--
-- Dumping data for table Comment
--


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
-- Dumping data for table CommentMetadata
--


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
-- Dumping data for table DBUser
--

INSERT INTO DBUser VALUES (1,'default','default','Default User','default_owner@email.com',NOW(),'',1);

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
-- Dumping data for table DBUserMetadata
--

INSERT INTO DBUserMetadata VALUES (1,1,'all_permissions_permission','true');
INSERT INTO DBUserMetadata VALUES (2,1,'display-response-text','false');
INSERT INTO DBUserMetadata VALUES (3,1,'use-richtext-editor','false');
--
-- Table structure for table Entry
--

-- DROP TABLE Entry;
CREATE TABLE Entry (
  entry_id SERIAL PRIMARY KEY,
  blog_id int NOT NULL REFERENCES Blog (id) ON DELETE CASCADE,
  title varchar(255),
  description text,
  entry_date timestamp NOT NULL,
  blog_category_id int NOT NULL REFERENCES Category (category_id) ON DELETE CASCADE,
  status varchar(255),
  author varchar(250),
  allow_comments int default '1',
  allow_trackbacks int default '1',
  allow_pingbacks int default '1',
  post_slug varchar(255) NOT NULL,
  modified_date timestamp NOT NULL
);

--
-- Dumping data for table Entry
--


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
-- Dumping data for table EntryMetadata
--


--
-- Table structure for table Pingback
--

-- DROP TABLE Pingback;
CREATE TABLE Pingback (
  pingback_id SERIAL PRIMARY KEY,
  entry_id int NOT NULL,
  title varchar(255),
  excerpt varchar(500),
  url varchar(255),
  blog_name varchar(255),
  trackback_date timestamp NOT NULL,
  blog_id int NOT NULL REFERENCES Blog (id) ON DELETE CASCADE,
  ip varchar(100) default NULL,
  status varchar(255) default NULL,
  source_uri varchar(255) NOT NULL,
  target_uri varchar(255) NOT NULL
);

--
-- Dumping data for table Pingback
--


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
-- Dumping data for table PingbackMetadata
--


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
-- Dumping data for table Plugin
--

INSERT INTO Plugin VALUES (1,'html','meta, tag-cloud, date-format, referer-log, calendar-gui, calendar-filter, comment, trackback, simple-search, emoticons, macro-expansion, days-since-posted, word-count, simple-obfuscation, nofollow, rss-enclosure, technorati-tags');
INSERT INTO Plugin VALUES (1,'default','conditional-get, meta, nofollow, rss-enclosure');
INSERT INTO Plugin VALUES (1,'admin','admin');

--
-- Table structure for table Properties
--

-- DROP TABLE Properties;
CREATE TABLE Properties (
  blog_id int NOT NULL REFERENCES Blog (id) ON DELETE CASCADE,
  property_name varchar(255) NOT NULL,
  property_value text
);

--
-- Dumping data for table Properties
--

INSERT INTO Properties VALUES (1,'blog-url','/blojsom/blog/default');
INSERT INTO Properties VALUES (1,'blog-admin-url','/blojsom/blog/default');
INSERT INTO Properties VALUES (1,'blog-base-url','/blojsom');
INSERT INTO Properties VALUES (1,'blog-base-admin-url','/blojsom');
INSERT INTO Properties VALUES (1,'blog-language','en');
INSERT INTO Properties VALUES (1,'blog-name','NAME YOUR BLOG');
INSERT INTO Properties VALUES (1,'blog-description','DESCRIBE YOUR BLOG');
INSERT INTO Properties VALUES (1,'blog-entries-display','15');
INSERT INTO Properties VALUES (1,'blog-owner','Default Owner');
INSERT INTO Properties VALUES (1,'blog-owner-email','default_owner@email.com');
INSERT INTO Properties VALUES (1,'blog-comments-enabled','true');
INSERT INTO Properties VALUES (1,'blog-trackbacks-enabled','true');
INSERT INTO Properties VALUES (1,'blog-email-enabled','true');
INSERT INTO Properties VALUES (1,'blog-default-flavor','html');
INSERT INTO Properties VALUES (1,'plugin-comment-autoformat','true');
INSERT INTO Properties VALUES (1,'linear-navigation-enabled','true');
INSERT INTO Properties VALUES (1,'comment-moderation-enabled','true');
INSERT INTO Properties VALUES (1,'trackback-moderation-enabled','true');
INSERT INTO Properties VALUES (1,'pingback-moderation-enabled','true');
INSERT INTO Properties VALUES (1,'blog-ping-urls','http://rpc.pingomatic.com');
INSERT INTO Properties VALUES (1,'blojsom-extension-metaweblog-accepted-types','image/jpeg, image/gif, image/png, img');
INSERT INTO Properties VALUES (1,'xmlrpc-enabled','true');
INSERT INTO Properties VALUES (1,'blog-pingbacks-enabled','true');

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
-- Dumping data for table Template
--

INSERT INTO Template VALUES (1,'rss','rss.vm, text/xml;charset=UTF-8');
INSERT INTO Template VALUES (1,'rsd','rsd.vm, application/rsd+xml;charset=UTF-8');
INSERT INTO Template VALUES (1,'html','asual.vm, text/html;charset=UTF-8');
INSERT INTO Template VALUES (1,'atom','atom.vm, application/atom+xml;charset=UTF-8');
INSERT INTO Template VALUES (1,'rss2','rss2.vm, text/xml;charset=UTF-8');
INSERT INTO Template VALUES (1,'rdf','rdf.vm, text/xml;charset=UTF-8');
INSERT INTO Template VALUES (1,'admin','org/blojsom/plugin/admin/templates/admin.vm, text/html;charset=UTF-8');

--
-- Table structure for table Trackback
--

-- DROP TABLE Trackback;
CREATE TABLE Trackback (
  trackback_id SERIAL PRIMARY KEY,
  entry_id int NOT NULL,
  title varchar(255),
  excerpt varchar(500),
  url varchar(255),
  blog_name varchar(255),
  trackback_date timestamp NOT NULL,
  blog_id int NOT NULL REFERENCES Blog (id) ON DELETE CASCADE,
  ip varchar(100) default NULL,
  status varchar(255) default NULL
);

--
-- Dumping data for table Trackback
--


--
-- Table structure for table TrackbackMetadata
--

-- DROP TABLE TrackbackMetadata;
CREATE TABLE TrackbackMetadata (
  trackback_id int NOT NULL REFERENCES Trackback (trackback_id) ON DELETE CASCADE,
  metadata_key varchar(255) NOT NULL,
  metadata_value text
);

--
-- Dumping data for table TrackbackMetadata
--
