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

import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.block.Block;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;

/**
 * Reads or writes a RootJid with just the length for a header.
 */
public class JFile extends JLPCActor {
    protected FileChannel fileChannel;
    public boolean metaData;
    private boolean written;
    private Path path;

    public void writeRootJid(Block block, int maxSize)
            throws Exception {
        written = true;
        byte[] bytes = block.serialize();
        if (maxSize > -1 && bytes.length > maxSize) {
            throw new Exception("" + bytes.length + " exceeds the maxSize of " + maxSize);
        }
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

    public void forceRootJid()
            throws Exception {
        written = false;
        fileChannel.force(metaData);
    }

    public void forcedWriteRootJid(Block block, int maxSize)
            throws Exception {
        writeRootJid(block, maxSize);
        forceRootJid();
    }

    public void forceBeforeWriteRootJid(Block block, int maxSize)
            throws Exception {
        if (written) {
            forceRootJid();
        }
        writeRootJid(block, maxSize);
    }

    public void readRootJid(Block block, int maxSize) {
        try {
            block.setRootJid(null);
            int rem = block.headerLength();
            byte[] hdr = new byte[rem];
            ByteBuffer hbb = ByteBuffer.wrap(hdr);
            long currentPosition = block.getCurrentPosition();
            int rl = fileChannel.read(hbb, currentPosition);
            if (rl == -1) {
                return;
            }
            currentPosition += rl;
            rem -= rl;
            while (rem > 0) {
                rl = fileChannel.read(hbb, currentPosition);
                if (rl == -1) {
                    System.out.println("eof2");
                    return;
                }
                currentPosition += rl;
                rem -= rl;
            }
            rem = block.setHeaderBytes(hdr);
            if (rem < 0) {
                System.out.println("eof3");
                return;
            }
            if (maxSize > -1 && rem > maxSize) {
                System.out.println("max size exceeded: " + rem);
                return;
            }
            byte[] rjb = new byte[rem];
            if (rem > 0) {
                ByteBuffer rjbb = ByteBuffer.wrap(rjb);
                while (rem > 0) {
                    rl = fileChannel.read(rjbb, currentPosition);
                    if (rl == -1) {
                        return;
                    }
                    currentPosition += rl;
                    rem -= rl;
                }
            }
            if (block.setRootJidBytes(rjb)) {
                block.setCurrentPosition(currentPosition);
            } else {
                System.out.println("eof5");
            }
        } catch (Exception ex) {
            System.out.println("eof6");
            block.setRootJid(null);
            return;
        }
        return;
    }

    public void open(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException {
        fileChannel = FileChannel.open(path, options, attrs);
    }

    public void open(Path path, OpenOption... options) throws IOException {
        fileChannel = FileChannel.open(path, options);
        this.path = path;
    }

    public void close() {
        path = null;
        try {
            fileChannel.close();
        } catch (Exception ex) {
        }
    }

    public String getFileName() {
        return path.getFileName().toString();
    }
}
