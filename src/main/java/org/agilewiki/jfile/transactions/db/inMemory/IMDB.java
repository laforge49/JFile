package org.agilewiki.jfile.transactions.db.inMemory;

import org.agilewiki.jactor.ExceptionHandler;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.factory.JAFactory;
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

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * In-memory database.
 */
public class IMDB extends DB {

    public static final String LOG_POSITION = "$$LOG_POSITION";
    public static final String LOG_FILE_NAME = "$$LOG_FILE_NAME";

    private JFile dbFile;
    private RootJid rootJid;
    private StringMapJid stringMapJid;
    private boolean pendingWrite;
    private boolean isFirstRootJid;
    public int maxSize;

    @Override
    public void openDbFile(int logReaderMaxSize, RP rp)
            throws Exception {
        if (dbFile == null) {
            dbFile = new JFile();
            dbFile.initialize(getMailboxFactory().createAsyncMailbox());
        }
        Path dbPath = directoryPath.resolve("imdb.jadb");
        dbFile.open(
                dbPath,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);
        initializeDb(logReaderMaxSize);
        Block block0 = newDbBlock();
        block0.setCurrentPosition(0L);
        block0.setFileName(dbPath.toString());
        dbFile.readRootJid(block0, maxSize);
        RootJid rootJid0 = block0.getRootJid(getMailboxFactory().createMailbox(), getParent());
        long timestamp0 = block0.getTimestamp();
        System.out.println("rootJid0 " + rootJid0);
        Block block1 = newDbBlock();
        block1.setCurrentPosition(maxSize);
        block1.setFileName(dbPath.toString());
        dbFile.readRootJid(block1, maxSize);
        RootJid rootJid1 = block1.getRootJid(getMailboxFactory().createMailbox(), getParent());
        long timestamp1 = block0.getTimestamp();
        System.out.println("rootJid1 " + rootJid1);
        if (rootJid0 == null) {
            rootJid = rootJid1;
            isFirstRootJid = false;
        } else if (rootJid1 == null) {
            rootJid = rootJid0;
            isFirstRootJid = true;
        } else if (timestamp0 < timestamp1) {
            rootJid = rootJid1;
            isFirstRootJid = false;
        } else {
            rootJid = rootJid0;
            isFirstRootJid = true;
        }
        Long position = getLong(LOG_POSITION);
        if (position == null) {
            processLogFile(0L, 0, rp);
            return;
        }
        String logFileName = getString(LOG_FILE_NAME);
        int fileIndex = 0;
        while (fileIndex < logFileNames.length && !logFileNames[fileIndex].equals(logFileName))
            fileIndex += 1;
        if (fileIndex == logFileNames.length)
            throw new IllegalStateException("Missing log file: " + logFileName);
        processLogFile(position, fileIndex, rp);
    }

    @Override
    public void closeDbFile() {
        super.closeDbFile();
        dbFile.close();
    }

    protected Block newDbBlock() {
        return new LTA32Block();
    }

    protected RootJid makeRootJid() throws Exception {
        if (rootJid == null) {
            JAFactory factory = (JAFactory) getAncestor(JAFactory.class);
            rootJid = (RootJid) factory.newActor(
                    JidFactories.ROOT_JID_TYPE,
                    getMailboxFactory().createMailbox(),
                    getParent());
        }
        return rootJid;
    }

    public StringMapJid getStringMapJid()
            throws Exception {
        if (rootJid == null)
            return null;
        if (stringMapJid == null) {
            stringMapJid = (StringMapJid) rootJid.getValue();
        }
        return stringMapJid;
    }

