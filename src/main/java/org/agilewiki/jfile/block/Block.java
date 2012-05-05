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
     * Convert a RootJid to a byte array that is prefaced by a header.
     *
     * @param rootJid The RootJid to be serialized.
     * @return A byte array containing both a header and the serialized RootJid.
     */
    public byte[] serialize(RootJid rootJid)
            throws Exception;

    /**
     * The length of the header which prefaces the actual data on disk.
     *
     * @return The header length.
     */
    public int headerLength();

    /**
     * Returns the headerLength() + the length of the serialized rootJid.
     *
     * @return The headerLength() + the length of the serialized rootJid.
     */
    public int totalLength();

    /**
     * Provides the raw header information.
     *
     * @param bytes The header bytes read from disk.
     * @return The length of the data following the header on disk.
     */
    public int setHeader(byte[] bytes);

    /**
     * Provides the data following the header on disk.
     *
     * @param bytes The data following the header on disk.
     * @return True when the data is valid.
     */
    public boolean setRootJidBytes(byte[] bytes);

    /**
     * Deserialize the RootJid.
     *
     * @param mailbox The mailbox.
     * @param parent  The parent.
     * @return The deserialized RootJid.
     */
    public RootJid rootJid(Mailbox mailbox, Actor parent)
            throws Exception;
}
