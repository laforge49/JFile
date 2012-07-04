package org.agilewiki.jfile.transactions.db.inMemory;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.transactionAggregator.AggregateTransaction;

/**
 * Factory for GetIntegerTransaction.
 */
public class GetIntegerTransactionFactory extends ActorFactory {
    final public static GetIntegerTransactionFactory fac = new GetIntegerTransactionFactory();

    public static AggregateTransaction at(Mailbox mailbox, String key)
            throws Exception {
        byte[] gitBytes = GetIntegerTransaction.bytes(mailbox, key);
        return new AggregateTransaction(fac, gitBytes);
    }

    public GetIntegerTransactionFactory() {
        super(JFileFactories.GET_INTEGER_TRANSACTION);
    }

    @Override
    protected JLPCActor instantiateActor() throws Exception {
        return new GetIntegerTransaction();
    }
}
