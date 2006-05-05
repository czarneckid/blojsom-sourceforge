--
-- Upgrade script for blojsom 3.0M1 to blojsom 3.0M2
--
ALTER TABLE Entry ADD CONSTRAINT `entry_category_categoryidfk` FOREIGN KEY (`blog_category_id`) REFERENCES `Category` (`category_id`) ON DELETE CASCADE;