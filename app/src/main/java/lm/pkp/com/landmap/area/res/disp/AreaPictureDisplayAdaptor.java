package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

final class AreaPictureDisplayAdaptor extends BaseAdapter {

    private final Context context;
    final ArrayList<PictureDisplayElement> dataSet = PictureDataHolder.INSTANCE.getData();

    AreaPictureDisplayAdaptor(Context context) {
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
        final String url = getItem(position);
        AreaContext ac = AreaContext.INSTANCE;
        AreaElement ae = ac.getAreaElement();
        String thumbnailRoot = ac.getAreaLocalPictureThumbnailRoot(ae.getUniqueId()).getAbsolutePath();
        String thumbnailFilePath = thumbnailRoot + File.separatorChar + dataSet.get(position).getName();
        File thumbFile = new File(thumbnailFilePath);

        final Picasso picassoElem = Picasso.with(context);//
        picassoElem.load(thumbFile) //
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .error(R.drawable.error) //
                .resize(300, 300)
                .tag(context) //
                .into(view);

        final View referredView = view;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                File file = new File(url);
                intent.setDataAndType(Uri.fromFile(file), "image/*");
                referredView.getContext().startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public String getItem(int position) {
        return dataSet.get(position).getAbsPath();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}