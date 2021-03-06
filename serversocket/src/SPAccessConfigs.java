/**
 * Created by NhanCao on 19-Oct-15.
 */
public class SPAccessConfigs {
    public static final String GETDP_COMMAND = "getDB";
    //config socket comm
    public static int PORT = 1234;
    private static boolean ENABLE_LOG = true;
    //register option
    private static boolean REGISTER_BACKUP = true;
    private static String DBNAME = "main.db";
    private static String PATH;

    public static boolean isEnableLog() {
        return ENABLE_LOG;
    }

    public static void setEnableLog(boolean enableLog) {
        ENABLE_LOG = enableLog;
    }

    public static void loge(String msg) {
        if (isEnableLog()) {
            System.out.println(msg);
        }
    }

    public static boolean isRegisterBackup() {
        return REGISTER_BACKUP;
    }

    public static void setRegisterBackup(boolean registerBackup) {
        REGISTER_BACKUP = registerBackup;
    }

    public static String getDBNAME() {
        return DBNAME;
    }

    public static String getPATH() {
        return PATH;
    }



}
