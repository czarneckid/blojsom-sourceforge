if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[CategoryMetadata]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
ALTER TABLE [dbo].[CategoryMetadata] ADD [category_metadata_id] [int] IDENTITY (1, 1) NOT NULL
GO
 
if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[CommentMetadata]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
ALTER TABLE [dbo].[CommentMetadata] ADD [comment_metadata_id] [int] IDENTITY (1, 1) NOT NULL
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[EntryMetadata]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
ALTER TABLE [dbo].[EntryMetadata] ADD [entry_metadata_id] [int] IDENTITY (1, 1) NOT NULL
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[PingbackMetadata]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
ALTER TABLE [dbo].[PingbackMetadata] ADD [pingback_metadata_id] [int] IDENTITY (1, 1) NOT NULL
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[TrackbackMetadata]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
ALTER TABLE [dbo].[TrackbackMetadata] ADD [trackback_metadata_id] [int] IDENTITY (1, 1) NOT NULL
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[Plugin]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
ALTER TABLE [dbo].[Plugin] ADD [plugin_id] [int] IDENTITY (1, 1) NOT NULL
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[Properties]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
ALTER TABLE [dbo].[Properties] ADD [property_id] [int] IDENTITY (1, 1) NOT NULL
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[Template]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
ALTER TABLE [dbo].[Template] ADD [template_id] [int] IDENTITY (1, 1) NOT NULL
GO