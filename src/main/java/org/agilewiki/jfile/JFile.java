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

import org.agilewiki.jactor.Actor;
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
    protected long position = 0;
    private Block block;
    
    protected Block createBlock() {
        return new LBlock();
    }
    
    final protected Block getBlock() {
        if (block == null)
            block = createBlock();
        return block;
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
            rp.processResponse(readRootJid(req.mailbox, req.parent, req.position));
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

    protected long writeRootJid(RootJid rootJid, long position)
            throws Exception {
        byte[] bytes = getBlock().serialize(rootJid);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int rem = bytes.length;
        if (position > -1) {
            this.position = position;
            rem -= fileChannel.write(byteBuffer, position);
        } else
            rem -= fileChannel.write(byteBuffer);
        while (rem > 0) {
            rem -= fileChannel.write(byteBuffer);
        }
        this.position += bytes.length;
        return this.position;
    }

    protected long forcedWriteRootJid(RootJid rootJid, long position)
            throws Exception {
        writeRootJid(rootJid, position);
        fileChannel.force(metaData);
        return this.position;
    }

    protected RootJid readRootJid(Mailbox mailbox, Actor parent, long position)
            throws Exception {
        int rem = getBlock().headerLength();
        byte[] hdr = new byte[rem];
        ByteBuffer hbb = ByteBuffer.wrap(hdr);
        int r;
        if (position > -1) {
            this.position = position;
            r = fileChannel.read(hbb, position);
        } else
            r = fileChannel.read(hbb);
        if (r == -1)
            throw new IOException("reached eof");
        rem -= r;
        while (rem > 0) {
            r = fileChannel.read(hbb);
            if (r == -1)
                throw new IOException("reached eof");
            rem -= r;
        }
        this.position += hdr.length;
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
        this.position += rjb.length;
        RootJid rootJid = block.deserialize(mailbox, parent, rjb);
        return rootJid;
    }
}
