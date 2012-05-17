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
import org.agilewiki.jactor.lpc.JLPCActor;

/**
 * Buffers the flow of blocks for increased parallelism.
 */
public class BlockFlowBuffer extends JLPCActor implements BlockProcessor {
    private ProcessBlock pendingRequest;
    private RP _rp;
    private boolean responsePending;
    public BlockProcessor next;
    
    /**
     * Create a LiteActor
     *
     * @param mailbox A mailbox which may be shared with other actors.
     */
    public BlockFlowBuffer(Mailbox mailbox) {
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
        Class reqClass = request.getClass();

        if (reqClass == ProcessBlock.class) {
            pendingRequest = (ProcessBlock) request;
            _rp = rp;
            nextSend();
            return;
        }

        if (reqClass == Finish.class) {
            Finish.req.send(this, next, rp);
            return;
        }

        throw new UnsupportedOperationException(reqClass.getName());
    }
    
    private void nextSend() 
            throws Exception {
        if (responsePending || pendingRequest == null)
            return;
        responsePending = true;
        ProcessBlock req = pendingRequest;
        pendingRequest = null;
        if (_rp != null) {
            _rp.processResponse(null);
            _rp = null;
        }
        req.send(this, next, new RP<Object>() {
            @Override
            public void processResponse(Object response) throws Exception {
                responsePending = false;
                nextSend();
            }
        });
    }
}
