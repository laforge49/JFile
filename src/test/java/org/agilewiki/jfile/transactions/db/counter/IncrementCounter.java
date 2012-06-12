package org.agilewiki.jfile.transactions.db.counter;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;
import org.agilewiki.jfile.transactions.db.DB;

public class IncrementCounter extends Request<Integer, CounterDB> {
    public final static IncrementCounter req = new IncrementCounter();

    @Override
    public void processRequest(JLPCActor targetActor, RP rp) throws Exception {
        CounterDB a = (CounterDB) targetActor;
        a.increment(rp);
    }

    @Override
    public boolean isTargetType(Actor targetActor) {
        return targetActor instanceof CounterDB;
    }
}
