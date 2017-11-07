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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lm.pkp.com.landmap.R;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

final class AreaVideoDisplayAdaptor extends BaseAdapter {

    private final Context context;
    private final List<String> urls = new ArrayList<>();

    AreaVideoDisplayAdaptor(Context context) {
        this.context = context;
        final ArrayList<VideoDisplayElement> dataSet = VideoDataHolder.INSTANCE.getData();
        for (int i = 0; i < dataSet.size(); i++) {
            urls.add(dataSet.get(i).getAbsPath());
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(context);
            view.setScaleType(CENTER_CROP);
        }

        // Get the image URL for the current position.
        final String url = getItem(position);

        // Trigger the download of the URL asynchronously into the image view.
        Bitmap bMap = ThumbnailUtils.createVideoThumbnail(url, MediaStore.Video.Thumbnails.MICRO_KIND);
        String thumbPath = Media.insertImage(context.getContentResolver(), bMap, "title", null);
        final String fileUrl = "file://"+ url;
        Picasso.with(context) //
                .load(Uri.parse(thumbPath)) //
                .placeholder(R.drawable.placeholder) //
                .error(R.drawable.error) //
                .fit() //
                .tag(context) //
                .into(view);

        final View referredView = view;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                File file = new File(url);
                intent.setDataAndType(Uri.fromFile(file), "video/*");
                referredView.getContext().startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public String getItem(int position) {
        return urls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}