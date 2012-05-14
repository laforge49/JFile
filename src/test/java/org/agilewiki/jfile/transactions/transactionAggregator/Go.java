package org.agilewiki.jfile.transactions.transactionAggregator;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.lpc.Request;

public class Go extends Request<Object, TransactionLoggerDriver> {
    public final static Go req = new Go();
    
    @Override
    public boolean isTargetType(Actor targetActor) {
        return targetActor instanceof TransactionLoggerDriver;
    }
}
