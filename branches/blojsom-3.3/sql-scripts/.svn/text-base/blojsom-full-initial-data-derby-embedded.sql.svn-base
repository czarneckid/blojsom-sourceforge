
CREATE TABLE Blog (
  id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  blog_id varchar(50) NOT NULL,
  PRIMARY KEY  (id)
) ;

INSERT INTO Blog VALUES (DEFAULT, 'default');

CREATE TABLE Category (
  category_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  blog_id int NOT NULL,
  parent_category_id int default NULL,
  name varchar(100) NOT NULL,
  description varchar(300),
  PRIMARY KEY  (category_id),
  CONSTRAINT category_blog_blogidfk FOREIGN KEY (blog_id) REFERENCES Blog (id) ON DELETE CASCADE
) ;

INSERT INTO Category VALUES (DEFAULT, 1, NULL, '/uncategorized/','Uncategorized');

CREATE TABLE CategoryMetadata (
  category_id int NOT NULL,
  metadata_key varchar(300) NOT NULL,
  metadata_value varchar(255),
  CONSTRAINT categorymetadata_category_categoryidfk FOREIGN KEY (category_id) REFERENCES Category (category_id) ON DELETE CASCADE
) ;

CREATE TABLE Comment (
  comment_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  entry_id int NOT NULL,
  author varchar(100),
  author_url varchar(255),
  author_email varchar(100),
  comment clob(64 K),
  date timestamp NOT NULL,
  ip varchar(30) default NULL,
  status varchar(100) default NULL,
  comment_parent int default NULL,
  blog_id int NOT NULL,
  PRIMARY KEY  (comment_id),
  CONSTRAINT comment_blog_blogidfk FOREIGN KEY (blog_id) REFERENCES Blog (id) ON DELETE CASCADE
) ;

CREATE TABLE CommentMetadata (
  comment_id int NOT NULL,
  metadata_key varchar(100) NOT NULL,
  metadata_value varchar(100),
  CONSTRAINT commentmetadata_comment_commentidfk FOREIGN KEY (comment_id) REFERENCES Comment (comment_id) ON DELETE CASCADE
) ;

CREATE TABLE Entry (
  entry_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  blog_id int NOT NULL,
  title varchar(255),
  description clob(64 k),
  entry_date timestamp NOT NULL,
  blog_category_id int NOT NULL,
  status varchar(100),
  author varchar(100),
  allow_comments int default 1,
  allow_trackbacks int default 1,
  allow_pingbacks int default 1,
  post_slug varchar(100) NOT NULL,
  modified_date timestamp NOT NULL,
  PRIMARY KEY  (entry_id),
  CONSTRAINT entry_blog_blogidfk FOREIGN KEY (blog_id) REFERENCES Blog (id) ON DELETE CASCADE,
  CONSTRAINT entry_category_categoryidfk FOREIGN KEY (blog_category_id) REFERENCES Category (category_id) ON DELETE CASCADE
) ;

CREATE TABLE EntryMetadata (
  entry_id int NOT NULL,
  metadata_key varchar(100) NOT NULL,
  metadata_value varchar(100),
  CONSTRAINT entrymetadata_entry_entryidfk FOREIGN KEY (entry_id) REFERENCES Entry (entry_id) ON DELETE CASCADE
) ;

CREATE TABLE Pingback (
  pingback_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  entry_id int NOT NULL,
  title varchar(255),
  excerpt varchar(255),
  url varchar(255),
  blog_name varchar(255),
  trackback_date timestamp NOT NULL,
  blog_id int NOT NULL,
  ip varchar(30) default NULL,
  status varchar(100) default NULL,
  source_uri varchar(255) NOT NULL,
  target_uri varchar(255) NOT NULL,
  PRIMARY KEY  (pingback_id),
  CONSTRAINT pingback_blog_blogidfk FOREIGN KEY (blog_id) REFERENCES Blog (id) ON DELETE CASCADE
) ;

CREATE TABLE PingbackMetadata (
  pingback_id int NOT NULL,
  metadata_key varchar(100) NOT NULL,
  metadata_value varchar(100),
  CONSTRAINT pingbackmetadata_pingback_pingbackidfk FOREIGN KEY (pingback_id) REFERENCES Pingback (pingback_id) ON DELETE CASCADE
) ;

CREATE TABLE Plugin (
  blog_id int NOT NULL,
  plugin_flavor varchar(50) NOT NULL,
  plugin_value varchar(4096) default NULL,
  CONSTRAINT plugin_blog_blogidfk FOREIGN KEY (blog_id) REFERENCES Blog (id) ON DELETE CASCADE
) ;

