package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.RemoveDriveResourcesActivity;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.permission.PermissionConstants;
import lm.pkp.com.landmap.permission.PermissionManager;

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

        Drawable drawable = view.getDrawable();
        if(drawable == null){
            drawable = view.getBackground();
        }
        if(drawable != null){
            Bitmap previousBitmap = ((BitmapDrawable) drawable).getBitmap();
            if(previousBitmap != null){
                previousBitmap.recycle();
            }
        }

        final File thumbFile = dataSet.get(position).getThumbnailFile();
        final File imageFile = dataSet.get(position).getImageFile();

        Bitmap bMap = null;
        if (thumbFile.exists()) {
            bMap = BitmapFactory.decodeFile(thumbFile.getAbsolutePath());
        }else {
            bMap = BitmapFactory.decodeResource(context.getResources(), R.drawable.error);
        }
        view.setImageBitmap(bMap);

        final View referredView = view;

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(imageFile), "image/*");
                referredView.getContext().startActivity(intent);
            }
        });

        view.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ImageView clickedImage = (ImageView) referredView;
                clickedImage.setBackgroundResource(R.drawable.image_border);

                fragment.getView().findViewById(id.res_delete_layout).setVisibility(View.VISIBLE);
                final PictureDisplayElement pictureDisplayElement = dataSet.get(position);
                final String resourceId = pictureDisplayElement.getResourceId();

                FloatingActionButton deleteButton = (FloatingActionButton) fragment.getView().findViewById(id.res_delete);
                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(PermissionManager.INSTANCE.hasAccess(PermissionConstants.REMOVE_RESOURCES)){
                            Intent intent = new Intent(referredView.getContext(), RemoveDriveResourcesActivity.class);
                            if (!resourceId.equalsIgnoreCase("1")) {
                                intent.putExtra("resource_ids", resourceId);
                                intent.putExtra("tab_position", tabPosition);
                                referredView.getContext().startActivity(intent);
                            } else {
                                DriveDBHelper ddh = new DriveDBHelper(fragment.getContext());
                                DriveResource driveResource = ddh.getDriveResourceByResourceId(resourceId);

                                AreaContext.INSTANCE.getAreaElement().getMediaResources().remove(driveResource);
                                ddh.deleteResourceLocally(driveResource);
                                ddh.deleteResourceFromServer(driveResource);

                                dataSet.remove(pictureDisplayElement);
                                notifyDataSetChanged();
                            }
                        }else {
                            showErrorMessage(referredView,"Do not have removal rights", "error");
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

    private void showErrorMessage(View view, String message, String type) {
        final Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);

        View sbView = snackbar.getView();
        snackbar.getView().setBackgroundColor(Color.WHITE);

        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        if (type.equalsIgnoreCase("info")) {
            textView.setTextColor(Color.GREEN);
        } else if (type.equalsIgnoreCase("error")) {
            textView.setTextColor(Color.RED);
        } else {
            textView.setTextColor(Color.DKGRAY);
        }
        textView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        textView.setTextSize(15);
        textView.setMaxLines(3);

        snackbar.setAction("Dismiss", new OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

}