package xtvapps.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class DialogUtils {
    public static void confirm(LocalContext context, String title, String message, String optYes, String optNo, SimpleCallback callback) {
        AppContext.dialogFactory.confirm(context, title, message, optYes, optNo, callback);
    }

    public static void message(LocalContext context, String title, String message, SimpleCallback callback) {
        AppContext.dialogFactory.message(context, title, message, callback);
    }

    public static void select(LocalContext context, String title, List<ListOption> options, Callback<String> callback, SimpleCallback dismissCallback) {
        AppContext.dialogFactory.select(context, title, options, callback, dismissCallback);
    }

    public static void input(LocalContext context, String title, String text, InputDialogBuilder inputDialogBuilder) {
        AppContext.dialogFactory.input(context, title, text, inputDialogBuilder);
    }

    public static void message(LocalContext context, String message) {
        message(context, null, message, null);
    }

    public static void message(LocalContext context, String title, String message) {
        message(context, title, message, null);
    }

    public static void confirm(LocalContext context, String message, String optYes, String optNo, SimpleCallback callback) {
        confirm(context, null, message, optYes, optNo, callback);
    }

    public static void select(LocalContext context, String title, List<ListOption> options, Callback<String> callback) {
        select(context, title, options, callback, null);
    }

    public static void selectByIndex(LocalContext context, String title, List<ListOption> options, final Callback<Integer> callback) {
        select(context, title, options, new Callback<String>() {
            @Override
            public void onResult(String result) {
                callback.onResult(CoreUtils.str2i(result));
            }
        }, null);
    }

    public static void input(LocalContext context, String title, Callback<String> callback) {
        input(context, title, "", callback, null);
    }

    public static void input(LocalContext context, String title, String text, Callback<String> callback) {
        input(context, title, text, callback, null);
    }

    public static void input(LocalContext context, String title, String text, Callback<String> callback, SuggestHandler suggestHandler) {
        InputDialogBuilder inputDialogBuilder = new InputDialogBuilder();
        inputDialogBuilder.callback = callback;
        inputDialogBuilder.suggestHandler = suggestHandler;
        input(context, title, text, inputDialogBuilder);
    }

    public static void showException(LocalContext context, Throwable e, SimpleCallback callback) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Throwable cause = e.getCause();
        if (cause!=null) {
            cause.printStackTrace(pw);
        } else {
            e.printStackTrace(pw);
        }

        String msg =  e.getMessage() + "\n" + sw.toString();
        message(context, "Error", msg, callback);
    }

}
