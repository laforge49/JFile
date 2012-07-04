package org.agilewiki.jfile.transactions.db.counter;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.RP;
import org.agilewiki.jfile.transactions.db.DB;

import java.nio.file.Path;

public class CounterDB extends DB {
    private int value;

    public CounterDB() {}

    public CounterDB(MailboxFactory mailboxFactory, Actor parent, Path directoryPath)
            throws Exception {
        super(mailboxFactory, parent, directoryPath);
    }

    public int increment() {
        value += 1;
        return value;
    }

    public int getCounter() {
        return value;
    }

    protected boolean generateCheckpoints() {
        return false;
    }
}
