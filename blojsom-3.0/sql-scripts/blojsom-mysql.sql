-- MySQL dump 10.9
--
-- Host: localhost    Database: blojsom
-- ------------------------------------------------------
-- Server version	5.0.15-standard

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
  `blog_id` varchar(50) NOT NULL,
  PRIMARY KEY  (`blog_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `Category`
--

DROP TABLE IF EXISTS `Category`;
CREATE TABLE `Category` (
  `category_id` int(11) NOT NULL auto_increment,
  `blog_id` char(64) NOT NULL,
  `parent_category_id` int(11) default '0',
  `name` text NOT NULL,
  `description` text,
  PRIMARY KEY  (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `CategoryMetadata`
--

DROP TABLE IF EXISTS `CategoryMetadata`;
CREATE TABLE `CategoryMetadata` (
  `category_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text
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
  `blog_id` varchar(50) NOT NULL,
  PRIMARY KEY  (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `CommentMetadata`
--

DROP TABLE IF EXISTS `CommentMetadata`;
CREATE TABLE `CommentMetadata` (
  `comment_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `Entry`
--

DROP TABLE IF EXISTS `Entry`;
CREATE TABLE `Entry` (
  `entry_id` int(11) NOT NULL auto_increment,
  `blog_id` char(64) NOT NULL,
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
  KEY `newfk` (`blog_category_id`),
  CONSTRAINT `newfk` FOREIGN KEY (`blog_category_id`) REFERENCES `category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `EntryMetadata`
--

DROP TABLE IF EXISTS `EntryMetadata`;
CREATE TABLE `EntryMetadata` (
  `entry_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `Pingback`
--

DROP TABLE IF EXISTS `Pingback`;
CREATE TABLE `Pingback` (
  `pingback_id` int(11) NOT NULL,
  `entry_id` int(11) NOT NULL,
  `title` text,
  `excerpt` text,
  `url` text,
  `blog_name` text,
  `trackback_date` datetime NOT NULL,
  `blog_id` varchar(50) NOT NULL,
  `ip` varchar(100) default NULL,
  `status` varchar(255) default NULL,
  PRIMARY KEY  (`pingback_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `PingbackMetadata`
--

DROP TABLE IF EXISTS `PingbackMetadata`;
CREATE TABLE `PingbackMetadata` (
  `pingback_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `Plugin`
--

DROP TABLE IF EXISTS `Plugin`;
CREATE TABLE `Plugin` (
  `blog_id` varchar(50) NOT NULL,
  `plugin_flavor` varchar(50) NOT NULL,
  `plugin_value` varchar(4096) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `Properties`
--

DROP TABLE IF EXISTS `Properties`;
CREATE TABLE `Properties` (
  `blog_id` varchar(50) NOT NULL,
  `property_name` varchar(255) NOT NULL,
  `property_value` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `Template`
--

DROP TABLE IF EXISTS `Template`;
CREATE TABLE `Template` (
  `blog_id` varchar(50) NOT NULL,
  `template_flavor` varchar(50) NOT NULL,
  `template_value` varchar(255) default NULL
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
  `blog_id` varchar(50) NOT NULL,
  `ip` varchar(100) default NULL,
  `status` varchar(255) default NULL,
  PRIMARY KEY  (`trackback_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `TrackbackMetadata`
--

DROP TABLE IF EXISTS `TrackbackMetadata`;
CREATE TABLE `TrackbackMetadata` (
  `trackback_id` int(11) NOT NULL,
  `metadata_key` text NOT NULL,
  `metadata_value` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `User`
--

DROP TABLE IF EXISTS `User`;
CREATE TABLE `User` (
  `user_id` int(11) NOT NULL auto_increment,
  `user_login` varchar(50) NOT NULL,
  `user_password` varchar(64) NOT NULL,
  `user_name` varchar(250) NOT NULL,
  `user_email` varchar(100) NOT NULL,
  `user_registered` datetime NOT NULL,
  `user_status` varchar(64) NOT NULL,
  `blog_id` varchar(50) NOT NULL,
  PRIMARY KEY  (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `UserMetadata`
--

DROP TABLE IF EXISTS `UserMetadata`;
CREATE TABLE `UserMetadata` (
  `user_metadata_id` int(11) NOT NULL auto_increment,
  `user_id` int(11) NOT NULL,
  `metadata_key` varchar(255) NOT NULL,
  `metadata_value` text,
  PRIMARY KEY  (`user_metadata_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

