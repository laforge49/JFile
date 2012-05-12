package org.agilewiki.jfile.transactions.transactionLogger;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.lpc.Request;

public class Go extends Request<Object, TransactionLoggerDriver> {
    @Override
    public boolean isTargetType(Actor targetActor) {
        return targetActor instanceof TransactionLoggerDriver;
    }
}
