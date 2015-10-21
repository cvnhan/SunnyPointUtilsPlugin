import com.google.gson.Gson;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.io.*;
import java.net.Socket;

/**
 * Created by NhanCao on 19-Oct-15.
 */
public class Controller {

//            file:/C:/Users/NhanCao/.IntelliJIdea14/config/plugins/SunnyPoint/lib/SunnyPoint.jar!/config.txt
//            cmd.exe /c cd C:\Users\NhanCao\.IntelliJIdea14\config\plugins\SunnyPoint\lib && jar xf SunnyPoint.jar sqlitebrowser

    private static Controller instance = new Controller();
    private Runtime runtime = Runtime.getRuntime();
    private String path_plugins = "";
    private int port = 1234;
    private String serverName;

    public Controller() {
        path_plugins = this.getClass().getResource("sqlitebrowser").getPath().replace("file:/", "").replace("SunnyPoint.jar!/sqlitebrowser", "");
//        Messages.showErrorDialog(path_plugins + "sqlitebrowser/config.txt", "path");
        extractResource();
        Notifications.Bus.register(Constant.GROUND_ID, NotificationDisplayType.BALLOON);
    }

    public static Controller getInstance() {
        return instance;
    }

    public void extractResource() {
        try {
            //extract sqlitebrowser in jar file
            File f = new File(path_plugins + "sqlitebrowser");
            if (!f.exists()) {
                runtime.exec("cmd.exe /c cd " + path_plugins + " && jar xf SunnyPoint.jar sqlitebrowser");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Notifications.Bus.notify(new Notification(Constant.GROUND_ID, "Error extract", e.toString(), NotificationType.ERROR));
        }
    }

    public void actionRooted(AnActionEvent event) {
        try {
            Project project = event.getData(PlatformDataKeys.PROJECT);
            extractResource();
            String packagename = Messages.showInputDialog(project, "What is your package name?", "Input your package name", Messages.getQuestionIcon());
            if (packagename == null || packagename.length() == 0) {
                packagename = "com.lcl.sunnypoints";
            }
            File f = new File(path_plugins + "sqlitebrowser");
            if (f.exists() && f.isDirectory()) {
                runtime.exec("TASKKILL /F /IM sqliteman.exe");
                runtime.exec(path_plugins + "sqlitebrowser/getdb.exe " + Constant.COMMAND_ROOTED + " " + path_plugins + " " + packagename);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.Bus.notify(new Notification(Constant.GROUND_ID, "Error actionRooted", e.toString(), NotificationType.ERROR));
        }
    }

    public void actionUnRoot(AnActionEvent event) {
        try {
            extractResource();
            File f = new File(path_plugins + "sqlitebrowser");
            if (f.exists() && f.isDirectory()) {
                runtime.exec("TASKKILL /F /IM sqliteman.exe");
                runtime.exec(path_plugins + "sqlitebrowser/getdb.exe " + Constant.COMMAND_UNROOT + " " + path_plugins);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.Bus.notify(new Notification(Constant.GROUND_ID, "Error actionUnRoot", e.toString(), NotificationType.ERROR));
        }
    }

    public void actionSocket(AnActionEvent event) {
        try {
            Config config = readConfigFile();
            serverName = config.serverName;
            port = config.port;
        } catch (Exception e) {
            e.printStackTrace();
            inputIPAddr();
        }
        try {
            extractResource();
            Thread t = new SocketClient();
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.Bus.notify(new Notification(Constant.GROUND_ID, "Error actionSocket", e.toString(), NotificationType.ERROR));
        }
    }

    private void setupConnect() {
        //System.out.println("Connecting to " + serverName + " on port " + port);
        Notifications.Bus.notify(new Notification(Constant.GROUND_ID,"Connecting...","Connecting to " + serverName + " on port " + port, NotificationType.INFORMATION));
        Socket client = null;
        try {
            client = new Socket(serverName, port);
        } catch (IOException e) {
            e.printStackTrace();
            Notifications.Bus.notify(new Notification(Constant.GROUND_ID, "Error setupConnect", e.toString(), NotificationType.ERROR));
            return;
        }
        try {
            Notifications.Bus.notify(new Notification(Constant.GROUND_ID,"Socket status", "Connected to "+client.getRemoteSocketAddress(), NotificationType.INFORMATION));
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF("getDB");
            InputStream inFromServer = client.getInputStream();
            DataInputStream in =
                    new DataInputStream(inFromServer);
            String response=in.readUTF();
            Notifications.Bus.notify(new Notification(Constant.GROUND_ID,"Response",response, NotificationType.INFORMATION));
            if (response.contains("Ok")) {
                actionUnRoot(null);
            }

            if (client != null)
                client.close();
        } catch (IOException e) {
            e.printStackTrace();
            Notifications.Bus.notify(new Notification(Constant.GROUND_ID, "Error talk", e.toString(), NotificationType.ERROR));
        }
    }

    private void inputIPAddr() {
        try {
            String ipSample=getIPAndroid();
            String hostStr = Messages.showInputDialog("What is your host?", "Input your host address", Messages.getQuestionIcon(), ipSample + ":" + port, null);
            if(hostStr==null){
                hostStr=ipSample+":1234";
            }
            int index = hostStr.indexOf(":");
            serverName = hostStr.substring(0, index);
            port = Integer.parseInt(hostStr.substring(index + 1));

            Gson gson = new Gson();
            String json = gson.toJson(new Config(serverName, port));
            try {
                writeConfigFile(json);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            serverName = "localhost";
            port = 1234;
            Notifications.Bus.notify(new Notification(Constant.GROUND_ID, "Error NumberFormatException", e.toString(), NotificationType.ERROR));
        }
    }

    private String getIPAndroid() {
        String host = "localhost";
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "adb shell netcfg");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                if (line.contains("0x00001043")) {
//                    wlan0    UP                                192.168.1.79/24  0x00001043 b4:52:7d:c5:8b:69
                    int index = line.indexOf("/24");
                    line = line.substring(0, index);
                    index = line.lastIndexOf(" ");
                    host = line.substring(index + 1);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Notifications.Bus.notify(new Notification(Constant.GROUND_ID, "Error getIPAndroid", e.toString(), NotificationType.ERROR));
        }
        return host;
    }

    private Config readConfigFile() throws Exception {
        BufferedReader br = new BufferedReader(
                new FileReader(path_plugins + "sqlitebrowser/config.txt"));
        return new Gson().fromJson(br, Config.class);
    }

    private void writeConfigFile(String json) throws IOException {
        FileWriter writer = new FileWriter(path_plugins + "sqlitebrowser/config.txt");
        writer.write(json);
        writer.close();
    }

    public class SocketClient extends Thread {
        public SocketClient() throws IOException {
//            serverName = getIPAndroid();
        }

        public void run() {
            try {
                setupConnect();
            } catch (Exception e) {
                try {
                    writeConfigFile("");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
                Notifications.Bus.notify(new Notification(Constant.GROUND_ID, "Error run SocketClient", e.toString(), NotificationType.ERROR));
            }
        }
    }
}
