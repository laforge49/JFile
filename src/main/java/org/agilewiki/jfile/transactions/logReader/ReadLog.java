package org.agilewiki.jfile.transactions.logReader;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.lpc.Request;

/**
 * Read a transaction log file.
 * The returned result is the remaining number of unprocessed bytes.
 */
public class ReadLog extends Request<Long, LogReader> {
    public final static ReadLog req = new ReadLog();

    /**
     * Returns true when targetActor is an instanceof TARGET_TYPE
     *
     * @param targetActor The actor to be called.
     * @return True when targetActor is an instanceof TARGET_TYPE.
     */
    @Override
    public boolean isTargetType(Actor targetActor) {
        return targetActor instanceof LogReader;
    }
}
