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
        AggregateTransaction inc = new AggregateTransaction("inc");
        AggregateTransaction get = new AggregateTransaction("get");

        System.out.println("db1");
        CounterDB db1 = new CounterDB(mailboxFactory, factory, directoryPath);
        db1.clearDirectory();
        (new OpenDbFile(10000)).send(future, db1);
        TransactionAggregator transactionAggregator1 = db1.getTransactionAggregator();
        inc.sendEvent(transactionAggregator1);
        int total1 = (Integer) (new AggregateTransaction("get")).send(future, transactionAggregator1);
        assertEquals(1, total1);
        db1.closeDbFile();

        System.out.println("db2");
        CounterDB db2 = new CounterDB(mailboxFactory, factory, directoryPath);
        (new OpenDbFile(10000)).send(future, db2);
        TransactionAggregator transactionAggregator2 = db2.getTransactionAggregator();
        inc.sendEvent(transactionAggregator2);
        inc.sendEvent(transactionAggregator2);
        int total2 = (Integer) (new AggregateTransaction("get")).send(future, transactionAggregator2);
        assertEquals(3, total2);
        db2.closeDbFile();

        System.out.println("db3");
        CounterDB db3 = new CounterDB(mailboxFactory, factory, directoryPath);
        (new OpenDbFile(10000)).send(future, db3);
        TransactionAggregator transactionAggregator3 = db3.getTransactionAggregator();
        inc.sendEvent(transactionAggregator3);
        inc.sendEvent(transactionAggregator3);
        inc.sendEvent(transactionAggregator3);
        int total3 = (Integer) get.send(future, transactionAggregator3);
        assertEquals(6, total3);
        db3.closeDbFile();

        mailboxFactory.close();
    }
}
