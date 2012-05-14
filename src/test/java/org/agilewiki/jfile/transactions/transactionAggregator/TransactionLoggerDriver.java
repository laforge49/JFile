package org.agilewiki.jfile.transactions.transactionAggregator;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.transactions.NullTransactionFactory;

public class TransactionLoggerDriver extends JLPCActor {
    PendingManager pendingManager;
    public int batch;
    public int count;
    public int win;
    int ndx;
    final NullTransactionFactory ntf = new NullTransactionFactory("n");


    public TransactionLoggerDriver(Mailbox mailbox) {
        super(mailbox);
    }

    @Override
    protected void processRequest(Object request, RP rp)
            throws Exception {
        pendingManager = new PendingManager();
        pendingManager.rp = rp;
        ndx = 0;
        int w = 0;
        while (w < win) {
            sender();
            w += 1;
        }
    }

    void sender()
            throws Exception {
        int i = 0;
        int l = batch - 1;
        while (i < l) {
            (new AggregateTransaction(ntf)).sendEvent(this, getParent());
            i += 1;
        }
        pendingManager.pending += 1;
        ndx += 1;
        if (ndx == count)
            pendingManager.fin = true;
        (new AggregateTransaction(ntf)).send(this, getParent(), pendingManager);
        //System.out.println("" + ndx + " " + pendingManager.fin + " " + pendingManager.pending);
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
