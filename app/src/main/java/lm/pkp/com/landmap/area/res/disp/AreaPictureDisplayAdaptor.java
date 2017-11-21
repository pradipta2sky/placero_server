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
import java.util.List;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.drawable;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.RemoveDriveResourcesActivity;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

final class AreaPictureDisplayAdaptor extends BaseAdapter {

    private final Context context;
    private final Fragment fragment;
    private final int tabPosition;

    final ArrayList<PictureDisplayElement> dataSet = PictureDataHolder.INSTANCE.getData();

    AreaPictureDisplayAdaptor(Context context, Fragment fragment, int tabPosition) {
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
        AreaContext ac = AreaContext.INSTANCE;
        AreaElement ae = ac.getAreaElement();
        String thumbnailRoot = ac.getAreaLocalPictureThumbnailRoot(ae.getUniqueId()).getAbsolutePath();
        String thumbnailFilePath = thumbnailRoot + File.separatorChar + this.dataSet.get(position).getName();
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
                intent.setDataAndType(Uri.fromFile(file), "image/*");
                referredView.getContext().startActivity(intent);
            }
        });

        view.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ImageView clickedImage = (ImageView) referredView;
                clickedImage.setBackgroundResource(drawable.image_border);

                AreaPictureDisplayAdaptor.this.fragment.getView().findViewById(id.res_delete_layout).setVisibility(View.VISIBLE);
                final String resourceId = AreaPictureDisplayAdaptor.this.dataSet.get(position).getResourceId();
                FloatingActionButton deleteButton = (FloatingActionButton) AreaPictureDisplayAdaptor.this.fragment.getView().findViewById(id.res_delete);
                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(referredView.getContext(), RemoveDriveResourcesActivity.class);
                        if (!resourceId.equalsIgnoreCase("1")) {
                            intent.putExtra("resource_ids", resourceId);
                            intent.putExtra("tab_position", AreaPictureDisplayAdaptor.this.tabPosition);
                            referredView.getContext().startActivity(intent);
                        } else {
                            Toast.makeText(referredView.getContext(),
                                    "Plot Image is auto generated. It will be recreated on Map plot.", Toast.LENGTH_LONG).show();

                            AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
                            List<DriveResource> driveResources = areaElement.getMediaResources();

                            DriveResource driveResource = new DriveResource();
                            driveResource.setResourceId(resourceId);
                            driveResources.remove(driveResource);

                            DriveDBHelper ddh = new DriveDBHelper(AreaPictureDisplayAdaptor.this.fragment.getContext());
                            ddh.deleteResourceByResourceId(resourceId);
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