package vn.penkun.obd2scantool.hexkeyboardlibrary;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.Spanned;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.Arrays;

public class CHexKeyboard {
    InputFilter mCharFilter;
    private Activity mHostActivity;
    private KeyboardView mKeyboardView;
    private OnKeyboardActionListener mOnKeyboardActionListener;
    private ArrayList<View> mViewList;

    /* renamed from: com.yatrim.hexkeyboardlibrary.CHexKeyboard$1 */
    class C02881 implements OnKeyboardActionListener {
        public static final int CODE_DELETE = 55010;
        public static final int CodeAllLeft = 55001;
        public static final int CodeAllRight = 55004;
        public static final int CodeBackspace = -5;
        public static final int CodeCancel = -3;
        public static final int CodeClear = 55006;
        public static final int CodeLeft = 55002;
        public static final int CodeNext = 55005;
        public static final int CodePrev = 55000;
        public static final int CodeRight = 55003;

        C02881() {
        }

        public void onKey(int primaryCode, int[] keyCodes) {
            View focusCurrent = CHexKeyboard.this.mHostActivity.getWindow().getCurrentFocus();
            if (focusCurrent != null && focusCurrent.getClass() == EditText.class) {
                EditText edittext = (EditText) focusCurrent;
                Editable editable = edittext.getText();
                int selStart = edittext.getSelectionStart();
                int selEnd = edittext.getSelectionEnd();
                int textLength = edittext.length();
                View focusNew;
                switch (primaryCode) {
                    case CodeBackspace /*-5*/:
                        if (editable != null && selStart > 0) {
                            editable.delete(selStart - 1, selStart);
                            return;
                        }
                        return;
                    case CodeCancel /*-3*/:
                        CHexKeyboard.this.hideCustomKeyboard();
                        return;
                    case CodePrev /*55000*/:
                        focusNew = edittext.focusSearch(1);
                        if (focusNew != null) {
                            focusNew.requestFocus();
                            return;
                        }
                        return;
                    case CodeAllLeft /*55001*/:
                        edittext.setSelection(0);
                        return;
                    case CodeLeft /*55002*/:
                        if (selStart > 0) {
                            edittext.setSelection(selStart - 1);
                            return;
                        }
                        return;
                    case CodeRight /*55003*/:
                        if (selStart < textLength) {
                            edittext.setSelection(selStart + 1);
                            return;
                        }
                        return;
                    case CodeAllRight /*55004*/:
                        edittext.setSelection(textLength);
                        return;
                    case CodeNext /*55005*/:
                        focusNew = edittext.focusSearch(2);
                        if (focusNew == null) {
                            CHexKeyboard.this.hideCustomKeyboard();
                            return;
                        } else if (CHexKeyboard.this.mViewList.size() < 1 || !((View) CHexKeyboard.this.mViewList.get(0)).equals(focusNew)) {
                            focusNew.requestFocus();
                            return;
                        } else {
                            CHexKeyboard.this.hideCustomKeyboard();
                            return;
                        }
                    case CodeClear /*55006*/:
                        if (editable != null) {
                            editable.clear();
                            return;
                        }
                        return;
                    case CODE_DELETE /*55010*/:
                        if (editable != null && selStart < textLength) {
                            editable.delete(selStart, selStart + 1);
                            return;
                        }
                        return;
                    default:
                        editable.insert(selStart, Character.toString((char) primaryCode));
                        return;
                }
            }
        }

        public void onPress(int arg0) {
        }

        public void onRelease(int primaryCode) {
        }

        public void onText(CharSequence text) {
        }

        public void swipeDown() {
        }

        public void swipeLeft() {
        }

        public void swipeRight() {
        }

        public void swipeUp() {
        }
    }

    /* renamed from: com.yatrim.hexkeyboardlibrary.CHexKeyboard$2 */
    class C02892 implements OnFocusChangeListener {
        C02892() {
        }

        public void onFocusChange(View v, boolean hasFocus) {
            EditText edittext = (EditText) v;
            if (hasFocus && edittext.isEnabled()) {
                CHexKeyboard.this.showCustomKeyboard(v);
            } else {
                CHexKeyboard.this.hideCustomKeyboard();
            }
        }
    }

