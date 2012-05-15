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
package org.agilewiki.jfile.transactions.db;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.JAFactoryFactory;
import org.agilewiki.jactor.factory.NewActor;
import org.agilewiki.jactor.factory.Requirement;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.transactions.DurableTransactionLogger;
import org.agilewiki.jfile.transactions.Serializer;
import org.agilewiki.jfile.transactions.TransactionProcessor;
import org.agilewiki.jfile.transactions.transactionAggregator.TransactionAggregator;

import java.nio.file.Path;

/**
 * A database must handle checkpoint requests.
 */
abstract public class DB extends JLPCActor {
    private TransactionAggregator transactionAggregator;
    private TransactionProcessor transactionProcessor;
    private DurableTransactionLogger durableTransactionLogger;
    
    /**
     * Create a LiteActor
     *
     * @param mailbox A mailbox which may be shared with other actors.
     */
    public DB(Mailbox mailbox) {
        super(mailbox);
    }

    /**
     * Create a transaction aggregator.
     * @param mailbox A mailbox which may be shared with other actors.
     * @return A TransactionAggregator.
     */
    protected TransactionAggregator newTransactionAggregator(Mailbox mailbox) {
        return new TransactionAggregator(mailbox);
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
    
    public TransactionAggregator getTransactionAggregator(int initialCapacity)
            throws Exception {
        if (transactionAggregator != null) {
            return transactionAggregator;
        }

        Actor parent = getParent();
        if (parent == null) {
            throw new IllegalStateException("call setParent before getTransactionAggregator");
        }

        transactionProcessor = new TransactionProcessor(getMailbox());
        transactionProcessor.setParent(this);

        durableTransactionLogger = new DurableTransactionLogger(getMailboxFactory().createAsyncMailbox());
        durableTransactionLogger.setParent(parent);
        durableTransactionLogger.setNext(transactionProcessor);

        Serializer serializer = new Serializer(getMailboxFactory().createAsyncMailbox());
        serializer.setParent(parent);
        serializer.setNext(durableTransactionLogger);

        transactionAggregator = newTransactionAggregator(getMailboxFactory().createAsyncMailbox());
        transactionAggregator.setParent(this);
        transactionAggregator.setNext(serializer);
        transactionAggregator.initialCapacity = 10000;

        return transactionAggregator;
    }
}
