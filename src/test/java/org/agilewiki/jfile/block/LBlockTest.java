package org.agilewiki.jfile.block;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jid.scalar.vlens.actor.RootJid;

public class LBlockTest extends TestCase {
    public void test()
            throws Exception {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(1);
        Mailbox mailbox = mailboxFactory.createMailbox();
        JAFactory factory = new JAFactory(mailbox);

        RootJid rj = new RootJid(mailbox);
        rj.setParent(factory);
        LBlock lb1 = new LBlock();
        byte[] bs = lb1.serialize(rj);

        int hl = lb1.headerLength();
        int rjl = rj.getSerializedLength();
        assertEquals(bs.length, hl + rjl);

        byte[] h = new byte[hl];
        System.arraycopy(bs, 0, h, 0, hl);
        byte[] sd = new byte[rjl];
        System.arraycopy(bs, hl, sd, 0, rjl);

        LBlock lb2 = new LBlock();
        int rjl2 = lb2.setHeader(h);
        lb2.setRootJidBytes(sd);
        RootJid rj2 = lb2.rootJid(mailbox, factory);

        mailboxFactory.close();
    }
}
