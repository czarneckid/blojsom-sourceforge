--
-- Initial database creation
--

-- Create the database schema blojsom and set the owner to the blojsom user 
-- prior to running this script.

--
-- Table structure for table Blojsom_Blog
--

-- -- DROP TABLE Blojsom_Blog CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_Blog (
  id int PRIMARY KEY,
  blog_id varchar2(50)
);

-- DROP SEQUENCE Blojsom_id_Seq;
CREATE SEQUENCE Blojsom_id_Seq START WITH 1 INCREMENT BY 1;

--
-- Dumping data for table Blojsom_Blog
--

INSERT INTO Blojsom_Blog VALUES (Blojsom_id_Seq.nextval,'default');

--
-- Table structure for table Blojsom_Category
--

-- DROP TABLE Blojsom_Category CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_Category (
  category_id int PRIMARY KEY,
  blog_id int NOT NULL REFERENCES Blojsom_Blog (id) ON DELETE CASCADE,
  parent_category_id int default NULL,
  name varchar2(4000) NOT NULL,
  description clob
);

-- DROP SEQUENCE Blojsom_category_id_Seq;
CREATE SEQUENCE Blojsom_category_id_Seq START WITH 1 INCREMENT BY 1;

--
-- Dumping data for table Blojsom_Category
--

INSERT INTO Blojsom_Category VALUES (Blojsom_category_id_Seq.nextval,Blojsom_id_Seq.currval,NULL,'/uncategorised/','Uncategorised');

--
-- Table structure for table Blojsom_CategoryMetadata
--

-- DROP TABLE Blojsom_CategoryMetadata CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_CategoryMetadata (
  category_metadata_id int PRIMARY KEY,
  category_id int NOT NULL REFERENCES Blojsom_Category (category_id) ON DELETE CASCADE,
  metadata_key varchar2(4000) NOT NULL,
  metadata_value varchar2(4000)
);

-- DROP SEQUENCE Blojsom_category_md_id_Seq;
CREATE SEQUENCE Blojsom_category_md_id_Seq START WITH 1 INCREMENT BY 1;

-- DROP SEQUENCE Blojsom_category_md_id_auto;
CREATE OR REPLACE TRIGGER Blojsom_category_md_id_auto
BEFORE INSERT ON Blojsom_CategoryMetadata FOR EACH ROW
BEGIN
	IF :new.category_metadata_id IS NULL THEN
		SELECT Blojsom_category_md_id_Seq.nextval INTO :new.category_metadata_id FROM DUAL;
	END IF;
END;
/

--
-- Dumping data for table Blojsom_CategoryMetadata
--


--
-- Table structure for table Blojsom_Comment
--

-- DROP TABLE Blojsom_Comment CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_Comment (
  comment_id int PRIMARY KEY,
  entry_id int NOT NULL,
  author clob,
  author_url clob,
  author_email clob,
  comment_entry clob,
  comment_date timestamp NOT NULL,
  ip varchar2(100) default NULL,
  status varchar2(255) default NULL,
  comment_parent int default NULL,
  blog_id int NOT NULL REFERENCES Blojsom_Blog (id) ON DELETE CASCADE
);

-- DROP SEQUENCE Blojsom_comment_id_Seq;
CREATE SEQUENCE Blojsom_comment_id_Seq START WITH 1 INCREMENT BY 1;

--
-- Dumping data for table Blojsom_Comment
--


--
-- Table structure for table Blojsom_CommentMetadata
--

-- DROP TABLE Blojsom_CommentMetadata CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_CommentMetadata (
  comment_metadata_id int PRIMARY KEY,
  comment_id int NOT NULL REFERENCES Blojsom_Comment (comment_id) ON DELETE CASCADE,
  metadata_key varchar2(4000) NOT NULL,
  metadata_value varchar2(4000)
);

-- DROP SEQUENCE Blojsom_comment_md_id_Seq;
CREATE SEQUENCE Blojsom_comment_md_id_Seq START WITH 1 INCREMENT BY 1;

-- DROP TRIGGER Blosjom_comment_md_id_auto;
CREATE OR REPLACE TRIGGER Blojsom_comment_md_id_auto
BEFORE INSERT ON Blojsom_CommentMetadata FOR EACH ROW
BEGIN
	IF :new.comment_metadata_id IS NULL THEN
		select Blojsom_comment_md_id_Seq.nextval INTO :new.comment_metadata_id FROM DUAL;
	END IF;
END;
/

--
-- Dumping data for table Blojsom_CommentMetadata
--


--
-- Table structure for table Blojsom_DBUser
--

