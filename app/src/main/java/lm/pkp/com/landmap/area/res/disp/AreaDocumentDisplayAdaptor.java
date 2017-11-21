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
import java.util.List;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.drawable;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.RemoveDriveResourcesActivity;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

final class AreaDocumentDisplayAdaptor extends BaseAdapter {

    private final Context context;
    private final List<DocumentDisplayElement> docElems = DocumentDataHolder.INSTANCE.getData();
    private final Fragment fragment;
    private final int tabPosition;

    public AreaDocumentDisplayAdaptor(Context context, Fragment fragment, int tabPosition) {
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

        AreaContext areaContext = AreaContext.INSTANCE;
        AreaElement areaElement = areaContext.getAreaElement();
        // Get the image URL for the current position.
        DocumentDisplayElement currElem = this.docElems.get(position);
        String thumbnailRoot = areaContext
                .getAreaLocalDocumentThumbnailRoot(areaElement.getUniqueId()).getAbsolutePath();
        String thumbnailFilePath = thumbnailRoot + File.separatorChar + currElem.getName();
        File thumbFile = new File(thumbnailFilePath);

        if (thumbFile.exists()) {
            Bitmap bMap = BitmapFactory.decodeFile(thumbnailFilePath);
            view.setImageBitmap(bMap);
        }

        final View referredView = view;
        final String url = this.getItem(position);

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                File file = new File(url);
                intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                referredView.getContext().startActivity(intent);
            }
        });

        view.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ImageView clickedImage = (ImageView) referredView;
                clickedImage.setBackgroundResource(drawable.image_border);

                FloatingActionButton deleteButton = (FloatingActionButton) AreaDocumentDisplayAdaptor.this.fragment.getView().findViewById(id.res_delete);
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(referredView.getContext(), RemoveDriveResourcesActivity.class);
                        String resourceId = AreaDocumentDisplayAdaptor.this.docElems.get(position).getResourceId();
                        if (!resourceId.equalsIgnoreCase("")) {
                            intent.putExtra("resource_ids", resourceId);
                            intent.putExtra("tab_position", AreaDocumentDisplayAdaptor.this.tabPosition);
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
        return this.docElems.size();
    }

    @Override
    public String getItem(int position) {
        return this.docElems.get(position).getAbsPath();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}