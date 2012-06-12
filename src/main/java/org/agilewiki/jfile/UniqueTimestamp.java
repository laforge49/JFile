package org.agilewiki.jfile;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

/**
 * Returns a unique timestamp result, within limits.
 */
public class UniqueTimestamp extends Request<Long, UniqueClock> {
    public final static UniqueTimestamp req = new UniqueTimestamp();

    @Override
    public void processRequest(JLPCActor targetActor, RP rp) throws Exception {
        UniqueClock a = (UniqueClock) targetActor;
        a.uniqueTimestamp(rp);
    }

    /**
     * Returns true when targetActor is an instanceof TARGET_TYPE
     *
     * @param targetActor The actor to be called.
     * @return True when targetActor is an instanceof TARGET_TYPE.
     */
    @Override
    public boolean isTargetType(Actor targetActor) {
        return targetActor instanceof UniqueClock;
    }
}
