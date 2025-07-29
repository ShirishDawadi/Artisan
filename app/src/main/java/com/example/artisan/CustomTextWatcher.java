package com.example.artisan;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

public class CustomTextWatcher implements TextWatcher {
    private EditText mEditText;
    private TextView mCounterTextView;
    private int mMaxLength;
    public CustomTextWatcher(EditText editText, TextView counterTextView, int maxLength) {
        this.mEditText = editText;
        this.mCounterTextView = counterTextView;
        this.mMaxLength = maxLength;
    }
    public CustomTextWatcher(EditText editText, int maxLength) {
        this.mEditText = editText;
        this.mMaxLength = maxLength;
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        int currentLength = s.length();
        if (mCounterTextView != null) {
            mCounterTextView.setText(currentLength + "/" + mMaxLength);
        }
        if (currentLength > mMaxLength) {
            mEditText.setText(s.subSequence(0, mMaxLength));
            mEditText.setSelection(mMaxLength);
        }
    }
    @Override
    public void afterTextChanged(Editable s) {
    }
}