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
package org.agilewiki.jfile.transactions;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jid.collection.vlenc.ListJid;

/**
 * A list of transaction actor's.
 */
public class TransactionListJid extends ListJid implements Transaction {
    private int ndx;
    private boolean sync;
    private boolean async;

    /**
     * Create a ListJid
     *
     * @param mailbox A mailbox which may be shared with other actors.
     */
    public TransactionListJid(Mailbox mailbox) {
        super(mailbox);
    }

    /**
     * The application method for processing requests sent to the actor.
     *
     * @param request A request.
     * @param rp      The response processor.
     * @throws Exception Any uncaught exceptions raised while processing the request.
     */
    @Override
    protected void processRequest(Object request, RP rp) throws Exception {
        if (request.getClass() == Eval.class) {
            ndx = 0;
            eval((Eval) request, rp);
            return;
        }

        super.processRequest(request, rp);
    }

    private void eval(final Eval req, final RP rp)
            throws Exception {
        while (true) {
            if (ndx == size()) {
                rp.processResponse(null);
                return;
            }
            Transaction transaction = (Transaction) iGet(ndx);
            ndx += 1;
            sync = false;
            async = true;
            req.send(this, transaction, new RP<Object>() {
                @Override
                public void processResponse(Object response) throws Exception {
                    if (!async)
                        sync = true;
                    else
                        eval(req, rp);
                }
            });
            if (!sync) {
                async = true;
                return;
            }
        }
    }
}
