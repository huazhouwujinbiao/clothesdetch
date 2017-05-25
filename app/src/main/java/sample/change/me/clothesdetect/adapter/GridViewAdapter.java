package sample.change.me.clothesdetect.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import sample.change.me.clothesdetect.R;

public class GridViewAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Bitmap> mList;

    public GridViewAdapter(Context mContext,
                           ArrayList<Bitmap> mList) {
        super();
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        if (mList == null) {
            return 0;
        } else {
            return this.mList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (mList == null) {
            return null;
        } else {
            return this.mList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(this.mContext).inflate(
                    R.layout.gridview_item, null, false);
            holder.img = (ImageView) convertView
                    .findViewById(R.id.image);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (this.mList != null) {
            Bitmap bitmap = this.mList.get(position);
            if (holder.img != null) {
                holder.img.setImageBitmap(bitmap);
                holder.img.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext, "第" + (position + 1) + "个",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        return convertView;
    }

    private class ViewHolder {
        ImageView img;
    }
}