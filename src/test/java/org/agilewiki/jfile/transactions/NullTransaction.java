package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;

public class NullTransaction extends JLPCActor {
    public NullTransaction(Mailbox mailbox) {
        super(mailbox);
    }

    @Override
    protected void processRequest(Object request, RP rp) throws Exception {
        rp.processResponse(null);
    }
}
