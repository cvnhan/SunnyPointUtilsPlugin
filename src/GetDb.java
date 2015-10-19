import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

/**
 * Created by NhanCao on 16-Oct-15.
 */
public class GetDb extends AnAction {

    public GetDb() {
        super();
        Controller.getInstance();
    }

    public void actionPerformed(AnActionEvent event) {
        try {
            Controller.getInstance().actionUnRoot(event);
        } catch (Exception e) {
            e.printStackTrace();
            Messages.showErrorDialog(e.toString(), "error");
        }
    }

}
