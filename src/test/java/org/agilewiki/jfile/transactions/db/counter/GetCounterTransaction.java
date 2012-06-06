package org.agilewiki.jfile.transactions.db.counter;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jfile.transactions._TransactionJid;

public class GetCounterTransaction extends _TransactionJid {
    @Override
    protected void eval(long blockTimestamp, RP rp) throws Exception {
        GetCounter.req.send(this, getParent(), rp);
    }
}
