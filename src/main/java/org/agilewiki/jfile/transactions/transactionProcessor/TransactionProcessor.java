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
package org.agilewiki.jfile.transactions.transactionProcessor;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.block.Block;
import org.agilewiki.jfile.transactions.Eval;
import org.agilewiki.jfile.transactions.Transaction;
import org.agilewiki.jfile.transactions.db.Checkpoint;
import org.agilewiki.jid.scalar.vlens.actor.GetActor;
import org.agilewiki.jid.scalar.vlens.actor.RootJid;

/**
 * On receipt of a ProcessBlock request, the transaction processor
 * first sends an Eval request to the contents of the block and then sends
 * a Checkpoint request to the database.
 * <p/>
 * ProcessBlock requests should only be sent on completion of the previous ProcessBlock request.
 */
final public class TransactionProcessor extends JLPCActor implements _TransactionProcessor {
    /**
     * Create a LiteActor
     *
     * @param mailbox A mailbox which may be shared with other actors.
     */
    public TransactionProcessor(Mailbox mailbox) {
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
        if (request.getClass() == ProcessBlock.class) {
            ProcessBlock req = (ProcessBlock) request;
            processBlock(req.isRestart, req.block, rp);
            return;
        }

        throw new UnsupportedOperationException(request.getClass().getName());
    }

    /**
     * Process the contents of the block and then send a Checkpoint request.
     *
     * @param block The block holding the list of transactions.
     * @param rp    The RP used to signal completion.
     */
    private void processBlock(final boolean isRestart, final Block block, final RP rp) throws Exception {
        RootJid rootJid = block.getRootJid();
        GetActor.req.send(this, rootJid, new RP<Actor>() {
            @Override
            public void processResponse(Actor response) throws Exception {
                Transaction transaction = (Transaction) response;
                Eval eval = new Eval(isRestart);
                eval.send(TransactionProcessor.this, transaction, new RP<Object>() {
                    @Override
                    public void processResponse(Object response) throws Exception {
                        Checkpoint checkpoint = new Checkpoint(block.getCurrentPosition(), block.getTimestamp());
                        checkpoint.send(TransactionProcessor.this, getParent(), rp);
                    }
                });
            }
        });
    }
}
