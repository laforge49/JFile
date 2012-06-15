package org.agilewiki.jfile.transactions.db.inMemory;

import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.JFileFactories;

/**
 * Factory for GetIntegerTransaction.
 */
public class IncrementIntegerTransactionFactory extends ActorFactory {
    final public static IncrementIntegerTransactionFactory fac = new IncrementIntegerTransactionFactory();

    public IncrementIntegerTransactionFactory() {
        super(JFileFactories.INCREMENT_INTEGER_TRANSACTION);
    }

    @Override
    protected JLPCActor instantiateActor() throws Exception {
        return new IncrementIntegerTransaction();
    }
}
