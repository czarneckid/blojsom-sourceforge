--
-- Initial database creation
--

-- Create the database blojsom and set the owner to the blojsom user prior to
-- running this script.
-- DROP DATABASE blojsom;
-- CREATE DATABASE blojsom OWNER blojsom ENCODING 'UTF8';
USE blojsom;
--
-- Table structure for table Blog
--

-- There is no conditional drop in PostgreSQL.
-- Commented out for now in case the error from a failed DROP is not handled.
-- DROP TABLE Blog;
CREATE TABLE Blog (
  blog_id varchar(50) PRIMARY KEY
);

--
-- Dumping data for table Blog
--

INSERT INTO Blog VALUES ('default');

--
-- Table structure for table Category
--

-- DROP TABLE Category;
CREATE TABLE Category (
  category_id SERIAL PRIMARY KEY,
  blog_id varchar(50) NOT NULL REFERENCES Blog (blog_id) ON DELETE CASCADE,
  parent_category_id int default NULL,
  name text NOT NULL,
  description text
);

--
-- Dumping data for table Category
--

INSERT INTO Category VALUES (1,'default',NULL,'/uncategorised/','Uncategorised');

--
-- Table structure for table CategoryMetadata
--

-- DROP TABLE CategoryMetadata;
CREATE TABLE CategoryMetadata (
  category_id int NOT NULL REFERENCES Category (category_id) ON DELETE CASCADE,
  metadata_key text NOT NULL,
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
  author text,
  author_url text,
  author_email text,
  comment text,
  date timestamp NOT NULL,
  ip varchar(100) default NULL,
  status varchar(255) default NULL,
  comment_parent int default NULL,
  blog_id varchar(50) NOT NULL REFERENCES Blog (blog_id) ON DELETE CASCADE
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
  metadata_key text NOT NULL,
  metadata_value text
);

--
-- Dumping data for table CommentMetadata
--


--
-- Table structure for table Entry
--

-- DROP TABLE Entry;
CREATE TABLE Entry (
  entry_id SERIAL PRIMARY KEY,
  blog_id varchar(50) NOT NULL REFERENCES Blog (blog_id) ON DELETE CASCADE,
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
-- Dumping data for table Entry
--


--
-- Table structure for table EntryMetadata
--

-- DROP TABLE EntryMetadata;
CREATE TABLE EntryMetadata (
  entry_id int NOT NULL REFERENCES Entry (entry_id) ON DELETE CASCADE,
  metadata_key text NOT NULL,
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
  title text,
  excerpt text,
  url text,
  blog_name text,
  trackback_date timestamp NOT NULL,
  blog_id varchar(50) NOT NULL REFERENCES Blog (blog_id) ON DELETE CASCADE,
  ip varchar(100) default NULL,
  status varchar(255) default NULL,
  source_uri text NOT NULL,
  target_uri text NOT NULL
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
  metadata_key text NOT NULL,
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
  blog_id varchar(50) NOT NULL REFERENCES blog (Blog_id) ON DELETE CASCADE,
  plugin_flavor varchar(50) NOT NULL,
  plugin_value varchar(4096) default NULL
);

--
-- Dumping data for table Plugin
--

INSERT INTO Plugin VALUES ('default','html','meta, tag-cloud, date-format, referer-log, calendar-gui, calendar-filter, comment, trackback, simple-search, emoticons, macro-expansion, days-since-posted, word-count, simple-obfuscation, nofollow, rss-enclosure, technorati-tags');
INSERT INTO Plugin VALUES ('default','default','conditional-get, meta, nofollow, rss-enclosure');
INSERT INTO Plugin VALUES ('default','admin','admin');

--
-- Table structure for table Properties
--

-- DROP TABLE Properties;
CREATE TABLE Properties (
  blog_id varchar(50) NOT NULL REFERENCES Blog (blog_id) ON DELETE CASCADE,
  property_name varchar(255) NOT NULL,
  property_value text
);

--
-- Dumping data for table Properties
--

INSERT INTO Properties VALUES ('default','blog-url','http://localhost:8080/blojsom/blog/default');
INSERT INTO Properties VALUES ('default','blog-admin-url','http://localhost:8080/blojsom/blog/default');
INSERT INTO Properties VALUES ('default','blog-base-url','http://localhost:8080/blojsom');
INSERT INTO Properties VALUES ('default','blog-base-admin-url','http://localhost:8080/blojsom');
INSERT INTO Properties VALUES ('default','blog-language','en');
INSERT INTO Properties VALUES ('default','blog-name','NAME YOUR BLOG');
INSERT INTO Properties VALUES ('default','blog-description','DESCRIBE YOUR BLOG');
INSERT INTO Properties VALUES ('default','blog-entries-display','15');
INSERT INTO Properties VALUES ('default','blog-owner','Default Owner');
INSERT INTO Properties VALUES ('default','blog-owner-email','default_owner@email.com');
INSERT INTO Properties VALUES ('default','blog-comments-enabled','true');
INSERT INTO Properties VALUES ('default','blog-trackbacks-enabled','true');
INSERT INTO Properties VALUES ('default','blog-email-enabled','true');
INSERT INTO Properties VALUES ('default','blog-default-flavor','html');
INSERT INTO Properties VALUES ('default','plugin-comment-autoformat','true');
INSERT INTO Properties VALUES ('default','linear-navigation-enabled','true');
INSERT INTO Properties VALUES ('default','comment-moderation-enabled','true');
INSERT INTO Properties VALUES ('default','trackback-moderation-enabled','true');
INSERT INTO Properties VALUES ('default','pingback-moderation-enabled','true');
INSERT INTO Properties VALUES ('default','blog-ping-urls','http://rpc.pingomatic.com');
INSERT INTO Properties VALUES ('default','blojsom-extension-metaweblog-accepted-types','image/jpeg, image/gif, image/png, img');
INSERT INTO Properties VALUES ('default','xmlrpc-enabled','true');
INSERT INTO Properties VALUES ('default','blog-pingbacks-enabled','true');

--
-- Table structure for table Template
--

-- DROP TABLE Template;
CREATE TABLE Template (
  blog_id varchar(50) NOT NULL REFERENCES Blog (blog_id) ON DELETE CASCADE,
  template_flavor varchar(50) NOT NULL,
  template_value varchar(255) default NULL
);

--
-- Dumping data for table Template
--

INSERT INTO Template VALUES ('default','rss','rss.vm, text/xml;charset=UTF-8');
INSERT INTO Template VALUES ('default','rsd','rsd.vm, application/rsd+xml;charset=UTF-8');
INSERT INTO Template VALUES ('default','html','asual.vm, text/html;charset=UTF-8');
INSERT INTO Template VALUES ('default','atom','atom.vm, application/atom+xml;charset=UTF-8');
INSERT INTO Template VALUES ('default','rss2','rss2.vm, text/xml;charset=UTF-8');
INSERT INTO Template VALUES ('default','rdf','rdf.vm, text/xml;charset=UTF-8');
INSERT INTO Template VALUES ('default','admin','org/blojsom/plugin/admin/templates/admin.vm, text/html;charset=UTF-8');

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
  blog_id varchar(50) NOT NULL REFERENCES Blog (blog_id) ON DELETE CASCADE,
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
  metadata_key text NOT NULL,
  metadata_value text
);

--
-- Dumping data for table TrackbackMetadata
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
  blog_id varchar(50) NOT NULL REFERENCES Blog (blog_id) ON DELETE CASCADE
);

--
-- Dumping data for table DBUser
--

INSERT INTO DBUser VALUES (1,'default','default','Default User','default_owner@email.com',NOW(),'','default');

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
