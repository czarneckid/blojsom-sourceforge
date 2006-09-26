-- MySQL dump 10.9
--
-- Host: localhost    Database: blojsom
-- ------------------------------------------------------
-- Server version	5.0.18-standard

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE="NO_AUTO_VALUE_ON_ZERO" */;

--
-- Initial database creation
--

DROP DATABASE IF EXISTS blojsom;
CREATE DATABASE IF NOT EXISTS blojsom;
USE blojsom;

--
-- Table structure for table `Blog`
--

DROP TABLE IF EXISTS `Blog`;
CREATE TABLE `Blog` (
  `id` int(11) NOT NULL auto_increment,
  `blog_id` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Blog`
--


/*!40000 ALTER TABLE `Blog` DISABLE KEYS */;
LOCK TABLES `Blog` WRITE;
INSERT INTO `Blog` VALUES (1, 'default');
UNLOCK TABLES;
/*!40000 ALTER TABLE `Blog` ENABLE KEYS */;

--
-- Table structure for table `Category`
--

DROP TABLE IF EXISTS `Category`;
CREATE TABLE `Category` (
  `category_id` int(11) NOT NULL auto_increment,
  `blog_id` int(11) NOT NULL,
  `parent_category_id` int(11) default NULL,
  `name` text NOT NULL,
  `description` text,
  PRIMARY KEY  (`category_id`),
  KEY `category_blog_blogidfk` (`blog_id`),
  CONSTRAINT `category_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Category`
--


/*!40000 ALTER TABLE `Category` DISABLE KEYS */;
LOCK TABLES `Category` WRITE;
INSERT INTO `Category` VALUES (1,1,NULL,'/uncategorized/','Uncategorized');
UNLOCK TABLES;
/*!40000 ALTER TABLE `Category` ENABLE KEYS */;

--
-- Table structure for table `CategoryMetadata`
--

DROP TABLE IF EXISTS `CategoryMetadata`;
CREATE TABLE `CategoryMetadata` (
  `category_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text,
  KEY `categorymetadata_category_categoryidfk` (`category_id`),
  CONSTRAINT `categorymetadata_category_categoryidfk` FOREIGN KEY (`category_id`) REFERENCES `Category` (`category_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `CategoryMetadata`
--


/*!40000 ALTER TABLE `CategoryMetadata` DISABLE KEYS */;
LOCK TABLES `CategoryMetadata` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `CategoryMetadata` ENABLE KEYS */;

--
-- Table structure for table `Comment`
--

DROP TABLE IF EXISTS `Comment`;
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
  KEY `comment_blog_blogidfk` (`blog_id`),
  CONSTRAINT `comment_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Comment`
--


/*!40000 ALTER TABLE `Comment` DISABLE KEYS */;
LOCK TABLES `Comment` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `Comment` ENABLE KEYS */;

--
-- Table structure for table `CommentMetadata`
--

DROP TABLE IF EXISTS `CommentMetadata`;
CREATE TABLE `CommentMetadata` (
  `comment_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text,
  KEY `commentmetadata_comment_commentidfk` (`comment_id`),
  CONSTRAINT `commentmetadata_comment_commentidfk` FOREIGN KEY (`comment_id`) REFERENCES `Comment` (`comment_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `CommentMetadata`
--


/*!40000 ALTER TABLE `CommentMetadata` DISABLE KEYS */;
LOCK TABLES `CommentMetadata` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `CommentMetadata` ENABLE KEYS */;


--
-- Table structure for table `DBUser`
--

DROP TABLE IF EXISTS `DBUser`;
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
  KEY `user_blog_blogidfk` (`blog_id`),
  CONSTRAINT `user_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `DBUser`
--


/*!40000 ALTER TABLE `DBUser` DISABLE KEYS */;
LOCK TABLES `DBUser` WRITE;
INSERT INTO `DBUser` VALUES (1,'default','default','Default User','default_owner@email.com',NOW(),'',1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `DBUser` ENABLE KEYS */;

--
-- Table structure for table `DBUserMetadata`
--

DROP TABLE IF EXISTS `DBUserMetadata`;
CREATE TABLE `DBUserMetadata` (
  `user_metadata_id` int(11) NOT NULL auto_increment,
  `user_id` int(11) NOT NULL,
  `metadata_key` varchar(255) NOT NULL,
  `metadata_value` text,
  PRIMARY KEY  (`user_metadata_id`),
  KEY `usermetadata_user_useridfk` (`user_id`),
  CONSTRAINT `usermetadata_user_useridfk` FOREIGN KEY (`user_id`) REFERENCES `DBUser` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `DBUserMetadata`
--


/*!40000 ALTER TABLE `DBUserMetadata` DISABLE KEYS */;
LOCK TABLES `DBUserMetadata` WRITE;
INSERT INTO `DBUserMetadata` VALUES (1,1,'all_permissions_permission','true');
UNLOCK TABLES;
/*!40000 ALTER TABLE `DBUserMetadata` ENABLE KEYS */;

--
-- Table structure for table `Entry`
--

DROP TABLE IF EXISTS `Entry`;
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
  KEY `entry_blog_blogidfk` (`blog_id`),
  CONSTRAINT `entry_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE,
  CONSTRAINT `entry_category_categoryidfk` FOREIGN KEY (`blog_category_id`) REFERENCES `Category` (`category_id`) ON DELETE CASCADE  
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Entry`
--


/*!40000 ALTER TABLE `Entry` DISABLE KEYS */;
LOCK TABLES `Entry` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `Entry` ENABLE KEYS */;

--
-- Table structure for table `EntryMetadata`
--

DROP TABLE IF EXISTS `EntryMetadata`;
CREATE TABLE `EntryMetadata` (
  `entry_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text,
  KEY `entrymetadata_entry_entryidfk` (`entry_id`),
  CONSTRAINT `entrymetadata_entry_entryidfk` FOREIGN KEY (`entry_id`) REFERENCES `Entry` (`entry_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `EntryMetadata`
--


/*!40000 ALTER TABLE `EntryMetadata` DISABLE KEYS */;
LOCK TABLES `EntryMetadata` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `EntryMetadata` ENABLE KEYS */;

--
-- Table structure for table `Pingback`
--

DROP TABLE IF EXISTS `Pingback`;
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
  KEY `pingback_blog_blogidfk` (`blog_id`),
  CONSTRAINT `pingback_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Pingback`
--


/*!40000 ALTER TABLE `Pingback` DISABLE KEYS */;
LOCK TABLES `Pingback` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `Pingback` ENABLE KEYS */;

--
-- Table structure for table `PingbackMetadata`
--

DROP TABLE IF EXISTS `PingbackMetadata`;
CREATE TABLE `PingbackMetadata` (
  `pingback_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text,
  KEY `pingbackmetadata_pingback_pingbackidfk` (`pingback_id`),
  CONSTRAINT `pingbackmetadata_pingback_pingbackidfk` FOREIGN KEY (`pingback_id`) REFERENCES `Pingback` (`pingback_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `PingbackMetadata`
--


/*!40000 ALTER TABLE `PingbackMetadata` DISABLE KEYS */;
LOCK TABLES `PingbackMetadata` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `PingbackMetadata` ENABLE KEYS */;

--
-- Table structure for table `Plugin`
--

DROP TABLE IF EXISTS `Plugin`;
CREATE TABLE `Plugin` (
  `blog_id` int(11) NOT NULL,
  `plugin_flavor` varchar(50) NOT NULL,
  `plugin_value` varchar(4096) default NULL,
  KEY `plugin_blog_blogidfk` (`blog_id`),
  CONSTRAINT `plugin_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Plugin`
--


/*!40000 ALTER TABLE `Plugin` DISABLE KEYS */;
LOCK TABLES `Plugin` WRITE;
INSERT INTO `Plugin` VALUES (1,'html','meta, tag-cloud, date-format, referer-log, calendar-gui, calendar-filter, comment, trackback, simple-search, emoticons, macro-expansion, days-since-posted, word-count, simple-obfuscation, nofollow, rss-enclosure, technorati-tags'),(1,'default','conditional-get, meta, nofollow, rss-enclosure'),(1,'admin','admin');
UNLOCK TABLES;
/*!40000 ALTER TABLE `Plugin` ENABLE KEYS */;

--
-- Table structure for table `Properties`
--

DROP TABLE IF EXISTS `Properties`;
CREATE TABLE `Properties` (
  `blog_id` int(11) NOT NULL,
  `property_name` varchar(255) NOT NULL,
  `property_value` longtext,
  KEY `properties_blog_blogidfk` (`blog_id`),
  CONSTRAINT `properties_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Properties`
--


/*!40000 ALTER TABLE `Properties` DISABLE KEYS */;
LOCK TABLES `Properties` WRITE;
INSERT INTO `Properties` VALUES (1,'blog-url','http://localhost:8080/blojsom/blog/default'),(1,'blog-admin-url','http://localhost:8080/blojsom/blog/default'),(1,'blog-base-url','http://localhost:8080/blojsom'),(1,'blog-base-admin-url','http://localhost:8080/blojsom'),(1,'blog-language','en'),(1,'blog-name','NAME YOUR BLOG'),(1,'blog-description','DESCRIBE YOUR BLOG'),(1,'blog-entries-display','15'),(1,'blog-owner','Default Owner'),(1,'blog-owner-email','default_owner@email.com'),(1,'blog-comments-enabled','true'),(1,'blog-trackbacks-enabled','true'),(1,'blog-email-enabled','true'),(1,'blog-default-flavor','html'),(1,'plugin-comment-autoformat','true'),(1,'linear-navigation-enabled','true'),(1,'comment-moderation-enabled','true'),(1,'trackback-moderation-enabled','true'),(1,'pingback-moderation-enabled','true'),(1,'blog-ping-urls','http://rpc.pingomatic.com'),(1,'blojsom-extension-metaweblog-accepted-types','image/jpeg, image/gif, image/png, img'),(1,'xmlrpc-enabled','true'),(1,'blog-pingbacks-enabled','true');
UNLOCK TABLES;
/*!40000 ALTER TABLE `Properties` ENABLE KEYS */;

--
-- Table structure for table `Template`
--

DROP TABLE IF EXISTS `Template`;
CREATE TABLE `Template` (
  `blog_id` int(11) NOT NULL,
  `template_flavor` varchar(50) NOT NULL,
  `template_value` varchar(255) default NULL,
  KEY `template_blog_blogidfk` (`blog_id`),
  CONSTRAINT `template_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Template`
--


/*!40000 ALTER TABLE `Template` DISABLE KEYS */;
LOCK TABLES `Template` WRITE;
INSERT INTO `Template` VALUES (1,'rss','rss.vm, text/xml;charset=UTF-8'),(1,'rsd','rsd.vm, application/rsd+xml;charset=UTF-8'),(1,'html','asual.vm, text/html;charset=UTF-8'),(1,'atom','atom.vm, application/atom+xml;charset=UTF-8'),(1,'rss2','rss2.vm, text/xml;charset=UTF-8'),(1,'rdf','rdf.vm, text/xml;charset=UTF-8'),(1,'admin','org/blojsom/plugin/admin/templates/admin.vm, text/html;charset=UTF-8');
UNLOCK TABLES;
/*!40000 ALTER TABLE `Template` ENABLE KEYS */;

--
-- Table structure for table `Trackback`
--

DROP TABLE IF EXISTS `Trackback`;
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
  KEY `trackback_blog_blogidfk` (`blog_id`),
  CONSTRAINT `trackback_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Trackback`
--


/*!40000 ALTER TABLE `Trackback` DISABLE KEYS */;
LOCK TABLES `Trackback` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `Trackback` ENABLE KEYS */;

--
-- Table structure for table `TrackbackMetadata`
--

DROP TABLE IF EXISTS `TrackbackMetadata`;
CREATE TABLE `TrackbackMetadata` (
  `trackback_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text,
  KEY `trackbackmetadata_trackback_trackbackidfk` (`trackback_id`),
  CONSTRAINT `trackbackmetadata_trackback_trackbackidfk` FOREIGN KEY (`trackback_id`) REFERENCES `Trackback` (`trackback_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `TrackbackMetadata`
--


/*!40000 ALTER TABLE `TrackbackMetadata` DISABLE KEYS */;
LOCK TABLES `TrackbackMetadata` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `TrackbackMetadata` ENABLE KEYS */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

