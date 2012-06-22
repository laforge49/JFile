package org.agilewiki.jfile.transactions.db.counter;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.DurableTransactionLogger;
import org.agilewiki.jfile.transactions.transactionAggregator.AggregateTransaction;
import org.agilewiki.jfile.transactions.transactionAggregator.TransactionAggregator;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CounterTest extends TestCase {
    public void test()
            throws Exception {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(10);
        Mailbox factoryMailbox = mailboxFactory.createMailbox();
        JAFactory factory = new JAFactory();
        factory.initialize(factoryMailbox);
        (new JFileFactories()).initialize(factoryMailbox, factory);
        factory.defineActorType("inc", IncrementCounterTransaction.class);
        factory.defineActorType("get", GetCounterTransaction.class);
        JAFuture future = new JAFuture();

        Mailbox dbMailbox = mailboxFactory.createAsyncMailbox();
        CounterDB db = new CounterDB();
        db.initialize(dbMailbox, factory);
        Path directoryPath = FileSystems.getDefault().getPath("CounterTest");
        db.setDirectoryPath(directoryPath);
        db.clearDirectory();

        DurableTransactionLogger durableTransactionLogger = db.getDurableTransactionLogger();
        Path path = directoryPath.resolve("CounterTest.jalog");
        System.out.println(path.toAbsolutePath());
        durableTransactionLogger.open(
                path,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);
        durableTransactionLogger.currentPosition = 0L;

        TransactionAggregator transactionAggregator = db.getTransactionAggregator();
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator);
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator);
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator);
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator);
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator);
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator);
        int total = (Integer) (new AggregateTransaction("get")).send(future, transactionAggregator);
        assertEquals(6, total);

        durableTransactionLogger.close();
        mailboxFactory.close();
    }
}
