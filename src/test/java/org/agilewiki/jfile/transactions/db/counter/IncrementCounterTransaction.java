package org.agilewiki.jfile.transactions.db.counter;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jfile.transactions.Transaction;
import org.agilewiki.jfile.transactions.TransactionEval;
import org.agilewiki.jfile.transactions.TransactionResult;
import org.agilewiki.jid.Jid;

public class IncrementCounterTransaction extends Jid implements Transaction {
    private RP requestReturn;

    public IncrementCounterTransaction(Mailbox mailbox) {
        super(mailbox);
    }

    @Override
    protected void processRequest(Object request, final RP rp) throws Exception {
        Class reqClass = request.getClass();

        if (reqClass == TransactionResult.class) {
            requestReturn = rp;
            return;
        }

        if (reqClass == TransactionEval.class) {
            if (requestReturn == null) {
                IncrementCounter.req.send(this, getParent(), rp);
            } else {
                IncrementCounter.req.send(this, getParent(), new RP<Integer>() {
                    @Override
                    public void processResponse(Integer response) throws Exception {
                        rp.processResponse(null);
                        requestReturn.processResponse(response);
                    }
                });
            }
            return;
        }

        super.processRequest(request, rp);
    }
}