    /* renamed from: com.yatrim.hexkeyboardlibrary.CHexKeyboard$3 */
    class C02903 implements OnClickListener {
        C02903() {
        }

        public void onClick(View v) {
            CHexKeyboard.this.showCustomKeyboard(v);
        }
    }

    /* renamed from: com.yatrim.hexkeyboardlibrary.CHexKeyboard$4 */
    class C02914 implements OnTouchListener {
        C02914() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            EditText edittext = (EditText) v;
            int pos = -1;
            if (event.getAction() == 1) {
                Layout layout = edittext.getLayout();
                float x = event.getX() + ((float) edittext.getScrollX());
                float y = event.getY() + ((float) edittext.getScrollY());
                if (layout != null) {
                    pos = layout.getOffsetForHorizontal(layout.getLineForVertical((int) y), x);
                    if (pos < 0) {
                        pos = 0;
                    }
                }
            }
            int inType = edittext.getInputType();
            edittext.setInputType(0);
            edittext.onTouchEvent(event);
            edittext.setInputType(inType);
            if (event.getAction() == 1 && pos >= 0) {
                edittext.setSelection(pos);
            }
            return true;
        }
    }

    /* renamed from: com.yatrim.hexkeyboardlibrary.CHexKeyboard$5 */
    class C02925 implements InputFilter {
        C02925() {
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String res = "";
            int i = 0;
            while (i < source.length()) {
                try {
                    Character c = Character.valueOf(source.charAt(i));
                    if (Character.isDigit(c.charValue())) {
                        res = res + c;
                    } else if (Character.isLetter(c.charValue()) && c.toString().matches("[a-fA-F]")) {
                        res = res + Character.toUpperCase(c.charValue());
                    }
                    i++;
                } catch (Exception e) {
                }
            }
            return res;
        }
    }

    public CHexKeyboard(Activity host, int viewid, int layoutid) {
        this.mOnKeyboardActionListener = new C02881();
        this.mCharFilter = new C02925();
        this.mHostActivity = host;
        this.mKeyboardView = (KeyboardView) this.mHostActivity.findViewById(viewid);
        this.mKeyboardView.setKeyboard(new Keyboard(this.mHostActivity, layoutid));
        this.mKeyboardView.setPreviewEnabled(false);
        this.mKeyboardView.setOnKeyboardActionListener(this.mOnKeyboardActionListener);
        this.mKeyboardView.setFocusable(false);
        this.mKeyboardView.setFocusableInTouchMode(false);
        this.mHostActivity.getWindow().setSoftInputMode(3);
        this.mViewList = new ArrayList();
    }

    public CHexKeyboard(Activity host, int viewid) {
        this(host, viewid, C0293R.xml.hexkbd);
    }

    public boolean isCustomKeyboardVisible() {
        return this.mKeyboardView.getVisibility() == 0;
    }

    public void showCustomKeyboard(View v) {
        this.mKeyboardView.setVisibility(0);
        this.mKeyboardView.setEnabled(true);
        if (v != null) {
            ((InputMethodManager) this.mHostActivity.getSystemService("input_method")).hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public void hideCustomKeyboard() {
        this.mKeyboardView.setVisibility(8);
        this.mKeyboardView.setEnabled(false);
    }

    public void registerEditText(int resid) {
        EditText edittext = (EditText) this.mHostActivity.findViewById(resid);
        edittext.setOnFocusChangeListener(new C02892());
        edittext.setOnClickListener(new C02903());
        edittext.setOnTouchListener(new C02914());
        edittext.setInputType(edittext.getInputType() | 524288);
        ArrayList<InputFilter> curInputFilters = new ArrayList(Arrays.asList(edittext.getFilters()));
        curInputFilters.add(this.mCharFilter);
        edittext.setFilters((InputFilter[]) curInputFilters.toArray(new InputFilter[curInputFilters.size()]));
        this.mViewList.add(edittext);
    }
}
