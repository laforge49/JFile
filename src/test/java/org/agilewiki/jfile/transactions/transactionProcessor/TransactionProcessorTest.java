package org.agilewiki.jfile.transactions.transactionProcessor;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.ForcedWriteRootJid;
import org.agilewiki.jfile.JFile;
import org.agilewiki.jfile.block.Block;
import org.agilewiki.jfile.block.LTBlock;
import org.agilewiki.jfile.transactions.HelloWorldTransaction;
import org.agilewiki.jfile.transactions.ProcessBlock;
import org.agilewiki.jfile.transactions.TransactionProcessor;
import org.agilewiki.jfile.transactions.db.StatelessDB;
import org.agilewiki.jid.scalar.vlens.actor.RootJid;
import org.agilewiki.jid.scalar.vlens.actor.SetActor;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TransactionProcessorTest extends TestCase {
    public void test()
            throws Exception {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(1);
        Mailbox mailbox = mailboxFactory.createMailbox();
        JAFactory factory = new JAFactory();
        factory.initialize(mailbox);
        factory.defineActorType("helloWorldTransaction", HelloWorldTransaction.class);
        JAFuture future = new JAFuture();
        StatelessDB db = new StatelessDB();
        db.initialize(mailbox, factory);
        Path directoryPath = FileSystems.getDefault().getPath("TransactionProcessorTest");
        db.setDirectoryPath(directoryPath);
        db.clearDirectory();
        TransactionProcessor transactionProcessor = new TransactionProcessor();
        transactionProcessor.initialize(mailbox, db);

        JFile jFile = new JFile();
        jFile.initialize(mailbox, factory);
        Path path = directoryPath.resolve("TransactionProcessorTest.jf");
        System.out.println(path.toAbsolutePath());
        jFile.open(
                path,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);

        RootJid rj = new RootJid();
        rj.initialize(mailbox, db);
        (new SetActor("helloWorldTransaction")).send(future, rj);
        Block block = new LTBlock();
        block.setRootJid(rj);
        long timestamp = System.currentTimeMillis();
        block.setTimestamp(timestamp);
        (new ForcedWriteRootJid(block)).send(future, jFile);

        (new ProcessBlock(block)).send(future, transactionProcessor);

        jFile.close();
        mailboxFactory.close();
    }
}
