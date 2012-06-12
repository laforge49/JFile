/*
 * Copyright 2012 Bill La Forge
 *
 * This file is part of AgileWiki and is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (LGPL) as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * or navigate to the following url http://www.gnu.org/licenses/lgpl-2.1.txt
 *
 * Note however that only Scala, Java and JavaScript files are being covered by LGPL.
 * All other files are covered by the Common Public License (CPL).
 * A copy of this license is also included and can be
 * found as well at http://www.opensource.org/licenses/cpl1.0.txt
 */
package org.agilewiki.jfile.transactions.logReader;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jfile.JFile;
import org.agilewiki.jfile.block.Block;
import org.agilewiki.jfile.block.LTA32Block;
import org.agilewiki.jfile.transactions.*;

/**
 * Reads a transaction log file and forwards the blocks..
 */
public class LogReader extends JFile implements Finisher {
    private BlockFlowBuffer blockFlowBuffer;
    public long currentPosition;
    public int maxSize;

    /**
     * Creates a new block.
     *
     * @return A new block.
     */
    protected Block newBlock() {
        return new LTA32Block();
    }

    /**
     * Creates a buffered connection to the block processor that is next in the pipeline.
     *
     * @param nextInPipeline The next block processor in the pipeline.
     */
    public void setNext(BlockProcessor nextInPipeline)
            throws Exception {
        blockFlowBuffer = new BlockFlowBuffer();
        blockFlowBuffer.initialize(getMailboxFactory().createMailbox());
        blockFlowBuffer.next = nextInPipeline;
    }

    public void readLog(RP rp)
            throws Exception {
        _rp = rp;
        reader();
    }

    public void finish(RP rp)
            throws Exception {
        Finish.req.send(this, blockFlowBuffer, rp);
    }

    private RP<Long> _rp;
    private boolean sync;
    private boolean async;

    private void reader()
            throws Exception {
        while (true) {
            Block block = newBlock();
            block.setCurrentPosition(currentPosition);
            readRootJid(block, maxSize);
            currentPosition = block.getCurrentPosition();
            if (block.isEmpty()) {
                long position = block.getCurrentPosition();
                long size = fileChannel.size();
                long rem = size - position;
                RP<Long> rp = _rp;
                _rp = null;
                rp.processResponse(rem);
                return;
            }
            ProcessBlock req = new ProcessBlock(block);
            sync = false;
            async = false;
            req.send(this, blockFlowBuffer, new RP<Object>() {
                @Override
                public void processResponse(Object response) throws Exception {
                    if (!async)
                        sync = true;
                    else
                        reader();
                }
            });
            if (!sync) {
                async = true;
                return;
            }
        }
    }
}
