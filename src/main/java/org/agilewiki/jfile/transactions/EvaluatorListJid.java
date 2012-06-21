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

import java.util.ArrayList;

/**
 * A list of transaction actor's.
 */
public class EvaluatorListJid extends ListJid implements Evaluator {
    private int ndx;
    private boolean sync;
    private boolean async;
    private RP _rp;

    public void eval(Eval req, final RP rp) throws Exception {
        if (_rp != null)
            throw new IllegalStateException("busy");
        ndx = 0;
        _rp = rp;
        eval((Eval) req);
    }

    private void eval(final Eval req)
            throws Exception {
        final ArrayList<Evaluator> evaluators = new ArrayList<Evaluator>(size());
        while (true) {
            if (ndx == size()) {
                RP rp = _rp;
                _rp = null;
                rp.processResponse(null);
                int i = 0;
                while (i < evaluators.size()) {
                    evaluators.get(i).sendTransactionResult();
                    i += 1;
                }
                return;
            }
            final Evaluator evaluator = (Evaluator) iGet(ndx);
            ndx += 1;
            sync = false;
            async = false;
            req.send(this, evaluator, new RP<Boolean>() {
                @Override
                public void processResponse(Boolean response) throws Exception {
                    if (response) evaluators.add(evaluator);
                    if (!async)
                        sync = true;
                    else
                        eval(req);
                }
            });
            if (!sync) {
                async = true;
                return;
            }
        }
    }

    public void sendTransactionResult()
            throws Exception {
    }
}
