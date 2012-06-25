package org.agilewiki.jfile.transactions.db.counter;

import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jactor.lpc.JLPCActor;

public class IncrementCounterFactory extends ActorFactory {
    public IncrementCounterFactory(String actorType) {
        super(actorType);
    }

    @Override
    protected JLPCActor instantiateActor() throws Exception {
        return new IncrementCounterTransaction();
    }
}
