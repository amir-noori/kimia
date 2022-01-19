package ir.kimia.client.controller;

import com.google.common.eventbus.Subscribe;
import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.data.model.User;
import ir.kimia.client.event.UserCreatedEvent;
import ir.kimia.client.event.UserEditedEvent;
import ir.kimia.client.exception.ApplicationException;
import ir.kimia.client.service.api.UserService;
import ir.kimia.client.service.di.FxAppScoped;
import ir.kimia.client.ui.ActionButtonTableCell;
import ir.kimia.client.util.FxUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

@FxAppScoped
public class UserManagementController extends BaseController {

    @FXML
    private TableView usersTable;

    @FXML
    private TextField userForm_firstName;

    @FXML
    private TextField userForm_lastName;

    @FXML
    private TextField userForm_username;

    @FXML
    private TextField userForm_password;

    private final UserService userService;

    private Stage currentOpenDialog;

    @Inject
    public UserManagementController(UserService userService) {
        this.userService = userService;
    }

    private User selectedUser;

    public User getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    @Override
    public void init(URL url, ResourceBundle resourceBundle) throws SQLException {
        if (url.getFile().indexOf("userManagement.fxml") > 0) { // userManagement.fxml
            List<User> allUsers = userService.getAllUsers();
            var firstNameColumn = new TableColumn<>(message("firstName"));
            firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));

            var lastNameColumn = new TableColumn<>(message("lastName"));
            lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));

            var usernameColumn = new TableColumn<>(message("username"));
            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

            TableColumn<User, Button> editColumn = new TableColumn<>(message("edit"));
            editColumn.setCellFactory(ActionButtonTableCell.<User>forTableColumn(message("edit"), (User targetUser) -> {
                editUser(targetUser);
                return targetUser;
            }));

            TableColumn<User, Button> changePasswordColumn = new TableColumn<>(message("password"));
            changePasswordColumn.setCellFactory(ActionButtonTableCell.<User>forTableColumn(message("password"), (User targetUser) -> {
                changePassword(targetUser);
                return targetUser;
            }));

            TableColumn<User, Button> deleteColumn = new TableColumn<>(message("remove"));
            deleteColumn.setCellFactory(ActionButtonTableCell.<User>forTableColumn(message("remove"), (User targetUser) -> {
                boolean confirmed = FxUtil.confirm("remove.confirm");
                if (confirmed) {
                    removeUser(targetUser);
                }
                return targetUser;
            }));

            usersTable.getItems().removeAll();
            usersTable.getColumns().clear();
            usersTable.getColumns().addAll(firstNameColumn, lastNameColumn, usernameColumn, editColumn, changePasswordColumn, deleteColumn);

            usersTable.setItems(FXCollections.observableList(allUsers));

        }
    }

    @Subscribe
    public void userCreated(UserCreatedEvent userCreatedEvent) {
        usersTable.getItems().add(userCreatedEvent.getUser());
    }

    @Subscribe
    public void userEdited(UserEditedEvent userEditedEvent) {
        User user = userEditedEvent.getUser();
        int count = 0;
        for (Object userItem : usersTable.getItems()) {
            if (((User) userItem).getUsername().equals(selectedUser.getUsername())) {
                break;
            }
            count++;
        }
        usersTable.getItems().set(count, user);
    }

    @FXML
    public void createUser(ActionEvent event) {
        currentOpenDialog = showDialog("createUser", 400, 300, Modality.APPLICATION_MODAL);
    }

    @FXML
    public void changePassword(User user) {
        selectedUser = user;
        currentOpenDialog = showDialog("changePassword", 350, 150,
                Modality.APPLICATION_MODAL,
                controller -> {
                    ((UserManagementController) controller).setSelectedUser(user);
                    return null;
                });
    }

    private void removeUser(User user) {
        try {
            userService.removeUser(user);
            usersTable.getItems().remove(user);
        } catch (ApplicationException | SQLException e) {
            FxUtil.exceptionOccurred(e);
        }
    }

    @FXML
    public void editUser(User user) {
        selectedUser = user;
        currentOpenDialog = showDialog("editUser", 400, 300,
                Modality.APPLICATION_MODAL,
                controller -> {
                    ((UserManagementController) controller).setSelectedUser(user);
                    ((UserManagementController) controller).setSelectedTargetUserData(user);
                    return null;
                });
    }

    @FXML
    public void doChangePassword(ActionEvent event) {
        try {
            String passwordText = userForm_password.getText();
            if (StringUtils.isNotEmpty(passwordText)) {
                selectedUser.setPassword(passwordText);
                userService.changeUserPassword(selectedUser);
                FxUtil.info("password.change.success");
                currentOpenDialog.close();
            } else {
                FxUtil.error("required");
            }
        } catch (SQLException | ApplicationException exception) {
            FxUtil.exceptionOccurred(exception);
        }
    }

    @FXML
    public void doEditUser(ActionEvent event) {
        User userToBeEdited = getSelectedTargetUser();
        if (!validateUserInfo(userToBeEdited)) {
            return;
        }
        try {
            userService.editUser(userToBeEdited);
            FxUtil.info("user.edit.success");
            UserEditedEvent userEditedEvent = new UserEditedEvent();
            userEditedEvent.setUser(userToBeEdited);
            ApplicationContext.getEventBus().post(userEditedEvent);
            currentOpenDialog.close();
        } catch (Exception exception) {
            FxUtil.exceptionOccurred(exception);
        }

    }

    @FXML
    public void doCreateUser(ActionEvent event)  {
        try {
            User userToBeCreated = getSelectedTargetUser();
            if (!validateUserInfo(userToBeCreated)) {
                return;
            }
            if(StringUtils.isEmpty(userToBeCreated.getPassword())) {
                FxUtil.error("please.enter.password");
                return;
            }
            userService.createUser(userToBeCreated);
            FxUtil.info("user.create.success");
            UserCreatedEvent userCreatedEvent = new UserCreatedEvent();
            userCreatedEvent.setUser(userToBeCreated);
            ApplicationContext.getEventBus().post(userCreatedEvent);
            currentOpenDialog.close();
        } catch (Exception e) {
            FxUtil.exceptionOccurred(e);
        }
    }

    private void setSelectedTargetUserData(User user) {
        userForm_firstName.setText(user.getFirstName());
        userForm_lastName.setText(user.getLastName());
        userForm_username.setText(user.getUsername());
    }

    private User getSelectedTargetUser() {
        String firstNameText = userForm_firstName.getText();
        String lastNameText = userForm_lastName.getText();
        String usernameText = userForm_username.getText();
        String passwordText = null;
        if (userForm_password != null) {
            passwordText = userForm_password.getText();
        }

        User user = new User(
                firstNameText,
                lastNameText,
                usernameText,
                passwordText);
        if (selectedUser != null) {
            user.setId(selectedUser.getId());
        }
        return user;
    }

    private boolean validateUserInfo(User user) {
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String username = user.getUsername();
        String password = user.getPassword();

        if (StringUtils.isEmpty(firstName)) {
            FxUtil.error("please.fill.all.inputs");
            return false;
        }
        if (StringUtils.isEmpty(lastName)) {
            FxUtil.error("please.fill.all.inputs");
            return false;
        }
        if (StringUtils.isEmpty(username)) {
            FxUtil.error("please.fill.all.inputs");
            return false;
        }

        return true;
    }

}
