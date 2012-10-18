package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.transactions.transactionAggregator.AggregateTransaction;

public class TransactionAggregatorDriver extends JLPCActor {
    PendingManager pendingManager;
    public int batch;
    public int count;
    public int win;
    int ndx;
    public AggregateTransaction aggregateTransaction;

    protected void go(RP rp)
            throws Exception {
        pendingManager = new PendingManager();
        pendingManager.rp = rp;
        ndx = 0;
        int w = 0;
        while (w < win && w < count) {
            sender();
            w += 1;
        }
    }

    void sender()
            throws Exception {
        int i = 0;
        int l = batch - 1;
        while (i < l) {
            aggregateTransaction.sendEvent(this, getParent());
            i += 1;
        }
        pendingManager.pending += 1;
        ndx += 1;
        if (ndx == count)
            pendingManager.fin = true;
        aggregateTransaction.send(this, getParent(), pendingManager);
    }

    class PendingManager extends RP {
        RP rp;
        int pending;
        boolean fin;

        @Override
        public void processResponse(Object response)
                throws Exception {
            pending -= 1;
            if (fin) {
                if (pending == 0) {
                    rp.processResponse(null);
                    return;
                }
            } else
                sender();
        }
    }
}
