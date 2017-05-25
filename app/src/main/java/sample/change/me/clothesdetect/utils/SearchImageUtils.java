package sample.change.me.clothesdetect.utils;

/**
 * Created by Administrator on 2017/5/24.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

public class SearchImageUtils {

    public static Bitmap getImageFromAssetsFile(Context context,
                                                String fileName) {
        Bitmap image = null;
        try {
            InputStream is =  context.getClass().getClassLoader().getResourceAsStream("assets/img/"+fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}