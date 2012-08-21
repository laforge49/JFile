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

import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;

/**
 * Generates unique timestamps, within limits.
 * Useful when you need a few timestamps for the same millisecond.
 */
public class UniqueClock extends JLPCActor {
    private static UniqueClock uniqueClock;

    public static UniqueClock uc(MailboxFactory mailboxFactory)
            throws Exception {
        if (uniqueClock == null) {
            uniqueClock = new UniqueClock();
            uniqueClock.initialize(mailboxFactory.createMailbox());
        }
        return uniqueClock;
    }

    private long oldts;
    private long olduts;

    private UniqueClock() {}

    void uniqueTimestamp(RP rp)
            throws Exception {
        long ts = System.currentTimeMillis();
        if (ts == oldts) olduts +=1;
        else {
            oldts = ts;
            olduts = ts << 4;
        }
        rp.processResponse(olduts);
    }
}
