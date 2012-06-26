package org.agilewiki.jfile.transactions;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.db.StatelessDB;
import org.agilewiki.jfile.transactions.transactionAggregator.*;
import org.agilewiki.jfile.transactions.transactionAggregator.TransactionAggregator;

public class TransactionAggregatorTest extends TestCase {
    public void test()
            throws Exception {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(10);
        Mailbox factoryMailbox = mailboxFactory.createMailbox();
        JAFactory factory = new JAFactory();
        factory.initialize(factoryMailbox);
        (new JFileFactories()).initialize(factoryMailbox, factory);
        factory.defineActorType("helloWorldTransaction", HelloWorldTransaction.class);
        JAFuture future = new JAFuture();
        Mailbox dbMailbox = mailboxFactory.createAsyncMailbox();
        StatelessDB db = new StatelessDB();
        db.initialize(dbMailbox, factory);
        TransactionProcessor transactionProcessor = new TransactionProcessor();
        transactionProcessor.initialize(dbMailbox, db);

        TransactionAggregator transactionAggregator = new org.agilewiki.jfile.transactions.transactionAggregator.TransactionAggregator();
        transactionAggregator.initialize(mailboxFactory.createAsyncMailbox(), factory);
        transactionAggregator.setNext(transactionProcessor);

        (new AggregateTransaction("helloWorldTransaction")).sendEvent(transactionAggregator);
        (new AggregateTransaction("helloWorldTransaction")).sendEvent(transactionAggregator);
        (new AggregateTransaction("helloWorldTransaction")).send(future, transactionAggregator);

        mailboxFactory.close();
    }
}
