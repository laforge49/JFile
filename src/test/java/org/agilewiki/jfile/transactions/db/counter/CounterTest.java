package org.agilewiki.jfile.transactions.db.counter;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.DurableTransactionLogger;
import org.agilewiki.jfile.transactions.Serializer;
import org.agilewiki.jfile.transactions.TransactionProcessor;
import org.agilewiki.jfile.transactions.transactionAggregator.AggregateTransaction;
import org.agilewiki.jfile.transactions.transactionAggregator.TransactionAggregator;

import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CounterTest extends TestCase {
    public void test()
            throws Exception {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(10);
        Mailbox factoryMailbox = mailboxFactory.createMailbox();
        JAFactory factory = new JAFactory(factoryMailbox);
        (new JFileFactories(factoryMailbox)).setParent(factory);
        factory.defineActorType("inc", IncrementCounterTransaction.class);
        JAFuture future = new JAFuture();
        
        Mailbox dbMailbox = mailboxFactory.createAsyncMailbox();
        CounterDB db = new CounterDB(dbMailbox);
        db.setParent(factory);
        TransactionProcessor transactionProcessor = new TransactionProcessor(dbMailbox);
        transactionProcessor.setParent(db);

        DurableTransactionLogger durableTransactionLogger = new DurableTransactionLogger(mailboxFactory.createAsyncMailbox());
        durableTransactionLogger.setParent(factory);
        durableTransactionLogger.setNext(transactionProcessor);
        Path path = FileSystems.getDefault().getPath("CounterTest.jf");
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

        (new AggregateTransaction("inc")).sendEvent(transactionAggregator);
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator);
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator);
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator);
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator);
        (new AggregateTransaction("inc")).sendEvent(transactionAggregator);
        int total = (Integer) (new AggregateTransaction("inc")).send(future, transactionAggregator);
        assertEquals(7, total);

        durableTransactionLogger.fileChannel.close();
        mailboxFactory.close();
    }
}
