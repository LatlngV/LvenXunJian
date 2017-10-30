package cn.eyesw.lvenxunjian.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;

/**
 * Dialog 的工具类
 */
public class DialogUtil {

    /**
     * 没有自定义样式的 Dialog
     */
    public static Dialog getDialog(Activity activity, int layoutRes) {
        Dialog dialog  = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = View.inflate(activity, layoutRes, null);
        dialog.setContentView(view);
        dialog.show();
        return dialog;
    }

    /**
     * 有自定义样式的 Dialog
     */
    public static Dialog getStyleDialog(Activity activity, int themeResId, int layoutRes) {
        Dialog dialog  = new Dialog(activity, themeResId);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = View.inflate(activity, layoutRes, null);
        dialog.setContentView(view);
        dialog.show();
        return dialog;
    }

    /**
     * 带有标题的 Dialog
     */
    public static Dialog getTitleDialog(Activity activity, int layoutRes, String title){
        Dialog dialog  = new Dialog(activity);
        dialog.setTitle(title);
        View view = View.inflate(activity, layoutRes, null);
        dialog.setContentView(view);
        dialog.show();
        return dialog;
    }

    public static void getAlertDialog(Activity activity, String title, String msg, OnClickListener onClickListener) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("确定", onClickListener::onClick)
                .setNegativeButton("取消", null)
                .show();
    }

    public interface OnClickListener{
        void onClick(DialogInterface dialog, int which);
    }

}
