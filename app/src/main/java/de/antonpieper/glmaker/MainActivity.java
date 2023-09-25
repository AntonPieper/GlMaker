package de.antonpieper.glmaker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.techisfun.android.topsheet.TopSheetBehavior;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.antonpieper.glmaker.opengl.ShaderError;
import de.antonpieper.glmaker.opengl.UniformPlugin;
import de.antonpieper.glmaker.widget.LineNumbers;
import de.antonpieper.glmaker.widget.SyntaxEditor;

public class MainActivity extends AppCompatActivity {
    private final List<ShaderError> errorList = new ArrayList<>();
    private GLSurfaceView glView;
    private GLRenderer glRenderer;
    boolean isFragmentShader = true;
    private String currentSource = "";
    private RecyclerView.Adapter<ShaderErrorHolder> errorAdapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable compile = () -> glView.queueEvent(() -> {
        if (isFragmentShader) glRenderer.setFragmentShader(currentSource);
        else glRenderer.setVertexShader(currentSource);
    });
    UniformPlugin.Float3 pointers = new UniformPlugin.Float3("pointers", 10);
    UniformPlugin.Int pointerCount = new UniformPlugin.Int("pointerCount");
    private static final int UPDATE_DELAY = 1000;


    private void updateToolbar() {
        MaterialToolbar myToolbar = findViewById(R.id.toolbar);
        errorAdapter.notifyDataSetChanged();
        Drawable background = myToolbar.getBackground();

        boolean hasError = errorList.isEmpty();

        TypedValue typedValue = new TypedValue();

        getTheme().resolveAttribute(hasError ? com.google.android.material.R.attr.colorSurface : com.google.android.material.R.attr.colorErrorContainer, typedValue, true);
        int backgroundColor = typedValue.data;

        background.setTintMode(PorterDuff.Mode.MULTIPLY);
        background.setTint(backgroundColor);

        getTheme().resolveAttribute(hasError ? com.google.android.material.R.attr.colorOnSurface : com.google.android.material.R.attr.colorOnErrorContainer, typedValue, true);
        int foregroundColor = typedValue.data;

        myToolbar.setTitleTextColor(foregroundColor);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setTopBarHeight();
        setSupportActionBar(myToolbar);
        glView = findViewById(R.id.gl_view);
        glView.setPreserveEGLContextOnPause(true);
        glView.setEGLContextClientVersion(3);

        setOnTouchListener();

        glRenderer = new GLRenderer();
        glView.setRenderer(glRenderer);

        glRenderer.attach(pointers).attach(pointerCount);
        glRenderer.setErrorHandler((error) -> {
            errorList.add(error);
            runOnUiThread(this::updateToolbar);
        });

        EditText editor = findViewById(R.id.editor);
        RecyclerView errorListHolder = findViewById(R.id.error_list);

        RecyclerView.LayoutManager errorListLayoutManager = new LinearLayoutManager(this);
        errorListHolder.setLayoutManager(errorListLayoutManager);

        errorListHolder.setItemAnimator(new DefaultItemAnimator());
        errorAdapter = new RecyclerView.Adapter<>() {
            @Override
            public int getItemCount() {
                return errorList.size();
            }

            @NonNull
            @Override
            public ShaderErrorHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                return new ShaderErrorHolder(editor, LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.error_item, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull ShaderErrorHolder vh, int position) {
                ShaderError error = errorList.get(position);
                vh.line.setText(String.format(Locale.getDefault(), "%d", error.line));
                vh.type.setText(error.type.name());
                vh.message.setText(error.message);
            }
        };
        errorListHolder.setAdapter(errorAdapter);

        editor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                errorList.clear();
                updateToolbar();
                currentSource = s.toString();
                handler.removeCallbacks(compile);
                handler.postDelayed(compile, UPDATE_DELAY);

            }
        });
        editor.setText(glRenderer.fragmentShader());

        LineNumbers lineNumbers = findViewById(R.id.line_numbers);
        lineNumbers.setSource(editor);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        RecyclerView recyclerView = findViewById(R.id.extra_row_button);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new KeyboardButtonsAdapter(editor, new String[]{"{", "}", "[", "]", "<", ">", "|", "^", "\\", "_", "~"}));
        editor.setOnFocusChangeListener((v, hasFocus) -> {
            View toolbar = findViewById(R.id.extra_row_container);
            toolbar.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
        });
        Button btn = findViewById(R.id.extra_row_tab_button);
        btn.setOnClickListener(view -> editor.getText().insert(editor.getSelectionStart(), "\t"));

    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnTouchListener() {
        glView.setOnTouchListener((view, event) -> {
            Log.d("Pointers", event.toString());
            int pointerCount1 = event.getPointerCount();
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                pointerCount1 = 0;
            }
            this.pointerCount.set(pointerCount1);
            int height = view.getHeight();
            float quality = glRenderer.getQuality();
            for (int i = 0; i < pointerCount1; ++i) {
                pointers.set(i, event.getX(i) * quality, (height - event.getY(i)) * quality, event.getTouchMajor(i));
            }
            return true;
        });
    }

    private void setTopBarHeight() {
        TopSheetBehavior<View> topSheetBehavior = TopSheetBehavior.from(findViewById(R.id.slidingPanel));
        topSheetBehavior.setState(TopSheetBehavior.STATE_HIDDEN);
        TypedValue typedValue = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            int actionBarSize = TypedValue.complexToDimensionPixelSize(typedValue.data, getResources().getDisplayMetrics());
            Rect rectangle = new Rect();
            Window window = getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
            int statusBarHeight = rectangle.top;
            actionBarSize += statusBarHeight;
            topSheetBehavior.setPeekHeight(actionBarSize);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item) {
            switchVisibility(item);
            return true;
        } else if (id == R.id.shaderType) {
            switchType(item);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void switchVisibility(@NonNull MenuItem item) {
        ScrollView scrollView = findViewById(R.id.scroll_view);
        boolean visible = scrollView.getVisibility() == View.VISIBLE;
        scrollView.setVisibility(visible ? View.GONE : View.VISIBLE);

        item.setIcon(visible ? R.drawable.visibility : R.drawable.visibility_off);
        View toolbar = findViewById(R.id.slidingPanel);
        Object imm = getSystemService(Context.INPUT_METHOD_SERVICE);
        if (visible && getSystemService(Context.INPUT_METHOD_SERVICE) instanceof InputMethodManager) {
            ((InputMethodManager) imm).hideSoftInputFromWindow(findViewById(R.id.main_content).getWindowToken(), 0);
        }
        toolbar.setAlpha(visible ? 0.25f : 1f);
        // toolbar.setElevation(visible ? 0 : TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
    }

    private void switchType(@NonNull MenuItem item) {
        isFragmentShader = !isFragmentShader;
        SyntaxEditor et = findViewById(R.id.editor);
        et.setText(isFragmentShader ? glRenderer.fragmentShader() : glRenderer.vertexShader());
        item.setTitle(isFragmentShader ? "▲" : "∴");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        glView.queueEvent(() -> glRenderer.close());
        super.onDestroy();
    }

    static class ShaderErrorHolder extends RecyclerView.ViewHolder {
        TextView line;
        TextView type;
        TextView message;

        public ShaderErrorHolder(EditText et, View view) {
            super(view);
            line = view.findViewById(R.id.error_item_line);
            type = view.findViewById(R.id.error_item_type);
            message = view.findViewById(R.id.error_item_message);
            view.setOnClickListener(v -> {
                        Layout layout = et.getLayout();
                        int maxLine = layout.getLineCount();
                        int lineSearch =
                                Integer.parseInt(line.getText().toString());
                        lineSearch = Math.min(Math.max(0, lineSearch), maxLine);
                        et.setSelection(lineSearch > 0 ? layout.getLineStart(lineSearch - 1) : 0);
                    }
            );
        }
    }

    private static class KeyboardButtonsAdapter extends RecyclerView.Adapter<KeyboardButtonsAdapter.ButtonViewHolder> {
        public String[] snippets;
        public EditText editText;

        public KeyboardButtonsAdapter(EditText editText, String[] buttons) {
            super();
            this.editText = editText;
            this.snippets = buttons;
        }

        @NonNull
        @Override
        public ButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Button btn = (Button) LayoutInflater.from(parent.getContext()).inflate(R.layout.extra_row_button, parent, false);
            btn.setOnClickListener(v -> editText.getText().insert(editText.getSelectionStart(), btn.getText()));
            return new ButtonViewHolder(btn);
        }

        @Override
        public void onBindViewHolder(@NonNull ButtonViewHolder vh, int position) {
            vh.button.setText(snippets[position]);
        }

        @Override
        public int getItemCount() {
            return snippets.length;
        }

        private static class ButtonViewHolder extends RecyclerView.ViewHolder {
            public Button button;

            public ButtonViewHolder(Button btn) {
                super(btn);
                this.button = btn;
            }
        }
    }

}
