package com.abhay.bloodradar.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.abhay.bloodradar.R;

public class ToastUtil {

    public enum Type {
        SUCCESS,
        ERROR,
        INFO
    }

    /**
     * Show custom toast message
     * @param context Context
     * @param message Message to display
     * @param type Type of toast (SUCCESS, ERROR, INFO)
     */
    public static void show(Context context, String message, Type type) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        LinearLayout container = layout.findViewById(R.id.toast_container);
        ImageView icon = layout.findViewById(R.id.toast_icon);
        TextView text = layout.findViewById(R.id.toast_message);

        // Set message
        text.setText(message);

        // Set icon and background based on type
        switch (type) {
            case SUCCESS:
                container.setBackgroundResource(R.drawable.toast_success_bg);
                icon.setImageResource(android.R.drawable.ic_dialog_info);
                break;
            case ERROR:
                container.setBackgroundResource(R.drawable.toast_error_bg);
                icon.setImageResource(android.R.drawable.ic_dialog_alert);
                break;
            case INFO:
                container.setBackgroundResource(R.drawable.toast_info_bg);
                icon.setImageResource(android.R.drawable.ic_dialog_info);
                break;
        }

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    /**
     * Show success toast
     */
    public static void showSuccess(Context context, String message) {
        show(context, message, Type.SUCCESS);
    }

    /**
     * Show error toast
     */
    public static void showError(Context context, String message) {
        show(context, message, Type.ERROR);
    }

    /**
     * Show info toast
     */
    public static void showInfo(Context context, String message) {
        show(context, message, Type.INFO);
    }
}
