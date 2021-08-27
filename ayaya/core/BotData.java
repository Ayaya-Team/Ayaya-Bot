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

    private static String name;
    private static String prefix;
    private static String version;
    private static String token;
    private static String dbConnection;
    private static String dbUser;
    private static String dbPassword;
    private static String inviteNormal;
    private static String inviteAdmin;
    private static String inviteMinimal;
    private static String serverInvite;
    private static String patreonLink;
    private static String consoleID;
    private static String description;
    private static List<String> owners = new ArrayList<>();
    private static List<String> statusQuotes = new ArrayList<>();
    private static List<String> pingQuotes = new ArrayList<>();
    private static List<String[]> botlists = new ArrayList<>();
    private static ReentrantLock jsLock = new ReentrantLock();
    private static ReentrantLock dbLock = new ReentrantLock();

    public static void refreshJSONData() throws IOException {

        jsLock.lock();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("config.json")));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            stringBuilder.append(line).append('\n');

        JSONObject json = new JSONObject(stringBuilder.toString().trim());
        JSONObject settings = json.getJSONObject("settings");
        JSONArray ownersArray = json.getJSONArray("owners");
        JSONObject invites = json.getJSONObject("invites");
        JSONArray statusQuotesArray = json.getJSONArray("status-quotes");
        JSONArray pingQuotesArray = json.getJSONArray("ping-quotes");

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
        patreonLink = settings.getString("patreon-link");
        description = settings.getString("description");
        inviteNormal = invites.getString("normal");
        inviteAdmin = invites.getString("admin");
        inviteMinimal = invites.getString("minimal");

        for (Object value: ownersArray)
            owners.add((String) value);
        for (Object value: statusQuotesArray)
            statusQuotes.add((String) value);
        for (Object value: pingQuotesArray)
            pingQuotes.add((String) value);
        jsLock.unlock();

    }

    public static void refreshDBData() throws SQLException {

        if (dbPassword.isEmpty()) {
            Console console = System.console();
            if (console != null) {
                dbPassword = new String(console.readPassword());
            }
        }

        dbLock.lock();
        Connection connection = DriverManager.getConnection(dbConnection, dbUser, dbPassword);
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM botlists;");
        while (rs.next()) {
            botlists.add(new String[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5)
            });
        }
        rs.close();
        statement.close();

        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT version FROM changelogs;",
                        ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        rs = preparedStatement.executeQuery();
        rs.last();
        version = rs.getString(1);
        rs.close();
        statement.close();
        connection.close();
        dbLock.unlock();

    }

    public static String getName() {

        String result;
        jsLock.lock();
        result = name;
        jsLock.unlock();
        return result;

    }

    public static String getPrefix() {

        String result;
        jsLock.lock();
        result = prefix;
        jsLock.unlock();
        return result;

    }

    static String getToken() {

        String result;
        jsLock.lock();
        result = token;
        jsLock.unlock();
        return result;

    }

    public static String getDBConnection() {

        String result;
        jsLock.lock();
        result = dbConnection;
        jsLock.unlock();
        return result;

    }

    public static String getDBUser() {

        String result;
        jsLock.lock();
        result = dbUser;
        jsLock.unlock();
        return result;

    }

    public static String getDbPassword() {

        String result;
        jsLock.lock();
        result = dbPassword;
        jsLock.unlock();
        return result;

    }

    public static String getVersion() {

        String result;
        dbLock.lock();
        result = version;
        dbLock.unlock();
        return result;

    }

    public static String getInviteNormal() {

        String result;
        jsLock.lock();
        result = inviteNormal;
        jsLock.unlock();
        return result;

    }

    public static String getInviteAdmin() {

        String result;
        jsLock.lock();
        result = inviteAdmin;
        jsLock.unlock();
        return result;

    }

    public static String getInviteMinimal() {

        String result;
        jsLock.lock();
        result = inviteMinimal;
        jsLock.unlock();
        return result;

    }

    public static String getServerInvite() {

        String result;
        jsLock.lock();
        result = serverInvite;
        jsLock.unlock();
        return result;

    }

    public static String getPatreonLink() {

        String result;
        jsLock.lock();
        result = patreonLink;
        jsLock.unlock();
        return result;

    }

    public static String getConsoleID() {

        String result;
        jsLock.lock();
        result = consoleID;
        jsLock.unlock();
        return result;

    }

    public static String getDescription() {

        String result;
        jsLock.lock();
        result = description;
        jsLock.unlock();
        return result;

    }

    public static List<String> getOwners() {

        List<String> list;
        dbLock.lock();
        list = owners;
        dbLock.unlock();
        return list;

    }

    static List<String> getStatusQuotes() {

        List<String> list;
        dbLock.lock();
        list = statusQuotes;
        dbLock.unlock();
        return list;

    }

    public static List<String> getPingQuotes() {

        List<String> list;
        dbLock.lock();
        list = pingQuotes;
        dbLock.unlock();
        return list;

    }

    public static List<String[]> getBotlists() {

        List<String[]> list;
        dbLock.lock();
        list = botlists;
        dbLock.unlock();
        return list;

    }

}