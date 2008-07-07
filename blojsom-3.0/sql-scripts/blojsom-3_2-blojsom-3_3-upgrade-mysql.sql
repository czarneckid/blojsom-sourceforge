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
    
ALTER TABLE `blojsom`.`Entry` MODIFY COLUMN `description` MEDIUMTEXT CHARACTER SET utf8 COLLATE utf8_general_ci
    DEFAULT NULL; 