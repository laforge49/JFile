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
package org.agilewiki.jfile.transactions.db.inMemory;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jfile.transactions._StringTransactionJid;

/**
 * Returns an Integer.
 */
public class GetIntegerTransaction extends _StringTransactionJid {
    public static byte[] bytes(Mailbox mailbox, String key)
            throws Exception {
        GetIntegerTransaction git = new GetIntegerTransaction(mailbox, key);
        return git.getSerializedBytes();
    }

    public GetIntegerTransaction() {
    }

    public GetIntegerTransaction(Mailbox mailbox, String key)
            throws Exception {
        initialize(mailbox);
        setValue(key);
    }

    @Override
    protected void eval(long blockTimestamp, RP rp) throws Exception {
        IMDB imdb = (IMDB) getAncestor(IMDB.class);
        rp.processResponse(imdb.getInteger(getValue()));
    }
}
