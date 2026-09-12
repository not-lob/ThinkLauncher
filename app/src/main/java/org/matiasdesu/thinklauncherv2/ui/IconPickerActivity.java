package org.matiasdesu.thinklauncherv2.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.matiasdesu.thinklauncherv2.MainActivity;
import org.matiasdesu.thinklauncherv2.R;
import org.matiasdesu.thinklauncherv2.utils.DynamicIconHelper;
import org.matiasdesu.thinklauncherv2.utils.EinkRefreshHelper;
import org.matiasdesu.thinklauncherv2.utils.FontHelper;
import org.matiasdesu.thinklauncherv2.utils.IconPackHelper;
import org.matiasdesu.thinklauncherv2.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Grid of every drawable the selected icon pack offers, for assigning a custom
 * icon to one app. Writes the override itself rather than returning a result,
 * so callers only need to start the activity.
 */
public class IconPickerActivity extends AppCompatActivity {

    public static final String EXTRA_PACKAGE = "package";

    private static final int CELL_DP = 72;

    private String targetPackage;
    private String packPackage;
    private int theme;
    private SharedPreferences prefs;
    private boolean screenAnimations;

    private final List<String> allIcons = new ArrayList<>();
    private final List<String> visibleIcons = new ArrayList<>();

    private IconAdapter adapter;
    private TextView emptyMessage;

    private ExecutorService decodeExecutor;
    private LruCache<String, Drawable> thumbCache;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private int cellPx;

    private BroadcastReceiver homeButtonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                String reason = intent.getStringExtra("reason");
                if ("homekey".equals(reason)) {
                    Intent mainIntent = new Intent(IconPickerActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(mainIntent);
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        theme = prefs.getInt("theme", 0);
        screenAnimations = prefs.getInt("screen_animations", 0) == 1;
        if (ThemeUtils.isDarkTheme(theme, this)) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_picker);

        targetPackage = getIntent().getStringExtra(EXTRA_PACKAGE);
        packPackage = IconPackHelper.getSelectedPack(this);

        View root = findViewById(R.id.root_layout);
        root.setBackgroundColor(ThemeUtils.getBgColor(theme, this));
        ThemeUtils.applyThemeToViewGroup((ViewGroup) root, theme, this);
        findViewById(R.id.divider).setBackgroundColor(ThemeUtils.getTextColor(theme, this));

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setColorFilter(ThemeUtils.getTextColor(theme, this));
        backButton.setOnClickListener(v -> close());

        findViewById(R.id.reset_button).setOnClickListener(v -> {
            prefs.edit()
                    .remove(IconPackHelper.PREF_CUSTOM_ICON + targetPackage)
                    .remove(IconPackHelper.PREF_CUSTOM_ICON_PACK + targetPackage)
                    .apply();
            DynamicIconHelper.bumpCacheEpoch();
            close();
        });

        emptyMessage = findViewById(R.id.empty_message);

        cellPx = Math.round(CELL_DP * getResources().getDisplayMetrics().density);
        int columns = Math.max(3, getResources().getDisplayMetrics().widthPixels / cellPx);

        thumbCache = new LruCache<>(96);
        decodeExecutor = Executors.newSingleThreadExecutor();

        adapter = new IconAdapter();
        RecyclerView grid = findViewById(R.id.icon_grid);
        grid.setLayoutManager(new GridLayoutManager(this, columns));
        grid.setOverScrollMode(View.OVER_SCROLL_NEVER);
        grid.setAdapter(adapter);

        EditText search = findViewById(R.id.search_edit_text);
        ThemeUtils.applyEditTextTheme(search, theme, this);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        FontHelper.applyToViewTree(this, root);

        if (packPackage.isEmpty()) {
            showMessage("No icon pack selected.");
            return;
        }

        showMessage("Loading icons...");
        new Thread(() -> {
            final List<String> names = IconPackHelper.listPackDrawables(this, packPackage);
            mainHandler.post(() -> {
                allIcons.clear();
                allIcons.addAll(names);
                filter("");
                if (names.isEmpty()) {
                    showMessage("This icon pack exposes no icons.");
                } else {
                    emptyMessage.setVisibility(View.GONE);
                }
            });
        }, "icon-picker-load").start();
    }

