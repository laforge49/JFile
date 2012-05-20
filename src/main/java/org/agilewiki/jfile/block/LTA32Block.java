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

import java.util.zip.Adler32;

/**
 * A block with a length, timestamp and Adler32 checksum in the header.
 */
public class LTA32Block extends LTBlock {
    Adler32 a32 = new Adler32();
    long checksum;

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
        super.saveHeader(ab, l);
        a32.reset();
        a32.update(blockBytes, headerLength(), blockBytes.length - headerLength());
        ab.writeLong(a32.getValue());
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
        checksum = rb.readLong();
        return l;
    }

    /**
     * Provides the data read from disk after the header.
     *
     * @param bytesRead The data following the header on disk.
     * @return True when the data is valid.
     */
    @Override
    public boolean setRootJidBytes(byte[] bytesRead) {
        if (!super.setRootJidBytes(bytesRead))
            return false;
        int i = 0;
        while (i < bytesRead.length) {
            i += 1;
        }
        a32.reset();
        a32.update(bytesRead);
        boolean match = checksum == a32.getValue();
        if (match)
            return true;
        System.out.println("bad checksum");
        rootJidBytes = null;
        return false;
    }
}
