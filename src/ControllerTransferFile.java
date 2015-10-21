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
    private static boolean isWakeupNeeded = false;
    private final Lock lock = new Lock();
    private Runtime runtime = Runtime.getRuntime();
    private String path_plugins = "";
    private int port = 1234;
    private String serverName;

    public ControllerTransferFile() {
        path_plugins = this.getClass().getResource("sqlitebrowser").getPath().replace("file:/", "").replace("SunnyPoint.jar!/sqlitebrowser", "");
//        Messages.showErrorDialog(path_plugins + "sqlitebrowser/config.txt", "path");
        extractResource();
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
            Messages.showErrorDialog(e.toString(), "error");
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
            //System.out.println(e.toString());
            Messages.showErrorDialog(e.toString(), "UnRoot error");

        }
    }

    public void actionSocketReceiveFile(AnActionEvent event) {
        inputIPAddr();
        try {
            extractResource();
            Thread t = new SocketClientTransferFile(TransferType.RECEIVE);
            t.start();
            synchronized (lock) {
                while (!isWakeupNeeded) {
                    lock.wait();
                }
                if (lock.success == false) {
                    Messages.showErrorDialog("Action Error", "Socket error");
                }
                isWakeupNeeded = false;

            }
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println(e.toString());
            Messages.showErrorDialog(e.toString(), "Socket error");
        }
    }

    public void actionSocketSendFile(AnActionEvent event) {
        inputIPAddr();
        try {
            extractResource();
            Thread t = new SocketClientTransferFile(TransferType.SEND);
            t.start();
            synchronized (lock) {
                while (!isWakeupNeeded) {
                    lock.wait();
                }
                if (lock.success == false) {
                    Messages.showErrorDialog("Action Error", "Socket error");
                }
                isWakeupNeeded = false;

            }
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println(e.toString());
            Messages.showErrorDialog(e.toString(), "Socket error");
        }
    }

    private void setupConnect(TransferType type) {
        //System.out.println("Connecting to " + serverName + " on port " + port);
        Socket client = null;
        try {
            client = new Socket(serverName, port);
        } catch (IOException e) {
            e.printStackTrace();
            lock.success = false;
            return;
        }
        try {
            //System.out.println("Just connected to " + client.getRemoteSocketAddress());

            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            if (type == TransferType.RECEIVE) {
                out.writeUTF(GETFILESK_COMMAND);
                InputStream inFromServer = client.getInputStream();
                DataInputStream in =
                        new DataInputStream(inFromServer);
                int fileSize = in.readInt();
                receiveFile(client, fileSize);
                actionOpenFile();
            } else if (type == TransferType.SEND) {
                out.writeUTF(SETFILESK_COMMAND);
                sendFile(client, out);
            }
            if (client != null)
                client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lock.success = true;
    }

    private void inputIPAddr() {
        String hostStr = Messages.showInputDialog("What is your host?", "Input your host address", Messages.getQuestionIcon(), getIPAndroid() + ":" + port, null);
        int index = hostStr.indexOf(":");
        serverName = hostStr.substring(0, index);
        port = Integer.parseInt(hostStr.substring(index + 1));
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
                if (line.contains("0x00001043")) {
//                    wlan0    UP                                192.168.1.79/24  0x00001043 b4:52:7d:c5:8b:69
                    int index = line.indexOf("/24");
                    line = line.substring(0, index);
                    index = line.lastIndexOf(" ");
                    host = line.substring(index + 1);
                    break;
                }
                if (line == null) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            //System.out.println(e.toString());
        }
        return host;
    }

    private void receiveFile(Socket client, int fileSize) throws IOException {
        InputStream is = client.getInputStream();
        int bytesRead;
        int byteCounts = 0;
        OutputStream output = new FileOutputStream(path_plugins + "main.db.zip");
        int sizeBuffer = 8096;
        byte[] buffer = new byte[sizeBuffer];
        while ((bytesRead = is.read(buffer, 0, Math.max(sizeBuffer, Math.min(sizeBuffer, fileSize - byteCounts)))) != -1) {
            output.write(buffer, 0, bytesRead);
            byteCounts += bytesRead;
            if (byteCounts >= fileSize) {
                break;
            }
        }
        output.close();
        GZipFile.getInstance().gunzipIt(path_plugins + "main.db.zip", path_plugins + "main.db");
    }

    private void sendFile(Socket client, DataOutputStream out) throws IOException {
        GZipFile.getInstance().gzipIt(path_plugins + "main.db", path_plugins + "main.db.zip");
        File myFile = new File(path_plugins + "main.db.zip");
        out.writeInt((int) myFile.length());
        byte[] mybytearray = new byte[(int) myFile.length()];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
        bis.read(mybytearray, 0, mybytearray.length);
        OutputStream os = client.getOutputStream();
        os.write(mybytearray, 0, mybytearray.length);
        os.flush();
        bis.close();
        os.close();
    }

    enum TransferType {
        SEND, RECEIVE
    }

    private static final class Lock {
        public boolean success = false;
        public String message="";
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
                isWakeupNeeded = false;
            } catch (Exception e) {
                //System.out.println(e.toString());
                e.printStackTrace();
            } finally {
                synchronized (lock) {
                    isWakeupNeeded = true;
                    lock.notifyAll();
                }
            }
        }
    }
}
