package org.agilewiki.jfile.block;

import org.agilewiki.jfile.transactions.ProcessBlock;

/**
 * A block used by BlockFlowBuffer in processing a Finish request.
 */
public class FBlock extends LBlock {
    public final static FBlock fBlock = new FBlock();
    public final static ProcessBlock process = new ProcessBlock(fBlock);
}
