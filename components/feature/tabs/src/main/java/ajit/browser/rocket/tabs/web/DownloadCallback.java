/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package ajit.browser.rocket.tabs.web;

import android.support.annotation.NonNull;

public interface DownloadCallback {
    void onDownloadStart(@NonNull Download download);
}
