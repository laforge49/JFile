package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jid.Jid;

public class HelloWorldTransaction extends _TransactionJid {
    public HelloWorldTransaction(Mailbox mailbox) {
        super(mailbox);
    }

    @Override
    protected void eval(long blockTimestamp, RP rp) throws Exception {
        System.out.println("Hello world!");
        rp.processResponse(null);
    }
}
