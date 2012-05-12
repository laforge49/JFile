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

        TransactionLogger transactionLogger =
                new TransactionLogger(mailboxFactory.createAsyncMailbox());
        transactionLogger.setParent(jFile);
        transactionLogger.initialCapacity = 2000;    //1000 => 2385ms

        int i = 0;
        while (i < 999999) {
            (new ProcessTransaction(ntf)).sendEvent(transactionLogger);
            i += 1;
            if (i % 2000 == 0)
                Thread.sleep(1);
        }
        (new ProcessTransaction(ntf)).send(future, transactionLogger);

        long t0 = System.currentTimeMillis();
        i = 0;
        while (i < 999999) {
            (new ProcessTransaction(ntf)).sendEvent(transactionLogger);
            i += 1;
            if (i % 2000 == 0)
                Thread.sleep(1);
        }
        (new ProcessTransaction(ntf)).send(future, transactionLogger);
        long t1 = System.currentTimeMillis();

        System.out.println("milliseconds: " + (t1 - t0));

        jFile.fileChannel.close();
        mailboxFactory.close();
    }
}
