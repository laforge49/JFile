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
    private long currentPosition;
    protected ReadableBytes rb;
    int l;
    protected byte[] bytes;

    /**
     * Convert a RootJid to a byte array that is prefaced by a header.
     *
     * @param rootJid The RootJid to be serialized.
     * @return A byte array containing both a header and the serialized RootJid.
     */
    @Override
    public byte[] serialize(RootJid rootJid)
            throws Exception {
        l = rootJid.getSerializedLength();
        byte[] bytes = new byte[headerLength() + l];
        AppendableBytes ab = new AppendableBytes(bytes, 0);
        saveHeader(ab, l);
        rootJid.save(ab);
        return bytes;
    }

    /**
     * Provides the raw header information to be written to disk.
     *
     * @param ab Append the data to this.
     * @param l  The length of the data.
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
     * Returns the file position.
     *
     * @return The file position.
     */
    public long getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Assigns the files current position.
     */
    public void setCurrentPosition(long position) {
        currentPosition = position;
    }

    /**
     * Provides the raw header information read from disk.
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
     * Provides the data read from disk after the header.
     *
     * @param bytes The data following the header on disk.
     * @return True when the data is valid.
     */
    public boolean setRootJidBytes(byte[] bytes) {
        this.bytes = bytes;
        if (l != bytes.length)
            return false;
        return true;
    }

    /**
     * Deserialize the RootJid.
     *
     * @param mailbox The mailbox.
     * @param parent  The parent.
     * @return The deserialized RootJid.
     */
    public RootJid rootJid(Mailbox mailbox, Actor parent)
            throws Exception {
        rb = null;
        RootJid rootJid = new RootJid(mailbox);
        rootJid.setParent(parent);
        rootJid.load(new ReadableBytes(bytes, 0));
        bytes = null;
        return rootJid;
    }

    /**
     * Returns the timestamp assigned when the RootJid was written.
     *
     * @return The timestamp assigned when the RootJid was written.
     */
    public long getTimestamp() {
        throw new UnsupportedOperationException();
    }
}
