package org.agilewiki.jfile.transactions.transactionAggregator;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.JFile;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.HelloWorldTransaction;
import org.agilewiki.jfile.transactions.TransactionProcessor;
import org.agilewiki.jfile.transactions.db.StatelessDB;

import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TransactionLoggerTest extends TestCase {
    public void test()
            throws Exception {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(10);
        Mailbox factoryMailbox = mailboxFactory.createMailbox();
        JAFactory factory = new JAFactory(factoryMailbox);
        (new JFileFactories(factoryMailbox)).setParent(factory);
        factory.defineActorType("helloWorldTransaction", HelloWorldTransaction.class);
        JAFuture future = new JAFuture();
        Mailbox dbMailbox = mailboxFactory.createAsyncMailbox();
        StatelessDB db = new StatelessDB(dbMailbox);
        db.setParent(factory);
        TransactionProcessor transactionProcessor = new TransactionProcessor(dbMailbox);
        transactionProcessor.setParent(db);

        JFile jFile = new JFile(mailboxFactory.createAsyncMailbox());
        jFile.setParent(transactionProcessor);
        Path path = FileSystems.getDefault().getPath("TransactionLoggerTest.jf");
        System.out.println(path.toAbsolutePath());
        jFile.fileChannel = FileChannel.open(
                path,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);

        TransactionLogger3 transactionLogger =
                new TransactionLogger3(mailboxFactory.createAsyncMailbox());
        transactionLogger.setParent(jFile);

        (new AggregateTransaction("helloWorldTransaction")).sendEvent(transactionLogger);
        (new AggregateTransaction("helloWorldTransaction")).sendEvent(transactionLogger);
        (new AggregateTransaction("helloWorldTransaction")).send(future, transactionLogger);

        jFile.fileChannel.close();
        mailboxFactory.close();
    }
}
