package org.agilewiki.jfile.transactions.db.inMemory;

import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jfile.JFileFactories;

/**
 * Factory for AddIntegerTransaction.
 */
public class AddIntegerTransactionFactory extends ActorFactory {
    final public static AddIntegerTransactionFactory fac = new AddIntegerTransactionFactory();

    public AddIntegerTransactionFactory() {
        super(JFileFactories.ADD_INTEGER_TRANSACTION);
    }

    @Override
    protected AddIntegerTransaction instantiateActor() throws Exception {
        return new AddIntegerTransaction();
    }
}
