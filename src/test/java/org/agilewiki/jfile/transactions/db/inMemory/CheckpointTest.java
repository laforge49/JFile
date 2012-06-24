package org.agilewiki.jfile.transactions.db.inMemory;

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

public class CheckpointTest extends TestCase {
    public void test()
            throws Exception {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(10);
        Mailbox factoryMailbox = mailboxFactory.createMailbox();
        JAFactory factory = new JAFactory();
        factory.initialize(factoryMailbox);
        (new JidFactories()).initialize(factoryMailbox, factory);
        (new JFileFactories()).initialize(factoryMailbox, factory);
        JAFuture future = new JAFuture();
        Path directoryPath = FileSystems.getDefault().getPath("CheckpointTest");
        OpenDbFile openDbFile = new OpenDbFile(10000);
        IncrementIntegerTransaction iit = new IncrementIntegerTransaction();
        iit.initialize(factoryMailbox);
        iit.setValue("counter");
        byte[] iitBytes = iit.getBytes();
        AggregateTransaction aggregateIncrementTransaction =
                new AggregateTransaction(JFileFactories.INCREMENT_INTEGER_TRANSACTION, iitBytes);
        GetIntegerTransaction git = new GetIntegerTransaction();
        git.initialize(factoryMailbox);
        git.setValue("counter");
        byte[] gitBytes = git.getBytes();
        AggregateTransaction aggregateGetTransaction =
                new AggregateTransaction(JFileFactories.GET_INTEGER_TRANSACTION, gitBytes);

        System.out.println("db1");
        Mailbox dbMailbox1 = mailboxFactory.createAsyncMailbox();
        IMDB db1 = new IMDB();
        db1.initialize(dbMailbox1, factory);
        db1.maxSize = 10240;
        db1.setDirectoryPath(directoryPath);
        db1.clearDirectory();
        openDbFile.send(future, db1);
        TransactionAggregator transactionAggregator1 = db1.getTransactionAggregator();
        aggregateIncrementTransaction.sendEvent(transactionAggregator1);
        int total1 = (Integer) aggregateGetTransaction.send(future, transactionAggregator1);
        assertEquals(1, total1);
        db1.closeDbFile();

        System.out.println("db2");
        Mailbox dbMailbox2 = mailboxFactory.createAsyncMailbox();
        IMDB db2 = new IMDB();
        db2.initialize(dbMailbox2, factory);
        db2.maxSize = 10240;
        db2.setDirectoryPath(directoryPath);
        openDbFile.send(future, db2);
        TransactionAggregator transactionAggregator2 = db2.getTransactionAggregator();
        aggregateIncrementTransaction.sendEvent(transactionAggregator2);
        aggregateIncrementTransaction.sendEvent(transactionAggregator2);
        int total2 = (Integer) aggregateGetTransaction.send(future, transactionAggregator2);
        assertEquals(3, total2);
        db2.closeDbFile();

        System.out.println("db3");
        Mailbox dbMailbox3 = mailboxFactory.createAsyncMailbox();
        IMDB db3 = new IMDB();
        db3.initialize(dbMailbox3, factory);
        db3.maxSize = 10240;
        db3.setDirectoryPath(directoryPath);
        openDbFile.send(future, db3);
        TransactionAggregator transactionAggregator3 = db3.getTransactionAggregator();
        aggregateIncrementTransaction.sendEvent(transactionAggregator3);
        aggregateIncrementTransaction.sendEvent(transactionAggregator3);
        aggregateIncrementTransaction.sendEvent(transactionAggregator3);
        int total3 = (Integer) aggregateGetTransaction.send(future, transactionAggregator3);
        assertEquals(6, total3);
        db3.closeDbFile();

        mailboxFactory.close();
    }
}
