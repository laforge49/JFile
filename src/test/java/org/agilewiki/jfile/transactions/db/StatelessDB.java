package org.agilewiki.jfile.transactions.db;

public class StatelessDB extends DB {

    protected boolean generateCheckpoints() {
        return false;
    }
}