    public StringMapJid makeStringMapJid()
            throws Exception {
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

    public ActorJid getActorJid(String key)
            throws Exception {
        StringMapJid smj = getStringMapJid();
        if (smj == null)
            return null;
        return (ActorJid) smj.kGet(key);
    }

    public ActorJid makeActorJid(String key)
            throws Exception {
        StringMapJid smj = makeStringMapJid();
        ActorJid actorJid = (ActorJid) smj.kGet(key);
        if (actorJid == null) {
            smj.kMake(key);
            actorJid = (ActorJid) smj.kGet(key);
        }
        return actorJid;
    }

    public _Jid getJid(String key)
            throws Exception {
        ActorJid aj = getActorJid(key);
        if (aj == null)
            return null;
        _Jid jid = aj.getValue();
        return jid;
    }

    public _Jid makeJid(String key, String factoryName)
            throws Exception {
        ActorJid aj = makeActorJid(key);
        _Jid jid = aj.getValue();
        if (jid == null) {
            aj.setValue(factoryName);
            jid = aj.getValue();
        }
        return jid;
    }

    public BooleanJid getBooleanJid(String key)
            throws Exception {
        return (BooleanJid) getJid(key);
    }

    public Boolean getBoolean(String key)
            throws Exception {
        BooleanJid ij = getBooleanJid(key);
        if (ij == null)
            return null;
        return ij.getValue();
    }

    public BooleanJid makeBooleanJid(String key)
            throws Exception {
        return (BooleanJid) makeJid(key, JidFactories.BOOLEAN_JID_TYPE);
    }

    public IntegerJid getIntegerJid(String key)
            throws Exception {
        return (IntegerJid) getJid(key);
    }

    public Integer getInteger(String key)
            throws Exception {
        IntegerJid ij = getIntegerJid(key);
        if (ij == null)
            return null;
        return ij.getValue();
    }

    public IntegerJid makeIntegerJid(String key)
            throws Exception {
        return (IntegerJid) makeJid(key, JidFactories.INTEGER_JID_TYPE);
    }

    public Integer incrementInteger(String key)
            throws Exception {
        IntegerJid ij = makeIntegerJid(key);
        int nv = ij.getValue() + 1;
        ij.setValue(nv);
        return nv;
    }

    public LongJid getLongJid(String key)
            throws Exception {
        return (LongJid) getJid(key);
    }

    public Long getLong(String key)
            throws Exception {
        LongJid ij = getLongJid(key);
        if (ij == null)
            return null;
        return ij.getValue();
    }

    public LongJid makeLongJid(String key)
            throws Exception {
        return (LongJid) makeJid(key, JidFactories.LONG_JID_TYPE);
    }

    public FloatJid getFloatJid(String key)
            throws Exception {
        return (FloatJid) getJid(key);
    }

    public Float getFloat(String key)
            throws Exception {
        FloatJid ij = getFloatJid(key);
        if (ij == null)
            return null;
        return ij.getValue();
    }

    public FloatJid makeFloatJid(String key)
            throws Exception {
        return (FloatJid) makeJid(key, JidFactories.FLOAT_JID_TYPE);
    }

    public DoubleJid getDoubleJid(String key)
            throws Exception {
        return (DoubleJid) getJid(key);
    }

    public Double getDouble(String key)
            throws Exception {
        DoubleJid ij = getDoubleJid(key);
        if (ij == null)
            return null;
        return ij.getValue();
    }

    public DoubleJid makeDoubleJid(String key)
            throws Exception {
        return (DoubleJid) makeJid(key, JidFactories.DOUBLE_JID_TYPE);
    }

    public StringJid getStringJid(String key)
            throws Exception {
        return (StringJid) getJid(key);
    }

    public String getString(String key)
            throws Exception {
        StringJid sj = getStringJid(key);
        if (sj == null)
            return null;
        return sj.getValue();
    }

    public StringJid makeStringJid(String key)
            throws Exception {
        return (StringJid) makeJid(key, JidFactories.STRING_JID_TYPE);
    }

    public BytesJid getBytesJid(String key)
            throws Exception {
        return (BytesJid) getJid(key);
    }

    public byte[] getBytes(String key)
            throws Exception {
        BytesJid ij = getBytesJid(key);
        if (ij == null)
            return null;
        return ij.getValue();
    }

    public BytesJid makeBytesJid(String key)
            throws Exception {
        return (BytesJid) makeJid(key, JidFactories.BYTES_JID_TYPE);
    }

    public void checkpoint(long logPosition, long timestamp, String logFileName, RP rp)
            throws Exception {
        setExceptionHandler(new ExceptionHandler() {
            @Override
            public void process(Exception exception) throws Exception {
                System.err.println("Checkpoint Exception: " + exception);
            }
        });
        if (!pendingWrite) {
            StringJid sj = makeStringJid(LOG_FILE_NAME);
            sj.setValue(logFileName);
            LongJid lj = makeLongJid(LOG_POSITION);
            lj.setValue(logPosition);
            pendingWrite = true;
            Block block = newDbBlock();
            RootJid rj = (RootJid) rootJid.copyJID(getMailboxFactory().createMailbox());
            block.setRootJid(rj);
            block.setTimestamp(timestamp);
            if (isFirstRootJid) {
                isFirstRootJid = false;
                block.setCurrentPosition(maxSize);
            } else {
                isFirstRootJid = true;
                block.setCurrentPosition(0L);
            }
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
