package org.agilewiki.jfile.transactions.db.inMemory;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.transactionAggregator.AggregateTransaction;

/**
 * Factory for GetIntegerTransaction.
 */
public class IncrementIntegerTransactionFactory extends ActorFactory {
    final public static IncrementIntegerTransactionFactory fac = new IncrementIntegerTransactionFactory();

    public static AggregateTransaction at(Mailbox mailbox, String key)
            throws Exception {
        byte[] gitBytes = IncrementIntegerTransaction.bytes(mailbox, key);
        return new AggregateTransaction(fac, gitBytes);
    }

    public IncrementIntegerTransactionFactory() {
        super(JFileFactories.INCREMENT_INTEGER_TRANSACTION);
    }

    @Override
    protected JLPCActor instantiateActor() throws Exception {
        return new IncrementIntegerTransaction();
    }
}
