package ua.naiksoftware.phprunner;

import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MyAdapter extends BaseAdapter {

    private ArrayList<Item> list = new ArrayList<Item>();
    private Context context;
    private LayoutInflater li;

    public MyAdapter(Context context, ArrayList<Item> arr) {
        if (arr != null) {
            list = arr;
        }
        this.context = context;
        li = LayoutInflater.from(context);
    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View view, ViewGroup viewGroup) {
		ViewHolder holder;
        if (view == null) {
            view = li.inflate(R.layout.list_row, null);
			holder = new ViewHolder();
			holder.imageView = (ImageView) view.findViewById(R.id.list_image);
			holder.header = (TextView) view.findViewById(R.id.list_header);
			holder.subheader = (TextView) view.findViewById(R.id.list_subheader);
			view.setTag(holder);
        } else {
			holder = (ViewHolder) view.getTag();
		}
        Item item = list.get(position);
        

        holder.imageView.setImageResource(item.getImageId());
        holder.header.setText(item.getHeader());
        holder.subheader.setText(item.getSubheader());
        return view;
    }
	
	private static class ViewHolder {
		ImageView imageView;
		TextView header, subheader;
	}
}
