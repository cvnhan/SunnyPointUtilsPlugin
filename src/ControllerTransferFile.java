import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

import java.io.*;
import java.net.Socket;

/**
 * Created by NhanCao on 19-Oct-15.
 */
public class ControllerTransferFile {

    //            file:/C:/Users/NhanCao/.IntelliJIdea14/config/plugins/SunnyPoint/lib/SunnyPoint.jar!/config.txt
//            cmd.exe /c cd C:\Users\NhanCao\.IntelliJIdea14\config\plugins\SunnyPoint\lib && jar xf SunnyPoint.jar sqlitebrowser
    public static final String GETFILESK_COMMAND = "getFilesk";
    public static final String SETFILESK_COMMAND = "setFilesk";

    private static ControllerTransferFile instance = new ControllerTransferFile();
    private Runtime runtime = Runtime.getRuntime();
    private String path_plugins = "";
    private int port = 1234;
    private String serverName;

    public ControllerTransferFile() {
        path_plugins = this.getClass().getResource("sqlitebrowser").getPath().replace("file:/", "").replace("SunnyPoint.jar!/sqlitebrowser", "");
//        Messages.showErrorDialog(path_plugins + "sqlitebrowser/config.txt", "path");
        extractResource();
        Notifications.Bus.register(Constant.GROUND_ID, NotificationDisplayType.BALLOON);
    }

    public static ControllerTransferFile getInstance() {
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

    public void actionOpenFile() {
        try {
            extractResource();
            File f = new File(path_plugins + "sqlitebrowser");
            if (f.exists() && f.isDirectory()) {
                runtime.exec("TASKKILL /F /IM sqliteman.exe");
                runtime.exec(path_plugins + "sqlitebrowser/getdb.exe " + Constant.COMMAND_GETFILE + " " + path_plugins);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Notifications.Bus.notify(new Notification(Constant.GROUND_ID, "Error actionOpenFile", e.toString(), NotificationType.ERROR));
        }
    }

    public void actionSocketReceiveFile(AnActionEvent event) {
        inputIPAddr();
        try {
            extractResource();
            Thread t = new SocketClientTransferFile(TransferType.RECEIVE);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.Bus.notify(new Notification(Constant.GROUND_ID, "Error SocketReceiveFile", e.toString(), NotificationType.ERROR));

        }
    }

    public void actionSocketSendFile(AnActionEvent event) {
        inputIPAddr();
        try {
            extractResource();
            Thread t = new SocketClientTransferFile(TransferType.SEND);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.Bus.notify(new Notification(Constant.GROUND_ID, "Error SocketSendFile", e.toString(), NotificationType.ERROR));
//            Messages.showErrorDialog(e.toString(), "Socket error");
        }
    }

    private void setupConnect(TransferType type) {
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
            //System.out.println("Just connected to " + client.getRemoteSocketAddress());
            Notifications.Bus.notify(new Notification(Constant.GROUND_ID,"Socket status", "Connected to "+client.getRemoteSocketAddress(), NotificationType.INFORMATION));
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            if (type == TransferType.RECEIVE) {
                out.writeUTF(GETFILESK_COMMAND);
                InputStream inFromServer = client.getInputStream();
                DataInputStream in =
                        new DataInputStream(inFromServer);
                int fileSize = in.readInt();
                receiveFile(inFromServer, fileSize);
                actionOpenFile();
            } else if (type == TransferType.SEND) {
                out.writeUTF(SETFILESK_COMMAND);
                sendFile(out);
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
        } catch (NumberFormatException e) {
            e.printStackTrace();
            serverName = "localhost";
            port = 1234;
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

    private void receiveFile(InputStream is, int fileSize) throws IOException {
        Notifications.Bus.notify(new Notification(Constant.GROUND_ID,"Action receiveFile","Begin to receive file zip - "+fileSize, NotificationType.INFORMATION));
        int bytesRead;
        int byteCounts = 0;
        OutputStream output = new FileOutputStream(path_plugins + "main.db.zip");
        int sizeBuffer = Constant.BUFFER_SIZE;
        byte[] buffer = new byte[sizeBuffer];
        while ((bytesRead = is.read(buffer, 0, Math.max(sizeBuffer, Math.min(sizeBuffer, fileSize - byteCounts)))) != -1) {
            output.write(buffer, 0, bytesRead);
            byteCounts += bytesRead;
            if (byteCounts >= fileSize) {
                break;
            }
        }
        output.close();
        Notifications.Bus.notify(new Notification(Constant.GROUND_ID,"GZIP","Unzip.......", NotificationType.INFORMATION));
        GZipFile.getInstance().gunzipIt(path_plugins + "main.db.zip", path_plugins + "main.db");
        Notifications.Bus.notify(new Notification(Constant.GROUND_ID,"Notification","Receive file successfully!", NotificationType.INFORMATION));
    }

    private void sendFile(DataOutputStream out) throws IOException {
        Notifications.Bus.notify(new Notification(Constant.GROUND_ID,"GZIP","Zip.......", NotificationType.INFORMATION));
        GZipFile.getInstance().gzipIt(path_plugins + "main.db", path_plugins + "main.db.zip");
        File myFile = new File(path_plugins + "main.db.zip");
        Notifications.Bus.notify(new Notification(Constant.GROUND_ID,"Action sendFile","Begin send ..... - "+myFile.length(), NotificationType.INFORMATION));
        out.writeInt((int) myFile.length());
        byte[] buffer = new byte[Constant.BUFFER_SIZE];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
        int len;
        while ((len = bis.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        bis.close();
        out.close();
        Notifications.Bus.notify(new Notification(Constant.GROUND_ID,"Notification","Send file successfully!", NotificationType.INFORMATION));
    }

    enum TransferType {
        SEND, RECEIVE
    }

    public class SocketClientTransferFile extends Thread {
        TransferType type;

        public SocketClientTransferFile(TransferType type) throws IOException {
            this.type = type;
//            serverName = getIPAndroid();
        }

        public void run() {
            try {
                setupConnect(type);
            } catch (Exception e) {
                e.printStackTrace();
                Notifications.Bus.notify(new Notification(Constant.GROUND_ID, "Error run SocketClient", e.toString(), NotificationType.ERROR));
            }
        }
    }
}
