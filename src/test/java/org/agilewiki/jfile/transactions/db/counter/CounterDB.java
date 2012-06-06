package org.agilewiki.jfile.transactions.db.counter;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.transactions.db.Checkpoint;
import org.agilewiki.jfile.transactions.db.DB;

public class CounterDB extends DB {
    private int value;

    @Override
    protected void processRequest(Object request, RP rp) throws Exception {
        Class reqClass = request.getClass();

        if (reqClass == IncrementCounter.class) {
            rp.processResponse(increment());
            return;
        }

        if (reqClass == GetCounter.class) {
            rp.processResponse(value);
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
    
    public int getCounter() {
        return value;
    }
}
