package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.io.File;
import java.util.List;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

final class AreaDocumentDisplayAdaptor extends BaseAdapter {

    private final Context context;
    private final List<DocumentDisplayElement> docElems = DocumentDataHolder.INSTANCE.getData();

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

        AreaContext areaContext = AreaContext.INSTANCE;
        AreaElement areaElement = areaContext.getAreaElement();
        // Get the image URL for the current position.
        final DocumentDisplayElement currElem = docElems.get(position);
        String thumbnailRoot = areaContext
                .getAreaLocalDocumentThumbnailRoot(areaElement.getUniqueId()).getAbsolutePath();
        String thumbnailFilePath = thumbnailRoot + File.separatorChar + currElem.getName();
        File thumbFile = new File(thumbnailFilePath);

        if(thumbFile.exists()){
            Bitmap bMap = BitmapFactory.decodeFile(thumbnailFilePath);
            view.setImageBitmap(bMap);
        }

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