package org.agilewiki.jfile.transactions.db.counter;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.transactions.Finish;
import org.agilewiki.jfile.transactions.db.OpenDbFile;
import org.agilewiki.jfile.transactions.logReader.LogReader;
import org.agilewiki.jfile.transactions.logReader.ReadLog;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class CounterRecoveryTest extends TestCase {
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
        (new OpenDbFile(10000)).send(future, db);

        LogReader logReader = db.getLogReader(10000);

        long rem = ReadLog.req.send(future, logReader);
        System.out.println("unprocessed bytes remaining: " + rem);
        Finish.req.send(future, logReader);
        logReader.close();

        int total = db.getCounter();
        assertEquals(6, total);

        mailboxFactory.close();
    }
}
