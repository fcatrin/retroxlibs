package xtvapps.core;

import java.util.List;

public interface DialogFactory {
    void confirm(LocalContext context, String title, String message, String optYes, String optNo, SimpleCallback callback);
    void message(LocalContext context, String title, String message, SimpleCallback callback);
    void select(LocalContext context, String title, List<ListOption> options, Callback<String> callback, SimpleCallback dismissCallback);
    void input(LocalContext context, String title, String text, InputDialogBuilder inputDialogBuilder);
}
