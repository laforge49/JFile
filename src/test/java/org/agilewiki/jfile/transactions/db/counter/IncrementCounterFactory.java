package org.agilewiki.jfile.transactions.db.counter;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.transactions.NullTransaction;

public class IncrementCounterFactory extends ActorFactory {
    public IncrementCounterFactory(String actorType) {
        super(actorType);
    }

    @Override
    protected JLPCActor instantiateActor() throws Exception {
        return new IncrementCounterTransaction();
    }
}
