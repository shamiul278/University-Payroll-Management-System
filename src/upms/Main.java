package upms;

import upms.ui.LoginFrame;
import upms.ui.util.Theme;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Theme.setLookAndFeel();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
