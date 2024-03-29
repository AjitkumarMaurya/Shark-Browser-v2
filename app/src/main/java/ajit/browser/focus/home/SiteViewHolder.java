/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ajit.browser.focus.home;

import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ajit.browser.lightning.R;
import ajit.browser.lightning.home.pinsite.PinViewWrapper;

class SiteViewHolder extends RecyclerView.ViewHolder {

    AppCompatImageView img;
    TextView text;
    PinViewWrapper pinView;

    public SiteViewHolder(View itemView) {
        super(itemView);
        img = itemView.findViewById(R.id.content_image);
        text = itemView.findViewById(R.id.text);
        pinView = new PinViewWrapper(itemView.findViewById(R.id.pin_indicator));
    }
}
