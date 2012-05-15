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

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jactor.factory.JAFactoryFactory;
import org.agilewiki.jactor.factory.NewActor;
import org.agilewiki.jactor.factory.Requirement;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.block.Block;
import org.agilewiki.jfile.block.LTA32Block;
import org.agilewiki.jfile.transactions.*;
import org.agilewiki.jid.scalar.vlens.actor.RootJid;

/**
 * Aggregates transactions into blocks.
 */
public class TransactionAggregator extends BlockSource {
    public int initialCapacity = 10;
    private RootJid rootJid;
    private TransactionListJid transactionListJid;
    private boolean writePending;

    /**
     * Create a LiteActor
     *
     * @param mailbox A mailbox which may be shared with other actors.
     */
    public TransactionAggregator(Mailbox mailbox) {
        super(mailbox);
    }

    /**
     * Creates a new block.
     * @return A new block.
     */
    protected Block newBlock() {
        return new LTA32Block();
    }

    /**
     * Generates a timestamp.
     * @return A new timestamp.
     */
    protected long newTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * Returns the actor's requirements.
     *
     * @return The actor's requirents.
     */
    @Override
    final protected Requirement[] requirements() throws Exception {
        Requirement[] requirements = new Requirement[1];
        requirements[0] = new Requirement(
                new NewActor(""),
                new JAFactoryFactory(JAFactoryFactory.TYPE));
        return requirements;
    }

    /**
     * The application method for processing requests sent to the actor.
     *
     * @param request A request.
     * @param rp      The response processor.
     * @throws Exception Any uncaught exceptions raised while processing the request.
     */
    @Override
    final protected void processRequest(Object request, RP rp) throws Exception {
        if (request.getClass() == AggregateTransaction.class) {
            AggregateTransaction req = (AggregateTransaction) request;
            aggregateTransaction(req.actorType, req.actorFactory, req.bytes, rp);
            return;
        }

        throw new UnsupportedOperationException(request.getClass().getName());
    }

    private void aggregateTransaction(String actorType, ActorFactory actorFactory, byte[] bytes, final RP rp)
            throws Exception {
        makeRootJid();
        transactionListJid.iAdd(-1);
        TransactionActorJid transactionActorJid = (TransactionActorJid) transactionListJid.iGet(-1);
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
            TransactionResult.req.send(this, transactionActorJid.getValue(), rp);
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
        rootJid = new RootJid(getMailboxFactory().createMailbox());
        rootJid.setParent(getParent());
        rootJid.setValue(JFileFactories.TRANSACTION_LIST_JID_TYPE);
        transactionListJid = (TransactionListJid) rootJid.getValue();
        transactionListJid.initialCapacity = initialCapacity;
    }

    private void writeBlock()
            throws Exception {
        if (rootJid == null)
            return;
        final Block block = newBlock();
        block.setRootJid(rootJid);
        rootJid = null;
        block.setTimestamp(newTimestamp());
        writePending = true;
        (new ProcessBlock(block)).send(this, blockFlowBuffer, new RP<Object>() {
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
}
