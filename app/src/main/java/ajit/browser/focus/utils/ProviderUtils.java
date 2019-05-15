/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ajit.browser.focus.utils;

/**
 * Created by hart on 15/08/2017.
 */

public class ProviderUtils {
    public static String getLimitParam(String offset, String limit) {
        return (limit == null) ? null : (offset == null) ? limit : offset + "," + limit;
    }
}