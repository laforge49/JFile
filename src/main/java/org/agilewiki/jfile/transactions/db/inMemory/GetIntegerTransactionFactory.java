package org.agilewiki.jfile.transactions.db.inMemory;

import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.JFileFactories;

/**
 * Factory for GetIntegerTransaction.
 */
public class GetIntegerTransactionFactory extends ActorFactory {
    final public static GetIntegerTransactionFactory fac = new GetIntegerTransactionFactory();

    public GetIntegerTransactionFactory() {
        super(JFileFactories.GET_INTEGER_TRANSACTION);
    }

    @Override
    protected JLPCActor instantiateActor() throws Exception {
        return new GetIntegerTransaction();
    }
}
