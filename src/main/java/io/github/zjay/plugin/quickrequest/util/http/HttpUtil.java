package io.github.zjay.plugin.quickrequest.util.http;

import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtil {

    public static String getFileNameFromDisposition(Response response) {
        String fileName = null;
        final String disposition = response.header(Header.CONTENT_DISPOSITION.getValue());
        if (StringUtils.isNotBlank(disposition)) {
            fileName = get("filename=\"(.*?)\"", disposition, 1);
            if (StringUtils.isBlank(fileName)) {
                fileName = subAfter(disposition, "filename=", true);
            }
        }
        return fileName;
    }

    public static String subAfter(CharSequence string, CharSequence separator, boolean isLastSeparator) {
        if (StringUtils.isEmpty(string)) {
            return null == string ? null : StringUtils.EMPTY;
        }
        if (separator == null) {
            return StringUtils.EMPTY;
        }
        final String str = string.toString();
        final String sep = separator.toString();
        final int pos = isLastSeparator ? str.lastIndexOf(sep) : str.indexOf(sep);
        if (-1 == pos || (string.length() - 1) == pos) {
            return StringUtils.EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    public static String get(String regex, CharSequence content, int groupIndex) {
        if (null == content || null == regex) {
            return null;
        }
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        return get(pattern, content, groupIndex);
    }

    public static String get(Pattern pattern, CharSequence content, int groupIndex) {
        if (null == content || null == pattern) {
            return null;
        }

        final Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(groupIndex);
        }
        return null;
    }

    public static String subSuf(CharSequence string, int fromIndex) {
        if (StringUtils.isEmpty(string)) {
            return null;
        }
        return sub(string, fromIndex, string.length());
    }

    public static String str(byte[] data, Charset charset) {
        if (data == null) {
            return null;
        }

        if (null == charset) {
            return new String(data);
        }
        return new String(data, charset);
    }

    public static byte[] bytes(CharSequence str, Charset charset) {
        if (str == null) {
            return null;
        }

        if (null == charset) {
            return str.toString().getBytes();
        }
        return str.toString().getBytes(charset);
    }

    public static String sub(CharSequence str, int fromIndexInclude, int toIndexExclude) {
        if (StringUtils.isEmpty(str)) {
            return str == null ? "" : str.toString();
        }
        int len = str.length();

        if (fromIndexInclude < 0) {
            fromIndexInclude = len + fromIndexInclude;
            if (fromIndexInclude < 0) {
                fromIndexInclude = 0;
            }
        } else if (fromIndexInclude > len) {
            fromIndexInclude = len;
        }

        if (toIndexExclude < 0) {
            toIndexExclude = len + toIndexExclude;
            if (toIndexExclude < 0) {
                toIndexExclude = len;
            }
        } else if (toIndexExclude > len) {
            toIndexExclude = len;
        }

        if (toIndexExclude < fromIndexInclude) {
            int tmp = fromIndexInclude;
            fromIndexInclude = toIndexExclude;
            toIndexExclude = tmp;
        }

        if (fromIndexInclude == toIndexExclude) {
            return StringUtils.EMPTY;
        }

        return str.toString().substring(fromIndexInclude, toIndexExclude);
    }
}
