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
import org.agilewiki.jid.scalar.vlens.actor.RootJid;

/**
 * A wrapper for data to be read from or written to disk.
 * The header added to the serialized data contains the length of the serialized
 * data and, optionally, a timestamp and checksum.
 */
public interface Block {
    /**
     * Reset the block and assign the RootJid.
     *
     * @param rootJid The RootJid to be assigned.
     */
    public void setRootJid(RootJid rootJid);

    /**
     * Serializes the header and the assigned RootJid.
     *
     * @return The bytes of the header and serialized RootJid.
     */
    public byte[] serialize()
            throws Exception;

    /**
     * The length of the header which prefaces the actual data on disk.
     *
     * @return The header length.
     */
    public int headerLength();

    /**
     * Returns the file name.
     *
     * @return The file name.
     */
    public String getFileName();

    /**
     * Assigns the file's name.
     */
    public void setFileName(String fileName);

    /**
     * Returns the file position.
     *
     * @return The file position.
     */
    public long getCurrentPosition();

    /**
     * Assigns the file's current position.
     */
    public void setCurrentPosition(long position);

    /**
     * Provides the raw header information read from disk.
     *
     * @param bytes The header bytes read from disk.
     * @return The length of the data following the header on disk.
     */
    public int setHeaderBytes(byte[] bytes);

    /**
     * Provides the data read from disk after the header.
     *
     * @param bytes The data following the header on disk.
     * @return True when the data is valid.
     */
    public boolean setRootJidBytes(byte[] bytes);

    /**
     * Get an existing RootJid.
     *
     * @return The RootJid.
     * @throws Exception An exception is thrown when there is no RootJid.
     */
    public RootJid getRootJid()
            throws Exception;

    /**
     * Return the RootJid, deserializing it as needed..
     *
     * @param mailbox The mailbox.
     * @param parent  The parent.
     * @return The RootJid, or null.
     */
    public RootJid getRootJid(Mailbox mailbox, Actor parent)
            throws Exception;

    /**
     * Indicates the absence of a root jic.
     *
     * @return True when a root jit is not present.
     */
    public boolean isEmpty();

    /**
     * Returns the timestamp.
     *
     * @return The timestamp.
     */
    public long getTimestamp();

    /**
     * Assigns the timestamp.
     *
     * @param timestamp The timestamp.
     */
    public void setTimestamp(long timestamp);
}
