package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

/**
 * Signals that it is now ok to send the transaction result.
 */
public class SendTransactionResult extends Request<Object, Evaluator> {
    public final static SendTransactionResult req = new SendTransactionResult();

    @Override
    public void processRequest(JLPCActor targetActor, RP rp) throws Exception {
        Transaction a = (Transaction) targetActor;
        a.sendTransactionResult();
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
        return targetActor instanceof Transaction;
    }
}
