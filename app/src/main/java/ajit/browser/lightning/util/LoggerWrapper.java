package ajit.browser.lightning.util;

import ajit.browser.focus.utils.AppConstants;
import ajit.browser.logger.Logger;

public class LoggerWrapper {

    public static void throwOrWarn(String tag, String msg) {
        throwOrWarn(tag, msg, null);
    }

    public static void throwOrWarn(String tag, String msg, RuntimeException exception) {
        Logger.throwOrWarn(AppConstants.isReleaseBuild(), tag, msg, exception);
    }
}
