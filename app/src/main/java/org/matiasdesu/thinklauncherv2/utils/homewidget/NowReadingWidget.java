package org.matiasdesu.thinklauncherv2.utils.homewidget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.matiasdesu.thinklauncherv2.ui.KOReaderHistoryActivity;
import org.matiasdesu.thinklauncherv2.ui.StrokeTextView;
import org.matiasdesu.thinklauncherv2.utils.KOReaderCoverHelper;
import org.matiasdesu.thinklauncherv2.utils.KOReaderHistoryHelper;
import org.matiasdesu.thinklauncherv2.utils.KOReaderLaunchHelper;
import org.matiasdesu.thinklauncherv2.utils.KOReaderProgressHelper;

/**
 * Shows the most recently opened KOReader book: title, author, reading progress and (EPUB-only,
 * best-effort) cover. Tapping it opens the book directly in KOReader via KOReaderLaunchHelper, the
 * same code path KOReaderHistoryActivity uses.
 */
public class NowReadingWidget implements HomeWidget {

    private static class Data {
        boolean permissionMissing;
        boolean pathMissing;
        KOReaderHistoryHelper.BookItem book;
        android.graphics.Bitmap coverBitmap;
    }

    private LinearLayout container;
    private ImageView coverView;
    private TextView titleView;
    private TextView authorView;
    private TextView progressText;
    private View progressFill;
    private View progressEmpty;
    private LinearLayout progressBar;

    private boolean showCover, showTitle, showAuthor, showProgress;
    private int progressStyle; // 0 = percent text, 1 = bar, 2 = both
    private int horizontalPosition;

    @Override
    public String id() { return "now_reading"; }

    @Override
    public boolean isEnabled(SharedPreferences prefs) {
        return prefs.getInt("now_reading_enabled", 0) == 1;
    }

    @Override
    public String[] prefKeys() {
        return new String[] {
                "now_reading_enabled", "now_reading_show_cover", "now_reading_show_title",
                "now_reading_show_author", "now_reading_show_progress", "now_reading_progress_style",
                "now_reading_title_font_size", "now_reading_author_font_size",
                "now_reading_cover_size", "now_reading_horizontal_position", "koreader_path"
        };
    }

