package org.agilewiki.jfile.transactions.db.counter;

import org.agilewiki.jactor.RP;
import org.agilewiki.jfile.transactions._TransactionJid;
import org.agilewiki.jfile.transactions.db.DB;

public class IncrementCounterTransaction extends _TransactionJid {
    @Override
    protected void eval(long blockTimestamp, RP rp) throws Exception {
        CounterDB db = (CounterDB) getAncestor(CounterDB.class);
        rp.processResponse(db.increment());
    }
}
