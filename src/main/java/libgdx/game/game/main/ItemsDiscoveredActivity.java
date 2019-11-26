package libgdx.game.game.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.simapps.memorygame.R;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.ListView;
import db.StoreManagement;
import model.Item;
import util.ActivityUtil;

public class ItemsDiscoveredActivity extends Activity {

	private static ItemsDiscoveredActivity instance;

	public ItemsDiscoveredActivity() {
		instance = this;
	}

	public static ItemsDiscoveredActivity getContext() {
		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_items_discovered);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		ListView list = (ListView) findViewById(R.id.discItemsList);
		list.setSelector(android.R.color.transparent);
		list.setDivider(null);

		createListview();
	}

	private void createListview() {
		StoreManagement storeManagement = new StoreManagement(this);
		List<String> discoveredItems = storeManagement.retrieveDiscoveredElements();
		List<String> allItems = getItemNames(ActivityUtil.getItemsFromResources(this));
		ItemsDiscoveredAdapter levelsListAdapter = new ItemsDiscoveredAdapter(getContext(), R.layout.item_discovered,
				discoveredItems, allItems);
		ListView list = (ListView) findViewById(R.id.discItemsList);
		list.setAdapter(levelsListAdapter);
	}

	private List<String> getItemNames(List<Item> allItems) {
		List<String> itemNames = new ArrayList<String>();
		for (Item item : allItems) {
			itemNames.add(item.getItemName());
		}
		Collections.reverse(itemNames);
		return itemNames;
	}

}
