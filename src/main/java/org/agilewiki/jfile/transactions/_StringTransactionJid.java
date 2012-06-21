package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.RP;
import org.agilewiki.jid.scalar.vlens.string.StringJid;

/**
 * Base class of transactions with a String.
 */
abstract public class _StringTransactionJid extends StringJid implements Transaction {
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
                _StringTransactionJid.this.response = response;
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
