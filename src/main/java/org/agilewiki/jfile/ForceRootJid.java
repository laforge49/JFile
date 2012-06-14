package org.agilewiki.jfile;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

/**
 * Performs a force (fsync) operation.
 */
public class ForceRootJid extends Request<Object, JFile> {
    public final static ForceRootJid req = new ForceRootJid();

    @Override
    public void processRequest(JLPCActor targetActor, RP rp) throws Exception {
        JFile a = (JFile) targetActor;
        a.forceRootJid();
        rp.processResponse(null);
    }

    /**
     * Returns true when targetActor is an instanceof TARGET_TYPE
     *
     * @param targetActor The actor to be called.
     * @return True when targetActor is an instanceof TARGET_TYPE.
     */
    @Override
    public boolean isTargetType(Actor targetActor) {
        return targetActor instanceof JFile;
    }
}