-- DROP TABLE Blojsom_DBUser CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_DBUser (
  user_id int PRIMARY KEY,
  user_login varchar2(50) NOT NULL,
  user_password varchar2(64) NOT NULL,
  user_name varchar2(250) NOT NULL,
  user_email varchar2(100) NOT NULL,
  user_registered timestamp NOT NULL,
  user_status varchar2(64) NOT NULL,
  blog_id int NOT NULL REFERENCES Blojsom_Blog (id) ON DELETE CASCADE
);

-- DROP SEQUENCE Blojsom_user_id_Seq;
CREATE SEQUENCE Blojsom_user_id_Seq START WITH 1 INCREMENT BY 1;

--
-- Dumping data for table Blojsom_DBUser
--

INSERT INTO Blojsom_DBUser VALUES (Blojsom_user_id_Seq.nextval,'default','default','Default User','default_owner@email.com',sysdate,' ',Blojsom_id_Seq.currval);

--
-- Table structure for table Blojsom_DBUserMetadata
--

-- DROP TABLE Blojsom_DBUserMetadata CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_DBUserMetadata (
  user_metadata_id int PRIMARY KEY,
  user_id int NOT NULL REFERENCES Blojsom_DBUser (user_id) ON DELETE CASCADE,
  metadata_key varchar2(255) NOT NULL,
  metadata_value varchar2(4000)
);

-- DROP SEQUENCE Blojsom_user_md_id_Seq;
CREATE SEQUENCE Blojsom_user_md_id_Seq START WITH 1 INCREMENT BY 1;

--
-- Dumping data for table Blojsom_DBUserMetadata
--

INSERT INTO Blojsom_DBUserMetadata VALUES (Blojsom_user_md_id_Seq.nextval,Blojsom_user_id_Seq.currval,'all_permissions_permission','true');

--
-- Table structure for table Blojsom_Entry
--

-- DROP TABLE Blojsom_Entry CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_Entry (
  entry_id int PRIMARY KEY,
  blog_id int NOT NULL REFERENCES Blojsom_Blog (id) ON DELETE CASCADE,
  title clob,
  description clob,
  entry_date timestamp NOT NULL,
  blog_category_id int NOT NULL REFERENCES Blojsom_Category (category_id) ON DELETE CASCADE,
  status varchar2(4000),
  author clob,
  allow_comments int default '1',
  allow_trackbacks int default '1',
  allow_pingbacks int default '1',
  post_slug varchar2(4000) NOT NULL,
  modified_date timestamp NOT NULL
);

-- DROP SEQUENCE Blojsom_entry_id_Seq;
CREATE SEQUENCE Blojsom_entry_id_Seq START WITH 1 INCREMENT BY 1;

--
-- Dumping data for table Blojsom_Entry
--


--
-- Table structure for table Blojsom_EntryMetadata
--

-- DROP TABLE Blojsom_EntryMetadata CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_EntryMetadata (
  entry_metadata_id int PRIMARY KEY,
  entry_id int NOT NULL REFERENCES Blojsom_Entry (entry_id) ON DELETE CASCADE,
  metadata_key varchar2(4000) NOT NULL,
  metadata_value varchar2(4000)
);

-- DROP SEQUENCE Blojsom_entry_md_id_Seq;
CREATE SEQUENCE Blojsom_entry_md_id_Seq START WITH 1 INCREMENT BY 1;

-- DROP TRIGGER Blojsom_entry_md_id_auto;
CREATE OR REPLACE TRIGGER Blojsom_entry_md_id_auto
BEFORE INSERT ON Blojsom_EntryMetadata FOR EACH ROW
BEGIN
	IF :new.entry_metadata_id IS NULL THEN
		select Blojsom_entry_md_id_Seq.nextval INTO :new.entry_metadata_id FROM DUAL;
	END IF;
END;
/

--
-- Dumping data for table Blojsom_EntryMetadata
--


--
-- Table structure for table Blojsom_Pingback
--

-- DROP TABLE Blojsom_Pingback CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_Pingback (
  pingback_id int PRIMARY KEY,
  entry_id int NOT NULL,
  title clob,
  excerpt clob,
  url clob,
  blog_name clob,
  trackback_date timestamp NOT NULL,
  blog_id int NOT NULL REFERENCES Blojsom_Blog (id) ON DELETE CASCADE,
  ip varchar2(100) default NULL,
  status varchar2(255) default NULL,
  source_uri varchar2(4000) NOT NULL,
  target_uri varchar2(4000) NOT NULL
);

-- DROP SEQUENCE Blojsom_pingback_id_Seq;
CREATE SEQUENCE Blojsom_pingback_id_Seq START WITH 1 INCREMENT BY 1;

--
-- Dumping data for table Blojsom_Pingback
--


--
-- Table structure for table Blojsom_PingbackMetadata
--

