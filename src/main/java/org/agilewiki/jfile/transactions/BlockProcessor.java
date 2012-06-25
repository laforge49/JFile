package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.TargetActor;

/**
 * An actor which accepts ProcessBlock requests.
 */
public interface BlockProcessor extends TargetActor, Finisher {
    void processBlock(ProcessBlock req, RP rp)
            throws Exception;
}
