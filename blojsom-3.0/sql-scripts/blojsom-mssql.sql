if exists (select * from dbo.sysobjects where id = object_id(N'[category_blog_blogidfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [Category] DROP CONSTRAINT category_blog_blogidfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[comment_blog_blogidfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [Comment] DROP CONSTRAINT comment_blog_blogidfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[user_blog_blogidfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [DBUser] DROP CONSTRAINT user_blog_blogidfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[entry_blog_blogidfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [Entry] DROP CONSTRAINT entry_blog_blogidfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[pingback_blog_blogidfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [Pingback] DROP CONSTRAINT pingback_blog_blogidfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[plugin_blog_blogidfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [Plugin] DROP CONSTRAINT plugin_blog_blogidfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[properties_blog_blogidfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [Properties] DROP CONSTRAINT properties_blog_blogidfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[template_blog_blogidfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [Template] DROP CONSTRAINT template_blog_blogidfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[trackback_blog_blogidfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [Trackback] DROP CONSTRAINT trackback_blog_blogidfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[categorymetadata_category_categoryidfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [CategoryMetadata] DROP CONSTRAINT categorymetadata_category_categoryidfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[entry_category_categoryidfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [Entry] DROP CONSTRAINT entry_category_categoryidfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[commentmetadata_comment_commentidfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [CommentMetadata] DROP CONSTRAINT commentmetadata_comment_commentidfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[usermetadata_user_useridfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [DBUserMetadata] DROP CONSTRAINT usermetadata_user_useridfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[entrymetadata_entry_entryidfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [EntryMetadata] DROP CONSTRAINT entrymetadata_entry_entryidfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[pingbackmetadata_pingback_pingbackidfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [PingbackMetadata] DROP CONSTRAINT pingbackmetadata_pingback_pingbackidfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[trackbackmetadata_trackback_trackbackidfk]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [TrackbackMetadata] DROP CONSTRAINT trackbackmetadata_trackback_trackbackidfk
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[Blog]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [Blog]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[Category]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [Category]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[CategoryMetadata]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [CategoryMetadata]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[Comment]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [Comment]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[CommentMetadata]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [CommentMetadata]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[DBUser]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [DBUser]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[DBUserMetadata]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [DBUserMetadata]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[Entry]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [Entry]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[EntryMetadata]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [EntryMetadata]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[Pingback]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [Pingback]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[PingbackMetadata]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [PingbackMetadata]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[Plugin]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [Plugin]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[Properties]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [Properties]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[Template]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [Template]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[Trackback]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [Trackback]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[TrackbackMetadata]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [TrackbackMetadata]
GO

CREATE TABLE [Blog] (
	[id] [int] IDENTITY(1,1) NOT NULL,
	[blog_id] [nvarchar] (50) NOT NULL
) ON [PRIMARY]
GO

CREATE TABLE [Category] (
	[category_id] [int] IDENTITY (1, 1) NOT NULL ,
	[blog_id] [int] NOT NULL ,
	[parent_category_id] [int] NULL ,
	[name] [nvarchar] (255) NOT NULL ,
	[description] [nvarchar] (3700) NULL
) ON [PRIMARY]
GO

CREATE TABLE [CategoryMetadata] (
	[category_id] [int] NOT NULL ,
	[metadata_key] [nvarchar] (255) NOT NULL ,
	[metadata_value] [text] NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

CREATE TABLE [Comment] (
	[comment_id] [int] IDENTITY (1, 1) NOT NULL ,
	[entry_id] [int] NOT NULL ,
	[author] [nvarchar] (75) NULL ,
	[author_url] [varchar] (255) NULL ,
	[author_email] [varchar] (50) NULL ,
	[comment] [ntext] NULL ,
	[date] [datetime] NOT NULL ,
	[ip] [varchar] (100) NULL ,
	[status] [nvarchar] (255) NULL ,
	[comment_parent] [int] NULL ,
	[blog_id] [int] NOT NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

CREATE TABLE [CommentMetadata] (
	[comment_id] [int] NOT NULL ,
	[metadata_key] [nvarchar] (255) NOT NULL ,
	[metadata_value] [ntext] NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

CREATE TABLE [DBUser] (
	[user_id] [int] IDENTITY (1, 1) NOT NULL ,
	[user_login] [nvarchar] (50) NOT NULL ,
	[user_password] [nvarchar] (64) NOT NULL ,
	[user_name] [nvarchar] (250) NOT NULL ,
	[user_email] [varchar] (100) NOT NULL ,
	[user_registered] [datetime] NOT NULL ,
	[user_status] [nvarchar] (64) NOT NULL ,
	[blog_id] [int] NOT NULL
) ON [PRIMARY]
GO

CREATE TABLE [DBUserMetadata] (
	[user_metadata_id] [int] IDENTITY (1, 1) NOT NULL ,
	[user_id] [int] NOT NULL ,
	[metadata_key] [nvarchar] (255) NOT NULL ,
	[metadata_value] [ntext] NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

CREATE TABLE [Entry] (
	[entry_id] [int] IDENTITY (1, 1) NOT NULL ,
	[blog_id] [int] NOT NULL ,
	[title] [nvarchar] (255) NULL ,
	[description] [ntext] NULL ,
	[entry_date] [datetime] NOT NULL ,
	[blog_category_id] [int] NOT NULL ,
	[status] [nvarchar] (100) NULL ,
	[author] [nvarchar] (75) NULL ,
	[allow_comments] [int] NULL ,
	[allow_trackbacks] [int] NULL ,
	[allow_pingbacks] [int] NULL ,
	[post_slug] [nvarchar] (255) NOT NULL ,
	[modified_date] [datetime] NOT NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

CREATE TABLE [EntryMetadata] (
	[entry_id] [int] NOT NULL ,
	[metadata_key] [nvarchar] (255) NOT NULL ,
	[metadata_value] [ntext] NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

CREATE TABLE [Pingback] (
	[pingback_id] [int] IDENTITY (1, 1) NOT NULL ,
	[entry_id] [int] NOT NULL ,
	[title] [nvarchar] (255) NULL ,
	[excerpt] [nvarchar] (500) NULL ,
	[url] [varchar] (255) NULL ,
	[blog_name] [nvarchar] (255) NULL ,
	[trackback_date] [datetime] NOT NULL ,
	[blog_id] [int] NOT NULL ,
	[ip] [varchar] (100) NULL ,
	[status] [nvarchar] (255) NULL ,
	[source_uri] [nvarchar] (255) NOT NULL ,
	[target_uri] [nvarchar] (255) NOT NULL
) ON [PRIMARY]
GO

CREATE TABLE [PingbackMetadata] (
	[pingback_id] [int] NOT NULL ,
	[metadata_key] [nvarchar] (255) NOT NULL ,
	[metadata_value] [ntext] NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

CREATE TABLE [Plugin] (
	[blog_id] [int] NOT NULL ,
	[plugin_flavor] [nvarchar] (50) NOT NULL ,
	[plugin_value] [nvarchar] (3950) NULL
) ON [PRIMARY]
GO

CREATE TABLE [Properties] (
	[blog_id] [int] NOT NULL ,
	[property_name] [nvarchar] (255) NOT NULL ,
	[property_value] [nvarchar] (255) NULL
) ON [PRIMARY]
GO

CREATE TABLE [Template] (
	[blog_id] [int] NOT NULL ,
	[template_flavor] [nvarchar] (50) NOT NULL ,
	[template_value] [nvarchar] (255) NULL
) ON [PRIMARY]
GO

CREATE TABLE [Trackback] (
	[trackback_id] [int] IDENTITY (1, 1) NOT NULL ,
	[entry_id] [int] NOT NULL ,
	[title] [nvarchar] (255) NULL ,
	[excerpt] [nvarchar] (500) NULL ,
	[url] [varchar] (255) NULL ,
	[blog_name] [nvarchar] (255) NULL ,
	[trackback_date] [datetime] NOT NULL ,
	[blog_id] [int] NOT NULL ,
	[ip] [varchar] (100) NULL ,
	[status] [nvarchar] (255) NULL
) ON [PRIMARY]
GO

CREATE TABLE [TrackbackMetadata] (
	[trackback_id] [int] NOT NULL ,
	[metadata_key] [nvarchar] (255) NOT NULL ,
	[metadata_value] [ntext] NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

ALTER TABLE [Blog] WITH NOCHECK ADD
	 PRIMARY KEY  CLUSTERED
	(
		[id]
	)  ON [PRIMARY]
GO

ALTER TABLE [Category] WITH NOCHECK ADD
	 PRIMARY KEY  CLUSTERED
	(
		[category_id]
	)  ON [PRIMARY]
GO

ALTER TABLE [Comment] WITH NOCHECK ADD
	 PRIMARY KEY  CLUSTERED
	(
		[comment_id]
	)  ON [PRIMARY]
GO

ALTER TABLE [DBUser] WITH NOCHECK ADD
	 PRIMARY KEY  CLUSTERED
	(
		[user_id]
	)  ON [PRIMARY]
GO

ALTER TABLE [DBUserMetadata] WITH NOCHECK ADD
	 PRIMARY KEY  CLUSTERED
	(
		[user_metadata_id]
	)  ON [PRIMARY]
GO

ALTER TABLE [Entry] WITH NOCHECK ADD
	 PRIMARY KEY  CLUSTERED
	(
		[entry_id]
	)  ON [PRIMARY]
GO

ALTER TABLE [Pingback] WITH NOCHECK ADD
	 PRIMARY KEY  CLUSTERED
	(
		[pingback_id]
	)  ON [PRIMARY]
GO

ALTER TABLE [Trackback] WITH NOCHECK ADD
	 PRIMARY KEY  CLUSTERED
	(
		[trackback_id]
	)  ON [PRIMARY]
GO

ALTER TABLE [Category] WITH NOCHECK ADD
	CONSTRAINT [DF__Category__parent__5D95E53A] DEFAULT (null) FOR [parent_category_id]
GO

ALTER TABLE [Comment] WITH NOCHECK ADD
	CONSTRAINT [DF__Comment__ip__634EBE90] DEFAULT (null) FOR [ip],
	CONSTRAINT [DF__Comment__status__6442E2C9] DEFAULT (null) FOR [status],
	CONSTRAINT [DF__Comment__comment__65370702] DEFAULT (null) FOR [comment_parent]
GO

ALTER TABLE [Entry] WITH NOCHECK ADD
	CONSTRAINT [DF__Entry__allow_com__6AEFE058] DEFAULT ('1') FOR [allow_comments],
	CONSTRAINT [DF__Entry__allow_tra__6BE40491] DEFAULT ('1') FOR [allow_trackbacks],
	CONSTRAINT [DF__Entry__allow_pin__6CD828CA] DEFAULT ('1') FOR [allow_pingbacks]
GO

ALTER TABLE [Pingback] WITH NOCHECK ADD
	CONSTRAINT [DF__Pingback__ip__73852659] DEFAULT (null) FOR [ip],
	CONSTRAINT [DF__Pingback__status__74794A92] DEFAULT (null) FOR [status]
GO

ALTER TABLE [Plugin] WITH NOCHECK ADD
	CONSTRAINT [DF__Plugin__plugin_v__793DFFAF] DEFAULT (null) FOR [plugin_value]
GO

ALTER TABLE [Template] WITH NOCHECK ADD
	CONSTRAINT [DF__Template__templa__7E02B4CC] DEFAULT (null) FOR [template_value]
GO

ALTER TABLE [Trackback] WITH NOCHECK ADD
	CONSTRAINT [DF__Trackback__ip__01D345B0] DEFAULT (null) FOR [ip],
	CONSTRAINT [DF__Trackback__statu__02C769E9] DEFAULT (null) FOR [status]
GO

ALTER TABLE [Category] ADD 
	CONSTRAINT [category_blog_blogidfk] FOREIGN KEY 
	(
		[blog_id]
	) REFERENCES [Blog] (
		[id]
	) ON DELETE CASCADE 
GO

ALTER TABLE [CategoryMetadata] ADD 
	CONSTRAINT [categorymetadata_category_categoryidfk] FOREIGN KEY 
	(
		[category_id]
	) REFERENCES [Category] (
		[category_id]
	) ON DELETE CASCADE 
GO

ALTER TABLE [Comment] ADD 
	CONSTRAINT [comment_blog_blogidfk] FOREIGN KEY 
	(
		[blog_id]
	) REFERENCES [Blog] (
		[id]
	) ON DELETE CASCADE 
GO

ALTER TABLE [CommentMetadata] ADD 
	CONSTRAINT [commentmetadata_comment_commentidfk] FOREIGN KEY 
	(
		[comment_id]
	) REFERENCES [Comment] (
		[comment_id]
	) ON DELETE CASCADE 
GO

ALTER TABLE [DBUser] ADD 
	CONSTRAINT [user_blog_blogidfk] FOREIGN KEY 
	(
		[blog_id]
	) REFERENCES [Blog] (
		[id]
	) ON DELETE CASCADE 
GO

ALTER TABLE [DBUserMetadata] ADD 
	CONSTRAINT [usermetadata_user_useridfk] FOREIGN KEY 
	(
		[user_id]
	) REFERENCES [DBUser] (
		[user_id]
	) ON DELETE CASCADE 
GO

ALTER TABLE [Entry] ADD 
	CONSTRAINT [entry_blog_blogidfk] FOREIGN KEY 
	(
		[blog_id]
	) REFERENCES [Blog] (
		[id]
	) ON DELETE CASCADE ,
	CONSTRAINT [entry_category_categoryidfk] FOREIGN KEY 
	(
		[blog_category_id]
	) REFERENCES [Category] (
		[category_id]
	)
GO

ALTER TABLE [EntryMetadata] ADD 
	CONSTRAINT [entrymetadata_entry_entryidfk] FOREIGN KEY 
	(
		[entry_id]
	) REFERENCES [Entry] (
		[entry_id]
	) ON DELETE CASCADE 
GO

ALTER TABLE [Pingback] ADD 
	CONSTRAINT [pingback_blog_blogidfk] FOREIGN KEY 
	(
		[blog_id]
	) REFERENCES [Blog] (
		[id]
	) ON DELETE CASCADE 
GO

ALTER TABLE [PingbackMetadata] ADD 
	CONSTRAINT [pingbackmetadata_pingback_pingbackidfk] FOREIGN KEY 
	(
		[pingback_id]
	) REFERENCES [Pingback] (
		[pingback_id]
	) ON DELETE CASCADE 
GO

ALTER TABLE [Plugin] ADD 
	CONSTRAINT [plugin_blog_blogidfk] FOREIGN KEY 
	(
		[blog_id]
	) REFERENCES [Blog] (
		[id]
	) ON DELETE CASCADE 
GO

ALTER TABLE [Template] ADD 
	CONSTRAINT [template_blog_blogidfk] FOREIGN KEY 
	(
		[blog_id]
	) REFERENCES [Blog] (
		[id]
	) ON DELETE CASCADE 
GO

ALTER TABLE [Trackback] ADD 
	CONSTRAINT [trackback_blog_blogidfk] FOREIGN KEY 
	(
		[blog_id]
	) REFERENCES [Blog] (
		[id]
	) ON DELETE CASCADE 
GO

ALTER TABLE [TrackbackMetadata] ADD 
	CONSTRAINT [trackbackmetadata_trackback_trackbackidfk] FOREIGN KEY 
	(
		[trackback_id]
	) REFERENCES [Trackback] (
		[trackback_id]
	) ON DELETE CASCADE 
GO

