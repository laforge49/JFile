package org.agilewiki.jfile.transactions.db.counter;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.lpc.Request;

public class GetCounter extends Request<Integer, CounterDB> {
    public final static GetCounter req = new GetCounter();

    @Override
    public boolean isTargetType(Actor targetActor) {
        return targetActor instanceof CounterDB;
    }
}
