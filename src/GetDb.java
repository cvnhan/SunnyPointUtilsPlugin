import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.io.*;
import java.util.Scanner;

/**
 * Created by NhanCao on 16-Oct-15.
 */
public class GetDb extends AnAction {

    public GetDb() {
        super();

    }

    public void actionPerformed(AnActionEvent event) {
        try {
            Project project = event.getData(PlatformDataKeys.PROJECT);

            String path=project.getBasePath()+"\n";
            path+=project.getProjectFilePath() + "\n";
            path+=project.getWorkspaceFile().getPath()+"\n";
            path+= this.getClass().getClassLoader().getResource("test.txt").getPath()+"\n";
            path+= this.getClass().getResource("test.txt").getFile()+"\n";
            path+= this.getClass().getClassLoader().getResource("test.txt").getHost()+"\n";
            path+= this.getClass().getClassLoader().getResource("test.txt").toURI()+"\n";




            Messages.showMessageDialog(project, path, "Information", Messages.getInformationIcon());

            String s= null;
            try {
                InputStream input = this.getClass().getResourceAsStream("test.txt");
                Scanner scanner = new Scanner(input);
                s = "";
                while(scanner.hasNext()){
                    s+=scanner.nextLine()+"\n";
                }

                InputStream is = this.getClass().getClassLoader().getResourceAsStream("test.txt");
                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                String str;
                while ((str = in.readLine()) != null) {
                    s+=str+"\n";
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Messages.showMessageDialog(project, s, "Information", Messages.getInformationIcon());

            Process process = Runtime.getRuntime().exec("./Sqliteman-1.2.2/sqliteman.exe");
//            Process process = Runtime.getRuntime().exec(path + "sqliteman.exe " + path + "main.db");
//            watch(process);
//

//            String txt= Messages.showInputDialog(project, "What is your name?", "Input your name", Messages.getQuestionIcon());
//            Messages.showMessageDialog(project, "Hello, " + txt + "!\n I am glad to see you.", "Information", Messages.getInformationIcon());




        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private static void watch(final Process process) {
        new Thread(new Runnable() {
            public void run() {
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                try {
                    while ((line = input.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
