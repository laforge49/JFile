package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jid.Jid;

final public class NullTransaction extends Jid implements Transaction {
    private RP requestReturn;

    public NullTransaction(Mailbox mailbox) {
        super(mailbox);
    }

    @Override
    protected void processRequest(Object request, RP rp) throws Exception {
        Class reqClass = request.getClass();

        if (reqClass == TransactionResult.class) {
            requestReturn = rp;
            return;
        }

        if (reqClass == TransactionEval.class) {
            rp.processResponse(null);
            if (requestReturn != null)
                requestReturn.processResponse(null);
            return;
        }

        super.processRequest(request, rp);
    }
}
