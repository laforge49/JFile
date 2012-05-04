package org.agilewiki.jfile.block;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jid._Jid;
import org.agilewiki.jid.scalar.vlens.actor.RootJid;

/**
 * A wrapper for data to be read from or written to disk.
 * The header added to the serialized data contains the length of the serialized
 * data and, optionally, a timestamp and checksum.
 */
public interface Block {
    /**
     * Convert a RootJid to a byte array that is prefaced by a header.
     * @param rootJid The RootJid to be serialized.
     * @return A byte array containing both a header and the serialized RootJid.
     */
    public byte[] serialize(RootJid rootJid) 
            throws Exception;
    
    /**
     * The length of the header which prefaces the actual data on disk.
     * @return The header length.
     */
    public int headerLength();

    /**
     * Provides the raw header information.
     * 
     * @param bytes The header bytes read from disk.
     * @return The length of the data following the header on disk.
     */
    public int setHeader(byte[] bytes);

    /**
     * Deserialize the data following the header on disk.
     * @param mailbox The mailbox.
     * @param parent The parent.
     * @param bytes The data following the header on disk.
     * @return The deserialized RootJid.
     */
    public RootJid deserialize(Mailbox mailbox, Actor parent, byte[] bytes)
            throws Exception;
}
