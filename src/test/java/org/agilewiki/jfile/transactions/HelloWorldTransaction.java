package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.RP;

public class HelloWorldTransaction extends _TransactionJid {
    @Override
    protected void eval(long blockTimestamp, RP rp) throws Exception {
        System.out.println("Hello world!");
        rp.processResponse(null);
    }
}
