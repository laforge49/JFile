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
package org.agilewiki.jfile.block;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jid.AppendableBytes;
import org.agilewiki.jid.ReadableBytes;
import org.agilewiki.jid.Util;
import org.agilewiki.jid.scalar.vlens.actor.RootJid;

/**
 * A block with a length in the header.
 * --A minimal block implementation.
 */
public class LBlock implements Block {
    ReadableBytes rb;
    int l;
    
    /**
     * Convert a RootJid to a byte array that is prefaced by a header.
     *
     * @param rootJid The RootJid to be serialized.
     * @return A byte array containing both a header and the serialized RootJid.
     */
    @Override
    public byte[] serialize(RootJid rootJid) 
            throws Exception {
        int l = rootJid.getSerializedLength();
        byte[] bytes = new byte[headerLength() + l];
        AppendableBytes ab = new AppendableBytes(bytes, 0);
        saveHeader(ab, l);
        rootJid.save(ab);
        return bytes;
    }

    /**
     * Serialize the header.
     * @param ab Append the data to this.
     * @param l The length of the data.
     */
    protected void saveHeader(AppendableBytes ab, int l) {
        ab.writeInt(l);
    }

    /**
     * The length of the header which prefaces the actual data on disk.
     *
     * @return The header length.
     */
    @Override
    public int headerLength() {
        return Util.INT_LENGTH;
    }

    /**
     * Provides the raw header information.
     *
     * @param bytes The header bytes read from disk.
     * @return The length of the data following the header on disk.
     */
    @Override
    public int setHeader(byte[] bytes) {
        rb = new ReadableBytes(bytes, 0);
        l = rb.readInt();
        return l;
    }

    /**
     * Deserialize the data following the header on disk.
     * @param mailbox The mailbox.
     * @param parent The parent.
     * @param bytes The data following the header on disk.
     * @return The deserialized RootJid.
     */
    @Override
    public RootJid deserialize(Mailbox mailbox, Actor parent, byte[] bytes)
            throws Exception {
        validate(bytes);
        rb = null;
        RootJid rootJid = new RootJid(mailbox);
        rootJid.setParent(parent);
        rootJid.load(new ReadableBytes(bytes, 0));
        return rootJid;
    }

    /**
     * Validate the data.
     *
     * @param bytes The data following the header on disk.
     */
    protected void validate(byte[] bytes)
            throws Exception {
        if (rb == null)
            throw new Exception("setHeader must be called before deserialize");
        if (l != bytes.length)
            throw new Exception("bytes.length is not " + l);
    }
}
