package general;

public class User {
    private String name, surname;
    private Type userType;

    public enum Type{
        ADMIN, COACH, PARENT
    }

    /**
     * @param n name of logged user
     * @param s surname of logged user
     * @param type type of logged user (admin, coach, parent)
     */
    public User(String n, String s, int type) {
        name = n;
        surname = s;
        switch (type) {
            case 1:
                userType = Type.ADMIN;
                break;
            case 2:
                userType = Type.COACH;
                break;
            case 3:
                userType = Type.PARENT;
                break;
        }

    }

    /**
     * @return name of logged user
     */
    public String getName() {
        return name;
    }

    /**
     * @return type of logged user
     */
    public Type getUserType() {
        return userType;
    }

    /**
     * @return surname of logged user
     */
    public String getSurname() {
        return surname;
    }
}
