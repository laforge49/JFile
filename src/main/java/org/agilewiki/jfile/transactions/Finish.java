package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.lpc.Request;

/**
 * Returns a result of null on completion.
 */
public class Finish extends Request<Object, Finisher> {
    public final static Finish req = new Finish();
    
    /**
     * Returns true when targetActor is an instanceof TARGET_TYPE
     *
     * @param targetActor The actor to be called.
     * @return True when targetActor is an instanceof TARGET_TYPE.
     */
    @Override
    public boolean isTargetType(Actor targetActor) {
        return targetActor instanceof Finisher;
    }
}
