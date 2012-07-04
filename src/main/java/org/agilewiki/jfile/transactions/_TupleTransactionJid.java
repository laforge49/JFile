package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.RP;
import org.agilewiki.jid.collection.flenc.TupleJid;

/**
 * Base class of transactions with a Tuple.
 */
abstract public class _TupleTransactionJid extends TupleJid implements Transaction {
    private RP requestReturn;
    private Object response;

    public void getTransactionResult(RP rp)
            throws Exception {
        requestReturn = rp;
    }

    public void eval(Eval req, final RP rp) throws Exception {
        eval(req.blockTimestamp, new RP<Object>() {
            @Override
            public void processResponse(Object response) throws Exception {
                _TupleTransactionJid.this.response = response;
                rp.processResponse(requestReturn != null);
            }
        });
    }

    public void sendTransactionResult()
            throws Exception {
        requestReturn.processResponse(response);
    }

    /**
     * Evaluate the transaction.
     *
     * @param rp The response processor.
     */
    abstract protected void eval(long blockTimestamp, RP rp)
            throws Exception;
}
