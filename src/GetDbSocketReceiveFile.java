import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

/**
 * Created by NhanCao on 16-Oct-15.
 */
public class GetDbSocketReceiveFile extends AnAction {

    public GetDbSocketReceiveFile() {
        super();
        ControllerTransferFile.getInstance();
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
    }

    public void actionPerformed(AnActionEvent event) {
        try {
            ControllerTransferFile.getInstance().actionSocketReceiveFile(event);
        } catch (Exception e) {
            e.printStackTrace();
            Messages.showErrorDialog(e.toString(), "error");

        }
    }

}
