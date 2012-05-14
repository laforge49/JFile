package org.agilewiki.jfile.transactions.transactionLogger;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jactor.factory.JAFactoryFactory;
import org.agilewiki.jactor.factory.NewActor;
import org.agilewiki.jactor.factory.Requirement;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.ForcedWriteRootJid;
import org.agilewiki.jfile.JFileFactories;
import org.agilewiki.jfile.block.Block;
import org.agilewiki.jfile.block.LTA32Block;
import org.agilewiki.jfile.transactions.*;
import org.agilewiki.jid.scalar.vlens.actor.RootJid;

/**
 * Aggregates transactions into blocks.
 */
public class TransactionAggregator extends JLPCActor implements _TransactionAggregator {
    public BlockProcessor next;
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

    protected Block newBlock() {
        return new LTA32Block();
    }

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
            processTransaction(req.actorType, req.actorFactory, req.bytes, rp);
            return;
        }

        throw new UnsupportedOperationException(request.getClass().getName());
    }

    private void processTransaction(String actorType, ActorFactory actorFactory, byte[] bytes, final RP rp)
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
        final Block block = newBlock();
        block.setRootJid(rootJid);
        rootJid = null;
        block.setTimestamp(newTimestamp());
        writePending = true;
        (new ForcedWriteRootJid(block)).send(this, next, new RP<Object>() {
            @Override
            public void processResponse(Object response)
                    throws Exception {
                writePending = false;
            }
        });
        getMailbox().sendPendingMessages();
    }
}
