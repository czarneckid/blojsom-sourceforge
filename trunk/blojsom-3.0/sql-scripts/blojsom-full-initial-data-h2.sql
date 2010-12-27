CREATE TABLE `Blog` (
  `id` int(11) NOT NULL auto_increment,
  `blog_id` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`)
) ;

INSERT INTO `Blog` VALUES (1, 'default');

CREATE TABLE `Category` (
  `category_id` int(11) NOT NULL auto_increment,
  `blog_id` int(11) NOT NULL,
  `parent_category_id` int(11) default NULL,
  `name` text NOT NULL,
  `description` text,
  PRIMARY KEY  (`category_id`),
  CONSTRAINT `category_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ;

INSERT INTO `Category` VALUES (1,1,NULL,'/uncategorized/','Uncategorized');

CREATE TABLE `CategoryMetadata` (
  `category_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text,
  `category_metadata_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`category_metadata_id`),
  CONSTRAINT `categorymetadata_category_categoryidfk` FOREIGN KEY (`category_id`) REFERENCES `Category` (`category_id`) ON DELETE CASCADE
) ;

CREATE TABLE `Comment` (
  `comment_id` int(11) NOT NULL auto_increment,
  `entry_id` int(11) NOT NULL,
  `author` text,
  `author_url` text,
  `author_email` text,
  `comment` text,
  `date` datetime NOT NULL,
  `ip` varchar(100) default NULL,
  `status` varchar(255) default NULL,
  `comment_parent` int(11) default NULL,
  `blog_id` int(11) NOT NULL,
  PRIMARY KEY  (`comment_id`),
  CONSTRAINT `comment_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ;

CREATE TABLE `CommentMetadata` (
  `comment_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text,
  `comment_metadata_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`comment_metadata_id`),
  CONSTRAINT `commentmetadata_comment_commentidfk` FOREIGN KEY (`comment_id`) REFERENCES `Comment` (`comment_id`) ON DELETE CASCADE
) ;

CREATE TABLE `DBUser` (
  `user_id` int(11) NOT NULL auto_increment,
  `user_login` varchar(50) NOT NULL,
  `user_password` varchar(64) NOT NULL,
  `user_name` varchar(250) NOT NULL,
  `user_email` varchar(100) NOT NULL,
  `user_registered` datetime NOT NULL,
  `user_status` varchar(64) NOT NULL,
  `blog_id` int(11) NOT NULL,
  PRIMARY KEY  (`user_id`),
  CONSTRAINT `user_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ;

INSERT INTO `DBUser` VALUES (1,'default','default','Default User','default_owner@email.com',NOW(),'',1);

CREATE TABLE `DBUserMetadata` (
  `user_metadata_id` int(11) NOT NULL auto_increment,
  `user_id` int(11) NOT NULL,
  `metadata_key` varchar(255) NOT NULL,
  `metadata_value` text,
  PRIMARY KEY  (`user_metadata_id`),
  CONSTRAINT `usermetadata_user_useridfk` FOREIGN KEY (`user_id`) REFERENCES `DBUser` (`user_id`) ON DELETE CASCADE
) ;

INSERT INTO `DBUserMetadata` VALUES (1,1,'all_permissions_permission','true');
CREATE TABLE `Entry` (
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
  CONSTRAINT `entry_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE,
  CONSTRAINT `entry_category_categoryidfk` FOREIGN KEY (`blog_category_id`) REFERENCES `Category` (`category_id`) ON DELETE CASCADE  
) ;

CREATE TABLE `EntryMetadata` (
  `entry_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text,
  `entry_metadata_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`entry_metadata_id`),
  CONSTRAINT `entrymetadata_entry_entryidfk` FOREIGN KEY (`entry_id`) REFERENCES `Entry` (`entry_id`) ON DELETE CASCADE
) ;

CREATE TABLE `Pingback` (
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
  CONSTRAINT `pingback_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ;

CREATE TABLE `PingbackMetadata` (
  `pingback_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text,
  `pingback_metadata_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`pingback_metadata_id`),
  CONSTRAINT `pingbackmetadata_pingback_pingbackidfk` FOREIGN KEY (`pingback_id`) REFERENCES `Pingback` (`pingback_id`) ON DELETE CASCADE
) ;

CREATE TABLE `Plugin` (
  `blog_id` int(11) NOT NULL,
  `plugin_flavor` varchar(50) NOT NULL,
  `plugin_value` varchar(4096) default NULL,
  `plugin_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`plugin_id`),
  CONSTRAINT `plugin_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ;

INSERT INTO `Plugin` VALUES (1,'html','meta, tag-cloud, date-format, referer-log, calendar-gui, calendar-filter, comment, trackback, simple-search, emoticons, macro-expansion, days-since-posted, word-count, simple-obfuscation, nofollow, rss-enclosure, technorati-tags',1),(1,'default','conditional-get, meta, nofollow, rss-enclosure',2),(1,'admin','admin',3);

CREATE TABLE `Properties` (
  `blog_id` int(11) NOT NULL,
  `property_name` varchar(255) NOT NULL,
  `property_value` longtext,
  `property_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`property_id`),
  CONSTRAINT `properties_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ;

INSERT INTO `Properties` VALUES (1,'blog-url','/blojsom/blog/default',1),(1,'blog-admin-url','/blojsom/blog/default',2),(1,'blog-base-url','/blojsom',3),(1,'blog-base-admin-url','/blojsom',4),(1,'blog-language','en',5),(1,'blog-name','NAME YOUR BLOG',6),(1,'blog-description','DESCRIBE YOUR BLOG',7),(1,'blog-entries-display','15',8),(1,'blog-owner','Default Owner',9),(1,'blog-owner-email','default_owner@email.com',10),(1,'blog-comments-enabled','true',11),(1,'blog-trackbacks-enabled','true',12),(1,'blog-email-enabled','true',13),(1,'blog-default-flavor','html',14),(1,'plugin-comment-autoformat','true',15),(1,'linear-navigation-enabled','true',16),(1,'comment-moderation-enabled','true',17),(1,'trackback-moderation-enabled','true',18),(1,'pingback-moderation-enabled','true',19),(1,'blog-ping-urls','http://rpc.pingomatic.com',20),(1,'blojsom-extension-metaweblog-accepted-types','image/jpeg, image/gif, image/png, img',21),(1,'xmlrpc-enabled','true',22),(1,'blog-pingbacks-enabled','true',23);

CREATE TABLE `Template` (
  `blog_id` int(11) NOT NULL,
  `template_flavor` varchar(50) NOT NULL,
  `template_value` varchar(255) default NULL,
  `template_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`template_id`),
  CONSTRAINT `template_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ;

INSERT INTO `Template` VALUES (1,'rss','rss.vm, text/xml;charset=UTF-8',1),(1,'rsd','rsd.vm, application/rsd+xml;charset=UTF-8',2),(1,'html','asual.vm, text/html;charset=UTF-8',3),(1,'atom','atom.vm, application/atom+xml;charset=UTF-8',4),(1,'rss2','rss2.vm, text/xml;charset=UTF-8',5),(1,'rdf','rdf.vm, text/xml;charset=UTF-8',6),(1,'admin','org/blojsom/plugin/admin/templates/admin.vm, text/html;charset=UTF-8',7);

CREATE TABLE `Trackback` (
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
  CONSTRAINT `trackback_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ;

CREATE TABLE `TrackbackMetadata` (
  `trackback_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text,
  `trackback_metadata_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`trackback_metadata_id`),
  CONSTRAINT `trackbackmetadata_trackback_trackbackidfk` FOREIGN KEY (`trackback_id`) REFERENCES `Trackback` (`trackback_id`) ON DELETE CASCADE
) ;

