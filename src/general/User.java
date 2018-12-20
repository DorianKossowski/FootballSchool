package general;

public class User {
    private String name, surname, login;
    private Type userType;
    private int id;


    public enum Type{
        ADMIN, COACH, PARENT
    }

    /**
     * @param n name of logged user
     * @param s surname of logged user
     * @param type type of logged user (admin, coach, parent)
     * @param l login of logged user
     * @param i id of logged user
     */
    public User(String n, String s, int type, String l, int i) {
        name = n;
        surname = s;
        login = l;
        id = i;

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

    /**
     * @return login of logged user
     */
    public String getLogin() {
        return login;
    }

    /**
     * @return id of logged user
     */
    public int getId() {
        return id;
    }
}