    @Override
    public View createView(Activity host, RelativeLayout root, SharedPreferences prefs, int bgColor, int textColor) {
        showCover = prefs.getInt("now_reading_show_cover", 1) == 1;
        showTitle = prefs.getInt("now_reading_show_title", 1) == 1;
        showAuthor = prefs.getInt("now_reading_show_author", 1) == 1;
        showProgress = prefs.getInt("now_reading_show_progress", 1) == 1;
        progressStyle = prefs.getInt("now_reading_progress_style", 0);
        int titleSize = prefs.getInt("now_reading_title_font_size", 18);
        int authorSize = prefs.getInt("now_reading_author_font_size", 14);
        int coverSizeDp = prefs.getInt("now_reading_cover_size", 64);
        horizontalPosition = prefs.getInt("now_reading_horizontal_position", 0);

        float density = host.getResources().getDisplayMetrics().density;
        int coverSizePx = (int) (coverSizeDp * density);
        int gapPx = (int) (10 * density);
        int padX = WidgetLayoutUtils.horizontalPaddingPx(density);
        int padY = (int) (16 * density / 2);

        container = new LinearLayout(host);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(padX, padY, padX, padY);
        container.setClickable(true);
        container.setOnClickListener(v -> {
            Data data = (Data) container.getTag();
            if (data != null && data.book != null) {
                KOReaderLaunchHelper.openBook(host, data.book.path);
            } else {
                host.startActivity(new Intent(host, KOReaderHistoryActivity.class));
            }
        });

        if (showCover) {
            coverView = new ImageView(host);
            coverView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(coverSizePx,
                    (int) (coverSizePx * 1.4f));
            lp.setMarginEnd(gapPx);
            coverView.setVisibility(View.GONE);
            container.addView(coverView, lp);
        }

        LinearLayout textColumn = new LinearLayout(host);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        container.addView(textColumn, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        if (showTitle) {
            titleView = new StrokeTextView(host);
            titleView.setTextColor(textColor);
            titleView.setTextSize(titleSize);
            titleView.setTypeface(null, Typeface.BOLD);
            titleView.setMaxLines(1);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            textColumn.addView(titleView);
        }
        if (showAuthor) {
            authorView = new StrokeTextView(host);
            authorView.setTextColor(textColor);
            authorView.setTextSize(authorSize);
            authorView.setMaxLines(1);
            authorView.setEllipsize(TextUtils.TruncateAt.END);
            textColumn.addView(authorView);
        }
        if (showProgress) {
            if (progressStyle == 0 || progressStyle == 2) {
                progressText = new StrokeTextView(host);
                progressText.setTextColor(textColor);
                progressText.setTextSize(authorSize);
                textColumn.addView(progressText);
            }
            if (progressStyle == 1 || progressStyle == 2) {
                progressBar = new LinearLayout(host);
                progressBar.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                        (int) (120 * density), (int) (3 * density));
                barLp.topMargin = (int) (4 * density);
                progressFill = new View(host);
                progressFill.setBackgroundColor(textColor);
                progressEmpty = new View(host);
                progressEmpty.setBackgroundColor(textColor);
                progressEmpty.setAlpha(0.25f);
                progressBar.addView(progressFill, new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                progressBar.addView(progressEmpty, new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                textColumn.addView(progressBar, barLp);
            }
        }

        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlp.addRule(WidgetLayoutUtils.relativeHorizontalRule(horizontalPosition));
        container.setLayoutParams(rlp);
        container.setVisibility(View.GONE); // shown once loadData/bind resolves a book
        return container;
    }

    @Override
    public Object loadData(Context ctx, SharedPreferences prefs) {
        Data data = new Data();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            data.permissionMissing = true;
            return data;
        }
        String path = prefs.getString("koreader_path", null);
        if (path == null || path.trim().isEmpty()) {
            data.pathMissing = true;
            return data;
        }
        KOReaderHistoryHelper.BookItem book = KOReaderHistoryHelper.getCurrentBook(path);
        if (book != null) {
            book.progress = KOReaderProgressHelper.resolveProgress(path, book);
            if (showCover) {
                book.coverCachePath = KOReaderCoverHelper.getOrExtractCover(ctx, book);
                if (book.coverCachePath != null) {
                    data.coverBitmap = BitmapFactory.decodeFile(book.coverCachePath);
                }
            }
        }
        data.book = book;
        return data;
    }

    @Override
    public void bind(Object result) {
        if (container == null || !(result instanceof Data)) return;
        Data data = (Data) result;
        container.setTag(data);

        if (data.permissionMissing || data.pathMissing || data.book == null) {
            // Degrade to a single tappable line rather than disappearing outright, mirroring the
            // calendar widget's "grant access" affordance.
            if (titleView != null) {
                titleView.setText(data.permissionMissing ? "Grant storage access for Now Reading"
                        : data.pathMissing ? "Set up KOReader history to show Now Reading"
                        : "No books read yet");
                titleView.setVisibility(View.VISIBLE);
            }
            if (authorView != null) authorView.setVisibility(View.GONE);
            if (progressText != null) progressText.setVisibility(View.GONE);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (coverView != null) coverView.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            return;
        }

        KOReaderHistoryHelper.BookItem book = data.book;
        if (titleView != null) {
            titleView.setText(book.title == null || book.title.trim().isEmpty() ? "Untitled" : book.title);
            titleView.setVisibility(View.VISIBLE);
        }
        if (authorView != null) {
            if (book.author != null && !book.author.trim().isEmpty()) {
                authorView.setText(book.author);
                authorView.setVisibility(View.VISIBLE);
            } else {
                authorView.setVisibility(View.GONE);
            }
        }
        if (book.progress != null) {
            int percent = Math.round(book.progress * 100);
            if (progressText != null) {
                progressText.setText(percent + "% read");
                progressText.setVisibility(View.VISIBLE);
            }
            if (progressBar != null && progressFill != null && progressEmpty != null) {
                LinearLayout.LayoutParams fillLp = (LinearLayout.LayoutParams) progressFill.getLayoutParams();
                LinearLayout.LayoutParams emptyLp = (LinearLayout.LayoutParams) progressEmpty.getLayoutParams();
                fillLp.weight = Math.max(0.001f, book.progress);
                emptyLp.weight = Math.max(0.001f, 1f - book.progress);
                progressFill.setLayoutParams(fillLp);
                progressEmpty.setLayoutParams(emptyLp);
                progressBar.setVisibility(View.VISIBLE);
            }
        } else {
            if (progressText != null) progressText.setVisibility(View.GONE);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
        }
        if (coverView != null) {
            if (data.coverBitmap != null) {
                coverView.setImageBitmap(data.coverBitmap);
                coverView.setVisibility(View.VISIBLE);
            } else {
                coverView.setVisibility(View.GONE);
            }
        }
        container.setVisibility(View.VISIBLE);
    }

    @Override
    public void applyInsets(int homePaddingLeftPx, int homePaddingRightPx) {
        if (container == null) return;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) container.getLayoutParams();
        if (horizontalPosition == 0) {
            lp.leftMargin = homePaddingLeftPx;
        } else if (horizontalPosition == 2) {
            lp.rightMargin = homePaddingRightPx;
        }
        container.setLayoutParams(lp);
    }

    @Override
    public View getView() { return container; }

    @Override
    public int horizontalPosition() { return horizontalPosition; }
}
