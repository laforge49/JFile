package org.agilewiki.jfile.transactions.db.inMemory;

import org.agilewiki.jactor.RP;
import org.agilewiki.jfile.ForceBeforeWriteRootJid;
import org.agilewiki.jfile.JFile;
import org.agilewiki.jfile.block.Block;
import org.agilewiki.jfile.block.LTA32Block;
import org.agilewiki.jfile.transactions.db.DB;
import org.agilewiki.jid.JidFactories;
import org.agilewiki.jid._Jid;
import org.agilewiki.jid.collection.vlenc.map.StringMapJid;
import org.agilewiki.jid.scalar.flens.lng.LongJid;
import org.agilewiki.jid.scalar.vlens.actor.ActorJid;
import org.agilewiki.jid.scalar.vlens.actor.RootJid;

/**
 * In-memory database.
 */
public class IMDB extends DB {
    private JFile dbFile;
    private RootJid rootJid;
    private StringMapJid stringMapJid;
    private LongJid logPositionJid;
    private boolean pendingWrite;
    private boolean isFirstRootJid;
    public int maxSize;

    public JFile getDbFile()
            throws Exception {
        if (dbFile == null) {
            dbFile = new JFile();
            dbFile.initialize(getMailboxFactory().createAsyncMailbox());
        }
        return dbFile;
    }

    protected Block newDbBlock() {
        return new LTA32Block();
    }

    protected RootJid makeRootJid() throws Exception {
        if (rootJid == null) {
            rootJid = new RootJid();
            rootJid.initialize(getMailboxFactory().createMailbox(), getParent());
        }
        return rootJid;
    }

    protected StringMapJid makeStringMapJid() throws Exception {
        if (stringMapJid == null) {
            RootJid rj = makeRootJid();
            stringMapJid = (StringMapJid) rj.getValue();
            if (stringMapJid == null) {
                rj.setValue(JidFactories.STRING_ACTOR_MAP_JID_TYPE);
                stringMapJid = (StringMapJid) rj.getValue();
            }
        }
        return stringMapJid;
    }

    protected ActorJid makeActorJid(String key) throws Exception {
        StringMapJid smj = makeStringMapJid();
        ActorJid actorJid = (ActorJid) smj.kGet(key);
        if (actorJid == null) {
            smj.kMake(key);
            actorJid = (ActorJid) smj.kGet(key);
        }
        return actorJid;
    }

    protected LongJid makeLongJid(String key) throws Exception {
        ActorJid aj = makeActorJid(key);
        LongJid longJid = (LongJid) aj.getValue();
        if (longJid == null) {
            aj.setValue(JidFactories.LONG_JID_TYPE);
            longJid = (LongJid) aj.getValue();
        }
        return longJid;
    }

    public static final String LOG_POSITION = "$$LOG_POSITION";

    public void checkpoint(long logPosition, long timestamp, RP rp)
            throws Exception {
        if (!pendingWrite) {
            LongJid lj = makeLongJid(LOG_POSITION);
            lj.setValue(logPosition);
            pendingWrite = true;
            Block block = newDbBlock();
            block.setTimestamp(timestamp);
            if (isFirstRootJid) {
                isFirstRootJid = false;
                block.setCurrentPosition(maxSize);
            } else {
                isFirstRootJid = true;
                block.setCurrentPosition(0L);
            }
            block.setRootJid((RootJid) rootJid.copyJID(getMailboxFactory().createMailbox()));
            (new ForceBeforeWriteRootJid(block)).send(this, dbFile, new RP<Object>() {
                @Override
                public void processResponse(Object response) throws Exception {
                    pendingWrite = false;
                }
            });
        }
        rp.processResponse(null);
    }
}
