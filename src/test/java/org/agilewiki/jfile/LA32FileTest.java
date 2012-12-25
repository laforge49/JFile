package org.agilewiki.jfile;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.block.Block;
import org.agilewiki.jfile.block.LA32Block;
import org.agilewiki.jid.JidFactories;
import org.agilewiki.jid.scalar.vlens.actor.RootJid;

import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class LA32FileTest extends TestCase {
    public void test()
            throws Exception {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(1);
        Mailbox mailbox = mailboxFactory.createMailbox();
        JAFactory factory = new JAFactory();
        factory.initialize(mailbox);
        (new JidFactories()).initialize(mailbox, factory);
        JAFuture future = new JAFuture();

        JFile jFile = new JFile();
        jFile.initialize(mailbox, factory);
        Path path = FileSystems.getDefault().getPath("LT32FileTest.jalog");
        System.out.println(path.toAbsolutePath());
        jFile.fileChannel = FileChannel.open(
                path,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);

        RootJid rj = new RootJid();
        rj.initialize(mailbox, factory);
        Block block = new LA32Block();
        block.setRootJid(rj);
        (new ForcedWriteRootJid(block)).send(future, jFile);
        assertEquals(12L, block.getCurrentPosition());

        Block block2 = new LA32Block();
        (new ReadRootJid(block2)).send(future, jFile);
        RootJid rj2 = block2.getRootJid(mailbox, factory);
        assertNotNull(rj2);

        (new ReadRootJid(block2)).send(future, jFile);
        rj2 = block2.getRootJid(mailbox, factory);
        assertNull(rj2);

        jFile.fileChannel.close();
        mailboxFactory.close();
    }
}
