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
        Path directoryPath = FileSystems.getDefault().getPath("CheckpointTest");
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
        db1.maxSize = 10240;
        db1.setDirectoryPath(directoryPath);
        db1.clearDirectory();
        openDbFile.send(future, db1);
        TransactionAggregator transactionAggregator1 = db1.getTransactionAggregator();
        aggregateIncrementTransaction.send(future, transactionAggregator1);
        db1.closeDbFile();

        mailboxFactory.close();
    }
}
