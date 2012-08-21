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
import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jfile.transactions._TupleTransactionJid;
import org.agilewiki.jid.scalar.flens.integer.IntegerJid;
import org.agilewiki.jid.scalar.flens.integer.IntegerJidFactory;
import org.agilewiki.jid.scalar.vlens.string.StringJid;
import org.agilewiki.jid.scalar.vlens.string.StringJidFactory;

/**
 * Adds to an integer and returns a new value.
 */
public class AddIntegerTransaction extends _TupleTransactionJid {
    private static ActorFactory afs[] = {StringJidFactory.fac, IntegerJidFactory.fac};

    public static byte[] bytes(Mailbox mailbox, String key, Integer increment)
            throws Exception {
        AddIntegerTransaction ait = new AddIntegerTransaction(mailbox, key, increment);
        return ait.getSerializedBytes();
    }

    public AddIntegerTransaction() {}

    public AddIntegerTransaction(Mailbox mailbox, String key, Integer increment)
            throws Exception {
        initialize(mailbox);
        getKeyJid().setValue(key);
        getIncrementJid().setValue(increment);
    }

    protected ActorFactory[] getTupleFactories() throws Exception {
        return afs;
    }

    protected StringJid getKeyJid()
            throws Exception {
        return (StringJid) iGet(0);
    }

    protected IntegerJid getIncrementJid()
            throws Exception {
        return (IntegerJid) iGet(1);
    }

    @Override
    protected void eval(long blockTimestamp, RP rp) throws Exception {
        IMDB imdb = (IMDB) getAncestor(IMDB.class);
        Integer nv = imdb.addInteger(getKeyJid().getValue(), getIncrementJid().getValue());
        rp.processResponse(nv);
    }
}
