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
import org.agilewiki.jfile.block.LBlock;
import org.agilewiki.jid.scalar.vlens.actor.RootJid;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Reads or writes a RootJid.
 */
public class JFile extends JLPCActor {
    public FileChannel fileChannel;
    public boolean metaData;

    protected Block createBlock() {
        return new LBlock();
    }

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
            rp.processResponse(readRootJid(req.position));
            return;
        }

        if (reqClass == ForcedWriteRootJid.class) {
            ForcedWriteRootJid req = (ForcedWriteRootJid) request;
            rp.processResponse(forcedWriteRootJid(req.rootJid, req.position));
            return;
        }

        if (reqClass == WriteRootJid.class) {
            WriteRootJid req = (WriteRootJid) request;
            rp.processResponse(writeRootJid(req.rootJid, req.position));
            return;
        }

        throw new UnsupportedOperationException(reqClass.getName());
    }

    protected Block writeRootJid(RootJid rootJid, long position)
            throws Exception {
        Block block = createBlock();
        byte[] bytes = block.serialize(rootJid);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int rem = bytes.length;
        if (position > -1) {
            rem -= fileChannel.write(byteBuffer, position);
        } else
            rem -= fileChannel.write(byteBuffer);
        while (rem > 0) {
            rem -= fileChannel.write(byteBuffer);
        }
        return block;
    }

    protected Block forcedWriteRootJid(RootJid rootJid, long position)
            throws Exception {
        Block block = writeRootJid(rootJid, position);
        fileChannel.force(metaData);
        return block;
    }

    protected Block readRootJid(long position) {
        try {
            Block block = createBlock();
            int rem = block.headerLength();
            byte[] hdr = new byte[rem];
            ByteBuffer hbb = ByteBuffer.wrap(hdr);
            int r;
            if (position > -1) {
                r = fileChannel.read(hbb, position);
            } else
                r = fileChannel.read(hbb);
            if (r == -1)
                return null;
            rem -= r;
            while (rem > 0) {
                r = fileChannel.read(hbb);
                if (r == -1)
                    return null;
                rem -= r;
            }
            rem = block.setHeader(hdr);
            byte[] rjb = new byte[rem];
            if (rem > 0) {
                ByteBuffer rjbb = ByteBuffer.wrap(rjb);
                while (rem > 0) {
                    r = fileChannel.read(rjbb);
                    if (r == -1)
                        throw new IOException("reached eof");
                    rem -= r;
                }
            }
            if (block.setRootJidBytes(rjb))
                return block;
        } catch (Exception ex) {
            return null;
        }
        return null;
    }
}
