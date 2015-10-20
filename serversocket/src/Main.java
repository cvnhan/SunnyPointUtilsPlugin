import java.io.*;
import java.net.Socket;

public class Main extends Thread {
    String serverName = "localhost";
    int port = Integer.parseInt("1234");

    public Main() throws IOException {
//        serverName=getIPAndroid();
    }

    public void run() {

        try {
            System.out.println("Connecting to " + serverName +
                    " on port " + port);
            Socket client = new Socket(serverName, port);
            client.setSoTimeout(500);
            System.out.println("Just connected to "
                    + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
//            out.writeUTF("Hello from "
//                    + client.getLocalSocketAddress());
            out.writeUTF("getDB");
            InputStream inFromServer = client.getInputStream();
            DataInputStream in =
                    new DataInputStream(inFromServer);
            System.out.println("Server says " + in.readUTF());
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
//            Thread t = new Main(port);
//            t.start();
            Thread t = new ServerSocketTest(1234);
            t.start();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }

    public String getIPAndroid() {
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
            System.out.println(e.toString());
        }
        return host;
    }
}