INSERT INTO Plugin VALUES (1,'html','meta, tag-cloud, date-format, referer-log, calendar-gui, calendar-filter, comment, trackback, simple-search, emoticons, macro-expansion, days-since-posted, word-count, simple-obfuscation, nofollow, rss-enclosure, technorati-tags'),(1,'default','conditional-get, meta, nofollow, rss-enclosure'),(1,'admin','admin');

CREATE TABLE Properties (
  blog_id int NOT NULL,
  property_name varchar(100) NOT NULL,
  property_value varchar(255),
  CONSTRAINT properties_blog_blogidfk FOREIGN KEY (blog_id) REFERENCES Blog (id) ON DELETE CASCADE
) ;

INSERT INTO Properties VALUES (1,'blog-url','http://blog.plutao:8080/blog/default'),(1,'blog-admin-url','http://blog.plutao:8080/blog/default'),(1,'blog-base-url','http://blog.plutao:8080'),(1,'blog-language','en'),(1,'blog-name','NAME YOUR BLOG'),(1,'blog-description','DESCRIBE YOUR BLOG'),(1,'blog-entries-display','15'),(1,'blog-owner','Default Owner'),(1,'blog-owner-email','default_owner@email.com'),(1,'blog-comments-enabled','true'),(1,'blog-trackbacks-enabled','true'),(1,'blog-email-enabled','true'),(1,'blog-default-flavor','html'),(1,'plugin-comment-autoformat','true'),(1,'linear-navigation-enabled','true'),(1,'comment-moderation-enabled','true'),(1,'trackback-moderation-enabled','true'),(1,'blog-ping-urls','http://rpc.pingomatic.com'),(1,'blojsom-extension-metaweblog-accepted-types','image/jpeg, image/gif, image/png, img'),(1,'xmlrpc-enabled','true'),(1,'blog-pingbacks-enabled','true');

CREATE TABLE Template (
  blog_id int NOT NULL,
  template_flavor varchar(50) NOT NULL,
  template_value varchar(255) default NULL,
  CONSTRAINT template_blog_blogidfk FOREIGN KEY (blog_id) REFERENCES Blog (id) ON DELETE CASCADE
) ;

INSERT INTO Template VALUES (1,'rss','rss.vm, text/xml;charset=UTF-8'),(1,'rsd','rsd.vm, application/rsd+xml;charset=UTF-8'),(1,'html','asual.vm, text/html;charset=UTF-8'),(1,'atom','atom.vm, application/atom+xml;charset=UTF-8'),(1,'rss2','rss2.vm, text/xml;charset=UTF-8'),(1,'rdf','rdf.vm, text/xml;charset=UTF-8'),(1,'admin','org/blojsom/plugin/admin/templates/admin.vm, text/html;charset=UTF-8');

CREATE TABLE Trackback (
  trackback_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  entry_id int NOT NULL,
  title varchar(100),
  excerpt varchar(255),
  url varchar(255),
  blog_name varchar(200),
  trackback_date timestamp NOT NULL,
  blog_id int NOT NULL,
  ip varchar(30) default NULL,
  status varchar(100) default NULL,
  PRIMARY KEY  (trackback_id),
  CONSTRAINT trackback_blog_blogidfk FOREIGN KEY (blog_id) REFERENCES Blog (id) ON DELETE CASCADE
) ;

CREATE TABLE TrackbackMetadata (
  trackback_id int NOT NULL,
  metadata_key varchar(100) NOT NULL,
  metadata_value varchar(100),
  CONSTRAINT trackbackmetadata_trackback_trackbackidfk FOREIGN KEY (trackback_id) REFERENCES Trackback (trackback_id) ON DELETE CASCADE
) ;

CREATE TABLE DBUser (
  user_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  user_login varchar(15) NOT NULL,
  user_password varchar(64) NOT NULL,
  user_name varchar(100) NOT NULL,
  user_email varchar(100) NOT NULL,
  user_registered timestamp NOT NULL,
  user_status varchar(64) NOT NULL,
  blog_id int NOT NULL,
  PRIMARY KEY  (user_id),
  CONSTRAINT user_blog_blogidfk FOREIGN KEY (blog_id) REFERENCES Blog (id) ON DELETE CASCADE
) ;

INSERT INTO DBUser VALUES (DEFAULT,'default','default','Default User','default_owner@email.com',CURRENT_TIMESTAMP,'',1);

CREATE TABLE DBUserMetadata (
  user_metadata_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  user_id int NOT NULL,
  metadata_key varchar(100) NOT NULL,
  metadata_value varchar(100),
  PRIMARY KEY  (user_metadata_id),
  CONSTRAINT usermetadata_user_useridfk FOREIGN KEY (user_id) REFERENCES DBUser (user_id) ON DELETE CASCADE
) ;

INSERT INTO DBUserMetadata VALUES (DEFAULT, 1, 'all_permissions_permission','true');
