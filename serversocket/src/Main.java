import java.io.*;
import java.net.Socket;

public class Main extends Thread {
    String serverName = "192.168.1.79";
    int port = Integer.parseInt("1234");

    public Main() throws IOException {
//        serverName=getIPAndroid();
    }

    public synchronized void run() {

        try {
            System.out.println("Connecting to " + serverName +
                    " on port " + port);
            Socket client = new Socket(serverName, port);
            client.setSoTimeout(500);
            System.out.println(client.getSoTimeout() + "= timeout");
            System.out.println("Just connected to "
                    + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            InputStream inFromServer = client.getInputStream();
            DataInputStream in =
                    new DataInputStream(inFromServer);
//            out.writeUTF("Hello from "
//                    + client.getLocalSocketAddress());
//            out.writeUTF("getDB");
//            out.writeUTF("setFilesk");


            ///////////////get file//////////////////
            out.writeUTF("getFilesk");
            int fileSize = in.readInt();
            System.out.println("Server says " + fileSize);
            receiveFile(client, fileSize);
//            String response = in.readUTF();
//            System.out.println(response);
            System.out.println("OK");

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }

    public static void main(String[] args) {
        try {
            Thread t = new Main();
            t.start();
//            Thread t = new ServerSocketTest(1234);
//            t.start();

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


    private void receiveFile(Socket client, int fileSize) throws IOException {
        InputStream is = client.getInputStream();
        int bytesRead;
        int byteCounts = 0;
        OutputStream output = new FileOutputStream("D:/main.db");
        int sizeBuffer=1024;
        byte[] buffer = new byte[sizeBuffer];
        System.out.println("start " + fileSize);
        while ((bytesRead = is.read(buffer, 0, Math.max(sizeBuffer, Math.min(sizeBuffer, fileSize - byteCounts)))) != -1) {
            output.write(buffer, 0, bytesRead);
            byteCounts += bytesRead;
            if (byteCounts >= fileSize) {
                break;
            }
        }
        output.close();
    }

    private void sendFile(Socket client, DataOutputStream out) throws IOException {
        File myFile = new File("D:/main.db");
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
}
