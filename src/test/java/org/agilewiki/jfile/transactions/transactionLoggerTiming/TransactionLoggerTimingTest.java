package org.agilewiki.jfile.transactions.transactionLoggerTiming;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.*;
import org.agilewiki.jfile.transactions.db.StatelessDB;
import org.agilewiki.jfile.transactions.db.counter.CounterDB;
import org.agilewiki.jfile.transactions.db.counter.IncrementCounterFactory;
import org.agilewiki.jfile.transactions.transactionAggregator.TransactionAggregator;

import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TransactionLoggerTimingTest extends TestCase {
    public void test()
            throws Exception {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(10);
        Mailbox factoryMailbox = mailboxFactory.createMailbox();
        JAFactory factory = new JAFactory(factoryMailbox);
        (new JFileFactories(factoryMailbox)).setParent(factory);
        IncrementCounterFactory ntf = new IncrementCounterFactory("n");
        factory.registerActorFactory(ntf);
        JAFuture future = new JAFuture();

        Mailbox dbMailbox = mailboxFactory.createAsyncMailbox();
        CounterDB db = new CounterDB(dbMailbox);
        db.setParent(factory);
        db.initialCapacity = 10000;

        DurableTransactionLogger durableTransactionLogger = db.getDurableTransactionLogger();
        Path path = FileSystems.getDefault().getPath("TransactionLoggerTimingTest.jf");
        System.out.println(path.toAbsolutePath());
        durableTransactionLogger.fileChannel = FileChannel.open(
                path,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);
        durableTransactionLogger.currentPosition = 0L;

        TransactionAggregator transactionAggregator = db.getTransactionAggregator();

        TransactionLoggerDriver transactionLoggerDriver =
                new TransactionLoggerDriver(mailboxFactory.createAsyncMailbox());
        transactionLoggerDriver.setParent(transactionAggregator);
        transactionLoggerDriver.setInitialBufferCapacity(10000);
        transactionLoggerDriver.win = 3;

       transactionLoggerDriver.batch = 10;
       transactionLoggerDriver.count = 10;
    //   transactionLoggerDriver.batch = 10000;
    //   transactionLoggerDriver.count = 1000;

        Go.req.send(future, transactionLoggerDriver);
        Finish.req.send(future, durableTransactionLogger);
        long t0 = System.currentTimeMillis();
        Go.req.send(future, transactionLoggerDriver);
        Finish.req.send(future, durableTransactionLogger);
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

        durableTransactionLogger.fileChannel.close();
        mailboxFactory.close();
    }
}
