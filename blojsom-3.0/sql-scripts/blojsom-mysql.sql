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
-- Table structure for table `Blog`
--

DROP TABLE IF EXISTS `Blog`;
CREATE TABLE `Blog` (
`id` int(11) NOT NULL auto_increment,
`blog_id` varchar(50) NOT NULL,
PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
-- Table structure for table `CategoryMetadata`
--

DROP TABLE IF EXISTS `CategoryMetadata`;
CREATE TABLE `CategoryMetadata` (
`category_id` int(11) NOT NULL,
`metadata_key` text NOT NULL,
`metadata_value` text,
`category_metadata_id` int(11) NOT NULL auto_increment,
PRIMARY KEY(`category_metadata_id`),
KEY `categorymetadata_category_categoryidfk` (`category_id`),
CONSTRAINT `categorymetadata_category_categoryidfk` FOREIGN KEY (`category_id`) REFERENCES `Category` (`category_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
-- Table structure for table `CommentMetadata`
--

DROP TABLE IF EXISTS `CommentMetadata`;
CREATE TABLE `CommentMetadata` (
`comment_id` int(11) NOT NULL,
`metadata_key` text NOT NULL,
`metadata_value` text,
`comment_metadata_id` int(11) NOT NULL auto_increment,
PRIMARY KEY(`comment_metadata_id`),
KEY `commentmetadata_comment_commentidfk` (`comment_id`),
CONSTRAINT `commentmetadata_comment_commentidfk` FOREIGN KEY (`comment_id`) REFERENCES `Comment` (`comment_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
-- Table structure for table `Entry`
--

DROP TABLE IF EXISTS `Entry`;
CREATE TABLE `Entry` (
  `entry_id` int(11) NOT NULL auto_increment,
  `blog_id` int(11) NOT NULL,
  `title` text,
  `description` mediumtext,
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
-- Table structure for table `EntryMetadata`
--

DROP TABLE IF EXISTS `EntryMetadata`;
CREATE TABLE `EntryMetadata` (
`entry_id` int(11) NOT NULL,
`metadata_key` text NOT NULL,
`metadata_value` text,
`entry_metadata_id` int(11) NOT NULL auto_increment,
PRIMARY KEY(`entry_metadata_id`),
KEY `entrymetadata_entry_entryidfk` (`entry_id`),
CONSTRAINT `entrymetadata_entry_entryidfk` FOREIGN KEY (`entry_id`) REFERENCES `Entry` (`entry_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
-- Table structure for table `PingbackMetadata`
--

DROP TABLE IF EXISTS `PingbackMetadata`;
CREATE TABLE `PingbackMetadata` (
`pingback_id` int(11) NOT NULL,
`metadata_key` text NOT NULL,
`metadata_value` text,
`pingback_metadata_id` int(11) NOT NULL auto_increment,
PRIMARY KEY(`pingback_metadata_id`),
KEY `pingbackmetadata_pingback_pingbackidfk` (`pingback_id`),
CONSTRAINT `pingbackmetadata_pingback_pingbackidfk` FOREIGN KEY (`pingback_id`) REFERENCES `Pingback` (`pingback_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `Plugin`
--

DROP TABLE IF EXISTS `Plugin`;
CREATE TABLE `Plugin` (
`blog_id` int(11) NOT NULL,
`plugin_flavor` varchar(50) NOT NULL,
`plugin_value` varchar(4096) default NULL,
`plugin_id` int(11) NOT NULL auto_increment,
PRIMARY KEY(`plugin_id`),
KEY `plugin_blog_blogidfk` (`blog_id`),
CONSTRAINT `plugin_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `Properties`
--

DROP TABLE IF EXISTS `Properties`;
CREATE TABLE `Properties` (
`blog_id` int(11) NOT NULL,
`property_name` varchar(255) NOT NULL,
`property_value` longtext,
`property_id` int(11) NOT NULL auto_increment,
PRIMARY KEY(`property_id`),
KEY `properties_blog_blogidfk` (`blog_id`),
CONSTRAINT `properties_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `Template`
--

DROP TABLE IF EXISTS `Template`;
CREATE TABLE `Template` (
  `blog_id` int(11) NOT NULL,
  `template_flavor` varchar(50) NOT NULL,
  `template_value` varchar(255) default NULL,
  `template_id` int(11) NOT NULL auto_increment,
  PRIMARY KEY(`template_id`),
  KEY `template_blog_blogidfk` (`blog_id`),
  CONSTRAINT `template_blog_blogidfk` FOREIGN KEY (`blog_id`) REFERENCES `Blog` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
-- Table structure for table `TrackbackMetadata`
--

DROP TABLE IF EXISTS `TrackbackMetadata`;
CREATE TABLE `TrackbackMetadata` (
`trackback_id` int(11) NOT NULL,
`metadata_key` text NOT NULL,
`metadata_value` text,
`trackback_metadata_id` int(11) NOT NULL auto_increment,
PRIMARY KEY(`trackback_metadata_id`),
KEY `trackbackmetadata_trackback_trackbackidfk` (`trackback_id`),
CONSTRAINT `trackbackmetadata_trackback_trackbackidfk` FOREIGN KEY (`trackback_id`) REFERENCES `Trackback` (`trackback_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

ALTER TABLE `blojsom`.`Comment` ADD CONSTRAINT `comment_entry_entryidfk` FOREIGN KEY (`entry_id`)
    REFERENCES `Entry` (`entry_id`)
    ON DELETE CASCADE;

ALTER TABLE `blojsom`.`Pingback` ADD CONSTRAINT `pingback_entry_entryidfk` FOREIGN KEY (`entry_id`)
    REFERENCES `Entry` (`entry_id`)
    ON DELETE CASCADE;

ALTER TABLE `blojsom`.`Trackback` ADD CONSTRAINT `trackback_entry_entryidfk` FOREIGN KEY (`entry_id`)
    REFERENCES `Entry` (`entry_id`)
    ON DELETE CASCADE;

ALTER TABLE `blojsom`.`Category` ADD CONSTRAINT `category_subcategory_categoryidfk` FOREIGN KEY (`parent_category_id`)
    REFERENCES `Category` (`category_id`)
    ON DELETE CASCADE;