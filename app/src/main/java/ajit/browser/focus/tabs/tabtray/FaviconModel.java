/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ajit.browser.focus.tabs.tabtray;

import android.graphics.Bitmap;

import ajit.browser.focus.utils.DimenUtils;
import ajit.browser.focus.utils.DimenUtils;
import ajit.browser.icon.FavIconUtils;
import ajit.browser.focus.utils.DimenUtils;

public class FaviconModel {
    String url;
    @DimenUtils.FavIconType
    int type;
    Bitmap originalIcon;

    FaviconModel(String url, @DimenUtils.FavIconType int type, Bitmap favicon) {
        this.url = url;
        this.type = type;
        this.originalIcon = favicon;
    }
}
