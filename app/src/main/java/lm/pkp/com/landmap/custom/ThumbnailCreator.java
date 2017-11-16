package lm.pkp.com.landmap.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileOutputStream;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;

public class ThumbnailCreator {

    private Context context;

    public ThumbnailCreator(Context context) {
        this.context = context;
    }

    public void createImageThumbnail(File resourceFile, String areaId) {
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

        bitmapOptions.inJustDecodeBounds = true; // obtain the size of the image, without loading it in memory
        BitmapFactory.decodeFile(resourceFile.getAbsolutePath(), bitmapOptions);

        // find the best scaling factor for the desired dimensions
        int desiredWidth = 300;
        int desiredHeight = 300;
        float widthScale = (float) bitmapOptions.outWidth / desiredWidth;
        float heightScale = (float) bitmapOptions.outHeight / desiredHeight;
        float scale = Math.min(widthScale, heightScale);

        int sampleSize = 1;
        while (sampleSize < scale) {
            sampleSize *= 2;
        }
        bitmapOptions.inSampleSize = sampleSize; // this value must be a power of 2,
        // this is why you can not have an image scaled as you would like
        bitmapOptions.inJustDecodeBounds = false; // now we want to load the image

        // Let's load just the part of the image necessary for creating the thumbnail, not the whole image
        Bitmap thumbnail = BitmapFactory.decodeFile(resourceFile.getAbsolutePath(), bitmapOptions);

        File thumbnailRoot = AreaContext.INSTANCE
                .getAreaLocalPictureThumbnailRoot(areaId);
        String thumbFilePath = thumbnailRoot.getAbsolutePath()
                + File.separatorChar + resourceFile.getName();
        // Save the thumbnail
        FileOutputStream fos = null;
        try {
            File thumbnailFile = new File(thumbFilePath);
            fos = new FileOutputStream(thumbnailFile);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            fos.flush();
            fos.close();

            thumbnail.recycle();
            thumbnail = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createVideoThumbnail(File resourceFile, String areaId) {
        Bitmap bMap = ThumbnailUtils.createVideoThumbnail(resourceFile.getAbsolutePath(),
                MediaStore.Video.Thumbnails.MICRO_KIND);
        FileOutputStream out = null;
        try {
            File thumbnailRoot = AreaContext.INSTANCE.getAreaLocalVideoThumbnailRoot(areaId);
            String thumbFilePath = thumbnailRoot.getAbsolutePath()
                    + File.separatorChar + resourceFile.getName();
            out = new FileOutputStream(thumbFilePath);
            bMap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
            out.flush();
            out.close();

            bMap.recycle();
            bMap = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createDocumentThumbnail(File resourceFile, String areaId) {
        try {
            if(!PDFBoxResourceLoader.isReady()){
                PDFBoxResourceLoader.init(context);
            }

            PDDocument document = PDDocument.load(resourceFile);
            PDFRenderer renderer = new PDFRenderer(document);
            Bitmap pageImage = renderer.renderImage(0, 1, Bitmap.Config.RGB_565);

            String thumbRoot = AreaContext.INSTANCE
                    .getAreaLocalDocumentThumbnailRoot(areaId).getAbsolutePath();
            String thumbFilePath = thumbRoot + File.separatorChar + resourceFile.getName();
            File thumbFile = new File(thumbFilePath);
            if (!thumbFile.exists()) {
                thumbFile.createNewFile();
            }

            FileOutputStream fileOut = new FileOutputStream(thumbFile);
            pageImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOut);

            fileOut.flush();
            fileOut.close();

            renderer = null;
            document.close();
            document = null;
            pageImage.recycle();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
