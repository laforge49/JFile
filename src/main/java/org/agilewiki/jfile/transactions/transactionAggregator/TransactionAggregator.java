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
package org.agilewiki.jfile.transactions.transactionAggregator;

import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.UniqueClock;
import org.agilewiki.jfile.UniqueTimestamp;
import org.agilewiki.jfile.block.Block;
import org.agilewiki.jfile.block.LTA32Block;
import org.agilewiki.jfile.transactions.*;
import org.agilewiki.jid.scalar.vlens.actor.RootJid;

/**
 * Aggregates transactions into blocks.
 */
public class TransactionAggregator extends BlockSource {
    private RootJid rootJid;
    private EvaluatorListJid transactionListJid;
    private boolean writePending;

    /**
     * Creates a new block.
     * @return A new block.
     */
    protected Block newBlock() {
        return new LTA32Block();
    }

    public void aggregateTransaction(String actorType, ActorFactory actorFactory, byte[] bytes, final RP rp)
            throws Exception {
        makeRootJid();
        transactionListJid.iAdd(-1);
        EvaluatorActorJid transactionActorJid = (EvaluatorActorJid) transactionListJid.iGet(-1);
        if (actorType != null) {
            if (bytes == null) {
                transactionActorJid.setValue(actorType);
            } else {
                transactionActorJid.setJidBytes(actorType, bytes);
            }
        } else {
            if (bytes == null) {
                transactionActorJid.setValue(actorFactory);
            } else {
                transactionActorJid.setJidBytes(actorFactory, bytes);
            }
        }
        if (!rp.isEvent()) {
            GetTransactionResult.req.send(this, transactionActorJid.getValue(), rp);
            getMailbox().sendPendingMessages();
        }
        if (!writePending) {
            writeBlock();
        }
    }

    private void makeRootJid()
            throws Exception {
        if (rootJid != null)
            return;
        rootJid = new RootJid();
        rootJid.initialize(getMailboxFactory().createMailbox(), getParent());
        rootJid.setValue(JFileFactories.EVALUATER_LIST_JID_TYPE);
        transactionListJid = (EvaluatorListJid) rootJid.getValue();
    }

    private void writeBlock()
            throws Exception {
        if (rootJid == null)
            return;
        final Block block = newBlock();
        block.setRootJid(rootJid);
        rootJid = null;
        writePending = true;
        UniqueClock uc = UniqueClock.uc(getMailboxFactory());
        UniqueTimestamp.req.send(this, uc, new RP<Long>() {
            public void processResponse(Long response)
                    throws Exception {
                block.setTimestamp(response);
                (new ProcessBlock(block)).send(TransactionAggregator.this, blockFlowBuffer, new RP<Object>() {
                    @Override
                    public void processResponse(Object response)
                            throws Exception {
                        writePending = false;
                        if (getMailbox().isEmpty())
                            writeBlock();
                    }
                });
                getMailbox().sendPendingMessages();
            }
        });
    }
}
