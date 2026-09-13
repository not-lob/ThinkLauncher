package org.matiasdesu.thinklauncherv2.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import org.matiasdesu.thinklauncherv2.MainActivity;
import org.matiasdesu.thinklauncherv2.R;
import org.matiasdesu.thinklauncherv2.utils.FontHelper;
import org.matiasdesu.thinklauncherv2.utils.ThemeUtils;

import java.io.InputStream;
import java.util.List;

/**
 * The one place a font comes from: import .ttf files into a library, then pick which one is the
 * default. Individual surfaces (clock, date, each home widget, ...) override the default from
 * their own settings screen via FontRowBinder, rather than from here - this screen only manages
 * the library and the default. Replaces the old single-font FontSettingsActivity.
 */
public class FontLibraryActivity extends BaseSettingsActivity {

    private ActivityResultLauncher<Intent> fontPickerLauncher;
    private LinearLayout listContainer;
    private View emptyText;

    private final BroadcastReceiver homeButtonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                String reason = intent.getStringExtra("reason");
                if ("homekey".equals(reason)) {
                    Intent mainIntent = new Intent(FontLibraryActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(mainIntent);
                }
            }
        }
    };

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_font_library;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int bgColor = ThemeUtils.getBgColor(theme, this);
        LinearLayout root = findViewById(R.id.root_layout);
        root.setBackgroundColor(bgColor);
        ThemeUtils.applyThemeToViewGroup(root, theme, this);

        listContainer = findViewById(R.id.font_library_list_container);
        emptyText = findViewById(R.id.empty_library_text);

        setupPickerLauncher();
        findViewById(R.id.add_font_button).setOnClickListener(v -> openFontPicker());

        renderList();
        initPagination(this::refreshVisibility);
    }

    /**
     * SettingsPaginationHelper.updateVisibleItemsList() force-resets every top-level child of
     * settings_items_container back to VISIBLE before re-running this callback (see e.g.
     * DateSettingsActivity.refreshVisibility) - so the empty-state text's visibility has to be
     * decided here, not as a one-off inside renderList(), or refreshPagination() undoes it on the
     * very next call.
     */
    private void refreshVisibility() {
        emptyText.setVisibility(listContainer.getChildCount() == 0 ? View.VISIBLE : View.GONE);

        // Same reason the empty-state text needs recomputing here rather than once inside
        // renderList(): each row's default checkmark (View.INVISIBLE for a non-default row) is a
        // descendant of listContainer, and the pagination helper's forced-VISIBLE reset recurses
        // into every descendant of every top-level child - so it wipes per-row state too, not
        // just top-level visibility.
        String defaultFile = FontHelper.getDefaultFontFile(this);
        for (int i = 0; i < listContainer.getChildCount(); i++) {
            View row = listContainer.getChildAt(i);
            boolean isDefault = row.getTag() != null && row.getTag().equals(defaultFile);
            View check = row.findViewById(R.id.row_default_check);
            if (check != null) {
                check.setVisibility(isDefault ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }

    private void setupPickerLauncher() {
        fontPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fontUri = result.getData().getData();
                        if (fontUri != null) {
                            importFont(fontUri);
                        }
                    }
                });
    }

    private void openFontPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            fontPickerLauncher.launch(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "No file picker available. Please install a file manager.", Toast.LENGTH_LONG).show();
        }
    }

    private void importFont(Uri fontUri) {
        try {
            InputStream in = getContentResolver().openInputStream(fontUri);
            if (in == null) {
                Toast.makeText(this, "Failed to import font", Toast.LENGTH_SHORT).show();
                return;
            }
            String displayName = displayNameFor(fontUri);
            FontHelper.FontEntry entry = FontHelper.addFont(this, in, displayName);
            if (entry == null) {
                Toast.makeText(this, "Failed to import font", Toast.LENGTH_SHORT).show();
                return;
            }
            if (FontHelper.getTypefaceByFile(this, entry.file) == null) {
                FontHelper.deleteFont(this, entry.file);
                Toast.makeText(this, "Not a valid font file", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Font added", Toast.LENGTH_SHORT).show();
            renderList();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to import font", Toast.LENGTH_SHORT).show();
        }
    }

    /** SAF only guarantees a content Uri, not a real filename - query it, then strip the
     *  extension so the library shows "Inter Bold" rather than "Inter Bold.ttf". */
    private String displayNameFor(Uri uri) {
        String name = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) {
                    name = cursor.getString(idx);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (name == null) {
            return null;
        }
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private void renderList() {
        listContainer.removeAllViews();
        List<FontHelper.FontEntry> library = FontHelper.getLibrary(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (FontHelper.FontEntry entry : library) {
            View row = inflater.inflate(R.layout.font_library_row, listContainer, false);
            row.setTag(entry.file);
            TextView label = row.findViewById(R.id.row_label);
            label.setText(entry.name);

            row.findViewById(R.id.row_root).setOnClickListener(v -> {
                FontHelper.setDefaultFontFile(this, entry.file);
                renderList();
            });

            ImageButton deleteButton = row.findViewById(R.id.row_delete_button);
            deleteButton.setOnClickListener(v -> {
                FontHelper.deleteFont(this, entry.file);
                renderList();
            });

            // renderList() runs after BaseSettingsActivity's onResume font/theme pass has already
            // walked the tree once, so a freshly inflated row needs its own theme + font pass -
            // same reasoning as HomeWidgetsSettingsActivity.renderList().
            ThemeUtils.applyThemeToViewGroup((ViewGroup) row, theme, this);
            FontHelper.applyFont(this, label);

            listContainer.addView(row);
        }

        refreshPagination();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderList();
        registerReceiver(homeButtonReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"),
                Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(homeButtonReceiver);
    }
}
