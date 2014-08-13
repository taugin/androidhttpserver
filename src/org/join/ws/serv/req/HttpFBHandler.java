package org.join.ws.serv.req;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.join.web.serv.R;
import org.join.ws.Constants.Config;
import org.join.ws.WSApplication;
import org.join.ws.serv.req.objs.FileRow;
import org.join.ws.serv.req.objs.TwoColumn;
import org.join.ws.serv.support.Progress;
import org.join.ws.serv.view.ViewFactory;
import org.join.ws.util.CommonUtil;

import se.unlogic.standardutils.crypto.Base64;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.chukong.apwebauthentication.util.Log;

/**
 * @brief 目录浏览页面请求处理
 * @author join
 */
public class HttpFBHandler implements HttpRequestHandler {

    private CommonUtil mCommonUtil = CommonUtil.getSingleton();
    private ViewFactory mViewFactory = ViewFactory.getSingleton();

    private String webRoot;

    public HttpFBHandler(final String webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        String target = URLDecoder.decode(request.getRequestLine().getUri(), Config.ENCODING);
        File file;
        if (target.equals("/")) {
            file = new File(this.webRoot);
        } else if (!target.startsWith(Config.SERV_ROOT_DIR) && !target.startsWith(this.webRoot)) {
            //response.setStatusCode(HttpStatus.SC_FORBIDDEN);
            //response.setEntity(resp403(request));
            response.setStatusCode(301);
            String ip = mCommonUtil.getLocalIpAddress();
            int port  = Config.PORT;
            if (!TextUtils.isEmpty(ip)) {
                ip = ip + ":" + port;
            } else {
                ip = "http://10.0.0.115:7766";
            }
            if (!ip.startsWith("http")) {
                ip = "http://" + ip;
            }
            response.addHeader("Location", ip);
            return;
        } else {
            file = new File(target);
        }

        HttpEntity entity;
        String contentType = "text/html;charset=" + Config.ENCODING;
        if (!file.exists()) { // 不存在
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            entity = resp404(request);
        } else if (file.canRead()) { // 可读
            response.setStatusCode(HttpStatus.SC_OK);
            if (file.isDirectory()) {
                entity = respView(request, file);
            } else {
                entity = respFile(request, file);
                contentType = entity.getContentType().getValue();
            }
        } else { // 不可读
            response.setStatusCode(HttpStatus.SC_FORBIDDEN);
            entity = resp403(request);
        }

        response.setHeader("Content-Type", contentType);
        response.setEntity(entity);

        Progress.clear();
    }

    private HttpEntity respFile(HttpRequest request, File file) throws IOException {
        return mViewFactory.renderFile(request, file);
    }

    private HttpEntity resp403(HttpRequest request) throws IOException {
        return mViewFactory.renderTemp(request, "403.html");
    }

    private HttpEntity resp404(HttpRequest request) throws IOException {
        return mViewFactory.renderTemp(request, "404.html");
    }

    private HttpEntity respView(HttpRequest request, File dir) throws IOException {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("dirpath", dir.getPath()); // 目录路径
        data.put("hasParent", !isSamePath(dir.getPath(), this.webRoot)); // 是否有上级目录
        data.put("fileRows", buildFileRows(dir)); // 文件行信息集合
        return mViewFactory.renderTemp(request, "view.html", data);
    }

    private boolean isSamePath(String a, String b) {
        String left = a.substring(b.length(), a.length()); // a以b开头
        if (left.length() >= 2) {
            return false;
        }
        if (left.length() == 1 && !left.equals("/")) {
            return false;
        }
        return true;
    }

