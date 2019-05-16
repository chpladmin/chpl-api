package gov.healthit.chpl.domain.auth;

import java.util.ArrayList;
import java.util.List;

public class UsersResponse {

    private List<User> users = new ArrayList<User>();

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(final List<User> users) {
        this.users = users;
    }

}
