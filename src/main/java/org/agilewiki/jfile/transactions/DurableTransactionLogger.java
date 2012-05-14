package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jfile.ForcedWriteRootJid;

/**
 * Durably (fsync'd/forced) logs blocks of transactions.
 */
public class DurableTransactionLogger extends BlockSource implements BlockProcessor {

    /**
     * Create a LiteActor
     *
     * @param mailbox A mailbox which may be shared with other actors.
     */
    public DurableTransactionLogger(Mailbox mailbox) {
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
    protected void processRequest(Object request, final RP rp) throws Exception {
        Class reqClass = request.getClass();

        if (reqClass == ProcessBlock.class) {
            final ProcessBlock req = (ProcessBlock) request;
            (new ForcedWriteRootJid(req.block)).send(this, this, new RP<Object>() {
                @Override
                public void processResponse(Object response) throws Exception {
                    req.send(DurableTransactionLogger.this, blockFlowBuffer, rp);
                }
            });
            return;
        }

        throw new UnsupportedOperationException(reqClass.getName());
    }
}
