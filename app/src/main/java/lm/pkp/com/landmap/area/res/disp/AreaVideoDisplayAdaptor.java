package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.drawable;
import lm.pkp.com.landmap.R.id;
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
            view = new SquaredImageView(this.context);
            view.setScaleType(CENTER_CROP);
        } else {
            return view;
        }

        // Get the image URL for the current position.
        final String url = this.getItem(position);
        VideoDisplayElement displayElement = this.dataSet.get(position);
        final File displayFile = new File(displayElement.getAbsPath());

        AreaContext ac = AreaContext.INSTANCE;
        AreaElement ae = ac.getAreaElement();

        String thumbPath = AreaContext.INSTANCE.getAreaLocalVideoThumbnailRoot(ae.getUniqueId()).getAbsolutePath();
        String thumbnailFilePath = thumbPath + File.separatorChar + displayElement.getName();
        File thumbFile = new File(thumbnailFilePath);

        if (thumbFile.exists()) {
            Bitmap bMap = BitmapFactory.decodeFile(thumbnailFilePath);
            view.setImageBitmap(bMap);
        }

        final View referredView = view;
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                File file = new File(url);
                intent.setDataAndType(Uri.fromFile(displayFile), "video/*");
                referredView.getContext().startActivity(intent);
            }
        });

        view.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ImageView clickedImage = (ImageView) referredView;
                clickedImage.setBackgroundResource(drawable.image_border);

                FloatingActionButton deleteButton = (FloatingActionButton) AreaVideoDisplayAdaptor.this.fragment.getView().findViewById(id.res_delete);
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(referredView.getContext(), RemoveDriveResourcesActivity.class);
                        String resourceId = AreaVideoDisplayAdaptor.this.dataSet.get(position).getResourceId();
                        if (!resourceId.equalsIgnoreCase("")) {
                            intent.putExtra("resource_ids", resourceId);
                            intent.putExtra("tab_position", AreaVideoDisplayAdaptor.this.tabPosition);
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
        return this.dataSet.size();
    }

    @Override
    public String getItem(int position) {
        return this.dataSet.get(position).getAbsPath();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}