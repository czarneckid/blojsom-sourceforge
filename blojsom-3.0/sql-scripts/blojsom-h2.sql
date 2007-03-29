CREATE TABLE BLOG (
  `id` int(11) NOT NULL auto_increment,
  `blog_id` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`)
) ;

CREATE TABLE CATEGORY (
  `category_id` int(11) NOT NULL auto_increment,
  `blog_id` int(11) NOT NULL,
  `parent_category_id` int(11) default NULL,
  `name` text NOT NULL,
  `description` text,
  PRIMARY KEY  (`category_id`),
  CONSTRAINT `category_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES BLOG (`id`) ON DELETE CASCADE
) ;

CREATE TABLE CATEGORYMETADATA (
  `category_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text,
  `category_metadata_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`category_metadata_id`),
  CONSTRAINT `categorymetadata_category_categoryidfk` FOREIGN KEY (`category_id`) REFERENCES CATEGORY (`category_id`) ON DELETE CASCADE
) ;

CREATE TABLE COMMENT (
  `comment_id` int(11) NOT NULL auto_increment,
  `entry_id` int(11) NOT NULL,
  `author` text,
  `author_url` text,
  `author_email` text,
  COMMENT text,
  `date` datetime NOT NULL,
  `ip` varchar(100) default NULL,
  `status` varchar(255) default NULL,
  `comment_parent` int(11) default NULL,
  `blog_id` int(11) NOT NULL,
  PRIMARY KEY  (`comment_id`),
  CONSTRAINT `comment_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES BLOG (`id`) ON DELETE CASCADE
) ;

CREATE TABLE COMMENTMETADATA (
  `comment_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text,
  `comment_metadata_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`comment_metadata_id`),
  CONSTRAINT `commentmetadata_comment_commentidfk` FOREIGN KEY (`comment_id`) REFERENCES COMMENT (`comment_id`) ON DELETE CASCADE
) ;

CREATE TABLE DBUSER (
  `user_id` int(11) NOT NULL auto_increment,
  `user_login` varchar(50) NOT NULL,
  `user_password` varchar(64) NOT NULL,
  `user_name` varchar(250) NOT NULL,
  `user_email` varchar(100) NOT NULL,
  `user_registered` datetime NOT NULL,
  `user_status` varchar(64) NOT NULL,
  `blog_id` int(11) NOT NULL,
  PRIMARY KEY  (`user_id`),
  CONSTRAINT `user_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES BLOG (`id`) ON DELETE CASCADE
) ;


CREATE TABLE DBUSERMETADATA (
  `user_metadata_id` int(11) NOT NULL auto_increment,
  `user_id` int(11) NOT NULL,
  `metadata_key` varchar(255) NOT NULL,
  `metadata_value` text,
  PRIMARY KEY  (`user_metadata_id`),
  CONSTRAINT `usermetadata_user_useridfk` FOREIGN KEY (`user_id`) REFERENCES DBUSER (`user_id`) ON DELETE CASCADE
) ;

CREATE TABLE ENTRY (
  `entry_id` int(11) NOT NULL auto_increment,
  `blog_id` int(11) NOT NULL,
  `title` text,
  `description` text,
  `entry_date` datetime NOT NULL,
  `blog_category_id` int(11) NOT NULL,
  `status` text,
  `author` text,
  `allow_comments` int(11) default '1',
  `allow_trackbacks` int(11) default '1',
  `allow_pingbacks` int(11) default '1',
  `post_slug` text NOT NULL,
  `modified_date` datetime NOT NULL,
  PRIMARY KEY  (`entry_id`),
  CONSTRAINT `entry_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES BLOG (`id`) ON DELETE CASCADE,
  CONSTRAINT `entry_category_categoryidfk` FOREIGN KEY (`blog_category_id`) REFERENCES CATEGORY (`category_id`) ON DELETE CASCADE  
) ;

CREATE TABLE ENTRYMETADATA (
  `entry_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text,
  `entry_metadata_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`entry_metadata_id`),
  CONSTRAINT `entrymetadata_entry_entryidfk` FOREIGN KEY (`entry_id`) REFERENCES ENTRY (`entry_id`) ON DELETE CASCADE
) ;

CREATE TABLE PINGBACK (
  `pingback_id` int(11) NOT NULL auto_increment,
  `entry_id` int(11) NOT NULL,
  `title` text,
  `excerpt` text,
  `url` text,
  `blog_name` text,
  `trackback_date` datetime NOT NULL,
  `blog_id` int(11) NOT NULL,
  `ip` varchar(100) default NULL,
  `status` varchar(255) default NULL,
  `source_uri` text NOT NULL,
  `target_uri` text NOT NULL,
  PRIMARY KEY  (`pingback_id`),
  CONSTRAINT `pingback_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES BLOG (`id`) ON DELETE CASCADE
) ;

CREATE TABLE PINGBACKMETADATA (
  `pingback_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text,
  `pingback_metadata_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`pingback_metadata_id`),
  CONSTRAINT `pingbackmetadata_pingback_pingbackidfk` FOREIGN KEY (`pingback_id`) REFERENCES PINGBACK (`pingback_id`) ON DELETE CASCADE
) ;

CREATE TABLE PLUGIN (
  `blog_id` int(11) NOT NULL,
  `plugin_flavor` varchar(50) NOT NULL,
  `plugin_value` varchar(4096) default NULL,
  `plugin_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`plugin_id`),
  CONSTRAINT `plugin_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES BLOG (`id`) ON DELETE CASCADE
) ;

CREATE TABLE PROPERTIES (
  `blog_id` int(11) NOT NULL,
  `property_name` varchar(255) NOT NULL,
  `property_value` longtext,
  `property_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`property_id`),
  CONSTRAINT `properties_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES BLOG (`id`) ON DELETE CASCADE
) ;

CREATE TABLE TEMPLATE (
  `blog_id` int(11) NOT NULL,
  `template_flavor` varchar(50) NOT NULL,
  `template_value` varchar(255) default NULL,
  `template_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`template_id`),
  CONSTRAINT `template_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES BLOG (`id`) ON DELETE CASCADE
) ;

CREATE TABLE TRACKBACK (
  `trackback_id` int(11) NOT NULL auto_increment,
  `entry_id` int(11) NOT NULL,
  `title` text,
  `excerpt` text,
  `url` text,
  `blog_name` text,
  `trackback_date` datetime NOT NULL,
  `blog_id` int(11) NOT NULL,
  `ip` varchar(100) default NULL,
  `status` varchar(255) default NULL,
  PRIMARY KEY  (`trackback_id`),
  CONSTRAINT `trackback_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES BLOG (`id`) ON DELETE CASCADE
) ;

CREATE TABLE TRACKBACKMETADATA (
  `trackback_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text,
  `trackback_metadata_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`trackback_metadata_id`),
  CONSTRAINT `trackbackmetadata_trackback_trackbackidfk` FOREIGN KEY (`trackback_id`) REFERENCES TRACKBACK (`trackback_id`) ON DELETE CASCADE
) ;

