package org.agilewiki.jfile.transactions.db.inMemory;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jfile.transactions._StringTransactionJid;

/**
 * Returns an Integer.
 */
public class GetIntegerTransaction extends _StringTransactionJid {
    public static byte[] bytes(Mailbox mailbox, String key)
            throws Exception {
        GetIntegerTransaction git = new GetIntegerTransaction(mailbox, key);
        return git.getSerializedBytes();
    }

    public GetIntegerTransaction() {
    }

    public GetIntegerTransaction(Mailbox mailbox, String key)
            throws Exception {
        initialize(mailbox);
        setValue(key);
    }

    @Override
    protected void eval(long blockTimestamp, RP rp) throws Exception {
        IMDB imdb = (IMDB) getAncestor(IMDB.class);
        rp.processResponse(imdb.getInteger(getValue()));
    }
}
