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
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jfile.transactions.*;
import org.agilewiki.jfile.transactions.logReader.LogReader;
import org.agilewiki.jfile.transactions.logReader.ReadLog;
import org.agilewiki.jfile.transactions.transactionAggregator.TransactionAggregator;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/**
 * A database must handle checkpoint requests.
 */
abstract public class DB extends JLPCActor {
    private TransactionAggregator transactionAggregator;
    private DurableTransactionLogger durableTransactionLogger;
    private LogReader logReader;
    protected Path directoryPath;
    private int logReaderMaxSize;
    protected String[] logFileNames = null;

    public DB() {
    }

    public DB(MailboxFactory mailboxFactory, Actor parent, Path directoryPath)
            throws Exception {
        initialize(mailboxFactory.createAsyncMailbox(), parent);
        setDirectoryPath(directoryPath);
    }

    public void openDbFile(int logReaderMaxSize, RP rp)
            throws Exception {
        initializeDb(logReaderMaxSize);
        processLogFile(0L, 0, rp);
    }

    protected void initializeDb(int logReaderMaxSize) {
        this.logReaderMaxSize = logReaderMaxSize;
        logFileNames = directoryPath.toFile().list();
        if (logFileNames == null)
            return;
        Arrays.sort(logFileNames);
    }

    public void processLogFile(long position, int fileIndex, final RP rp)
            throws Exception {
        while (fileIndex < logFileNames.length && !logFileNames[fileIndex].endsWith(".jalog")) {
            fileIndex += 1;
        }
        if (fileIndex == logFileNames.length) {
            rp.processResponse(null);
            return;
        }
        final int fi = fileIndex;
        final String logFileName = logFileNames[fileIndex];
        getLogReader(logReaderMaxSize);
        Path path = directoryPath.resolve(logFileName);
        System.out.println("processing " + fi + " " + path);
        logReader.open(
                path,
                StandardOpenOption.READ);
        logReader.currentPosition = position;
        ReadLog.req.send(this, logReader, new RP<Long>() {
            public void processResponse(Long response)
                    throws Exception {
                System.out.println("unprocessed bytes remaining: " + response);
                Finish.req.send(DB.this, logReader, new RP<Object>() {
                    @Override
                    public void processResponse(Object response) throws Exception {
                        logReader.close();
                        checkpoint(logReader.currentPosition, logReader.timestamp, logFileName, new RP() {
                            @Override
                            public void processResponse(Object response) throws Exception {
                                processLogFile(0L, fi + 1, rp);
                            }
                        });
                    }
                });
            }
        });
    }

    public void closeDbFile() {
        if (durableTransactionLogger != null) {
            durableTransactionLogger.close();
            durableTransactionLogger = null;
        }
    }

    /**
     * Create a transaction log reader.
     *
     * @return A LogReader.
     */
    protected LogReader newLogReader() {
        return new LogReader();
    }

    /**
     * Create a transaction aggregator.
     *
     * @return A TransactionAggregator.
     */
    protected TransactionAggregator newTransactionAggregator() {
        return new TransactionAggregator();
    }

    /**
     * Returns the transaction log reader.
     *
     * @param maxSize The maximum possible block size.
     * @return The LogReader
     */
    public LogReader getLogReader(int maxSize)
            throws Exception {
        if (logReader != null)
            return logReader;

        Actor parent = getParent();
        if (parent == null) {
            throw new IllegalStateException("call setParent before getLogReader");
        }

        TransactionProcessor transactionProcessor = new TransactionProcessor();
        transactionProcessor.initialize(getMailbox(), this);
        transactionProcessor.generateCheckpoints = generateCheckpoints();

        Deserializer deserializer = new Deserializer();
        deserializer.initialize(getMailboxFactory().createAsyncMailbox(), this);
        deserializer.setNext(transactionProcessor);

        logReader = newLogReader();
        logReader.initialize(getMailboxFactory().createAsyncMailbox(), parent);
        logReader.setNext(deserializer);
        logReader.maxSize = maxSize;

        return logReader;
    }

    protected boolean generateCheckpoints() {
        return true;
    }

    /**
     * Returns the transaction aggregator.
     *
     * @return The transaction aggregator.
     */
    public TransactionAggregator getTransactionAggregator()
            throws Exception {
        if (transactionAggregator != null) {
            return transactionAggregator;
        }

        logReader = null;

        Actor parent = getParent();
        if (parent == null) {
            throw new IllegalStateException("call setParent before getTransactionAggregator");
        }

        TransactionProcessor transactionProcessor = new TransactionProcessor();
        transactionProcessor.initialize(getMailbox(), this);

        durableTransactionLogger = new DurableTransactionLogger();
        durableTransactionLogger.initialize(getMailboxFactory().createAsyncMailbox(), parent);
        durableTransactionLogger.setNext(transactionProcessor);
        String ts = (new DateTime()).toString("yyyy-MM-dd_HH-mm-ss_SSS");
        Path path = directoryPath.resolve(ts + ".jalog");
        durableTransactionLogger.open(
                path,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);
        durableTransactionLogger.currentPosition = 0L;

        Serializer serializer = new Serializer();
        serializer.initialize(getMailboxFactory().createAsyncMailbox(), parent);
        serializer.setNext(durableTransactionLogger);

        transactionAggregator = newTransactionAggregator();
        transactionAggregator.initialize(getMailboxFactory().createAsyncMailbox(), this);
        transactionAggregator.setNext(serializer);

        return transactionAggregator;
    }

    public DurableTransactionLogger getDurableTransactionLogger() throws Exception {
        if (transactionAggregator != null)
            return durableTransactionLogger;

        Actor parent = getParent();
        if (parent == null) {
            throw new IllegalStateException("call setParent before getDurableTransactionLogger");
        }

        getTransactionAggregator();
        return durableTransactionLogger;
    }

    public void checkpoint(long logPosition, long timestamp, String logFileName, RP rp)
            throws Exception {
        rp.processResponse(null);
    }

    public void setDirectoryPath(Path path)
            throws Exception {
        directoryPath = path;
        File directoryFile = path.toFile();
        if (!directoryFile.exists())
            directoryFile.mkdir();
    }

    public void clearDirectory()
            throws Exception {
        File[] files = directoryPath.toFile().listFiles();
        int i = 0;
        while (i < files.length) {
            File file = files[i];
            if (!file.delete())
                throw new IOException("unable to delete " + file);
            i += 1;
        }
    }
}
