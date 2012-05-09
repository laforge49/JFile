package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jid.Jid;

public class HelloWorldTransaction extends Jid implements Transaction {
    public HelloWorldTransaction(Mailbox mailbox) {
        super(mailbox);
    }

    @Override
    protected void processRequest(Object request, RP rp) throws Exception {
        if (request.getClass() == Eval.class) {
            System.out.println("Hello world!");
            rp.processResponse(null);
            return;
        }

        super.processRequest(request, rp);
    }
}
