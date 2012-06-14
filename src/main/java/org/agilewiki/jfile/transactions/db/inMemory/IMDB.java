package org.agilewiki.jfile.transactions.db.inMemory;

import org.agilewiki.jfile.JFile;
import org.agilewiki.jfile.block.Block;
import org.agilewiki.jfile.block.LTA32Block;
import org.agilewiki.jfile.transactions.db.DB;

/**
 * In-memory database.
 */
public class IMDB extends DB {
    private JFile dbFile;

    public JFile getDbFile()
            throws Exception {
        if (dbFile == null) {
            dbFile = new JFile();
            dbFile.initialize(getMailboxFactory().createAsyncMailbox());
        }
        return dbFile;
    }

    protected Block newDbBlock() {
        return new LTA32Block();
    }
}
