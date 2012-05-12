package org.agilewiki.jfile.transactions.db.counter;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.JFile;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.HelloWorldTransaction;
import org.agilewiki.jfile.transactions.db.StatelessDB;
import org.agilewiki.jfile.transactions.transactionLogger.ProcessTransaction;
import org.agilewiki.jfile.transactions.transactionLogger.TransactionLogger;
import org.agilewiki.jfile.transactions.transactionProcessor.TransactionProcessor;

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

        JFile jFile = new JFile(mailboxFactory.createAsyncMailbox());
        jFile.setParent(transactionProcessor);
        Path path = FileSystems.getDefault().getPath("CounterTest.jf");
        System.out.println(path.toAbsolutePath());
        jFile.fileChannel = FileChannel.open(
                path,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);

        TransactionLogger transactionLogger =
                new TransactionLogger(mailboxFactory.createAsyncMailbox());
        transactionLogger.setParent(jFile);

        (new ProcessTransaction("inc")).sendEvent(transactionLogger);
        (new ProcessTransaction("inc")).sendEvent(transactionLogger);
        int total = (Integer) (new ProcessTransaction("inc")).send(future, transactionLogger);
        assertEquals(3, total);

        jFile.fileChannel.close();
        mailboxFactory.close();
    }
}
