package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jactor.lpc.JLPCActor;

final public class NullTransactionFactory extends ActorFactory {
    public NullTransactionFactory(String actorType) {
        super(actorType);
    }

    @Override
    protected JLPCActor instantiateActor() throws Exception {
        return new NullTransaction();
    }
}
