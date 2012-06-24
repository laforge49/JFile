package org.agilewiki.jfile.transactions.db.counter;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.db.OpenDbFile;
import org.agilewiki.jfile.transactions.transactionAggregator.AggregateTransaction;
import org.agilewiki.jfile.transactions.transactionAggregator.TransactionAggregator;
import org.agilewiki.jid.JidFactories;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class CounterTest extends TestCase {
    public void test()
            throws Exception {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(10);
        Mailbox factoryMailbox = mailboxFactory.createMailbox();
        JAFactory factory = new JAFactory();
        factory.initialize(factoryMailbox);
        (new JidFactories()).initialize(factoryMailbox, factory);
        (new JFileFactories()).initialize(factoryMailbox, factory);
        factory.defineActorType("inc", IncrementCounterTransaction.class);
        factory.defineActorType("get", GetCounterTransaction.class);
        Path directoryPath = FileSystems.getDefault().getPath("CounterTest");
        JAFuture future = new JAFuture();

        System.out.println("db1");
        Mailbox dbMailbox1 = mailboxFactory.createAsyncMailbox();
        CounterDB db1 = new CounterDB();
        db1.initialize(dbMailbox1, factory);
        db1.setDirectoryPath(directoryPath);
        db1.clearDirectory();
        (new OpenDbFile(10000)).send(future, db1);
        TransactionAggregator transactionAggregator1 = db1.getTransactionAggregator();
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator1);
        int total1 = (Integer) (new AggregateTransaction("get")).send(future, transactionAggregator1);
        assertEquals(1, total1);
        db1.closeDbFile();

        System.out.println("db2");
        Mailbox dbMailbox2 = mailboxFactory.createAsyncMailbox();
        CounterDB db2 = new CounterDB();
        db2.initialize(dbMailbox2, factory);
        db2.setDirectoryPath(directoryPath);
        (new OpenDbFile(10000)).send(future, db2);
        TransactionAggregator transactionAggregator2 = db2.getTransactionAggregator();
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator2);
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator2);
        int total2 = (Integer) (new AggregateTransaction("get")).send(future, transactionAggregator2);
        assertEquals(3, total2);
        db2.closeDbFile();

        System.out.println("db3");
        Mailbox dbMailbox3 = mailboxFactory.createAsyncMailbox();
        CounterDB db3 = new CounterDB();
        db3.initialize(dbMailbox3, factory);
        db3.setDirectoryPath(directoryPath);
        (new OpenDbFile(10000)).send(future, db3);
        TransactionAggregator transactionAggregator3 = db3.getTransactionAggregator();
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator3);
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator3);
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator3);
        int total3 = (Integer) (new AggregateTransaction("get")).send(future, transactionAggregator3);
        assertEquals(6, total3);
        db3.closeDbFile();

        mailboxFactory.close();
    }
}
