package org.agilewiki.jfile.transactions.db;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.lpc.Request;
import org.agilewiki.jactor.lpc.SynchronousRequest;

public class IncrementCounter extends Request<Integer, CounterDB> {
    public final static IncrementCounter req = new IncrementCounter();

    @Override
    public boolean isTargetType(Actor targetActor) {
        return targetActor instanceof CounterDB;
    }
}
