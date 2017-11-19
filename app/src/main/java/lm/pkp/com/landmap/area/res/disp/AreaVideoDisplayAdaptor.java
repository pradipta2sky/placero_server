package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.RemoveDriveResourcesActivity;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

final class AreaVideoDisplayAdaptor extends BaseAdapter {

    private final Context context;
    private final Fragment fragment;
    private final int tabPosition;

    final ArrayList<VideoDisplayElement> dataSet = VideoDataHolder.INSTANCE.getData();

    public AreaVideoDisplayAdaptor(Context context, Fragment fragment, int tabPosition) {
        this.context = context;
        this.fragment = fragment;
        this.tabPosition = tabPosition;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(context);
            view.setScaleType(CENTER_CROP);
        } else {
            return view;
        }

        // Get the image URL for the current position.
        final String url = getItem(position);
        final VideoDisplayElement displayElement = dataSet.get(position);
        final File displayFile = new File(displayElement.getAbsPath());

        AreaContext ac = AreaContext.INSTANCE;
        AreaElement ae = ac.getAreaElement();

        String thumbPath = ac.INSTANCE.getAreaLocalVideoThumbnailRoot(ae.getUniqueId()).getAbsolutePath();
        File thumbFile = new File(thumbPath + File.separatorChar + displayElement.getName());

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
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                File file = new File(url);
                intent.setDataAndType(Uri.fromFile(displayFile), "video/*");
                referredView.getContext().startActivity(intent);
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                FloatingActionButton deleteButton = (FloatingActionButton) fragment.getView().findViewById(R.id.res_delete);
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(referredView.getContext(), RemoveDriveResourcesActivity.class);
                        String resourceId = dataSet.get(position).getResourceId();
                        if (!resourceId.equalsIgnoreCase("")) {
                            intent.putExtra("resource_ids", resourceId);
                            intent.putExtra("tab_position", tabPosition);
                            referredView.getContext().startActivity(intent);
                        } else {
                            Toast.makeText(referredView.getContext(),
                                    "This image is auto generated. It cannot be removed.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                return false;
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