import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.io.File;
import java.io.IOException;

/**
 * Created by NhanCao on 16-Oct-15.
 */
public class GetDbRooted extends AnAction {

    Runtime runtime = Runtime.getRuntime();
    String path_plugins = "";

    public GetDbRooted() {
        super();
//            file:/C:/Users/NhanCao/.IntelliJIdea14/config/plugins/SunnyPoint/lib/SunnyPoint.jar!/config.txt
//            cmd.exe /c cd C:\Users\NhanCao\.IntelliJIdea14\config\plugins\SunnyPoint\lib && jar xf SunnyPoint.jar sqlitebrowser
        try {
            path_plugins = this.getClass().getResource("config.txt").getPath().replace("file:/", "").replace("SunnyPoint.jar!/config.txt", "");
            //extract sqlitebrowser in jar file
            File f = new File(path_plugins + "sqlitebrowser");
            if (!f.exists()) {
                runtime.exec("cmd.exe /c cd " + path_plugins + " && jar xf SunnyPoint.jar sqlitebrowser");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(AnActionEvent event) {
        try {

            Project project = event.getData(PlatformDataKeys.PROJECT);
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
            String packagename = Messages.showInputDialog(project, "What is your package name?", "Input your package name", Messages.getQuestionIcon());
            if (packagename == null || packagename.length() == 0) {
                packagename = "com.lcl.sunnypoints";
            }
            File f = new File(path_plugins + "sqlitebrowser");
            if (f.exists() && f.isDirectory()) {
                runtime.exec("TASKKILL /F /IM sqliteman.exe");
                runtime.exec(path_plugins + "sqlitebrowser/getdb.exe " + path_plugins + " " + packagename);
            }
        } catch (Exception e) {
            e.printStackTrace();
//            Messages.showErrorDialog(e.toString(), "error");
        }
    }

}
