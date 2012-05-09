package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jid.collection.vlenc.ListJid;

/**
 * A list of transaction actor's.
 */
public class TransactionListJid extends ListJid implements Transaction {
    private int ndx;
    private boolean sync;
    private boolean async;

    /**
     * Create a ListJid
     *
     * @param mailbox A mailbox which may be shared with other actors.
     */
    public TransactionListJid(Mailbox mailbox) {
        super(mailbox);
    }

    /**
     * The application method for processing requests sent to the actor.
     *
     * @param request A request.
     * @param rp      The response processor.
     * @throws Exception Any uncaught exceptions raised while processing the request.
     */
    @Override
    protected void processRequest(Object request, RP rp) throws Exception {
        if (request.getClass() == Eval.class) {
            ndx = 0;
            eval((Eval) request, rp);
            return;
        }

        super.processRequest(request, rp);
    }

    private void eval(final Eval req, final RP rp)
            throws Exception {
        while (true) {
            if (ndx == size()) {
                rp.processResponse(null);
                return;
            }
            Transaction transaction = (Transaction) iGet(ndx);
            ndx += 1;
            sync = false;
            async = true;
            req.send(this, transaction, new RP<Object>() {
                @Override
                public void processResponse(Object response) throws Exception {
                    if (!async)
                        sync = true;
                    else
                        eval(req, rp);
                }
            });
            if (!sync) {
                async = true;
                return;
            }
        }
    }
}
