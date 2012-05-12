package org.agilewiki.jfile.transactions.transactionLogger;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.transactions.NullTransactionFactory;

public class TransactionLoggerDriver extends JLPCActor {
    PendingManager pendingManager;
    public int batch;
    final NullTransactionFactory ntf = new NullTransactionFactory("n");


    public TransactionLoggerDriver(Mailbox mailbox) {
        super(mailbox);
    }

    @Override
    protected void processRequest(Object request, RP rp)
            throws Exception {
        pendingManager = new PendingManager();
        pendingManager.rp = rp;
        sender();
    }
    
    void sender()
            throws Exception {
        int i = 0;
        int l = batch -1;
        while (i < l) {
            (new ProcessTransaction(ntf)).sendEvent(getParent());
            i += 1;
        }
        pendingManager.pending += 1;
        (new ProcessTransaction(ntf)).send(this, getParent(), pendingManager);
    }
    
    class PendingManager extends RP {
        RP rp;
        int pending;
        boolean fin;

        @Override
        public void processResponse(Object response)
                throws Exception {
            pending -= 1;
            if (fin && pending == 0) {
                rp.processResponse(null);
                return;
            }
            sender();
        }
    }
}
