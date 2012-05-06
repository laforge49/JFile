package org.agilewiki.jfile;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jfile.block.Block;
import org.agilewiki.jfile.block.LBlock;
import org.agilewiki.jid.scalar.vlens.actor.RootJid;

import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class LFileTest extends TestCase {
    public void test()
            throws Exception {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(1);
        Mailbox mailbox = mailboxFactory.createMailbox();
        JAFactory factory = new JAFactory(mailbox);
        JAFuture future = new JAFuture();

        JFile jFile = new JFile(mailbox);
        jFile.setParent(factory);
        Path path = FileSystems.getDefault().getPath("LFileTest.jf");
        System.out.println(path.toAbsolutePath());
        jFile.fileChannel = FileChannel.open(
                path,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);

        RootJid rj = new RootJid(mailbox);
        rj.setParent(factory);
        Block block = new LBlock();
        block.setRootJid(rj);
        (new ForcedWriteRootJid(block)).send(future, jFile);
        assertEquals(4L, block.getCurrentPosition());

        Block block2 = new LBlock();
        (new ReadRootJid(block2)).send(future, jFile);
        RootJid rj2 = block2.rootJid(mailbox, factory);
        assertNotNull(rj2);

        (new ReadRootJid(block2)).send(future, jFile);
        rj2 = block2.rootJid(mailbox, factory);
        assertNull(rj2);

        jFile.fileChannel.close();
        mailboxFactory.close();
    }
}
