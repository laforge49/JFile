package org.agilewiki.jfile.transactions.transactionLogger;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.JFile;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.NullTransactionFactory;
import org.agilewiki.jfile.transactions.db.StatelessDB;
import org.agilewiki.jfile.transactions.transactionProcessor.TransactionProcessor;

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

        JFile jFile = new JFile(mailboxFactory.createAsyncMailbox());
        jFile.setParent(transactionProcessor);
        Path path = FileSystems.getDefault().getPath("TransactionLoggerTimingTest.jf");
        System.out.println(path.toAbsolutePath());
        jFile.fileChannel = FileChannel.open(
                path,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);

        Mailbox transactionLoggerMailbox = mailboxFactory.createAsyncMailbox();
        TransactionLogger transactionLogger =
                new TransactionLogger(transactionLoggerMailbox);
        transactionLogger.setParent(jFile);
        transactionLogger.initialCapacity = 2000;
        
        TransactionLoggerDriver transactionLoggerDriver =
                new TransactionLoggerDriver(mailboxFactory.createAsyncMailbox());
        transactionLoggerDriver.setParent(transactionLogger);
        transactionLoggerDriver.batch = 2000;
        transactionLoggerDriver.count = 50000;
        transactionLoggerDriver.win = 5;

        long t0 = System.currentTimeMillis();
        Go.req.send(future, transactionLoggerDriver);
        long t1 = System.currentTimeMillis();

        System.out.println("milliseconds: " + (t1 - t0));

        jFile.fileChannel.close();
        mailboxFactory.close();
    }
}
