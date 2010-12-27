ALTER TABLE CategoryMetadata ADD COLUMN category_metadata_id SERIAL, ADD PRIMARY KEY(category_metadata_id);

ALTER TABLE CommentMetadata ADD COLUMN comment_metadata_id SERIAL, ADD PRIMARY KEY(comment_metadata_id);

ALTER TABLE EntryMetadata ADD COLUMN entry_metadata_id SERIAL, ADD PRIMARY KEY(entry_metadata_id);

ALTER TABLE PingbackMetadata ADD COLUMN pingback_metadata_id SERIAL, ADD PRIMARY KEY(pingback_metadata_id);

ALTER TABLE TrackbackMetadata ADD COLUMN trackback_metadata_id SERIAL, ADD PRIMARY KEY(trackback_metadata_id);

ALTER TABLE Plugin ADD COLUMN plugin_id SERIAL, ADD PRIMARY KEY(plugin_id);

ALTER TABLE Properties ADD COLUMN property_id SERIAL, ADD PRIMARY KEY(property_id);

ALTER TABLE Template ADD COLUMN template_id SERIAL, ADD PRIMARY KEY(template_id);

