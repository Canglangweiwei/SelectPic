package com.dev.testselectpics;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.dev.testselectpics.app.AppApplication;

import butterknife.ButterKnife;

@SuppressWarnings("ALL")
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppApplication.get(getApplicationContext()).pushActivityToStack(this);
        setContentView(initContentView());
        ButterKnife.bind(this);
        initUi();
        initDatas();
        initListener();
    }

    /**
     * 页面绑定
     */
    protected abstract int initContentView();

    /**
     * 初始化UI
     */
    protected abstract void initUi();

    /**
     * 初始化数据
     */
    protected abstract void initDatas();

    /**
     * 初始化监听事件
     */
    protected abstract void initListener();

    protected void showToast(int resId) {
        Toast.makeText(BaseActivity.this, resId, Toast.LENGTH_SHORT).show();
    }

    protected void showToast(String text) {
        Toast.makeText(BaseActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    protected void showToastLong(String text) {
        Toast.makeText(BaseActivity.this, text, Toast.LENGTH_LONG).show();
    }

    /**
     * 获取点击事件
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (isHideInput(view, ev)) {
                HideSoftInput(view.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判定是否需要隐藏软键盘
     */
    private boolean isHideInput(View v, MotionEvent ev) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
            if (ev.getX() > left && ev.getX() < right && ev.getY() > top && ev.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * 隐藏软键盘
     */
    private void HideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
