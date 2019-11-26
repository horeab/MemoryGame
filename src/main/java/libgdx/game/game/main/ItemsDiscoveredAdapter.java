package libgdx.game.game.main;

import java.util.List;

import util.Utils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.simapps.memorygame.R;

class ItemsDiscoveredAdapter extends ArrayAdapter<String> {

	private final Activity context;

	private Typeface font;

	private List<String> items;
	private List<String> allItems;

	public ItemsDiscoveredAdapter(Activity context, int textViewResourceId, List<String> items, List<String> allItems) {
		super(context, textViewResourceId);
		font = Utils.getTypeFace(context);
		this.context = context;
		this.items = items;
		this.allItems = allItems;
	}

	@Override
	public int getCount() {
		return allItems.size();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_discovered, null);
		}

		String item = allItems.get(position);

		String imgId = items.contains(item) ? "item" + ((allItems.size() - position) - 1) : "unknownitem";
		int resID = context.getResources().getIdentifier(imgId, "drawable", context.getPackageName());
		ImageView itemImg = (ImageView) convertView.findViewById(R.id.itemImg);
		itemImg.setImageResource(resID);

		String itemName = items.contains(item) ? item : "???";
		TextView name = (TextView) convertView.findViewById(R.id.itemName);
		name.setText(itemName);
		name.setTypeface(font);

		return convertView;
	}
}