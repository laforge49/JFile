package org.agilewiki.jfile.transactions.transactionLoggerTiming;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

public class Go extends Request<Object, TransactionAggregator> {
    public final static Go req = new Go();

    @Override
    public void processRequest(JLPCActor targetActor, RP rp) throws Exception {
        TransactionAggregator a = (TransactionAggregator) targetActor;
        a.go(rp);
    }

    @Override
    public boolean isTargetType(Actor targetActor) {
        return targetActor instanceof TransactionAggregator;
    }
}
