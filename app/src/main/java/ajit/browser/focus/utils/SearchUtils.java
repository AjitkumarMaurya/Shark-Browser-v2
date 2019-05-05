package ajit.browser.focus.utils;

import android.content.Context;

import ajit.browser.focus.search.SearchEngine;
import ajit.browser.focus.search.SearchEngineManager;
import ajit.browser.focus.search.SearchEngine;
import ajit.browser.focus.search.SearchEngineManager;
import ajit.browser.focus.search.SearchEngine;
import ajit.browser.focus.search.SearchEngineManager;

public class SearchUtils {

    public static String createSearchUrl(Context context, String searchTerm) {
        final SearchEngine searchEngine = SearchEngineManager.getInstance()
                .getDefaultSearchEngine(context);

        return searchEngine.buildSearchUrl(searchTerm);
    }
}
