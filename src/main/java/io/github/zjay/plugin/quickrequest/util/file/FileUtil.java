package io.github.zjay.plugin.quickrequest.util.file;


import io.github.zjay.plugin.quickrequest.util.CharPool;
import io.github.zjay.plugin.quickrequest.util.URLEncoder;
import io.github.zjay.plugin.quickrequest.util.http.HttpUtil;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    public static final URLEncoder QUERY = createQuery();

    public static File completeFileNameFromHeader(File destFile, Response response) {
        if (!destFile.isDirectory()) {
            // 非目录直接返回
            return destFile;
        }

        // 从头信息中获取文件名
        String fileName = HttpUtil.getFileNameFromDisposition(response);
        if (StringUtils.isBlank(fileName)) {
            final String path = response.request().url().toString();
            String[] split = path.split("/");
            for (int i = split.length - 1; i >= 0; i--) {
                if(StringUtils.isNotBlank(split[i])){
                    if(split[i].contains("?")){
                        fileName = split[i].substring(0, split[i].indexOf("?"));
                    }else {
                        fileName = split[i];
                    }
                    break;
                }
            }
            if (StringUtils.isBlank(fileName)) {
                // 编码后的路径做为文件名
                fileName = encodeQuery(path, StandardCharsets.UTF_8);
            }
        }
        return file(destFile, fileName);
    }

    public static void move(File src, File target, boolean isOverride) {
        Asserts.notNull(src, "Src file");
        Asserts.notNull(target, "target file");
        move(src.toPath(), target.toPath(), isOverride);
    }

    public static Path move(Path src, Path target, boolean isOverride) {
        Asserts.notNull(src, "Src path");
        Asserts.notNull(target, "Target path");


        if (isDirectory(target, false)) {
            target = target.resolve(src.getFileName());
        }
        return moveContent(src, target, isOverride);
    }

    public static boolean isDirectory(Path path, boolean isFollowLinks) {
        if (null == path) {
            return false;
        }
        final LinkOption[] options = isFollowLinks ? new LinkOption[0] : new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
        return Files.isDirectory(path, options);
    }

    public static Path moveContent(Path src, Path target, boolean isOverride) {
        Asserts.notNull(src, "Src path");
        Asserts.notNull(target, "Target path");
        final CopyOption[] options = isOverride ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING} : new CopyOption[]{};

        // 自动创建目标的父目录
        mkdir(target.getParent());
        try {
            return Files.move(src, target, options);
        } catch (IOException e) {
            // 移动失败，可能是跨分区移动导致的，采用递归移动方式
            try {
                Files.walkFileTree(src, new MoveVisitor(src, target, options));
                // 移动后空目录没有删除，
                del(src);
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
            return target;
        }
    }

    public static Path mkdir(Path dir) {
        if (null != dir && !exists(dir, false)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir;
    }

    public static boolean del(Path path) throws Exception {
        if (Files.notExists(path)) {
            return true;
        }

        try {
            if (isDirectory(path, false)) {
                Files.walkFileTree(path, DelVisitor.INSTANCE);
            } else {
                delFile(path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    protected static void delFile(Path path) throws IOException {
        try {
            Files.delete(path);
        } catch (AccessDeniedException e) {
            // 可能遇到只读文件，无法删除.使用 file 方法删除
            if (!path.toFile().delete()) {
                throw e;
            }
        }
    }

    public static boolean exists(Path path, boolean isFollowLinks) {
        final LinkOption[] options = isFollowLinks ? new LinkOption[0] : new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
        return Files.exists(path, options);
    }

    public static File file(File parent, String path) {
        if (StringUtils.isBlank(path)) {
            throw new NullPointerException("File path is blank!");
        }
        return checkSlip(parent, buildFile(parent, path));
    }

    public static String encodeQuery(String url, Charset charset) {
        if (StringUtils.isEmpty(url)) {
            return url;
        }
        if (null == charset) {
            charset = Charset.defaultCharset();
        }
        return QUERY.encode(url, charset);
    }

    public static URLEncoder createQuery() {
        final URLEncoder encoder = new URLEncoder();
        // Special encoding for space
        encoder.setEncodeSpaceAsPlus(true);
        // Alpha and digit are safe by default
        // Add the other permitted characters
        encoder.addSafeCharacter('*');
        encoder.addSafeCharacter('-');
        encoder.addSafeCharacter('.');
        encoder.addSafeCharacter('_');
        encoder.addSafeCharacter('=');
        encoder.addSafeCharacter('&');
        return encoder;
    }



    public static File checkSlip(File parentFile, File file) throws IllegalArgumentException {
        if (null != parentFile && null != file) {
            String parentCanonicalPath;
            String canonicalPath;
            try {
                parentCanonicalPath = parentFile.getCanonicalPath();
                canonicalPath = file.getCanonicalPath();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!canonicalPath.startsWith(parentCanonicalPath)) {
                throw new IllegalArgumentException("New file is outside of the parent dir: " + file.getName());
            }
        }
        return file;
    }

    /**
     * 根据压缩包中的路径构建目录结构，在Win下直接构建，在Linux下拆分路径单独构建
     *
     * @param outFile  最外部路径
     * @param fileName 文件名，可以包含路径
     * @return 文件或目录
     * @since 5.0.5
     */
    private static File buildFile(File outFile, String fileName) {
        // 替换Windows路径分隔符为Linux路径分隔符，便于统一处理
        fileName = fileName.replace('\\', '/');
        if (File.separatorChar != CharPool.BACKSLASH
                // 检查文件名中是否包含"/"，不考虑以"/"结尾的情况
                && fileName.lastIndexOf(CharPool.SLASH, fileName.length() - 2) > 0) {
            // 在Linux下多层目录创建存在问题，/会被当成文件名的一部分，此处做处理
            // 使用/拆分路径（zip中无\），级联创建父目录
            final List<String> pathParts = split(fileName, '/', 0,false, true, false);
            final int lastPartIndex = pathParts.size() - 1;//目录个数
            for (int i = 0; i < lastPartIndex; i++) {
                //由于路径拆分，slip不检查，在最后一步检查
                outFile = new File(outFile, pathParts.get(i));
            }
            //noinspection ResultOfMethodCallIgnored
            outFile.mkdirs();
            // 最后一个部分如果非空，作为文件名
            fileName = pathParts.get(lastPartIndex);
        }
        return new File(outFile, fileName);
    }

    public static List<String> split(String str, char separator, int limit, boolean isTrim, boolean ignoreEmpty, boolean ignoreCase) {
        if (StringUtils.isEmpty(str)) {
            return new ArrayList<>(0);
        }
        if (limit == 1) {
            return addToList(new ArrayList<>(1), str, isTrim, ignoreEmpty);
        }

        final ArrayList<String> list = new ArrayList<>(limit > 0 ? limit : 16);
        int len = str.length();
        int start = 0;//切分后每个部分的起始
        for (int i = 0; i < len; i++) {
            if (numEquals(separator, str.charAt(i), ignoreCase)) {
                addToList(list, str.substring(start, i), isTrim, ignoreEmpty);
                start = i + 1;//i+1同时将start与i保持一致

                //检查是否超出范围（最大允许limit-1个，剩下一个留给末尾字符串）
                if (limit > 0 && list.size() > limit - 2) {
                    break;
                }
            }
        }
        return addToList(list, str.substring(start, len), isTrim, ignoreEmpty);//收尾
    }

    public static boolean numEquals(char c1, char c2, boolean ignoreCase) {
        if (ignoreCase) {
            return Character.toLowerCase(c1) == Character.toLowerCase(c2);
        }
        return c1 == c2;
    }

    private static List<String> addToList(List<String> list, String part, boolean isTrim, boolean ignoreEmpty) {
        if (isTrim) {
            part = part == null ? "" : part.trim();
        }
        if (!ignoreEmpty || !part.isEmpty()) {
            list.add(part);
        }
        return list;
    }
}
