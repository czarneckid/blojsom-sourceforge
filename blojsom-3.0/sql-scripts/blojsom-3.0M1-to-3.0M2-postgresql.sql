--
-- Upgrade script for blojsom 3.0M1 to blojsom 3.0M2
--

--
-- Add constraint so that if a category is deleted, its associated entries are deleted
--
ALTER TABLE Entry ADD CONSTRAINT entry_blog_category_id_fkey FOREIGN KEY (blog_category_id) REFERENCES Category (category_id) ON DELETE CASCADE;

--
-- Alter the User tables since User is a reserved keyword in most database environments
-- Not relevant in a PostgreSQL environment as it wouldn't have worked in the first place!
--