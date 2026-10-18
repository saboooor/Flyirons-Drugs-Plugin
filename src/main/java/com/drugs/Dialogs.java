package com.drugs;

import com.drugs.DialogView.*;
import java.util.List;

public final class Dialogs {
    private Dialogs() {}

    public static DialogBase base(String title, String message) {
        return new DialogBase(new TextComponent(title))
                .body(List.of(new PlainMessageBody(new TextComponent(message), 350)));
    }

    public static ActionButton button(String label, String command, boolean dynamic) {
        return button(label, command, dynamic, Icon.INFO, "");
    }

    public static ActionButton button(String label, String command, boolean dynamic, Icon icon) {
        return button(label, command, dynamic, icon, "");
    }

    public static ActionButton button(String label, String command, boolean dynamic, Icon icon, String lore) {
        Action action = new Action(command, dynamic);
        return new ActionButton(new TextComponent(label), action, 300, icon, lore);
    }

    public static MultiActionDialog menu(DialogBase base, List<ActionButton> actions) {
        return new MultiActionDialog(base, actions, 1, button("Close", "/drugs guide close", false, Icon.CLOSE, "Close this dialog."));
    }

    public static MultiActionDialog menu(DialogBase base, List<ActionButton> actions, ActionButton exit) {
        return new MultiActionDialog(base, actions, 1, exit);
    }
}

