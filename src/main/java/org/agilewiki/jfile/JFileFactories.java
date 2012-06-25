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

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.transactions.EvaluatorActerJidFactory;
import org.agilewiki.jfile.transactions.EvaluatorListJidFactory;
import org.agilewiki.jfile.transactions.db.inMemory.GetIntegerTransactionFactory;
import org.agilewiki.jfile.transactions.db.inMemory.IncrementIntegerTransactionFactory;

/**
 * Defines JFactory actor types and registers the factories.
 */
public class JFileFactories extends JLPCActor {

    public final static String EVALUATER_ACTOR_JID_TYPE = "EVALUATER_ACTOR_JID";
    public final static String EVALUATER_LIST_JID_TYPE = "EVALUATER_LIST_JID";
    public final static String GET_INTEGER_TRANSACTION = "GET_INTEGER_TRANSACTION";
    public final static String INCREMENT_INTEGER_TRANSACTION = "INCREMENT_INTEGER_TRANSACTION";

    /**
     * Process the requirements and assign the parent actor.
     * Once assigned, it can not be changed.
     *
     * @param mailbox A mailbox which may be shared with other actors.
     * @param parent  The parent actor.
     * @param actorFactory The factory.
     */
    @Override
    public void initialize(Mailbox mailbox, Actor parent, ActorFactory actorFactory)
            throws Exception {
        if (parent == null) {
            parent = new JAFactory();
            ((JAFactory) parent).initialize(mailbox);
        }
        super.initialize(mailbox, parent, actorFactory);

        Actor f = parent;
        while (!(f instanceof JAFactory)) f = f.getParent();
        JAFactory factory = (JAFactory) f;

        factory.registerActorFactory(EvaluatorActerJidFactory.fac);
        factory.registerActorFactory(new EvaluatorListJidFactory(EVALUATER_LIST_JID_TYPE));
        factory.registerActorFactory(GetIntegerTransactionFactory.fac);
        factory.registerActorFactory(IncrementIntegerTransactionFactory.fac);
    }
}
