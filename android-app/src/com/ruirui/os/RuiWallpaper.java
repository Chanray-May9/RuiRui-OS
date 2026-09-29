package com.ruirui.os;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;

final class RuiWallpaper {
    private RuiWallpaper() {}
    static void applyDefault(Context context) {
        var prefs = context.getSharedPreferences("wallpaper", Context.MODE_PRIVATE);
        if (prefs.getBoolean("installed", false)) return;
        try (InputStream input = context.getAssets().open("ruirui-wallpaper.png")) {
            Bitmap image = BitmapFactory.decodeStream(input);
            if (image == null) return;
            WallpaperManager.getInstance(context).setBitmap(image, null, true,
                    WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
            image.recycle();
            prefs.edit().putBoolean("installed", true).apply();
        } catch (Exception ignored) { }
    }
}
