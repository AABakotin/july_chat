package ru.geekbrains.july_chat.chat_app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.july_chat.chat_app.net.NetworkService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.io.*;
import java.util.Collections;
import java.util.List;


public class HistoryManager {


    private static final int SIZE_OF_RETRIEVED_HISTORY = 100;
    private static final String HISTORY_PATH = "history/";
    private File history;
    private String nick;
    private static final Logger logger = LogManager.getLogger(HistoryManager.class.getName());

    public HistoryManager(String nickName) {
        this.nick= nickName;
        this.history = new File(HISTORY_PATH + "history_" + nickName + ".txt");
        if (!history.exists()) {
            File path = new File(HISTORY_PATH);
            path.mkdirs();
        }
    }

    public List<String> readHistory() {
        if (!history.exists()) return Collections.singletonList("No previous history");
        List<String> result = null;
        if (history.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(history))) {
                String historyString;
                List<String> historyStrings = new ArrayList<>();
                while ((historyString = reader.readLine()) != null) {
                    historyStrings.add(historyString);
                }
                if (historyStrings.size() <= SIZE_OF_RETRIEVED_HISTORY) {
                    result = historyStrings;
                }
                if (historyStrings.size() > SIZE_OF_RETRIEVED_HISTORY) {
                    int firstIndex = historyStrings.size() - SIZE_OF_RETRIEVED_HISTORY;
                    result = new ArrayList<>(SIZE_OF_RETRIEVED_HISTORY);

                    for (int counter = firstIndex - 1; counter < historyStrings.size(); counter++) {
                        result.add(historyStrings.get(counter));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (result != null) {
            logger.info("History for " + this.nick);
        }
        return result;
    }

    public void writeHistory(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(history, true))) {
            writer.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
