--
-- Upgrade script for blojsom 3.0M1 to blojsom 3.0M2
--

--
-- Add constraint so that if a category is deleted, its associated entries are deleted
--
ALTER TABLE Entry ADD CONSTRAINT `entry_category_categoryidfk` FOREIGN KEY (`blog_category_id`) REFERENCES `Category` (`category_id`) ON DELETE CASCADE;

--
-- Alter the User tables since User is a reserved keyword in most database environments
--
ALTER TABLE User RENAME TO DBUser;
ALTER TABLE UserMetadata DROP FOREIGN KEY `usermetadata_user_useridfk`;
ALTER TABLE UserMetadata RENAME TO DBUserMetadata;
ALTER TABLE DBUserMetadata ADD CONSTRAINT `usermetadata_user_useridfk` FOREIGN KEY (`user_id`) REFERENCES `DBUser` (`user_id`) ON DELETE CASCADE;