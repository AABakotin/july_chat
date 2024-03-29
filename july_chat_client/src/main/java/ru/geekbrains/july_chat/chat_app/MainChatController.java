package ru.geekbrains.july_chat.chat_app;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.july_chat.chat_app.net.ChatMessageService;
import ru.geekbrains.july_chat.chat_app.net.MessageProcessor;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainChatController implements Initializable, MessageProcessor {

    public static final String REGEX = "%&%";
    public TextField loginField;
    public PasswordField passwordField;
    public AnchorPane changePasswordPanel;
    public PasswordField oldPassField;
    public PasswordField newPasswordField;
    public AnchorPane changeNickPanel;
    public TextField newNickField;
    public TextField regLoginField;
    public PasswordField regPasswordField;
    public TextField regNickNameField;
    public AnchorPane loginPanel;
    public AnchorPane regPanel;
    public VBox mainChatPanel;
    public TextArea mainChatArea;
    public ListView<String> contactList;
    public TextField inputField;
    public Button btnSendMessage;

    private ChatMessageService chatMessageService;
    private HistoryManager historyManager;
    private static final Logger logger = LogManager.getLogger(MainChatController.class.getName());

    private void parseMessage(String message) {
        String[] parsedMessage = message.split(REGEX);
        switch (parsedMessage[0]) {
            case "authok:":
                String nickName = parsedMessage[1];
                loginPanel.setVisible(false);
                mainChatPanel.setVisible(true);
              this.historyManager = new HistoryManager(nickName);
                List<String> history = historyManager.readHistory();
                for (String s : history) {
                    mainChatArea.appendText(s + System.lineSeparator());
                }
                break;
            case "register_ok:":
                regPanel.setVisible(false);
                loginPanel.setVisible(true);
                showContext();
            case "ERROR:":
                try {
                    showError(parsedMessage[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    logger.info("Registration complete");
                }
                break;
            case "/list:":
                parsedMessage[0] = "ALL";
                ObservableList<String> list = FXCollections.observableArrayList(parsedMessage);
                contactList.setItems(list);
                contactList.getSelectionModel().select(0);
                break;
            case "/change_nick_ok":
                changeNickPanel.setVisible(false);
                mainChatPanel.setVisible(true);
                break;
            case "/change_pass_ok":
                changePasswordPanel.setVisible(false);
                mainChatPanel.setVisible(true);
                break;
            default:
                mainChatArea.appendText(parsedMessage[0] + System.lineSeparator());
        }

    }

    public void mockAction(ActionEvent actionEvent) {

    }

    public void exit(ActionEvent actionEvent) {
        chatMessageService.send("/exit");

        Platform.exit();
    }

    public void sendMessage(ActionEvent actionEvent) {
        String text = inputField.getText();
        if (text.isEmpty()) return;
        String recipient = contactList.getSelectionModel().getSelectedItem();
        String message = "";
        if (recipient.equals("ALL")) message = "/" + recipient + REGEX + text;
        else message = "/w" + REGEX + recipient + REGEX + text;
        chatMessageService.send(message);
        historyManager.writeHistory(String.format("[ME] %s\n", text));
        inputField.clear();
    }


    @Override
    public void processMessage(String message) {
        Platform.runLater(() -> parseMessage(message));
    }

    public void sendAuth(ActionEvent actionEvent) {
        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) return;
        chatMessageService.connect();
        chatMessageService.send("/auth" + REGEX + loginField.getText() + REGEX + passwordField.getText());
    }

    public void sendRegister(ActionEvent actionEvent) {
        if (regLoginField.getText().isEmpty() || regPasswordField.getText().isEmpty() || regNickNameField.getText().isEmpty())
            return;
        chatMessageService.connect();
        chatMessageService.send("/register" + REGEX + regLoginField.getText() + REGEX + regPasswordField.getText() + REGEX + regNickNameField.getText());

    }

    public void sendChangeNick(ActionEvent actionEvent) {
        if (newNickField.getText().isEmpty()) return;
        chatMessageService.send("/change_nick" + REGEX + newNickField.getText());
    }

    public void sendChangePass(ActionEvent actionEvent) {
        if (newPasswordField.getText().isEmpty() || oldPassField.getText().isEmpty()) return;
        chatMessageService.send("/change_pass" + REGEX + oldPassField.getText() + REGEX + newPasswordField.getText());
    }


    public void sendEternalLogout(ActionEvent actionEvent) {
        chatMessageService.send("/remove");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    private void showContext() {
        Alert context = new Alert(Alert.AlertType.INFORMATION);
        context.setTitle("Login and Password has been created!");
        context.setHeaderText("Please, enter your login and password.");
        context.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> list = FXCollections.observableArrayList("ALL");
        contactList.setItems(list);
        contactList.getSelectionModel().select(0);
        this.chatMessageService = new ChatMessageService(this);
    }

    public void returnToChat(ActionEvent actionEvent) {
        changeNickPanel.setVisible(false);
        changePasswordPanel.setVisible(false);
        mainChatPanel.setVisible(true);
    }

    public void returnToLoginPanel() {
        regPanel.setVisible(false);
        loginPanel.setVisible(true);
    }

    public void showChangeNick(ActionEvent actionEvent) {
        mainChatPanel.setVisible(false);
        changeNickPanel.setVisible(true);
    }

    public void showChangePass(ActionEvent actionEvent) {
        mainChatPanel.setVisible(false);
        changePasswordPanel.setVisible(true);
    }


    public void regNow(ActionEvent actionEvent) {
        loginPanel.setVisible(false);
        regPanel.setVisible(true);
    }
}
