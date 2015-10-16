import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

import java.io.File;
import java.io.IOException;

/**
 * Created by NhanCao on 16-Oct-15.
 */
public class GetDb extends AnAction {

    Runtime runtime = Runtime.getRuntime();
    String path_plugins = "";

    public GetDb() {
        super();
//            file:/C:/Users/NhanCao/.IntelliJIdea14/config/plugins/SunnyPoint/lib/SunnyPoint.jar!/config.txt
//            cmd.exe /c cd C:\Users\NhanCao\.IntelliJIdea14\config\plugins\SunnyPoint\lib && jar xf SunnyPoint.jar sqlitebrowser
        path_plugins = this.getClass().getResource("config.txt").getPath().replace("file:/", "").replace("SunnyPoint.jar!/config.txt", "");
        extractResource();
    }
    public void extractResource(){
        try {
            //extract sqlitebrowser in jar file
            File f = new File(path_plugins+"sqlitebrowser");
            if (!f.exists()) {
                runtime.exec("cmd.exe /c cd " + path_plugins + " && jar xf SunnyPoint.jar sqlitebrowser");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Messages.showErrorDialog(e.toString(), "error");
        }
    }

    public void actionPerformed(AnActionEvent event) {
        try {
//            Project project = event.getData(PlatformDataKeys.PROJECT);
//            String s = null;
//            try {
//                InputStream input = this.getClass().getResourceAsStream("config.txt");
//                Scanner scanner = new Scanner(input);
//                s = "";
//                while (scanner.hasNext()) {
//                    s += scanner.nextLine() + "\n";
//                }
//                Messages.showMessageDialog(project, s, "Information", Messages.getInformationIcon());
//            } catch (Exception e) {
//                e.printStackTrace();
//                Messages.showErrorDialog(e.toString(), "error");
//
//            }
//            Runtime.getRuntime().exec("adb pull sdcard/main.db " + path_plugins + "main.db");
            extractResource();
            File f = new File(path_plugins+"sqlitebrowser");
            if (f.exists() && f.isDirectory()) {
                runtime.exec("TASKKILL /F /IM sqliteman.exe");
                runtime.exec(path_plugins + "sqlitebrowser/getdb.exe " + path_plugins);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Messages.showErrorDialog(e.toString(), "error");

        }
    }

}