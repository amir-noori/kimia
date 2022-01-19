package ir.kimia.client.common;

import ir.kimia.client.data.model.Office;
import ir.kimia.client.data.model.User;

public class UserSession {

    private User currentUser;
    private Office currentOffice;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public Office getCurrentOffice() {
        return currentOffice;
    }

    public void setCurrentOffice(Office currentOffice) {
        this.currentOffice = currentOffice;
    }
}
