package org.agilewiki.jfile;

import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.block.Block;

/**
 * Performs a forceRootJid (fsync) operation before doing a write.
 */
public class ForceBeforeWriteRootJid extends WriteRootJid {
    public ForceBeforeWriteRootJid(Block block) {
        super(block);
    }

    /**
     * Write a RootJid and its header,
     * and then forceRootJid the operation to complete.
     * An exception is thrown if the total length of the data to be written exceeds maxSize.
     *
     * @param block   The Block used to manage the operation.
     * @param maxSize The maximum length to be written.
     */
    public ForceBeforeWriteRootJid(Block block, int maxSize) {
        super(block, maxSize);
    }

    @Override
    public void processRequest(JLPCActor targetActor, RP rp) throws Exception {
        JFile a = (JFile) targetActor;
        a.forceBeforeWriteRootJid(block, maxSize);
        rp.processResponse(null);
    }
}