    private void showMessage(String message) {
        emptyMessage.setText(message);
        emptyMessage.setVisibility(View.VISIBLE);
    }

    private void filter(String query) {
        String needle = query.trim().toLowerCase(Locale.getDefault());
        visibleIcons.clear();
        if (needle.isEmpty()) {
            visibleIcons.addAll(allIcons);
        } else {
            for (String name : allIcons) {
                if (name.toLowerCase(Locale.getDefault()).contains(needle)) {
                    visibleIcons.add(name);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void apply(String drawableName) {
        prefs.edit()
                .putString(IconPackHelper.PREF_CUSTOM_ICON + targetPackage, drawableName)
                .putString(IconPackHelper.PREF_CUSTOM_ICON_PACK + targetPackage, packPackage)
                .apply();
        DynamicIconHelper.bumpCacheEpoch();
        close();
    }

    private void close() {
        finish();
        overridePendingTransition(0, screenAnimations ? R.anim.dialog_fade_out : 0);
    }

    /**
     * Packs ship thousands of icons, so thumbnails are decoded downsampled, off
     * the main thread, and kept in a small cache.
     */
    private Drawable decodeThumb(String name) {
        Drawable cached = thumbCache.get(name);
        if (cached != null) {
            return cached;
        }

        Resources res = IconPackHelper.getPackResources(this, packPackage);
        if (res == null) {
            return null;
        }

        try {
            int id = res.getIdentifier(name, "drawable", packPackage);
            if (id == 0) {
                return null;
            }

            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, id, bounds);

            Drawable result = null;
            if (bounds.outWidth > 0 && bounds.outHeight > 0) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, cellPx);
                Bitmap bitmap = BitmapFactory.decodeResource(res, id, opts);
                if (bitmap != null) {
                    result = new BitmapDrawable(getResources(), bitmap);
                }
            }

            if (result == null) {
                // Vector or adaptive XML drawables can't be decoded as a bitmap.
                result = renderToBitmap(res.getDrawable(id, null));
            }

            if (result != null) {
                thumbCache.put(name, result);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private Drawable renderToBitmap(Drawable source) {
        if (source == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(cellPx, cellPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        source.setBounds(0, 0, cellPx, cellPx);
        source.draw(canvas);
        return new BitmapDrawable(getResources(), bitmap);
    }

    private static int sampleSize(int width, int height, int target) {
        int sample = 1;
        while (width / (sample * 2) >= target && height / (sample * 2) >= target) {
            sample *= 2;
        }
        return sample;
    }

    private class IconAdapter extends RecyclerView.Adapter<IconAdapter.Holder> {

        class Holder extends RecyclerView.ViewHolder {
            final ImageView image;

            Holder(ImageView image) {
                super(image);
                this.image = image;
            }
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView image = new ImageView(IconPickerActivity.this);
            image.setLayoutParams(new ViewGroup.LayoutParams(cellPx, cellPx));
            int padding = cellPx / 8;
            image.setPadding(padding, padding, padding, padding);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            // Line-art packs are white on transparent, so untinted previews
            // would be invisible against the light background.
            image.setColorFilter(ThemeUtils.getTextColor(theme, IconPickerActivity.this));
            return new Holder(image);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            final String name = visibleIcons.get(position);
            holder.image.setImageDrawable(null);
            holder.image.setTag(name);
            holder.image.setOnClickListener(v -> apply(name));

            Drawable cached = thumbCache.get(name);
            if (cached != null) {
                holder.image.setImageDrawable(cached);
                return;
            }

            decodeExecutor.execute(() -> {
                final Drawable drawable = decodeThumb(name);
                if (drawable == null) {
                    return;
                }
                mainHandler.post(() -> {
                    if (name.equals(holder.image.getTag())) {
                        holder.image.setImageDrawable(drawable);
                    }
                });
            });
        }

        @Override
        public int getItemCount() {
            return visibleIcons.size();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(homeButtonReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"),
                Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(homeButtonReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (decodeExecutor != null) {
            decodeExecutor.shutdownNow();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            EinkRefreshHelper.refreshEink(getWindow(), prefs, prefs.getInt("eink_refresh_delay", 100));
        }
    }
}
