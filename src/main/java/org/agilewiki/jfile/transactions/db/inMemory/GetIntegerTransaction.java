package org.agilewiki.jfile.transactions.db.inMemory;

import org.agilewiki.jactor.RP;
import org.agilewiki.jfile.transactions._StringTransactionJid;

/**
 * Returns an Integer.
 */
public class GetIntegerTransaction extends _StringTransactionJid {
    @Override
    protected void eval(long blockTimestamp, RP rp) throws Exception {
        IMDB imdb = (IMDB) getAncestor(IMDB.class);
        rp.processResponse(imdb.getInteger(getValue()));
    }
}
