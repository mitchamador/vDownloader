package by.mitchamador;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by vicok on 07.06.2016.
 */
public class UrlItemDB {

    private static final String FILE_NAME = "vDownloader.db";

    private static SqlJetDb db;

    public static SqlJetDb open() throws SqlJetException {
        db = SqlJetDb.open(new File(FILE_NAME), true);
        if (db.getSchema().getTable("url_items") == null) {
            db.runWriteTransaction(new ISqlJetTransaction() {
                public Object run(SqlJetDb arg0) throws SqlJetException {
                    db.createTable(
                            "create table url_items (date int not null, name text, url text, hash int not null)");
                    db.createIndex(
                            "create index url_items_date on url_items (date)");
                    db.createIndex(
                            "create index url_items_hash on url_items (hash)");
                    return null;
                }
            });
        }
        return db;
    }

    public static void close() throws SqlJetException {
        db.close();
        db = null;
    }

    public static void beginReadTransaction() throws SqlJetException {
        db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
    }

    public static void commitTransaction() throws SqlJetException {
        db.commit();
    }

    public static ISqlJetCursor getAllItems() throws SqlJetException {
        return db.getTable("url_items").order("url_items_date");
    }

    public static ArrayList<UrlItem> getAllItemsList() throws SqlJetException {
        ArrayList<UrlItem> items = new ArrayList<UrlItem>();
        try {
            beginReadTransaction();
            ISqlJetCursor cursor = UrlItemDB.getAllItems();
            for (; !cursor.eof(); cursor.next()) {
                items.add(UrlItem.read(cursor));
            }
        } finally {
            commitTransaction();
        }
        return items;
    }

    public static ISqlJetCursor getItemByHash(long hash) throws SqlJetException {
        return db.getTable("url_items").lookup("url_items_hash", hash);
    }

    public static long containsHash(long hash) throws SqlJetException {
        long rowID = -1;
        try {
            beginReadTransaction();
            ISqlJetCursor cursor = UrlItemDB.getItemByHash(hash);
            for (; !cursor.eof(); cursor.next()) {
                rowID = UrlItem.read(cursor).rowID;
            }
        } finally {
            commitTransaction();
        }
        return rowID;
    }


    public static long insertItem(final UrlItem item) throws SqlJetException {
        long rowId = containsHash(item.getHash());
        if (rowId == -1) {
            return item.rowID = (Long) db.runWriteTransaction(new ISqlJetTransaction() {
                public Object run(SqlJetDb db) throws SqlJetException {
                    return db.getTable("url_items").insert(item.date, item.name, item.url, item.getHash());
                }
            });
        } else {
            return rowId;
        }
    }
}
