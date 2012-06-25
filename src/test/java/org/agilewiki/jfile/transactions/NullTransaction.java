package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.RP;

final public class NullTransaction extends _TransactionJid {
    @Override
    protected void eval(long blockTimestamp, RP rp) throws Exception {
        rp.processResponse(null);
    }
}
