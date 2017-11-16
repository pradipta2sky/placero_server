package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.area.AreaContext;
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
        } else {
            return view;
        }

        // Get the image URL for the current position.
        final DocumentDisplayElement currElem = docElems.get(position);
        String thumbnailRoot = AreaContext.INSTANCE.getAreaLocalDocumentThumbnailRoot().getAbsolutePath();
        String thumbnailFilePath = thumbnailRoot + File.separatorChar + currElem.getName();
        File thumbFile = new File(thumbnailFilePath);

        final Picasso picassoElem = Picasso.with(context);//
        picassoElem.load(thumbFile) //
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .error(R.drawable.error) //
                .config(Bitmap.Config.RGB_565)
                .centerCrop()
                .resize(300, 300)
                .tag(context) //
                .into(view);

        final View referredView = view;
        final String url = getItem(position);

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