package org.agilewiki.jfile.transactions.db.inMemory;

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
import org.agilewiki.jid.JidFactories;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class ImdbTimingTest extends TestCase {
    public void test()
            throws Exception {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(10);
        Mailbox factoryMailbox = mailboxFactory.createMailbox();
        JAFactory factory = new JAFactory();
        factory.initialize(factoryMailbox);
        (new JidFactories()).initialize(factoryMailbox, factory);
        (new JFileFactories()).initialize(factoryMailbox, factory);
        JAFuture future = new JAFuture();

        Path directoryPath = FileSystems.getDefault().getPath("ImdbTimingTest");
        OpenDbFile openDbFile = new OpenDbFile(10000);
        System.out.println("online");
        IncrementIntegerTransaction iit = new IncrementIntegerTransaction();
        iit.initialize(factoryMailbox);
        iit.setValue("counter");
        byte[] iitBytes = iit.getBytes();
        AggregateTransaction aggregateIncrementTransaction =
                new AggregateTransaction(JFileFactories.INCREMENT_INTEGER_TRANSACTION, iitBytes);

        Mailbox dbMailbox1 = mailboxFactory.createAsyncMailbox();
        IMDB db1 = new IMDB();
        db1.initialize(dbMailbox1, factory);
        db1.maxSize = 1024;
        db1.setDirectoryPath(directoryPath);
        db1.clearDirectory();
        openDbFile.send(future, db1);
        TransactionAggregator transactionAggregator1 = db1.getTransactionAggregator();

        TransactionAggregatorDriver transactionAggregatorDriver =
                new TransactionAggregatorDriver();
        transactionAggregatorDriver.initialize(mailboxFactory.createAsyncMailbox(), transactionAggregator1);
        transactionAggregatorDriver.setInitialBufferCapacity(1024);
        transactionAggregatorDriver.win = 3;
        transactionAggregatorDriver.aggregateTransaction = aggregateIncrementTransaction;

        transactionAggregatorDriver.batch = 10;
        transactionAggregatorDriver.count = 10;
        //   transactionLoggerDriver.batch = 10000;
        //   transactionLoggerDriver.count = 1000;

        Go.req.send(future, transactionAggregatorDriver);
        GetIntegerTransaction git = new GetIntegerTransaction();
        git.initialize(factoryMailbox);
        git.setValue("counter");
        byte[] gitBytes = git.getBytes();
        AggregateTransaction aggregateGetTransaction =
                new AggregateTransaction(JFileFactories.GET_INTEGER_TRANSACTION, gitBytes);
        int total1 = (Integer) aggregateGetTransaction.send(future, transactionAggregator1);
        assertEquals(transactionAggregatorDriver.batch * transactionAggregatorDriver.count, total1);

        long t0 = System.currentTimeMillis();
        Go.req.send(future, transactionAggregatorDriver);
        Finish.req.send(future, transactionAggregator1);
        long t1 = System.currentTimeMillis();

        int transactions = transactionAggregatorDriver.batch * transactionAggregatorDriver.count;

        System.out.println("milliseconds: " + (t1 - t0));
        System.out.println("transactions: " + transactions);
        System.out.println("transactions per second = " + (1000L * transactions / (t1 - t0)));

        //latency = 3 ms

        //batch = 10,000
        //count = 1,000
        //transactions = 10,000,000
        //time = 29.614 seconds
        //throughput = 337,678 tps

        db1.closeDbFile();
        mailboxFactory.close();
    }
}
