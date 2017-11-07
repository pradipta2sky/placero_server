package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import lm.pkp.com.landmap.custom.PermittedFileArrayList;
import lm.pkp.com.landmap.lib.fe.BaseFragmentAdapter;
import lm.pkp.com.landmap.lib.fe.TextDetailDocumentsCell;

/**
 * Created by USER on 11/7/2017.
 */
public class DocumentChooserAdaptor extends BaseFragmentAdapter {

    private Context mContext;
    private PermittedFileArrayList<FileDisplayItem> items;

    public DocumentChooserAdaptor(Context context, PermittedFileArrayList<FileDisplayItem> chosenItems) {
        this.mContext = context;
        this.items = chosenItems;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public void clear() {
        items.clear();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public int getViewTypeCount() {
        return 2;
    }

    public int getItemViewType(int pos) {
        return items.get(pos).getDesc().length() > 0 ? 0 : 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new TextDetailDocumentsCell(mContext);
        }
        FileDisplayItem item = items.get(position);
        ((TextDetailDocumentsCell) convertView)
                .setTextAndValueAndTypeAndThumb(item.getName(), item.getDesc(), item.getIcon());
        return convertView;
    }
}