    private List<TwoColumn> buildTwoColumn(File dir) {
        List<FileRow> fileRows = buildFileRows(dir);
        if (fileRows == null) {
            return null;
        }
        int index = 0;
        int len = fileRows.size();
        List<TwoColumn> twoColumns = new ArrayList<TwoColumn>();
        TwoColumn twoColumn = null;
        for (index = 0; index < len; index+=2) {
            twoColumn = new TwoColumn();
            if (index < len) {
                twoColumn.fileRow1 = fileRows.get(index);
            }
            if (index + 1 < len) {
                twoColumn.fileRow2 = fileRows.get(index + 1);
            }
            twoColumns.add(twoColumn);
        }
        return twoColumns;
    }
    private List<FileRow> buildFileRows(File dir) {
        File[] files = dir.listFiles(); // 目录列表
        if (files != null) {
            sort(files); // 排序
            ArrayList<FileRow> fileRows = new ArrayList<FileRow>();
            for (File file : files) {
                fileRows.add(buildFileRow(file));
            }
            return fileRows;
        }
        return null;
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd ahh:mm");

    private FileRow buildFileRow(File f) {
        boolean isDir = f.isDirectory();
        String clazz, name, link, size, icon = null;
        if (isDir) {
            clazz = "icon dir";
            name = f.getName() + "/";
            link = f.getPath() + "/";
            size = "";
        } else {
            clazz = "icon file";
            name = f.getName();
            link = f.getPath();
            if (name != null && name.endsWith(".apk")) {
                String label = getApkName(link);
                if (label != null) {
                    name = label;
                }
                icon = getApkIcon(link);
            }
            size = mCommonUtil.readableFileSize(f.length());
        }
        FileRow row = new FileRow(clazz, name, link, size, icon);
        row.time = sdf.format(new Date(f.lastModified()));
        if (f.canRead()) {
            row.can_browse = true;
            if (Config.ALLOW_DOWNLOAD) {
                row.can_download = true;
            }
            if (f.canWrite() && !hasWsDir(f)) {
                if (Config.ALLOW_DELETE) {
                    row.can_delete = true;
                }
                if (Config.ALLOW_UPLOAD && isDir) {
                    row.can_upload = true;
                }
            }
        }
        return row;
    }

    private String getApkName(String apkFile) {
        Context context = WSApplication.getInstance().getBaseContext();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        PackageInfo info = pm.getPackageArchiveInfo(apkFile, PackageManager.GET_ACTIVITIES);
        ApplicationInfo appInfo = null;
        if (info != null) {
            appInfo = info.applicationInfo;
            if (appInfo != null) {
                appInfo.publicSourceDir = apkFile;
                CharSequence label = appInfo.loadLabel(pm);
                if (label != null) {
                    return label.toString();
                }
            }
        }
        return null;
    }
    private String getApkIcon(String apkFile) {
        Context context = WSApplication.getInstance().getBaseContext();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        PackageInfo info = pm.getPackageArchiveInfo(apkFile, PackageManager.GET_ACTIVITIES);
        ApplicationInfo appInfo = null;
        if (info != null) {
            appInfo = info.applicationInfo;
            if (appInfo != null) {
                appInfo.publicSourceDir = apkFile;
                try {
                    Drawable drawable = appInfo.loadIcon(pm);
                    Bitmap bmp = null;
                    if (drawable instanceof BitmapDrawable) {
                        bmp = ((BitmapDrawable) drawable).getBitmap();
                    } else {
                        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_default);
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte iconbyte[] = baos.toByteArray();
                    return Base64.encodeBytes(iconbyte);
                } catch(Exception e) {
                    Log.d(Log.TAG, "e = " + e);
                }
            }
        }
        return null;
    }
    private boolean hasWsDir(File f) {
        return HttpDelHandler.hasWsDir(f);
    }

    /** 排序：文件夹、文件，再各安字符顺序 */
    private void sort(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1.isDirectory() && !f2.isDirectory()) {
                    return -1;
                } else if (!f1.isDirectory() && f2.isDirectory()) {
                    return 1;
                } else {
                    return f1.toString().compareToIgnoreCase(f2.toString());
                }
            }
        });
    }

}
