package org.agilewiki.jfile.transactions.db.inMemory;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jfile.transactions._TupleTransactionJid;
import org.agilewiki.jid.scalar.flens.integer.IntegerJid;
import org.agilewiki.jid.scalar.flens.integer.IntegerJidFactory;
import org.agilewiki.jid.scalar.vlens.string.StringJid;
import org.agilewiki.jid.scalar.vlens.string.StringJidFactory;

/**
 * Adds to an integer and returns a new value.
 */
public class AddIntegerTransaction extends _TupleTransactionJid {
    private static ActorFactory afs[] = {StringJidFactory.fac, IntegerJidFactory.fac};

    public static byte[] bytes(Mailbox mailbox, String key, Integer increment)
            throws Exception {
        AddIntegerTransaction ait = new AddIntegerTransaction(mailbox, key, increment);
        return ait.getBytes();
    }

    public AddIntegerTransaction() {}

    public AddIntegerTransaction(Mailbox mailbox, String key, Integer increment)
            throws Exception {
        initialize(mailbox);
        getKeyJid().setValue(key);
        getIncrementJid().setValue(increment);
    }

    protected ActorFactory[] getTupleFactories() throws Exception {
        return afs;
    }

    protected StringJid getKeyJid()
            throws Exception {
        return (StringJid) iGet(0);
    }

    protected IntegerJid getIncrementJid()
            throws Exception {
        return (IntegerJid) iGet(1);
    }

    @Override
    protected void eval(long blockTimestamp, RP rp) throws Exception {
        IMDB imdb = (IMDB) getAncestor(IMDB.class);
        Integer nv = imdb.addInteger(getKeyJid().getValue(), getIncrementJid().getValue());
        rp.processResponse(nv);
    }
}
