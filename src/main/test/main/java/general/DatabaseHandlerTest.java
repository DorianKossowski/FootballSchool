package main.java.general;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DatabaseHandlerTest {
    private static DatabaseHandler db;

    @BeforeClass
    public static void open() {
        db = DatabaseHandler.getInstance();
    }

    @AfterClass
    public static void close() {
        db.closeConnection();
    }

    @Test
    public void getConnection() {
        assertNotNull(db.getConnection());
    }

    @Test
    public void closeConnection() {
        db.closeConnection();
        assertNull(db.getConnection());
    }
}