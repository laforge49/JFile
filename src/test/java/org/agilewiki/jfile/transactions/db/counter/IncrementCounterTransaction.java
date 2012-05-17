package org.agilewiki.jfile.transactions.db.counter;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jfile.transactions.Transaction;
import org.agilewiki.jfile.transactions.TransactionEval;
import org.agilewiki.jfile.transactions.TransactionResult;
import org.agilewiki.jfile.transactions._TransactionJid;
import org.agilewiki.jid.Jid;

public class IncrementCounterTransaction extends _TransactionJid {
    public IncrementCounterTransaction(Mailbox mailbox) {
        super(mailbox);
    }

    @Override
    protected void eval(RP rp) throws Exception {
        System.out.println("inc!");
        IncrementCounter.req.send(this, getParent(), rp);
    }
}
