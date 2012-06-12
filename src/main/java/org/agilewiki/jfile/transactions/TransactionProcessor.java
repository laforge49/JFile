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

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.block.Block;
import org.agilewiki.jfile.block.FBlock;
import org.agilewiki.jfile.transactions.db.Checkpoint;
import org.agilewiki.jid.scalar.vlens.actor.GetActor;
import org.agilewiki.jid.scalar.vlens.actor.RootJid;

/**
 * On receipt of a ProcessBlock request, the transaction processor
 * first sends an Eval request to the contents of the block and then sends
 * a Checkpoint request to the database.
 */
final public class TransactionProcessor extends JLPCActor implements BlockProcessor {
    public boolean generateCheckpoints = true;

    public void finish(RP rp)
            throws Exception {
        rp.processResponse(null);
    }

    public void processBlock(ProcessBlock req, RP rp) throws Exception {
        processBlock(req.block, rp);
    }

    /**
     * Process the contents of the block and then send a Checkpoint request.
     *
     * @param block The block holding the list of transactions.
     * @param rp    The RP used to signal completion.
     */
    private void processBlock(final Block block, final RP rp) throws Exception {
        RootJid rootJid = block.getRootJid();
        GetActor.req.send(this, rootJid, new RP<Actor>() {
            @Override
            public void processResponse(Actor response) throws Exception {
                Evaluator evaluater = (Evaluator) response;
                Eval eval = new Eval(block.getTimestamp());
                if (generateCheckpoints) {
                    eval.send(TransactionProcessor.this, evaluater, new RP<Object>() {
                        @Override
                        public void processResponse(Object response) throws Exception {
                            Checkpoint checkpoint = new Checkpoint(block.getCurrentPosition(), block.getTimestamp());
                            checkpoint.send(TransactionProcessor.this, getParent(), rp);
                        }
                    });
                } else {
                    eval.send(TransactionProcessor.this, evaluater, rp);
                }
            }
        });
    }
}
