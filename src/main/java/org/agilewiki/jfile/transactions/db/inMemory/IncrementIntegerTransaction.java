package org.agilewiki.jfile.transactions.db.inMemory;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jfile.transactions._StringTransactionJid;

/**
 * Increments an integer and returns the new value.
 */
public class IncrementIntegerTransaction extends _StringTransactionJid {
    public static byte[] bytes(Mailbox mailbox, String key)
            throws Exception {
        IncrementIntegerTransaction iit = new IncrementIntegerTransaction(mailbox, key);
        return iit.getBytes();
    }

    public IncrementIntegerTransaction() {
    }

    public IncrementIntegerTransaction(Mailbox mailbox, String key)
            throws Exception {
        initialize(mailbox);
        setValue(key);
    }

    @Override
    protected void eval(long blockTimestamp, RP rp) throws Exception {
        IMDB imdb = (IMDB) getAncestor(IMDB.class);
        Integer nv = imdb.incrementInteger(getValue());
        rp.processResponse(nv);
    }
}
