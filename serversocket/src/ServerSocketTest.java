import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by NhanCao on 20-Oct-15.
 */
public class ServerSocketTest extends Thread {
    public static final String GETFILESK_COMMAND = "getFilesk";
    public static final String SETFILESK_COMMAND = "setFilesk";
    private static ServerSocketTest instance;
    private ServerSocket serverSocket;

    public ServerSocketTest(int port) throws IOException {
        serverSocket = new ServerSocket(port);
//        serverSocket.setSoTimeout(10000);
    }

    private static ServerSocketTest getInstance() throws IOException {
        if (instance == null) {
            instance = new ServerSocketTest(SPAccessConfigs.PORT);
        }
        return instance;
    }

    public static void startDaemon(Integer port) {
        if (port != null)
            SPAccessConfigs.PORT = port;
        try {
            getInstance().start();
        } catch (IOException e) {
            e.printStackTrace();
            SPAccessConfigs.loge(e.toString());
        }
    }

    public void run() {
        while (true) {
            try {
                SPAccessConfigs.loge("Waiting for client on port " +
                        serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                SPAccessConfigs.loge("Server is: " + server.getLocalSocketAddress() + " just connected to "
                        + server.getRemoteSocketAddress());
                DataInputStream in =
                        new DataInputStream(server.getInputStream());
                DataOutputStream out =
                        new DataOutputStream(server.getOutputStream());
                solveOptions(server,in, out);
                server.close();
            } catch (SocketTimeoutException s) {
                SPAccessConfigs.loge("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void solveOptions(Socket socket, DataInputStream in, DataOutputStream out) throws IOException {
        String input= in.readUTF();
        SPAccessConfigs.loge(input);
        try {
            if (input.equals(SPAccessConfigs.GETDP_COMMAND) && SPAccessConfigs.isRegisterBackup()){
                out.writeUTF("Ok - " + socket.getLocalSocketAddress());
            }else if(input.equals(GETFILESK_COMMAND)){
                sendFile(socket, out);
            }else if(input.contains(SETFILESK_COMMAND)){
                int fileSize=in.readInt();
                receiveFile(socket,fileSize);
                out.writeUTF("Ok - " + socket.getLocalSocketAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFile(Socket client, int fileSize) throws IOException{
        byte[] mybytearray = new byte[fileSize];
        InputStream is = client.getInputStream();
        FileOutputStream fos = new FileOutputStream("D:/main.db");
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        int bytesRead = is.read(mybytearray, 0, mybytearray.length);
        bos.write(mybytearray, 0, bytesRead);
        bos.close();
    }

    private void sendFile(Socket client, DataOutputStream out) throws IOException{
        File myFile = new File("D:/test.txt");
        out.writeInt((int) myFile.length());
        byte[] mybytearray = new byte[(int) myFile.length()];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
        bis.read(mybytearray, 0, mybytearray.length);
        OutputStream os = client.getOutputStream();
        os.write(mybytearray, 0, mybytearray.length);
        os.flush();
    }

}
