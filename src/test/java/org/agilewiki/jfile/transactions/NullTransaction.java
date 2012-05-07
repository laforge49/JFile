package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jid.Jid;

public class NullTransaction extends Jid {
    public NullTransaction(Mailbox mailbox) {
        super(mailbox);
    }

    @Override
    protected void processRequest(Object request, RP rp) throws Exception {
        if (request.getClass() == Eval.class) {
            rp.processResponse(null);
            return;
        }

        super.processRequest(request, rp);
    }
}
