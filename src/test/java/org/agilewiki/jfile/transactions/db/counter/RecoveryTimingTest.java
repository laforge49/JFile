package org.agilewiki.jfile.transactions.db.counter;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.Finish;
import org.agilewiki.jfile.transactions.logReader.LogReader;
import org.agilewiki.jfile.transactions.logReader.ReadLog;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class RecoveryTimingTest extends TestCase {
    public void test()
            throws Exception {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(10);
        Mailbox factoryMailbox = mailboxFactory.createMailbox();
        JAFactory factory = new JAFactory();
        factory.initialize(factoryMailbox);
        (new JFileFactories()).initialize(factoryMailbox, factory);
        factory.defineActorType("n", IncrementCounterTransaction.class);
        JAFuture future = new JAFuture();

        Mailbox dbMailbox = mailboxFactory.createAsyncMailbox();
        CounterDB db = new CounterDB();
        db.initialize(dbMailbox, factory);
        Path directoryPath = FileSystems.getDefault().getPath("TransactionLoggerTimingTest");
        db.setDirectoryPath(directoryPath);

        LogReader logReader = db.getLogReader(1000000);
        Path path = directoryPath.resolve("TransactionLoggerTimingTest.jalog");
        System.out.println(path.toAbsolutePath());
        try {
            logReader.open(
                    path,
                    StandardOpenOption.READ);
        } catch (Exception ex) {
            System.out.println("-------------> unable to open log file");
            mailboxFactory.close();
            return;
        }
        logReader.currentPosition = 0;

        long t0 = System.currentTimeMillis();
        long rem = ReadLog.req.send(future, logReader);
        long t1 = System.currentTimeMillis();
        Finish.req.send(future, logReader);
        System.out.println("unprocessed bytes remaining: " + rem);
        logReader.close();

        int transactions = db.getCounter();
        System.out.println("milliseconds: " + (t1 - t0));
        System.out.println("transactions: " + transactions);
        System.out.println("transactions per second = " + (1000L * transactions / (t1 - t0)));
        //tps = 956,937
        mailboxFactory.close();
    }
}
