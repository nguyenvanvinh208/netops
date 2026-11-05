package com.example.netops;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImagesFragment extends BaseFragment {
    @Override protected int layout() { return R.layout.fragment_images; }

    @Override protected void init() {
        ((TextView) v(R.id.hdr_title)).setText("Containers");
        ((TextView) v(R.id.hdr_sub)).setText("Azure Container Registry \u00b7 3 images");
        tap(R.id.hdr_add, () -> nav().go("newdeploy"));
        UI.segmented((LinearLayout) v(R.id.seg_img), new String[]{"Containers", "Images"}, 1, i -> { if (i == 0) nav().replace("containers"); });
        tap(R.id.im_sync, () -> Store.say("Registry synced"));
        LinearLayout list = v(R.id.im_list);
        for (final Data.Img m : Data.IMAGES) {
            View it = UI.inflate(list, R.layout.item_image);
            UI.tint(it.findViewById(R.id.im_tile), m.tile, 14);
            UI.icon((ImageView) it.findViewById(R.id.im_icon), m.icon, m.tile);
            ((TextView) it.findViewById(R.id.im_name)).setText(m.name);
            ((TextView) it.findViewById(R.id.im_digest)).setText(m.digest);
            UI.pill((TextView) it.findViewById(R.id.im_tag), m.tag, m.tile);
            ((TextView) it.findViewById(R.id.im_size)).setText("Size \u00a0" + m.size);
            ((TextView) it.findViewById(R.id.im_pushed)).setText("Pushed \u00a0" + m.pushed);
            it.findViewById(R.id.im_deploy).setOnClickListener(x -> nav().go("newdeploy", m.id));
            list.addView(it);
        }
    }
}
