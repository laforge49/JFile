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

import org.agilewiki.jid.AppendableBytes;
import org.agilewiki.jid.Util;
import org.agilewiki.jid.scalar.vlens.actor.RootJid;

/**
 * A block with a length and a timestamp in the header.
 */
public class LTBlock extends LBlock {
    long timestamp;

    /**
     * Reset the block and assign the RootJid.
     *
     * @param rootJid The RootJid to be assigned.
     */
    @Override
    public void setRootJid(RootJid rootJid) {
        super.setRootJid(rootJid);
        timestamp = 0L;
    }

    /**
     * The length of the header which prefaces the actual data on disk.
     *
     * @return The header length.
     */
    @Override
    public int headerLength() {
        return super.headerLength() + Util.LONG_LENGTH;
    }

    /**
     * Provides the raw header information to be written to disk.
     *
     * @param ab Append the data to this.
     * @param l  The length of the data.
     */
    @Override
    protected void saveHeader(AppendableBytes ab, int l)
            throws Exception {
        if (timestamp == 0)
            throw new IllegalStateException("timestamp not set");
        super.saveHeader(ab, l);
        ab.writeLong(timestamp);
    }

    /**
     * Provides the raw header information read from disk.
     *
     * @param bytes The header bytes read from disk.
     * @return The length of the data following the header on disk.
     */
    @Override
    public int setHeaderBytes(byte[] bytes) {
        int l = super.setHeaderBytes(bytes);
        timestamp = rb.readLong();
        return l;
    }

    /**
     * Returns the timestamp.
     *
     * @return The timestamp.
     */
    @Override
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Assigns the timestamp.
     *
     * @param timestamp The timestamp.
     */
    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
