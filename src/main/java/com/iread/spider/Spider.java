package com.iread.spider;

import com.iread.bean.Category;
import com.iread.bean.Storable;
import com.iread.conf.ConfMan;
import com.iread.util.HttpClientVM;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by liu on 16/9/18.
 */
public abstract class Spider {
    public static HttpClientVM clientVM = new HttpClientVM();
    public static ConfMan conf = new ConfMan();

    public abstract ArrayList<Category> fetchCategorys();

    protected static Document fetchDocument(String url) {
        String content = clientVM.get(url);
        return Jsoup.parse(content);
    }

    protected Document fetchDocument(Storable storable) throws IOException {
        String path = conf.getWarehouse(storable.getSpecies()) + "/" + storable.getStoreFilename();
        File storeFile = new File(path);
        // 文件不存在或已过期
        if(!storeFile.exists() || expired(storeFile)) {
            String html = clientVM.get(storable.getUrl());
            FileUtils.write(storeFile, html, false);
            return Jsoup.parse(html);
        }
        return Jsoup.parse(storeFile, "UTF-8");
    }

    protected boolean expired(File file) {
        return (System.currentTimeMillis() - file.lastModified()) / 86400 >= conf.getShelflife();
    }

    protected String getHrefInElement(Element el) {
        String html = el.toString();
        String url = StringUtils.substringBetween(html, "href=\"", "\"");
        return url.replaceAll("amp;", "");
    }

    protected String getSquareSortUrlFromDoc(Document document) {
        Element squareSortEl = document.getElementsByAttributeValue("title", "图像视图").first();
        String squareSortUrl = getHrefInElement(squareSortEl);
        return squareSortUrl;
    }
}
