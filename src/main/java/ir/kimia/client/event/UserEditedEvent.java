package ir.kimia.client.event;

import ir.kimia.client.data.model.User;

public class UserEditedEvent {

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
