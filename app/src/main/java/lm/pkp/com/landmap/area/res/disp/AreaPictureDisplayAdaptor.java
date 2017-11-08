package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import lm.pkp.com.landmap.R;
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
        }else {
            return view;
        }

        // Get the image URL for the current position.
        final String url = getItem(position);

        // Trigger the download of the URL asynchronously into the image view.
        final String fileUrl = "file://"+ url;

        final Picasso picassoElem = Picasso.with(context);//
        picassoElem.setIndicatorsEnabled(true);
        picassoElem.load(fileUrl) //
                .placeholder(R.drawable.placeholder) //
                .error(R.drawable.error) //
                .resize(300,300)
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