package org.agilewiki.jfile.transactions.transactionLoggerTiming;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.Finish;
import org.agilewiki.jfile.transactions.db.OpenDbFile;
import org.agilewiki.jfile.transactions.db.counter.CounterDB;
import org.agilewiki.jfile.transactions.db.counter.IncrementCounterFactory;
import org.agilewiki.jfile.transactions.transactionAggregator.AggregateTransaction;

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

        Mailbox dbMailbox = mailboxFactory.createAsyncMailbox();
        CounterDB db = new CounterDB();
        db.initialize(dbMailbox, factory);
        db.initialCapacity = 10000;
        Path directoryPath = FileSystems.getDefault().getPath("TransactionLoggerTimingTest");
        db.setDirectoryPath(directoryPath);
        db.clearDirectory();
        (new OpenDbFile(10000)).send(future, db);

        org.agilewiki.jfile.transactions.transactionAggregator.TransactionAggregator transactionAggregator = db.getTransactionAggregator();

        TransactionAggregator transactionLoggerDriver =
                new TransactionAggregator();
        transactionLoggerDriver.initialize(mailboxFactory.createAsyncMailbox(), transactionAggregator);
        transactionLoggerDriver.setInitialBufferCapacity(10000);
        transactionLoggerDriver.win = 3;
        transactionLoggerDriver.aggregateTransaction = aggregateTransaction;

        transactionLoggerDriver.batch = 10;
        transactionLoggerDriver.count = 10;
        //   transactionLoggerDriver.batch = 10000;
        //   transactionLoggerDriver.count = 1000;

        Go.req.send(future, transactionLoggerDriver);
        Finish.req.send(future, transactionAggregator);
        long t0 = System.currentTimeMillis();
        Go.req.send(future, transactionLoggerDriver);
        Finish.req.send(future, transactionAggregator);
        long t1 = System.currentTimeMillis();

        int transactions = transactionLoggerDriver.batch * transactionLoggerDriver.count;
        assertEquals(2 * transactions, db.getCounter());

        System.out.println("milliseconds: " + (t1 - t0));
        System.out.println("transactions: " + transactions);
        System.out.println("transactions per second = " + (1000L * transactions / (t1 - t0)));

        //latency = 2 ms

        //batch = 10000
        //count = 1000
        //transactions = 10,000,000
        //time = 9.953 seconds
        //throughput = 1,004,722 tps

        db.closeDbFile();
        mailboxFactory.close();
    }
}
