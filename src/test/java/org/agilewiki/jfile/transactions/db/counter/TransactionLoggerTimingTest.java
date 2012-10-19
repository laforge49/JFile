package org.agilewiki.jfile.transactions.db.counter;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.Finish;
import org.agilewiki.jfile.transactions.Go;
import org.agilewiki.jfile.transactions.TransactionAggregatorDriver;
import org.agilewiki.jfile.transactions.db.OpenDbFile;
import org.agilewiki.jfile.transactions.transactionAggregator.AggregateTransaction;
import org.agilewiki.jfile.transactions.transactionAggregator.TransactionAggregator;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class TransactionLoggerTimingTest extends TestCase {
    public void test()
            throws Exception {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(10);
        Mailbox factoryMailbox = mailboxFactory.createMailbox();
        JAFactory factory = new JAFactory();
        factory.initialize(factoryMailbox);
        (new JFileFactories()).initialize(factoryMailbox, factory);
        IncrementCounterFactory ntf = new IncrementCounterFactory("n");
        factory.registerActorFactory(ntf);
        JAFuture future = new JAFuture();
        AggregateTransaction aggregateTransaction = new AggregateTransaction(ntf);

        Path directoryPath = FileSystems.getDefault().getPath("TransactionLoggerTimingTest");
        CounterDB db = new CounterDB(mailboxFactory, factory, directoryPath);
        db.clearDirectory();
        (new OpenDbFile(10000)).send(future, db);

        TransactionAggregator transactionAggregator = db.getTransactionAggregator();

        TransactionAggregatorDriver transactionAggregatorDriver =
                new TransactionAggregatorDriver();
        transactionAggregatorDriver.initialize(mailboxFactory.createAsyncMailbox(), transactionAggregator);
        transactionAggregatorDriver.setInitialBufferCapacity(10000);
        transactionAggregatorDriver.win = 3;
        transactionAggregatorDriver.aggregateTransaction = aggregateTransaction;

        transactionAggregatorDriver.batch = 10;
        transactionAggregatorDriver.count = 10;

        //System.out.println("###########################################################");
        //transactionAggregatorDriver.batch = 10000;
        //transactionAggregatorDriver.count = 1000;

        Go.req.send(future, transactionAggregatorDriver);
        Finish.req.send(future, transactionAggregator);
        long t0 = System.currentTimeMillis();
        Go.req.send(future, transactionAggregatorDriver);
        Finish.req.send(future, transactionAggregator);
        long t1 = System.currentTimeMillis();

        int transactions = transactionAggregatorDriver.batch * transactionAggregatorDriver.count;
        assertEquals(2 * transactions, db.getCounter());

        System.out.println("milliseconds: " + (t1 - t0));
        System.out.println("transactions: " + transactions);
        if (t1 != t0)
            System.out.println("transactions per second = " + (1000L * transactions / (t1 - t0)));

        //latency = 402 microseconds

        //batch = 10000
        //count = 1000
        //transactions = 10,000,000
        //throughput = 4,056,795 tps

        db.closeDbFile();
        mailboxFactory.close();
    }
}
