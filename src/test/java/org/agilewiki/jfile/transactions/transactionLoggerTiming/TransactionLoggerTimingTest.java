package org.agilewiki.jfile.transactions.transactionLoggerTiming;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.DurableTransactionLogger;
import org.agilewiki.jfile.transactions.NullTransactionFactory;
import org.agilewiki.jfile.transactions.Serializer;
import org.agilewiki.jfile.transactions.TransactionProcessor;
import org.agilewiki.jfile.transactions.db.StatelessDB;
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
        NullTransactionFactory ntf = new NullTransactionFactory("n");
        factory.registerActorFactory(ntf);
        JAFuture future = new JAFuture();

        Mailbox dbMailbox = mailboxFactory.createAsyncMailbox();
        StatelessDB db = new StatelessDB(dbMailbox);
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

        transactionLoggerDriver.batch = 1;
        transactionLoggerDriver.count = 1;
    //  transactionLoggerDriver.batch = 10000;
    //  transactionLoggerDriver.count = 1000;

        Go.req.send(future, transactionLoggerDriver);
        long t0 = System.currentTimeMillis();
        Go.req.send(future, transactionLoggerDriver);
        long t1 = System.currentTimeMillis();

        System.out.println("milliseconds: " + (t1 - t0));
        System.out.println("transactions: " +
                (transactionLoggerDriver.batch * transactionLoggerDriver.count));

        //latency = 3 ms

        //batch = 10000
        //count = 1000
        //transactions = 10,000,000
        //time = 7.4 seconds
        //throughput = 1,351,351 tps

        durableTransactionLogger.fileChannel.close();
        mailboxFactory.close();
    }
}
