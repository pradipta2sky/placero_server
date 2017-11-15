package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import lm.pkp.com.landmap.custom.PermittedFileArrayList;
import lm.pkp.com.landmap.area.res.doc.BaseFragmentAdapter;
import lm.pkp.com.landmap.area.res.doc.TextDetailDocumentsCell;

/**
 * Created by USER on 11/7/2017.
 */
public class DocumentChooserAdaptor extends BaseFragmentAdapter {

    private Context mContext;
    private PermittedFileArrayList<FileDisplayElement> items;

    public DocumentChooserAdaptor(Context context, PermittedFileArrayList<FileDisplayElement> chosenItems) {
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
        FileDisplayElement item = items.get(position);
        ((TextDetailDocumentsCell) convertView).setValues(item.getName(), item.getDesc(), item.getIcon());
        return convertView;
    }
}
