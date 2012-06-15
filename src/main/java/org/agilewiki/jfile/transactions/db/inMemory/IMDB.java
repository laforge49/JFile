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
import org.agilewiki.jid.scalar.flens.bool.BooleanJid;
import org.agilewiki.jid.scalar.flens.dbl.DoubleJid;
import org.agilewiki.jid.scalar.flens.flt.FloatJid;
import org.agilewiki.jid.scalar.flens.integer.IntegerJid;
import org.agilewiki.jid.scalar.flens.lng.LongJid;
import org.agilewiki.jid.scalar.vlens.actor.ActorJid;
import org.agilewiki.jid.scalar.vlens.actor.RootJid;
import org.agilewiki.jid.scalar.vlens.bytes.BytesJid;
import org.agilewiki.jid.scalar.vlens.string.StringJid;

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

    public StringMapJid makeStringMapJid() throws Exception {
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

    public ActorJid makeActorJid(String key) throws Exception {
        StringMapJid smj = makeStringMapJid();
        ActorJid actorJid = (ActorJid) smj.kGet(key);
        if (actorJid == null) {
            smj.kMake(key);
            actorJid = (ActorJid) smj.kGet(key);
        }
        return actorJid;
    }

    public _Jid makeJid(String key, String factoryName) throws Exception {
        ActorJid aj = makeActorJid(key);
        _Jid jid = aj.getValue();
        if (jid == null) {
            aj.setValue(factoryName);
            jid = aj.getValue();
        }
        return jid;
    }

    public BooleanJid makeBooleanJid(String key) throws Exception {
        return (BooleanJid) makeJid(key, JidFactories.BOOLEAN_JID_TYPE);
    }

    public IntegerJid makeIntegerJid(String key) throws Exception {
        return (IntegerJid) makeJid(key, JidFactories.INTEGER_JID_TYPE);
    }

    public LongJid makeLongJid(String key) throws Exception {
        return (LongJid) makeJid(key, JidFactories.LONG_JID_TYPE);
    }

    public FloatJid makeFloatJid(String key) throws Exception {
        return (FloatJid) makeJid(key, JidFactories.FLOAT_JID_TYPE);
    }

    public DoubleJid makeDoubleJid(String key) throws Exception {
        return (DoubleJid) makeJid(key, JidFactories.DOUBLE_JID_TYPE);
    }

    public StringJid makeStringJid(String key) throws Exception {
        return (StringJid) makeJid(key, JidFactories.STRING_JID_TYPE);
    }

    public BytesJid makeBytesJid(String key) throws Exception {
        return (BytesJid) makeJid(key, JidFactories.BYTES_JID_TYPE);
    }

    public static final String LOG_POSITION = "$$LOG_POSITION";
    public static final String LOG_FILE_NAME = "$$LOG_FILE_NAME";

    public void checkpoint(long logPosition, long timestamp, String logFileName, RP rp)
            throws Exception {
        if (!pendingWrite) {
            StringJid sj = makeStringJid(LOG_FILE_NAME);
            sj.setValue(logFileName);
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
            (new ForceBeforeWriteRootJid(block, maxSize)).send(this, dbFile, new RP<Object>() {
                @Override
                public void processResponse(Object response) throws Exception {
                    pendingWrite = false;
                }
            });
        }
        rp.processResponse(null);
    }
}