-- DROP TABLE Blojsom_PingbackMetadata CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_PingbackMetadata (
  pingback_metadata_id int PRIMARY KEY,
  pingback_id int NOT NULL REFERENCES Blojsom_Pingback (pingback_id) ON DELETE CASCADE,
  metadata_key varchar2(4000) NOT NULL,
  metadata_value varchar2(4000)
);

-- DROP SEQUENCE Blojsom_pingback_md_id_Seq;
CREATE SEQUENCE Blojsom_pingback_md_id_Seq START WITH 1 INCREMENT BY 1;

-- DROP TRIGGER Blojsom_pingback_md_id_auto;
CREATE OR REPLACE TRIGGER Blojsom_pingback_md_id_auto
BEFORE INSERT ON Blojsom_PingbackMetadata FOR EACH ROW
BEGIN
	IF :new.pingback_metadata_id IS NULL THEN
		select Blojsom_pingback_md_id_Seq.nextval INTO :new.pingback_metadata_id FROM DUAL;
	END IF;
END;
/

--
-- Dumping data for table Blojsom_PingbackMetadata
--


--
-- Table structure for table Blojsom_Plugin
--

-- DROP TABLE Blojsom_Plugin CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_Plugin (
  plugin_id int PRIMARY KEY,
  blog_id int NOT NULL REFERENCES Blojsom_Blog (id) ON DELETE CASCADE,
  plugin_flavor varchar2(50) NOT NULL,
  plugin_value varchar2(4000) default NULL
);

-- DROP SEQUENCE Blojsom_plugin_id_Seq;
CREATE SEQUENCE Blojsom_plugin_id_Seq START WITH 1 INCREMENT BY 1;

-- DROP TRIGGER Blojsom_plugin_id_auto;
CREATE OR REPLACE TRIGGER Blojsom_plugin_id_auto
BEFORE INSERT ON Blojsom_Plugin FOR EACH ROW
BEGIN
	IF :new.plugin_id IS NULL THEN
		select Blojsom_plugin_id_Seq.nextval INTO :new.plugin_id FROM DUAL;
	END IF;
END;
/

--
-- Dumping data for table Blojsom_Plugin
--

INSERT INTO Blojsom_Plugin VALUES (Blojsom_plugin_id_Seq.nextval, Blojsom_id_Seq.currval,'html','meta, tag-cloud, date-format, referer-log, calendar-gui, calendar-filter, comment, trackback, simple-search, emoticons, macro-expansion, days-since-posted, word-count, simple-obfuscation, nofollow, rss-enclosure, technorati-tags');
INSERT INTO Blojsom_Plugin VALUES (Blojsom_plugin_id_Seq.nextval, Blojsom_id_Seq.currval,'default','conditional-get, meta, nofollow, rss-enclosure');
INSERT INTO Blojsom_Plugin VALUES (Blojsom_plugin_id_Seq.nextval, Blojsom_id_Seq.currval,'admin','admin');

--
-- Table structure for table Properties
--

-- DROP TABLE Blojsom_Properties CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_Properties (
  property_id int PRIMARY KEY,
  blog_id int NOT NULL REFERENCES Blojsom_Blog (id) ON DELETE CASCADE,
  property_name varchar2(255) NOT NULL,
  property_value clob
);

-- DROP SEQUENCE Blojsom_property_id_Seq;
CREATE SEQUENCE Blojsom_property_id_Seq START WITH 1 INCREMENT BY 1;

-- DROP TRIGGER Blojsom_property_id_auto;
CREATE OR REPLACE TRIGGER Blojsom_property_id_auto
BEFORE INSERT ON Blojsom_Properties FOR EACH ROW
BEGIN
	IF :new.property_id IS NULL THEN
		select Blojsom_property_id_Seq.nextval INTO :new.property_id FROM DUAL;
	END IF;
END;
/

--
-- Dumping data for table Blojsom_Properties
--

INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-url','http://localhost:8080/blojsom/blog/default');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-admin-url','http://localhost:8080/blojsom/blog/default');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-base-url','http://localhost:8080/blojsom');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-base-admin-url','http://localhost:8080/blojsom');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-language','en');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-name','NAME YOUR BLOG');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-description','DESCRIBE YOUR BLOG');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-entries-display','15');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-owner','Default Owner');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-owner-email','default_owner@email.com');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-comments-enabled','true');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-trackbacks-enabled','true');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-email-enabled','true');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-default-flavor','html');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'plugin-comment-autoformat','true');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'linear-navigation-enabled','true');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'comment-moderation-enabled','true');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'trackback-moderation-enabled','true');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'pingback-moderation-enabled','true');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-ping-urls','http://rpc.pingomatic.com');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blojsom-extension-metaweblog-accepted-types','image/jpeg, image/gif, image/png, img');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'xmlrpc-enabled','true');
INSERT INTO Blojsom_Properties VALUES (Blojsom_property_id_Seq.nextval, Blojsom_id_Seq.currval,'blog-pingbacks-enabled','true');

