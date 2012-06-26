package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

public class Go extends Request<Object, TransactionAggregatorDriver> {
    public final static Go req = new Go();

    @Override
    public void processRequest(JLPCActor targetActor, RP rp) throws Exception {
        TransactionAggregatorDriver a = (TransactionAggregatorDriver) targetActor;
        a.go(rp);
    }

    @Override
    public boolean isTargetType(Actor targetActor) {
        return targetActor instanceof TransactionAggregatorDriver;
    }
}
