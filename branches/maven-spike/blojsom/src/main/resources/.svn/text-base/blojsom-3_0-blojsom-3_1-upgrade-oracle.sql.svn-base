
ALTER TABLE Blojsom_CategoryMetadata ADD category_metadata_id INT;
CREATE SEQUENCE Blojsom_category_md_id_Seq START WITH 1 INCREMENT BY 1;
UPDATE Blojsom_CategoryMetadata SET category_metadata_id = Blojsom_category_md_id_Seq.nextval WHERE category_metadata_id IS NULL;
ALTER TABLE Blojsom_CategoryMetadata MODIFY category_metadata_id INT PRIMARY KEY;

CREATE OR REPLACE TRIGGER Blojsom_category_md_id_auto
BEFORE INSERT ON Blojsom_CategoryMetadata FOR EACH ROW
BEGIN
	IF :new.category_metadata_id IS NULL THEN
		SELECT Blojsom_category_md_id_Seq.nextval INTO :new.category_metadata_id FROM DUAL;
	END IF;
END;
/


ALTER TABLE Blojsom_CommentMetadata ADD comment_metadata_id INT;
CREATE SEQUENCE Blojsom_comment_md_id_Seq START WITH 1 INCREMENT BY 1;
UPDATE Blojsom_CommentMetadata SET comment_metadata_id = Blojsom_comment_md_id_Seq.nextval WHERE comment_metadata_id IS NULL;
ALTER TABLE Blojsom_CommentMetadata MODIFY comment_metadata_id INT PRIMARY KEY;

CREATE OR REPLACE TRIGGER Blojsom_comment_md_id_auto
BEFORE INSERT ON Blojsom_CommentMetadata FOR EACH ROW
BEGIN
	IF :new.comment_metadata_id IS NULL THEN
		select Blojsom_comment_md_id_Seq.nextval INTO :new.comment_metadata_id FROM DUAL;
	END IF;
END;
/


ALTER TABLE Blojsom_EntryMetadata ADD entry_metadata_id INT;
CREATE SEQUENCE Blojsom_entry_md_id_Seq START WITH 1 INCREMENT BY 1;
UPDATE Blojsom_EntryMetadata SET entry_metadata_id = Blojsom_entry_md_id_Seq.nextval WHERE entry_metadata_id IS NULL;
ALTER TABLE Blojsom_EntryMetadata MODIFY entry_metadata_id INT PRIMARY KEY;

CREATE OR REPLACE TRIGGER Blojsom_entry_md_id_auto
BEFORE INSERT ON Blojsom_EntryMetadata FOR EACH ROW
BEGIN
	IF :new.entry_metadata_id IS NULL THEN
		select Blojsom_entry_md_id_Seq.nextval INTO :new.entry_metadata_id FROM DUAL;
	END IF;
END;
/


ALTER TABLE Blojsom_PingbackMetadata ADD pingback_metadata_id INT;
CREATE SEQUENCE Blojsom_pingback_md_id_Seq START WITH 1 INCREMENT BY 1;
UPDATE Blojsom_PingbackMetadata SET pingback_metadata_id = Blojsom_pingback_md_id_Seq.nextval WHERE pingback_metadata_id IS NULL;
ALTER TABLE Blojsom_PingbackMetadata MODIFY pingback_metadata_id INT PRIMARY KEY;

CREATE OR REPLACE TRIGGER Blojsom_pingback_md_id_auto
BEFORE INSERT ON Blojsom_PingbackMetadata FOR EACH ROW
BEGIN
	IF :new.pingback_metadata_id IS NULL THEN
		select Blojsom_pingback_md_id_Seq.nextval INTO :new.pingback_metadata_id FROM DUAL;
	END IF;
END;
/


ALTER TABLE Blojsom_TrackbackMetadata ADD trackback_metadata_id INT;
CREATE SEQUENCE Blojsom_trackback_md_id_Seq START WITH 1 INCREMENT BY 1;
UPDATE Blojsom_TrackbackMetadata SET trackback_metadata_id = Blojsom_trackback_md_id_Seq.nextval WHERE trackback_metadata_id IS NULL;
ALTER TABLE Blojsom_TrackbackMetadata MODIFY trackback_metadata_id INT PRIMARY KEY;

CREATE OR REPLACE TRIGGER Blojsom_trackback_md_id_auto
BEFORE INSERT ON Blojsom_TrackbackMetadata FOR EACH ROW
BEGIN
	IF :new.trackback_metadata_id IS NULL THEN
		select Blojsom_trackback_md_id_Seq.nextval INTO :new.trackback_metadata_id FROM DUAL;
	END IF;
END;
/


ALTER TABLE Blojsom_Plugin ADD plugin_id INT;
CREATE SEQUENCE Blojsom_plugin_id_Seq START WITH 1 INCREMENT BY 1;
UPDATE Blojsom_Plugin SET plugin_id = Blojsom_plugin_id_Seq.nextval WHERE plugin_id IS NULL;
ALTER TABLE Blojsom_Plugin MODIFY plugin_id INT PRIMARY KEY;

CREATE OR REPLACE TRIGGER Blojsom_plugin_id_auto
BEFORE INSERT ON Blojsom_Plugin FOR EACH ROW
BEGIN
	IF :new.plugin_id IS NULL THEN
		select Blojsom_plugin_id_Seq.nextval INTO :new.plugin_id FROM DUAL;
	END IF;
END;
/


ALTER TABLE Blojsom_Properties ADD property_id INT;
CREATE SEQUENCE Blojsom_property_id_Seq START WITH 1 INCREMENT BY 1;
UPDATE Blojsom_Properties SET property_id = Blojsom_property_id_Seq.nextval WHERE property_id IS NULL;
ALTER TABLE Blojsom_Properties MODIFY property_id INT PRIMARY KEY;

CREATE OR REPLACE TRIGGER Blojsom_property_id_auto
BEFORE INSERT ON Blojsom_Properties FOR EACH ROW
BEGIN
	IF :new.property_id IS NULL THEN
		select Blojsom_property_id_Seq.nextval INTO :new.property_id FROM DUAL;
	END IF;
END;
/


ALTER TABLE Blojsom_Template ADD template_id INT;
CREATE SEQUENCE Blojsom_template_id_Seq START WITH 1 INCREMENT BY 1;
UPDATE Blojsom_Template SET template_id = Blojsom_template_id_Seq.nextval WHERE template_id IS NULL;
ALTER TABLE Blojsom_Template MODIFY template_id INT PRIMARY KEY;

CREATE OR REPLACE TRIGGER Blojsom_template_id_auto
BEFORE INSERT ON Blojsom_Template FOR EACH ROW
BEGIN
	IF :new.template_id IS NULL THEN
		select Blojsom_template_id_Seq.nextval INTO :new.template_id FROM DUAL;
	END IF;
END;
/


RENAME Blojsom_user_metadata_id_Seq TO Blojsom_user_md_id_Seq;
