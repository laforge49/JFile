package org.agilewiki.jfile.transactions.db.inMemory;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.transactionAggregator.AggregateTransaction;

/**
 * Factory for AddIntegerTransaction.
 */
public class AddIntegerTransactionFactory extends ActorFactory {
    final public static AddIntegerTransactionFactory fac = new AddIntegerTransactionFactory();

    public static AggregateTransaction at(Mailbox mailbox, String key, Integer increment)
            throws Exception {
        byte[] gitBytes = AddIntegerTransaction.bytes(mailbox, key, increment);
        return new AggregateTransaction(fac, gitBytes);
    }

    public AddIntegerTransactionFactory() {
        super(JFileFactories.ADD_INTEGER_TRANSACTION);
    }

    @Override
    protected AddIntegerTransaction instantiateActor() throws Exception {
        return new AddIntegerTransaction();
    }
}
