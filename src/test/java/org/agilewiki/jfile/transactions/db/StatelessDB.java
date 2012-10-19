package org.agilewiki.jfile.transactions.db;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.MailboxFactory;

import java.nio.file.Path;

public class StatelessDB extends DB {

    public StatelessDB() {
    }

    public StatelessDB(MailboxFactory mailboxFactory, Actor parent, Path directoryPath)
            throws Exception {
        super(mailboxFactory, parent, directoryPath);
    }

    protected boolean generateCheckpoints() {
        return false;
    }
}
