package org.blojsom.plugin.xpath;

import org.blojsom.blog.BlogEntry;

/**
 * BlogEntryWrapper is a helper class for the XPath plugin to allow for running XPath expressions
 * over BlogEntry array's.
 *
 * @author Mark Lussier
 * @version $Id: BlogEntryWrapper.java,v 1.1 2003-09-24 03:20:34 intabulas Exp $
 */

public class BlogEntryWrapper {

    private BlogEntry[] _entries;

    public BlogEntryWrapper(BlogEntry[] entries) {
        _entries = entries;
    }

    public BlogEntry[] getEntry() {
        return _entries;
    }


}
