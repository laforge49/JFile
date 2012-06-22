package org.agilewiki.jfile.transactions.db.inMemory;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.DurableTransactionLogger;
import org.agilewiki.jfile.transactions.db.OpenDbFile;
import org.agilewiki.jfile.transactions.transactionAggregator.AggregateTransaction;
import org.agilewiki.jfile.transactions.transactionAggregator.TransactionAggregator;
import org.agilewiki.jid.JidFactories;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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

        Mailbox dbMailbox = mailboxFactory.createAsyncMailbox();
        IMDB db = new IMDB();
        db.initialize(dbMailbox, factory);
        db.maxSize = 10240;
        Path directoryPath = FileSystems.getDefault().getPath("CheckpointTest");
        db.setDirectoryPath(directoryPath);
        db.clearDirectory();
        (new OpenDbFile(10000)).send(future, db);

        DurableTransactionLogger durableTransactionLogger = db.getDurableTransactionLogger();
        Path logPath = directoryPath.resolve("CheckpointTestLog.jalog");
        System.out.println(logPath);
        durableTransactionLogger.open(
                logPath,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);
        durableTransactionLogger.currentPosition = 0L;

        TransactionAggregator transactionAggregator = db.getTransactionAggregator();

        IncrementIntegerTransaction iit = new IncrementIntegerTransaction();
        iit.initialize(factoryMailbox);
        iit.setValue("counter");
        byte[] iitBytes = iit.getBytes();
        (new AggregateTransaction(JFileFactories.INCREMENT_INTEGER_TRANSACTION, iitBytes)).
                sendEvent(transactionAggregator);
        (new AggregateTransaction(JFileFactories.INCREMENT_INTEGER_TRANSACTION, iitBytes)).
                sendEvent(transactionAggregator);
        (new AggregateTransaction(JFileFactories.INCREMENT_INTEGER_TRANSACTION, iitBytes)).
                sendEvent(transactionAggregator);
        (new AggregateTransaction(JFileFactories.INCREMENT_INTEGER_TRANSACTION, iitBytes)).
                sendEvent(transactionAggregator);
        (new AggregateTransaction(JFileFactories.INCREMENT_INTEGER_TRANSACTION, iitBytes)).
                sendEvent(transactionAggregator);
        (new AggregateTransaction(JFileFactories.INCREMENT_INTEGER_TRANSACTION, iitBytes)).
                sendEvent(transactionAggregator);

        GetIntegerTransaction git = new GetIntegerTransaction();
        git.initialize(factoryMailbox);
        git.setValue("counter");
        byte[] gitBytes = git.getBytes();
        int total = (Integer) (new AggregateTransaction(JFileFactories.GET_INTEGER_TRANSACTION, gitBytes)).
                send(future, transactionAggregator);
        assertEquals(6, total);

        db.closeDbFile();
        mailboxFactory.close();
    }
}
