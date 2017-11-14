package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.sync.LocalFolderStructureManager;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

final class AreaDocumentDisplayAdaptor extends BaseAdapter {

    private final Context context;
    private final List<DocumentDisplayElement> docElems = DocumentDataHolder.INSTANCE.getData();

    private final String tempRoot = LocalFolderStructureManager.getTempStorageDir().getAbsolutePath();
    private final String thumbsDirPath = tempRoot + File.separatorChar + "thumb" + File.separatorChar;

    public AreaDocumentDisplayAdaptor(Context context) {
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(context);
            view.setScaleType(CENTER_CROP);
        }else {
            return view;
        }

        // Get the image URL for the current position.
        final String url = getItem(position);
        final DocumentDisplayElement currElem = docElems.get(position);

        final Picasso picassoElem = Picasso.with(context);//
        picassoElem.setIndicatorsEnabled(true);
        picassoElem.load(Uri.fromFile(getThumbFile(currElem))) //
                .error(R.drawable.error) //
                .config(Bitmap.Config.RGB_565)
                .centerCrop()
                .resize(300, 300)
                .tag(context) //
                .into(view);

        final View referredView = view;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                File file = new File(url);
                intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                referredView.getContext().startActivity(intent);
            }
        });
        return view;
    }

    private File getThumbFile(DocumentDisplayElement currElem) {
        String thumbPath = thumbsDirPath + File.separatorChar + currElem.getName() + "_t.jpg";
        File thumbFile = new File(thumbPath);
        if(thumbFile.exists()){
            return thumbFile;
        }else {
            return createThumbFile(thumbPath, currElem);
        }
    }

    private File createThumbFile(String thumbPath, DocumentDisplayElement currElem) {
        try {
            // Get PDF thumbnail here.
            PDDocument document = PDDocument.load(new File(currElem.getAbsPath()));
            // Create a renderer for the document
            PDFRenderer renderer = new PDFRenderer(document);
            // Render the image to an RGB Bitmap
            Bitmap pageImage = renderer.renderImage(0, 1, Bitmap.Config.RGB_565);

            // Save the render result to an image
            File thumbsDir = new File(thumbsDirPath);
            if(!thumbsDir.exists()){
                thumbsDir.mkdirs();
            }
            File thumbFile = new File(thumbPath);
            FileOutputStream fileOut = new FileOutputStream(thumbFile);
            pageImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
            fileOut.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getCount() {
        return docElems.size();
    }

    @Override
    public String getItem(int position) {
        return docElems.get(position).getAbsPath();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}