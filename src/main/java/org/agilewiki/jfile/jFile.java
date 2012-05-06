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
package org.agilewiki.jfile;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.block.Block;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Reads or writes a RootJid with just the length for a header.
 */
public class JFile extends JLPCActor {
    public FileChannel fileChannel;
    public boolean metaData;

    /**
     * Create a LiteActor
     *
     * @param mailbox A mailbox which may be shared with other actors.
     */
    public JFile(Mailbox mailbox) {
        super(mailbox);
    }

    /**
     * The application method for processing requests sent to the actor.
     *
     * @param request A request.
     * @param rp      The response processor.
     * @throws Exception Any uncaught exceptions raised while processing the request.
     */
    @Override
    protected void processRequest(Object request, RP rp) throws Exception {
        Class reqClass = request.getClass();

        if (reqClass == ReadRootJid.class) {
            ReadRootJid req = (ReadRootJid) request;
            readRootJid(req.block);
            rp.processResponse(null);
            return;
        }

        if (reqClass == ForcedWriteRootJid.class) {
            ForcedWriteRootJid req = (ForcedWriteRootJid) request;
            forcedWriteRootJid(req.block, req.maxSize);
            rp.processResponse(null);
            return;
        }

        if (reqClass == WriteRootJid.class) {
            WriteRootJid req = (WriteRootJid) request;
            writeRootJid(req.block, req.maxSize);
            rp.processResponse(null);
            return;
        }

        throw new UnsupportedOperationException(reqClass.getName());
    }

    protected void writeRootJid(Block block, int maxSize)
            throws Exception {
        byte[] bytes = block.getBytes();
        if (maxSize > -1 && bytes.length > maxSize)
            throw new Exception("" + bytes.length + " exceeds the maxSize of " + maxSize);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int rem = bytes.length;
        long currentPosition = block.getCurrentPosition();
        while (rem > 0) {
            int wl = fileChannel.write(byteBuffer, currentPosition);
            currentPosition += wl;
            rem -= wl;
        }
        block.setCurrentPosition(currentPosition);
    }

    protected void forcedWriteRootJid(Block block, int maxSize)
            throws Exception {
        writeRootJid(block, maxSize);
        fileChannel.force(metaData);
    }

    protected void readRootJid(Block block) {
        try {
            int rem = block.headerLength();
            byte[] hdr = new byte[rem];
            ByteBuffer hbb = ByteBuffer.wrap(hdr);
            long currentPosition = block.getCurrentPosition();
            int rl = fileChannel.read(hbb, currentPosition);
            if (rl == -1)
                return;
            currentPosition += rl;
            rem -= rl;
            while (rem > 0) {
                rl = fileChannel.read(hbb, currentPosition);
                if (rl == -1)
                    return;
                currentPosition += rl;
                rem -= rl;
            }
            rem = block.setHeader(hdr);
            byte[] rjb = new byte[rem];
            if (rem > 0) {
                ByteBuffer rjbb = ByteBuffer.wrap(rjb);
                while (rem > 0) {
                    rl = fileChannel.read(rjbb, currentPosition);
                    if (rl == -1)
                        return;
                    currentPosition += rl;
                    rem -= rl;
                }
            }
            if (block.setRootJidBytes(rjb)) {
                block.setCurrentPosition(currentPosition);
            }
        } catch (Exception ex) {
            return;
        }
        return;
    }
}
