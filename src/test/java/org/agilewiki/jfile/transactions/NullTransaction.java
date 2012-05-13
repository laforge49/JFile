package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jid.Jid;

final public class NullTransaction extends _TransactionJid {
    public NullTransaction(Mailbox mailbox) {
        super(mailbox);
    }

    @Override
    protected void eval(RP rp) throws Exception {
        rp.processResponse(null);
    }
}
