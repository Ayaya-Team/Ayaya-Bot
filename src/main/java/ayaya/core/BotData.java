package ayaya.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that will store important data of the bot.
 */
public class BotData {

    private static String id;
    private static String name;
    private static String prefix;
    private static String version;
    private static String token;
    private static String dbConnection;
    private static String dbUser;
    private static String dbPassword;
    private static String serverInvite;
    private static String consoleID;
    private static String description;
    private static List<String> owners = new ArrayList<>();
    private static List<String> statusQuotes = new ArrayList<>();
    private static List<String> pingQuotes = new ArrayList<>();
    private static ReentrantLock jsLock = new ReentrantLock();
    private static ReentrantLock dbLock = new ReentrantLock();

    public static void refreshJSONData() throws IOException {

        jsLock.lock();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("config.json")));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            stringBuilder.append(line).append('\n');
        reader.close();

        JSONObject json = new JSONObject(stringBuilder.toString().trim());
        JSONObject settings = json.getJSONObject("settings");
        JSONArray ownersArray = json.getJSONArray("owners");
        JSONArray statusQuotesArray = json.getJSONArray("status-quotes");
        JSONArray pingQuotesArray = json.getJSONArray("ping-quotes");

        id = settings.getString("discord-id");
        name = settings.getString("name");
        prefix = settings.getString("prefix");
        token = settings.getString("token");

        try {
            dbConnection = settings.getString("db-connection");
        } catch (JSONException e) {
            dbConnection = "";
        }

        dbUser = settings.getString("db-user");
        dbPassword = settings.getString("db-password");
        consoleID = settings.getString("console-id");
        serverInvite = settings.getString("server-invite");
        description = settings.getString("description");

        owners = new ArrayList<>();
        for (Object value: ownersArray)
            owners.add((String) value);
        statusQuotes = new ArrayList<>();
        for (Object value: statusQuotesArray)
            statusQuotes.add((String) value);
        pingQuotes = new ArrayList<>();
        for (Object value: pingQuotesArray)
            pingQuotes.add((String) value);
        jsLock.unlock();

    }

    public static void refreshDBData() throws SQLException {

        dbLock.lock();
        Connection connection = DriverManager.getConnection(dbConnection, dbUser, dbPassword);
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT version FROM changelogs;",
                        ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet rs = preparedStatement.executeQuery();
        rs.last();
        version = rs.getString(1);
        rs.close();
        connection.close();
        dbLock.unlock();

    }

    public static String getId() {
        return id;
    }

    public static String getName() {
        return name;
    }

    public static String getPrefix() {
        return prefix;
    }

    static String getToken() {
        return token;
    }

    public static String getDBConnection() {
        return dbConnection;
    }

    public static String getDBUser() {
        return dbUser;
    }

    public static String getDbPassword() {
        return dbPassword;
    }

    public static String getVersion() {
        return version;
    }

    public static String getServerInvite() {
        return serverInvite;
    }

    public static String getConsoleID() {
        return consoleID;
    }

    public static String getDescription() {
        return description;
    }

    public static List<String> getOwners() {
        return owners;
    }

    static List<String> getStatusQuotes() {
        return statusQuotes;
    }

    public static List<String> getPingQuotes() {
        return pingQuotes;
    }

}