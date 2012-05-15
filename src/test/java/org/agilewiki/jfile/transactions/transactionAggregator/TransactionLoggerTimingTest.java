package org.agilewiki.jfile.transactions.transactionAggregator;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.JFile;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.DurableTransactionLogger;
import org.agilewiki.jfile.transactions.NullTransactionFactory;
import org.agilewiki.jfile.transactions.Serializer;
import org.agilewiki.jfile.transactions.TransactionProcessor;
import org.agilewiki.jfile.transactions.db.StatelessDB;

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
        TransactionProcessor transactionProcessor = new TransactionProcessor(dbMailbox);
        transactionProcessor.setParent(db);

        DurableTransactionLogger durableTransactionLogger =
                new DurableTransactionLogger(mailboxFactory.createAsyncMailbox());
        durableTransactionLogger.setParent(factory);
        durableTransactionLogger.setNext(transactionProcessor);
        Path path = FileSystems.getDefault().getPath("TransactionLoggerTimingTest.jf");
        System.out.println(path.toAbsolutePath());
        durableTransactionLogger.fileChannel = FileChannel.open(
                path,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);

        Serializer serializer = new Serializer(mailboxFactory.createAsyncMailbox());
        serializer.setParent(factory);
        serializer.setNext(durableTransactionLogger);

        TransactionAggregator transactionAggregator =
                new TransactionAggregator(mailboxFactory.createAsyncMailbox());
        transactionAggregator.setParent(db);
        transactionAggregator.setNext(serializer);
        transactionAggregator.initialCapacity = 10000;

        TransactionLoggerDriver transactionLoggerDriver =
                new TransactionLoggerDriver(mailboxFactory.createAsyncMailbox());
        transactionLoggerDriver.setParent(transactionAggregator);
        transactionLoggerDriver.setInitialBufferCapacity(10000);
        transactionLoggerDriver.batch = 10000;
        transactionLoggerDriver.count = 1000;
        transactionLoggerDriver.win = 3;

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
