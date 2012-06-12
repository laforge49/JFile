package org.agilewiki.jfile;

import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;

/**
 * Generates unique timestamps, within limits.
 * Useful when you need a few timestamps for the same millisecond.
 */
public class UniqueClock extends JLPCActor {
    private static UniqueClock uniqueClock;

    public static UniqueClock uc(MailboxFactory mailboxFactory)
            throws Exception {
        if (uniqueClock == null) {
            uniqueClock = new UniqueClock();
            uniqueClock.initialize(mailboxFactory.createMailbox());
        }
        return uniqueClock;
    }

    private long oldts;
    private long olduts;

    private UniqueClock() {}

    void uniqueTimestamp(RP rp)
            throws Exception {
        long ts = System.currentTimeMillis();
        if (ts == oldts) olduts +=1;
        else {
            oldts = ts;
            olduts = ts << 4;
        }
        rp.processResponse(olduts);
    }
}
