package org.agilewiki.jfile.transactions.db.counter;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.transactions.db.Checkpoint;
import org.agilewiki.jfile.transactions.db.DB;

public class CounterDB extends DB {
    private int value;

    public void increment(RP rp)
            throws Exception {
        rp.processResponse(increment());
    }

    public int increment() {
        value += 1;
        return value;
    }

    public void getCounter(RP rp)
            throws Exception {
        rp.processResponse(getCounter());
    }

    public int getCounter() {
        return value;
    }

    protected boolean generateCheckpoints() {
        return false;
    }
}