--
-- Table structure for table Blojsom_Template
--

-- DROP TABLE Blojsom_Template CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_Template (
  template_id int PRIMARY KEY,
  blog_id int NOT NULL REFERENCES Blojsom_Blog (id) ON DELETE CASCADE,
  template_flavor varchar2(50) NOT NULL,
  template_value varchar2(255) default NULL
);

-- DROP SEQUENCE Blojsom_template_id_Seq;
CREATE SEQUENCE Blojsom_template_id_Seq START WITH 1 INCREMENT BY 1;

-- DROP TRIGGER Blojsom_template_id_auto;
CREATE OR REPLACE TRIGGER Blojsom_template_id_auto
BEFORE INSERT ON Blojsom_Template FOR EACH ROW
BEGIN
	IF :new.template_id IS NULL THEN
		select Blojsom_template_id_Seq.nextval INTO :new.template_id FROM DUAL;
	END IF;
END;
/

--
-- Dumping data for table Blojsom_Template
--

INSERT INTO Blojsom_Template VALUES (Blojsom_template_id_Seq.nextval, Blojsom_id_Seq.currval,'rss','rss.vm, text/xml;charset=UTF-8');
INSERT INTO Blojsom_Template VALUES (Blojsom_template_id_Seq.nextval, Blojsom_id_Seq.currval,'rsd','rsd.vm, application/rsd+xml;charset=UTF-8');
INSERT INTO Blojsom_Template VALUES (Blojsom_template_id_Seq.nextval, Blojsom_id_Seq.currval,'html','asual.vm, text/html;charset=UTF-8');
INSERT INTO Blojsom_Template VALUES (Blojsom_template_id_Seq.nextval, Blojsom_id_Seq.currval,'atom','atom.vm, application/atom+xml;charset=UTF-8');
INSERT INTO Blojsom_Template VALUES (Blojsom_template_id_Seq.nextval, Blojsom_id_Seq.currval,'rss2','rss2.vm, text/xml;charset=UTF-8');
INSERT INTO Blojsom_Template VALUES (Blojsom_template_id_Seq.nextval, Blojsom_id_Seq.currval,'rdf','rdf.vm, text/xml;charset=UTF-8');
INSERT INTO Blojsom_Template VALUES (Blojsom_template_id_Seq.nextval, Blojsom_id_Seq.currval,'admin','org/blojsom/plugin/admin/templates/admin.vm, text/html;charset=UTF-8');

--
-- Table structure for table Blojsom_Trackback
--

-- DROP TABLE Blojsom_Trackback CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_Trackback (
  trackback_id int PRIMARY KEY,
  entry_id int NOT NULL,
  title clob,
  excerpt clob,
  url clob,
  blog_name clob,
  trackback_date timestamp NOT NULL,
  blog_id int NOT NULL REFERENCES Blojsom_Blog (id) ON DELETE CASCADE,
  ip varchar2(100) default NULL,
  status varchar2(255) default NULL
);

-- DROP SEQUENCE Blojsom_trackback_id_Seq;
CREATE SEQUENCE Blojsom_trackback_id_Seq START WITH 1 INCREMENT BY 1;

--
-- Dumping data for table Blojsom_Trackback
--


--
-- Table structure for table Blojsom_TrackbackMetadata
--

-- DROP TABLE Blojsom_TrackbackMetadata CASCADE CONSTRAINTS;
CREATE TABLE Blojsom_TrackbackMetadata (
  trackback_metadata_id int PRIMARY KEY,
  trackback_id int NOT NULL REFERENCES Blojsom_Trackback (trackback_id) ON DELETE CASCADE,
  metadata_key varchar2(4000) NOT NULL,
  metadata_value varchar2(4000)
);

-- DROP SEQUENCE Blojsom_trackback_md_id_Seq;
CREATE SEQUENCE Blojsom_trackback_md_id_Seq START WITH 1 INCREMENT BY 1;

-- DROP TRIGGER Blojsom_trackback_md_id_auto;
CREATE OR REPLACE TRIGGER Blojsom_trackback_md_id_auto
BEFORE INSERT ON Blojsom_TrackbackMetadata FOR EACH ROW
BEGIN
	IF :new.trackback_metadata_id IS NULL THEN
		select Blojsom_trackback_md_id_Seq.nextval INTO :new.trackback_metadata_id FROM DUAL;
	END IF;
END;
/
--
-- Dumping data for table Blojsom_TrackbackMetadata
--
