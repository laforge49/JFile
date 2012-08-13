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

        AggregateTransaction aggregateIncrementTransaction =
                IncrementIntegerTransactionFactory.at(factoryMailbox, "counter");

        IMDB db1 = new IMDB(mailboxFactory, factory, directoryPath, 1024);
        db1.clearDirectory();
        openDbFile.send(future, db1);
        TransactionAggregator transactionAggregator1 = db1.getTransactionAggregator();

        TransactionAggregatorDriver transactionAggregatorDriver =
                new TransactionAggregatorDriver();
        transactionAggregatorDriver.initialize(mailboxFactory.createAsyncMailbox(), transactionAggregator1);
        transactionAggregatorDriver.setInitialBufferCapacity(1024);
        transactionAggregatorDriver.win = 3;
        transactionAggregatorDriver.aggregateTransaction = aggregateIncrementTransaction;

        transactionAggregatorDriver.batch = 1;
        transactionAggregatorDriver.count = 1;

        //System.out.println("###########################################################");
        //transactionAggregatorDriver.batch = 10000;
        //transactionAggregatorDriver.count = 1000;

        Go.req.send(future, transactionAggregatorDriver);
        GetIntegerTransaction git = new GetIntegerTransaction();
        git.initialize(factoryMailbox);
        git.setValue("counter");
        byte[] gitBytes = git.getSerializedBytes();
        AggregateTransaction aggregateGetTransaction =
                new AggregateTransaction(JFileFactories.GET_INTEGER_TRANSACTION, gitBytes);
        long t8 = System.currentTimeMillis();
        int k = 0;
        int total1 = 0;
        while (k < 1000) {
            total1 = (Integer) aggregateGetTransaction.send(future, transactionAggregator1);
            k += 1;
        }
        long t9 = System.currentTimeMillis();
        assertEquals(transactionAggregatorDriver.batch * transactionAggregatorDriver.count, total1);
        System.out.println("latency = " + (t9 - t8) + " microseconds");

        long t0 = System.currentTimeMillis();
        Go.req.send(future, transactionAggregatorDriver);
        Finish.req.send(future, transactionAggregator1);
        long t1 = System.currentTimeMillis();

        int transactions = transactionAggregatorDriver.batch * transactionAggregatorDriver.count;

        System.out.println("milliseconds: " + (t1 - t0));
        System.out.println("transactions: " + transactions);
        System.out.println("transactions per second = " + (1000L * transactions / (t1 - t0)));

        //latency = 296 microseconds

        //batch = 10,000
        //count = 1,000
        //transactions = 10,000,000
        //throughput = 1,244,555 tps

        db1.closeDbFile();
        mailboxFactory.close();
    }
}
