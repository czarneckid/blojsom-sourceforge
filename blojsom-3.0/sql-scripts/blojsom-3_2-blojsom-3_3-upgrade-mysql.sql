ALTER TABLE `blojsom`.`Comment` ADD CONSTRAINT `comment_entry_entryidfk` FOREIGN KEY (`entry_id`)
    REFERENCES `Entry` (`entry_id`)
    ON DELETE CASCADE;

ALTER TABLE `blojsom`.`Pingback` ADD CONSTRAINT `pingback_entry_entryidfk` FOREIGN KEY (`entry_id`)
    REFERENCES `Entry` (`entry_id`)
    ON DELETE CASCADE;

ALTER TABLE `blojsom`.`Trackback` ADD CONSTRAINT `trackback_entry_entryidfk` FOREIGN KEY (`entry_id`)
    REFERENCES `Entry` (`entry_id`)
    ON DELETE CASCADE;
