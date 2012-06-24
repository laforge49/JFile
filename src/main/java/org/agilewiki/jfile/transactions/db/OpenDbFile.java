package org.agilewiki.jfile.transactions.db;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

/**
 * Opens the database file.
 */
public class OpenDbFile extends Request<Object, DB> {
    public final int logReaderMaxSize;

    public OpenDbFile(int logReaderMaxSize) {
        this.logReaderMaxSize = logReaderMaxSize;
    }

    @Override
    public void processRequest(JLPCActor targetActor, RP rp) throws Exception {
        DB a = (DB) targetActor;
        a.openDbFile(logReaderMaxSize, rp);
    }

    /**
     * Returns true when targetActor is an instanceof TARGET_TYPE
     *
     * @param targetActor The actor to be called.
     * @return True when targetActor is an instanceof TARGET_TYPE.
     */
    @Override
    public boolean isTargetType(Actor targetActor) {
        return targetActor instanceof DB;
    }
}
