package org.agilewiki.jfile.transactions.db;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;

public class CounterDB extends JLPCActor implements DB {
    private int value;
    
    public CounterDB(Mailbox mailbox) {
        super(mailbox);
    }

    @Override
    protected void processRequest(Object request, RP rp) throws Exception {
        Class reqClass = request.getClass();

        if (reqClass == IncrementCounter.class) {
            rp.processResponse(increment());
            return;
        }

        if (reqClass == Checkpoint.class) {
            rp.processResponse(null);
            return;
        }

        throw new UnsupportedOperationException(reqClass.getName());
    }
    
    public int increment() {
        value += 1;
        return value;
    }
}
