package org.matiasdesu.thinklauncherv2.utils;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.matiasdesu.thinklauncherv2.R;

import java.util.List;

/**
 * Binds a {@code plus_minus_layout} container to a font-slot override cycler: "DEFAULT" plus every
 * imported library font by name, wrapping modulo like every other enum row (see
 * TextSettingsActivity's text-effect cycler). One call replaces what would otherwise be a
 * copy-pasted cycler block on every screen that exposes a font override - Time/Date/Status
 * Row/Now Reading/Home Calendar/Font Sizes (app list)/Music Dock Settings.
 */
public final class FontRowBinder {

    private static final String DEFAULT_LABEL = "DEFAULT";

    private FontRowBinder() {
    }

    public static void bind(Activity activity, View container, String slotId) {
        TextView valueTv = container.findViewById(R.id.value_text);
        ImageButton minusBtn = container.findViewById(R.id.btn_minus);
        ImageButton plusBtn = container.findViewById(R.id.btn_plus);

        List<FontHelper.FontEntry> library = FontHelper.getLibrary(activity);
        String[] names = new String[library.size() + 1];
        names[0] = DEFAULT_LABEL;
        for (int i = 0; i < library.size(); i++) {
            names[i + 1] = library.get(i).name;
        }

        String currentFile = FontHelper.getSlotFontFile(activity, slotId);
        int startIndex = 0;
        for (int i = 0; i < library.size(); i++) {
            if (library.get(i).file.equals(currentFile)) {
                startIndex = i + 1;
                break;
            }
        }

        // Row width jitter as the label changes is exactly what TextWidthHelper is for elsewhere.
        valueTv.setMinWidth(TextWidthHelper.getMaxTextWidthPx(valueTv, names));
        valueTv.setText(names[startIndex]);

        int[] index = { startIndex };
        minusBtn.setOnClickListener(v -> {
            index[0] = (index[0] - 1 + names.length) % names.length;
            persist(activity, valueTv, slotId, library, index[0], names);
        });
        plusBtn.setOnClickListener(v -> {
            index[0] = (index[0] + 1) % names.length;
            persist(activity, valueTv, slotId, library, index[0], names);
        });
    }

    private static void persist(Activity activity, TextView valueTv, String slotId,
            List<FontHelper.FontEntry> library, int index, String[] names) {
        valueTv.setText(names[index]);
        String file = index == 0 ? null : library.get(index - 1).file;
        FontHelper.setSlotFontFile(activity, slotId, file);
    }
}
