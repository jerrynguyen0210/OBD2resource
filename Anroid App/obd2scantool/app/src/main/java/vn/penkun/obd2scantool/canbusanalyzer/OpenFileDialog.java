package vn.penkun.obd2scantool.canbusanalyzer;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OpenFileDialog extends Builder {
    private String accessDeniedMessage;
    private String currentPath = Environment.getExternalStorageDirectory().getPath();
    private Drawable fileIcon;
    private FilenameFilter filenameFilter;
    private List<File> files = new ArrayList();
    private Drawable folderIcon;
    private OpenDialogListener listener;
    private View mBackView;
    private Button mBtPositive;
    private EditText mEdInputName;
    private ViewGroup mEditGroup;
    private ListView mFileListView;
    private boolean mIsDirectorySelection = false;
    private boolean mIsEditMode = false;
    private boolean mIsKeepFileName = false;
    private String mSelectButtonCaption;
    private int mSelectedIndex = -1;
    private String mStartFileName = null;
    private TextView mTvInputPath;
    private TextView title;

    /* renamed from: com.yatrim.canbusanalyzer.OpenFileDialog$1 */
    class C02721 implements OnClickListener {
        C02721() {
        }

        public void onClick(DialogInterface dialog, int which) {
            if (OpenFileDialog.this.listener == null) {
                return;
            }
            if (OpenFileDialog.this.mIsDirectorySelection) {
                OpenFileDialog.this.listener.OnSelectedFile(OpenFileDialog.this.currentPath);
            } else if (OpenFileDialog.this.mIsEditMode) {
                OpenFileDialog.this.listener.OnSelectedFile(OpenFileDialog.this.currentPath + "/" + OpenFileDialog.this.mEdInputName.getText().toString());
            } else if (OpenFileDialog.this.mSelectedIndex > -1) {
                OpenFileDialog.this.listener.OnSelectedFile(OpenFileDialog.this.mFileListView.getItemAtPosition(OpenFileDialog.this.mSelectedIndex).toString());
            }
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.OpenFileDialog$3 */
    class C02743 implements OnKeyListener {
        C02743() {
        }

        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (OpenFileDialog.this.isSelectedIndexSet()) {
                OpenFileDialog.this.clearSelectedIndex();
                ((FileAdapter) OpenFileDialog.this.mFileListView.getAdapter()).notifyDataSetChanged();
            }
            return false;
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.OpenFileDialog$4 */
    class C02754 implements OnEditorActionListener {
        C02754() {
        }

        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            boolean z;
            Button access$1100 = OpenFileDialog.this.mBtPositive;
            if (v.getText().length() > 0) {
                z = true;
            } else {
                z = false;
            }
            access$1100.setEnabled(z);
            return false;
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.OpenFileDialog$5 */
    class C02765 implements OnTouchListener {
        C02765() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (!v.isFocusable()) {
                OpenFileDialog.this.mEdInputName.setFocusableInTouchMode(true);
                OpenFileDialog.this.mEdInputName.requestFocus();
            }
            return false;
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.OpenFileDialog$6 */
    class C02776 implements View.OnClickListener {
        C02776() {
        }

        public void onClick(View view) {
            File parentDirectory = new File(OpenFileDialog.this.currentPath).getParentFile();
            if (parentDirectory != null) {
                OpenFileDialog.this.currentPath = parentDirectory.getPath();
                OpenFileDialog.this.RebuildFiles((FileAdapter) OpenFileDialog.this.mFileListView.getAdapter());
            }
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.OpenFileDialog$7 */
    class C02787 implements Comparator<File> {
        C02787() {
        }

        public int compare(File file, File file2) {
            if (file.isDirectory() && file2.isFile()) {
                return -1;
            }
            if (file.isFile() && file2.isDirectory()) {
                return 1;
            }
            return file.getName().toLowerCase().compareTo(file2.getName().toLowerCase());
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.OpenFileDialog$8 */
    class C02798 implements OnItemClickListener {
        C02798() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
            FileAdapter adapter = (FileAdapter) adapterView.getAdapter();
            File file = (File) adapter.getItem(index);
            if (file.isDirectory()) {
                OpenFileDialog.this.currentPath = file.getPath();
                OpenFileDialog.this.RebuildFiles(adapter);
            } else if (!OpenFileDialog.this.mIsDirectorySelection) {
                if (index != OpenFileDialog.this.mSelectedIndex) {
                    if (OpenFileDialog.this.mIsEditMode) {
                        OpenFileDialog.this.mEdInputName.setText(file.getName());
                    }
                    OpenFileDialog.this.setSelectedIndex(index);
                } else {
                    OpenFileDialog.this.clearSelectedIndex();
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    private class FileAdapter extends ArrayAdapter<File> {
        public FileAdapter(Context context, List<File> files) {
            super(context, 17367043, files);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            File file = (File) getItem(position);
            if (view != null) {
                view.setText(file.getName());
                int backColorId = 17170445;
                if (file.isDirectory()) {
                    setDrawable(view, OpenFileDialog.this.folderIcon);
                } else {
                    setDrawable(view, OpenFileDialog.this.fileIcon);
                    if (OpenFileDialog.this.mSelectedIndex == position) {
                        backColorId = 17170451;
                    }
                }
                view.setBackgroundColor(getContext().getResources().getColor(backColorId));
            }
            return view;
        }

        private void setDrawable(TextView view, Drawable drawable) {
            if (view == null) {
                return;
            }
            if (drawable != null) {
                drawable.setBounds(0, 0, 60, 60);
                view.setCompoundDrawables(drawable, null, null, null);
                return;
            }
            view.setCompoundDrawables(null, null, null, null);
        }
    }

    public interface OpenDialogListener {
        void OnSelectedFile(String str);
    }

    public OpenFileDialog(Context context) {
        super(context);
        this.mSelectButtonCaption = context.getResources().getString(17039370);
        this.title = createTitle(context);
        changeTitle();
        ViewGroup mainLayout = createMainLayout(context);
        createEditGroup(context);
        LayoutParams lp = new LayoutParams(-1, -2);
        lp.addRule(12);
        mainLayout.addView(this.mEditGroup, lp);
        LinearLayout linearLayout = createLinearLayout(context);
        this.mBackView = createBackItem(context);
        linearLayout.addView(this.mBackView);
        updateBackVisibilty();
        this.mFileListView = createListView(context);
        linearLayout.addView(this.mFileListView);
        lp = new LayoutParams(-1, -1);
        lp.addRule(2, this.mEditGroup.getId());
        mainLayout.addView(linearLayout, lp);
        setCustomTitle(this.title).setView(mainLayout).setNegativeButton(17039360, null);
    }

    public AlertDialog show() {
        setPositiveButton(this.mSelectButtonCaption, new C02721());
        this.files.addAll(getFiles(this.currentPath));
        this.mFileListView.setAdapter(new FileAdapter(getContext(), this.files));
        if (!this.mIsEditMode) {
            this.mEditGroup.setVisibility(8);
        }
        if (new File(this.currentPath).getParentFile() == null) {
            this.mBackView.setVisibility(4);
        }
        changeTitle();
        AlertDialog dialog = super.show();
        this.mBtPositive = dialog.getButton(-1);
        if (!this.mIsDirectorySelection && this.mEdInputName.getText().equals(null)) {
            this.mBtPositive.setEnabled(false);
        }
        if (this.mIsEditMode) {
            this.mEdInputName.setFocusable(false);
        }
        return dialog;
    }

    public OpenFileDialog setFilter(final String filter) {
        this.filenameFilter = new FilenameFilter() {
            public boolean accept(File file, String fileName) {
                File tempFile = new File(String.format("%s/%s", new Object[]{file.getPath(), fileName}));
                if (tempFile.isFile()) {
                    return tempFile.getName().matches(filter);
                }
                return true;
            }
        };
        return this;
    }

    public OpenFileDialog setOpenDialogListener(OpenDialogListener listener) {
        this.listener = listener;
        return this;
    }

    public OpenFileDialog setFolderIcon(Drawable drawable) {
        this.folderIcon = drawable;
        return this;
    }

    public OpenFileDialog setFileIcon(Drawable drawable) {
        this.fileIcon = drawable;
        return this;
    }

    public OpenFileDialog setStartPath(String path) {
        if (path != null && new File(path).exists()) {
            this.currentPath = path;
        }
        return this;
    }

    public OpenFileDialog setStartFileName(String fileName) {
        this.mStartFileName = fileName;
        return this;
    }

    public OpenFileDialog setIsDirectorySelectionOption() {
        this.mIsDirectorySelection = true;
        return this;
    }

    public OpenFileDialog setIsEditModeOption() {
        this.mIsEditMode = true;
        return this;
    }

    public OpenFileDialog setIsKeepFileNameOption() {
        this.mIsKeepFileName = true;
        return this;
    }

    public OpenFileDialog setSelectButtonCaption(String caption) {
        this.mSelectButtonCaption = caption;
        return this;
    }

    public OpenFileDialog setAccessDeniedMessage(String message) {
        this.accessDeniedMessage = message;
        return this;
    }

    private static Display getDefaultDisplay(Context context) {
        return ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
    }

    private static Point getScreenSize(Context context) {
        Point screeSize = new Point();
        getDefaultDisplay(context).getSize(screeSize);
        return screeSize;
    }

    private static int getLinearLayoutMinHeight(Context context) {
        return getScreenSize(context).y;
    }

    private RelativeLayout createRelativeLayout(Context context) {
        return new RelativeLayout(context);
    }

    private LinearLayout createLinearLayout(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(1);
        linearLayout.setMinimumHeight(getLinearLayoutMinHeight(context));
        return linearLayout;
    }

    private LinearLayout createLinearLayoutH(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(0);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        return linearLayout;
    }

    private RelativeLayout createMainLayout(Context context) {
        return createRelativeLayout(context);
    }

    private int getItemHeight(Context context) {
        TypedValue value = new TypedValue();
        DisplayMetrics metrics = new DisplayMetrics();
        context.getTheme().resolveAttribute(16843655, value, true);
        getDefaultDisplay(context).getMetrics(metrics);
        return (int) TypedValue.complexToDimension(value.data, metrics);
    }

    private TextView createTextView(Context context, int style) {
        TextView textView = new TextView(context);
        textView.setTextAppearance(context, style);
        int itemHeight = getItemHeight(context);
        textView.setLayoutParams(new ViewGroup.LayoutParams(-1, itemHeight));
        textView.setMinHeight(itemHeight);
        textView.setGravity(16);
        textView.setPadding(15, 0, 0, 0);
        return textView;
    }

    private TextView createTitle(Context context) {
        return createTextView(context, 16974264);
    }

    private void createEditGroup(Context context) {
        this.mEditGroup = createLinearLayoutH(context);
        this.mEditGroup.setId(C0280R.id.openFileDialogEditGroupId);
        this.mEdInputName = new EditText(context);
        this.mEdInputName.setInputType(17);
        this.mEdInputName.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        this.mEdInputName.setOnKeyListener(new C02743());
        this.mEdInputName.setOnEditorActionListener(new C02754());
        this.mEdInputName.setOnTouchListener(new C02765());
        this.mTvInputPath = new TextView(context);
        this.mEditGroup.addView(this.mTvInputPath);
        this.mEditGroup.addView(this.mEdInputName);
        this.mTvInputPath.setText("/sdcard/_work/downloads/");
    }

    private TextView createBackItem(Context context) {
        TextView textView = createTextView(context, 16974255);
        Drawable drawable = getContext().getResources().getDrawable(17301565);
        drawable.setBounds(0, 0, 60, 60);
        textView.setCompoundDrawables(drawable, null, null, null);
        textView.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        textView.setText("..");
        textView.setOnClickListener(new C02776());
        return textView;
    }

    public int getTextWidth(String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return (bounds.left + bounds.width()) + 80;
    }

    private void updateBackVisibilty() {
        if (this.mBackView == null) {
            return;
        }
        if (new File(this.currentPath).getParentFile() == null) {
            this.mBackView.setVisibility(4);
        } else {
            this.mBackView.setVisibility(0);
        }
    }

    private void changeTitle() {
        String titleText = this.currentPath;
        int maxWidth = (int) (((double) getScreenSize(getContext()).x) * 0.99d);
        if (getTextWidth(titleText, this.title.getPaint()) > maxWidth) {
            while (getTextWidth("..." + titleText, this.title.getPaint()) > maxWidth) {
                int start = titleText.indexOf("/", 2);
                if (start > 0) {
                    titleText = titleText.substring(start);
                } else {
                    titleText = titleText.substring(2);
                }
            }
            titleText = "..." + titleText;
        }
        this.title.setText(titleText);
        if (this.mIsEditMode) {
            this.mTvInputPath.setText(titleText + "/");
            if (this.mStartFileName != null) {
                this.mEdInputName.setText(this.mStartFileName);
                this.mStartFileName = null;
            } else if (!this.mIsKeepFileName) {
                this.mEdInputName.setText("");
            }
            this.mEdInputName.setClickable(true);
            this.mEdInputName.setFocusableInTouchMode(true);
            this.mEdInputName.setFocusable(true);
        }
        updateBackVisibilty();
    }

    private List<File> getFiles(String directoryPath) {
        File[] list = new File(directoryPath).listFiles(this.filenameFilter);
        if (list == null) {
            list = new File[0];
        }
        List<File> fileList = Arrays.asList(list);
        Collections.sort(fileList, new C02787());
        return fileList;
    }

    private void RebuildFiles(ArrayAdapter<File> adapter) {
        try {
            List<File> fileList = getFiles(this.currentPath);
            this.files.clear();
            clearSelectedIndex();
            this.files.addAll(fileList);
            adapter.notifyDataSetChanged();
            changeTitle();
        } catch (NullPointerException e) {
            String message = getContext().getResources().getString(17039374);
            if (!this.accessDeniedMessage.equals("")) {
                message = this.accessDeniedMessage;
            }
            Toast.makeText(getContext(), message, 0).show();
        }
        this.mFileListView.setSelection(-1);
    }

    private ListView createListView(Context context) {
        ListView listView = new ListView(context);
        listView.setOnItemClickListener(new C02798());
        return listView;
    }

    private EditText createEditView(Context context) {
        return new EditText(context);
    }

    private void setSelectedIndex(int value) {
        boolean z = true;
        this.mSelectedIndex = value;
        if (isSelectedIndexSet()) {
            this.mBtPositive.setEnabled(true);
        } else if (!this.mIsDirectorySelection) {
            if (this.mIsEditMode) {
                Button button = this.mBtPositive;
                if (this.mEdInputName.getText().toString().length() <= 0) {
                    z = false;
                }
                button.setEnabled(z);
                return;
            }
            this.mBtPositive.setEnabled(false);
        }
    }

    private void clearSelectedIndex() {
        setSelectedIndex(-1);
    }

    private boolean isSelectedIndexSet() {
        return this.mSelectedIndex >= 0;
    }
}
