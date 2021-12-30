package xtvapps.core.android;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xtvapps.core.Callback;
import xtvapps.core.DialogFactory;
import xtvapps.core.InputDialogBuilder;
import xtvapps.core.ListOption;
import xtvapps.core.LocalContext;
import xtvapps.core.SimpleCallback;
import xtvapps.core.content.KeyValue;

@SuppressWarnings("unused")
public class AndroidStandardDialogs implements DialogFactory {

    @Override
    public void confirm(LocalContext context, String title, String message, String optYes, String optNo, final SimpleCallback callback) {
        AndroidLocalContext androidLocalContext = (AndroidLocalContext)context;
        new AlertDialog.Builder(androidLocalContext.getAndroidContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(optYes, (dialog, which) -> {
                    callback.onResult();
                    callback.onFinally();
                })
                .setNegativeButton(optNo, (dialog, which) -> {
                    callback.onError();
                    callback.onFinally();
                })
                .show();
    }

    @Override
    public void message(LocalContext context, String title, String message, final SimpleCallback callback) {
        AndroidLocalContext androidLocalContext = (AndroidLocalContext)context;
        AlertDialog.Builder dialog = new AlertDialog.Builder(androidLocalContext.getAndroidContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", (dialog12, which) -> {
                    if (callback!=null) callback.onResult();
                });

        dialog.setOnDismissListener(dialog1 -> {
            if (callback!=null) callback.onError();
        });

        dialog.show();
    }

    @Override
    public void select(LocalContext context, String title, List<ListOption> options, Callback<String> callback, SimpleCallback dismissCallback) {
        AndroidLocalContext androidLocalContext = (AndroidLocalContext)context;
        ArrayAdapter<ListOption> adapter = new ArrayAdapter<>(androidLocalContext.getAndroidContext(), android.R.layout.simple_list_item_1, options);
        select(context, title, adapter, callback, dismissCallback);
    }

    private void select(LocalContext context, String title, final BaseAdapter adapter, final Callback<String> callback, final SimpleCallback dismissCallback) {
        AndroidLocalContext androidLocalContext = (AndroidLocalContext)context;

        final ListView lv = new ListView(androidLocalContext.getAndroidContext());
        lv.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(androidLocalContext.getAndroidContext());
        builder.setTitle(title);
        builder.setView(lv);

        final AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            lv.requestFocus();
            lv.setSelection(0);
        });

        lv.setOnItemClickListener((viewAdapter, v, position, id) -> {
            KeyValue result = (KeyValue)adapter.getItem(position);
            alert.dismiss();
            callback.onResult(result.getKey());
        });

        alert.setOnDismissListener(dialog -> {
            if (dismissCallback!=null) dismissCallback.onResult();
        });

        alert.show();
    }

    public void input(LocalContext localContext, String title, String text, InputDialogBuilder inputDialogBuilder) {
        final Context context = ((AndroidLocalContext)localContext).getAndroidContext();

        final String[] suggestions = new String[0];
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, suggestions) {
            final List<String> list = new ArrayList<>();

            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public String getItem(int position) {
                return list.get(position);
            }

            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence text) {
                        FilterResults filterResults = new FilterResults();
                        list.clear();
                        if (text != null && inputDialogBuilder.suggestHandler!=null) {
                            String[] suggestions = inputDialogBuilder.suggestHandler.loadSuggestions(text.toString());
                            list.addAll(Arrays.asList(suggestions));
                        }
                        filterResults.values = list;
                        filterResults.count = list.size();
                        return filterResults;
                    }

                    @Override
                    protected void publishResults(CharSequence arg0,
                                                  FilterResults results) {
                        if (results != null && results.count > 0) {
                            notifyDataSetChanged();
                        } else {
                            notifyDataSetInvalidated();
                        }

                    }

                };
            }
        };
        final AutoCompleteTextView input = new AutoCompleteTextView(context);
        input.setThreshold(3);
        input.setSingleLine(true);
        input.setText(text);
        input.setSelection(text.length());
        input.setAdapter(adapter);

        if (inputDialogBuilder.inputFilters!=null) {
            InputFilter inputFilter = (source, start, end, dest, dStart, dEnd) -> {
                String input1 = source.subSequence(start, end).toString();
                if (input1.isEmpty()) return null;

                String output = inputDialogBuilder.inputFilters.filter(input1);
                if (input1.equals(output)) return null;

                return output;
            };
            input.setFilters(new InputFilter[] {inputFilter});
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(input);
        builder.setPositiveButton(context.getString(R.string.msg_default_ok), (dialog, whichButton) -> inputDialogBuilder.callback.onResult(input.getText().toString()));
        builder.setNegativeButton(context.getString(R.string.msg_default_cancel), (dialog, whichButton) -> {
        });

        final AlertDialog alert = builder.create();
        alert.setOnDismissListener(dialog -> AndroidCoreUtils.hideSoftKeyboard(context, input));

        input.setOnKeyListener(new View.OnKeyListener() {
            boolean keyboardShown = false;
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String text = input.getText().toString();
                boolean isEnter = keyCode == KeyEvent.KEYCODE_ENTER;
                if (!keyboardShown
                        && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                    keyboardShown = false;
                    AndroidCoreUtils.showSoftKeyboard(context, input);
                    return true;
                }

                if (isEnter) {
                    alert.dismiss();
                    inputDialogBuilder.callback.onResult(text);
                    return true;
                }
                return false;
            }
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        input.requestFocus();
        alert.show();

    }

    public static void custom(LocalContext localContext, String title, String optYes, String optNo, View v, SimpleCallback callback){
        final Context context = ((AndroidLocalContext)localContext).getAndroidContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(v);

        if (optYes !=null) {
            builder.setPositiveButton(optYes, (dialog, whichButton) -> {
                if (callback!=null) callback.onResult();
            });
        }

        if (optNo!=null) {
            builder.setNegativeButton(optNo, (dialog, whichButton) -> {
                if (callback!=null) callback.onError();
            });
        }

        final AlertDialog alert = builder.create();
        alert.show();
    }

    public static AlertDialog showAndGetCustomDialog(Context context, String title, View v, String optYes, String optNo, final SimpleCallback callback){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(v);
        builder.setPositiveButton(optYes, (dialog, whichButton) -> callback.onResult());
        builder.setNegativeButton(optNo, (dialog, whichButton) -> callback.onError());

        final AlertDialog alert = builder.create();
        alert.show();
        return alert;
    }

}
