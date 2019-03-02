package main.java.general;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserTest {
    private static User user;

    @BeforeClass
    public static void open() {
        user = User.getInstance();
    }

    @Test
    public void userCreation() {
        user.init("Eden", "Hazard", 1, "ehazard", 1);
        assertEquals(User.Type.ADMIN, user.getUserType());
        user.init("Eden", "Hazard", 2, "ehazard", 1);
        assertEquals("Eden", user.getName());
        user.init("Eden", "Hazard", 3, "ehazard", 1);
        assertEquals("Hazard", user.getSurname());

        String expectedLogin = (user.getName().charAt(0) + user.getSurname()).toLowerCase();
        assertEquals(expectedLogin, user.getLogin());
        assertEquals(1, user.getId());
    }
}