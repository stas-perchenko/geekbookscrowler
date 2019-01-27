package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.data.*;
import com.alperez.geekbooks.crowler.data.dbmodel.AuthorModel;
import com.alperez.geekbooks.crowler.data.dbmodel.BookCategoryModel;
import com.alperez.geekbooks.crowler.data.dbmodel.BookModel;
import com.alperez.geekbooks.crowler.data.parsing.BookRefItem;
import com.alperez.geekbooks.crowler.utils.*;
//import com.alperez.geekbooks.crowler.utils.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.management.modelmbean.XMLParseException;
import java.awt.geom.Dimension2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

public class BookItemProcessor implements Runnable {


    public interface BookRefItemProvider {
        @Nullable
        BookRefItem getBookRefItem();
    }

    public interface OnBookDecodeListener {
        void onBookDecoded(@NonNull BookModel book, List<URL> relatedBookLinks);
    }

    public interface OnCompleteListener {
        void onComplete();
    }

    private final String host;
    private final BookRefItemProvider src;
    private final OnBookDecodeListener dst;
    private final OnCompleteListener complCallback;

    public BookItemProcessor(@NonNull URL urlHost, @NonNull BookRefItemProvider src, @NonNull OnBookDecodeListener dst, @Nullable OnCompleteListener complCallback) {
        host = urlHost.toString();
        this.src = src;
        this.dst = dst;
        this.complCallback = complCallback;
    }

    @Override
    public void run() {
        try {
            BookRefItem ref;
            while((ref = src.getBookRefItem()) != null) {
                Map<String, Object> result = evaluateBookReference(ref);
                if (result != null) dst.onBookDecoded((BookModel) result.get("book"), (List<URL>) result.get("related"));
            }
        } catch (Exception e) {

        } finally {
            if (complCallback != null) complCallback.onComplete();
        }
    }

    public static final String BREADCRUMBS_TAG = "<ul class=\"breadcrumbs\">";
    public static final String CONTENT_TABLE_TAG = "<table class=\"book-info\">";



    @Nullable
    private Map<String, Object> evaluateBookReference(BookRefItem ref) {

        List<Log.LogEntry> logs = new ArrayList<>(4);
        JSONObject jBookContent = null;
        try {
            logs.add(new Log.LogEntry("\r\n\r\nBOOK", String.format("%1$tT.%1$tL ---> Start loading %2$s", new Date(), ref)));
            String pageHtml = new HtmlPageLoader(ref.getUrl()).load(50000);

            BookCategoryModel category = extractBookCategory(pageHtml);

            jBookContent = extractJsonContent(pageHtml, logs);

            BookModel.Builder bookBuilder = BookModel.builder()
                    .setGeekBooksAddress(ref.getUrl())
                    .setCategory(category)
                    .setRelatedBookIds(new ArrayList<>());
            List<URL> related = decodeBookContent(jBookContent, bookBuilder);

            Map<String, Object> ret = new HashMap<>();
            ret.put("book", bookBuilder.build());
            ret.put("related", related);

            logs.add(new Log.LogEntry("BOOK", "<--- Decoded OK."));
            return ret;
        } catch (Exception e) {
            logs.add(new Log.LogEntry("BOOK", "<~~~ Decode ERROR - " + e.getMessage()));
            if (jBookContent != null) {
                logs.add(new Log.LogEntry("JSON_CONTENT", jBookContent.toString()));
            }
            e.printStackTrace(System.out);
            return null;
        } finally {
            Log.d(logs);
        }
    }

    private BookCategoryModel extractBookCategory(String html) throws JSONException {
        int iStart = html.indexOf(BREADCRUMBS_TAG);
        if (iStart < 0) throw new JSONException("Breadcrumbs tag not found");

        String htmlBreadcrumbs = new XmlTagExtractor(html).getTag("ul", iStart);
        if (htmlBreadcrumbs == null) throw new JSONException("Error extract breadcrumbs tag");

        JSONObject json = org.json.XML.toJSONObject(htmlBreadcrumbs, false);

        JSONObject jUL = json.getJSONObject("ul");
        if (!"breadcrumbs".equals(jUL.getString("class"))) {
            throw new JSONException("'class' property must have 'breadcrumbs' value");
        }


        BookCategoryModel categ = null;

        JSONArray jItems = jUL.getJSONArray("li");
        for (int i=0; i<jItems.length()-1; i++) {
            JSONObject jStage = jItems.getJSONObject(i).getJSONObject("a");
            jStage.getString("href");       // check has HREF
            String name = jStage.getString("content");
            categ = BookCategoryModel.create(i, name, categ);
        }

        return categ;
    }

    private JSONObject extractJsonContent(String html, List<Log.LogEntry> logs) throws XMLParseException {
        int iStart = html.indexOf(CONTENT_TABLE_TAG);
        if (iStart < 0) throw new JSONException("Breadcrumbs tag not found");

        String htmlTable = new XmlTagExtractor(html).getTag("table", iStart);
        if (htmlTable == null) throw new JSONException("Error extract breadcrumbs tag");


        //--- Fix <img> and <hr> tags closing ---
        StringBuilder sb = new StringBuilder(htmlTable);
        fixTagClosing("img", sb);
        fixTagClosing("<hr", sb);


        //--- Fix for single ampersands ---
        int iAmp;
        int startSearch = 0;
        while ((iAmp = sb.indexOf("&", startSearch)) >= 0) {
            int i = iAmp + 1;
            while (i < sb.length()) {
                char ch = sb.charAt(i ++);
                if ((ch >= 'a') && (ch <= 'z')) {
                    continue;
                } else if (ch == ';') {
                    break;
                } else {
                    sb.insert(iAmp + 1, "amp;");
                    i += 4;
                }
            }
            startSearch = i;
        }

        //--- Fix for &nbsp; ---
        htmlTable = sb.toString().replaceAll("&nbsp;", " ");

        JSONObject json = null;
        try {
            json = org.json.XML.toJSONObject(htmlTable, false);
        } catch (JSONException e) {
            logs.add(new Log.LogEntry("HTML_CONTENT", htmlTable));
            throw e;
        }

        JSONObject jTable = json.getJSONObject("table");
        if (!"book-info".equals(jTable.getString("class"))) {
            throw new JSONException("'class' property must have 'book-info' value");
        }

        return json;
    }

    private int fixTagClosing(String tag, StringBuilder sb) throws XMLParseException {
        String realTag = (tag.charAt(0) == '<') ? tag : "<"+tag;
        int nFix = 0;
        int startSearch = 0, iOpen;
        while ((iOpen = sb.indexOf(realTag, startSearch)) >= 0) {
            int iClose = sb.indexOf(">", iOpen);
            if (iClose < iOpen) throw new XMLParseException("No close bracket for <img tag. position="+iOpen);

            sb.insert(iClose, '/');
            nFix ++;
            startSearch = iClose;
        }
        return nFix;
    }

    private List<URL> decodeBookContent(JSONObject jContent, BookModel.Builder dst) throws JSONException {

        //----  Get sections of the content (table columns)  ----
        JSONArray jSections = jContent.getJSONObject("table").getJSONObject("tbody").getJSONObject("tr").getJSONArray("td");

        //----  Parse Section #1 - Cover  ----
        JSONObject jSection0 = jSections.getJSONObject(0);
        if (!jSection0.getString("class").equals("cover")) throw new JSONException("The Cover section must contains \"class\"=\"cover\" property");
        JSONArray jArr = jSection0.getJSONArray("div");
        for (int i=0; i<jArr.length(); i++) {
            JSONObject jCoverItem = jArr.getJSONObject(i);
            if (jCoverItem.has("img")) {
                optCoverImage(jCoverItem, dst);
            } else if ("download".equals(jCoverItem.optString("class"))) {
                decodeDownloadButton(jCoverItem, dst);
            }
        }


        //----  Parse Section #2 - Main book description content  ----
        JSONObject jSection1 = jSections.getJSONObject(1);
        if (!jSection1.getString("class").equals("info")) throw new JSONException("The Main book description content section must contains \"class\"=\"info\" property");
        dst.setTitle(jSection1.getString("h1"));
        dst.setSubtitle(jSection1.optString("h2"));


        Object o = jSection1.opt("div");
        if (o instanceof JSONArray) {
            //When comments are presented on a book's page
            jArr = (JSONArray) o;
            JSONObject jFields = null;
            for (int i=0; i<jArr.length(); i++) {
                JSONObject jO = jArr.getJSONObject(i);
                if ("fields".equals(jO.optString("class"))) {
                    jFields = jO; break;
                }
            }
            if (jFields == null) {
                throw new JSONException("No 'div' object containing Year, Npages, ISBN");
            } else {
                decodeFieldsSection(jFields, dst);
            }
        } else if (o instanceof JSONObject) {
            // When there are not comments on a book's page
            decodeFieldsSection((JSONObject) o, dst);
        } else {
            throw new JSONException("No 'div' object containing Year, Npages, ISBN");
        }


        dst.setAuthors(new ArrayList<>());  // fallback in case of parse error
        dst.setTags(new ArrayList<>());     // fallback in case of parse error
        jArr = jSection1.getJSONArray("p");
        for (int i=0; i<jArr.length(); i++) {
            JSONObject jItem = jArr.getJSONObject(i);
            if ("desc".equals(jItem.optString("class"))) {
                dst.setDescription(jItem.getString("content"));
            } else if ("author".equals(jItem.optString("class"))) {
                decodeAuthors(jItem, dst);
            } else if ("tags".equals(jItem.optString("class"))) {
                decodeTags(jItem, dst);
            }
        }



        //----  Parse Section #3 - Related books  ----
        List<URL> related = new ArrayList<>(10);
        JSONObject jRel = jSections.getJSONObject(2);
        if ("related-books".equals(jRel.optString("class"))) {
            String relHead = jRel.optString("h3");
            if ((relHead != null) && relHead.toLowerCase().equals("related books")) {
                JSONArray jRelItems = jRel.optJSONArray("div");
                if (jRelItems != null) {
                    for (int i=0; i<jRelItems.length(); i++) {
                        JSONObject jRItem = jRelItems.getJSONObject(i);
                        try {
                            related.add(getUrlFromPath(jRItem.getJSONObject("a").getString("href")));
                        } catch (JSONException e) {}
                    }
                }
            }
        }
        return related;


    }

    private void optCoverImage(@NonNull JSONObject jObj, @NonNull BookModel.Builder dst) {
        try {
            JSONObject jCoverImg = jObj.getJSONObject("img");
            dst.setImagePath(getUrlFromPath(jCoverImg.getString("src")));
            dst.setImageDimensions(new Dimension2D() {
                @Override
                public double getWidth() {
                    return jCoverImg.getInt("width");
                }

                @Override
                public double getHeight() {
                    return jCoverImg.getInt("height");
                }

                @Override
                public void setSize(double v, double v1) {
                    throw new NotImplementedException();
                }
            });
        } catch (Exception e) {
            dst.setImagePath(null);
            dst.setImageDimensions(null);
        }
    }

    private void decodeDownloadButton(@NonNull JSONObject jDownl, @NonNull BookModel.Builder dst) throws JSONException {
        if (!jDownl.getString("class").equals("download")) throw new JSONException("The Download sub-section must contains \"class\"=\"download\" property");
        JSONObject jBtn = jDownl.getJSONObject("a");
        if (!jBtn.getString("class").equals("link-button rr")) throw new JSONException("The download button tag must contains \"class\"=\"link-button rr\" property");
        String title = jBtn.getString("span");
        try {
            int iStart = title.indexOf('(');
            int iEnd = title.indexOf(')', iStart);
            String subs = title.substring(iStart + 1, iEnd);
            iEnd = subs.indexOf(' ');
            dst.setPdfSize(Float.parseFloat((iEnd > 0) ? subs.substring(0, iEnd) : subs));
        } catch (Exception e) {
            throw new JSONException("Cannot extract file size from the Download button title - "+title, e);
        }
        dst.setOrigPdfPath(getUrlFromPath(jBtn.getString("href")));
    }

    private void decodeFieldsSection(@NonNull JSONObject jObj, @NonNull BookModel.Builder dst) throws JSONException {
        if (!jObj.getString("class").equals("fields")) {
            throw new JSONException("The 'div' object containing Year, Npages, ISBN must contains \"class\"=\"fields\" property");
        } else {
            JSONArray jArr = jObj.getJSONArray("p");
            for (int i=0; i<jArr.length(); i++) {
                String field = jArr.getString(i).toLowerCase();
                if (field.startsWith("year:")) {
                    dst.setYear(Integer.valueOf(field.substring("year:".length()).trim()));
                } else if (field.startsWith("pages:")) {
                    dst.setNumPages(Integer.valueOf(field.substring("pages:".length()).trim()));
                } else if (field.startsWith("isbn:")) {
                    String isbns[] = jArr.getString(i).substring("isbn:".length()).trim().split(", ");
                    for (String s : isbns) {
                        if (TextUtils.checkISBN(s)) {
                            try {
                                dst.setIsbn(TextUtils.ensureISBN(s));
                            } catch (ParseException e) {
                                throw new JSONException(e);
                            }
                        } else if (TextUtils.checkAsin(s)) {
                            dst.setAsin(s);
                        }
                    }
                }
            }
        }
    }

    private void decodeAuthors(@NonNull JSONObject jO, @NonNull BookModel.Builder dst) throws JSONException {
        if (!jO.getString("class").equals("author")) {
            throw new JSONException("The Authors sub-section must contains \"class\"=\"author\" property");
        } else {
            Object o = jO.get("a");
            JSONArray jAuth;
            if (o instanceof JSONArray) {
                jAuth = (JSONArray) o;
            } else {
                jAuth = new JSONArray();
                jAuth.put(o);
            }

            List<AuthorModel> authors = new ArrayList<>(jAuth.length());
            dst.setAuthors(authors);
            for (int i=0; i<jAuth.length(); i++) {
                JSONObject jA = jAuth.getJSONObject(i);
                String content = jA.getString("content");
                int spaceIndex = content.indexOf(' ');
                if (spaceIndex > 0) {
                    authors.add(AuthorModel.builder().setName(content.substring(0, spaceIndex)).setFamilyName(content.substring(spaceIndex + 1)).build());
                } else {
                    authors.add(AuthorModel.builder().setFamilyName(content).build());
                }
            }
        }
    }

    /**
     * Tags are optional, so this decoder may throw no exception.
     * @param jO
     * @param dst
     */
    private void decodeTags(@NonNull JSONObject jO, @NonNull BookModel.Builder dst) {
        List<TagModel> tags = new ArrayList<>(8);
        dst.setTags(tags);
        try {
            if (jO.getString("class").equals("tags")) {
                Object o = jO.opt("a");
                JSONArray jArr;
                if (o == null) {
                    return;
                } else if (o instanceof JSONArray) {
                    jArr = (JSONArray) o;
                } else {
                    jArr = new JSONArray();
                    jArr.put(o);
                }

                for (int i=0; i < jArr.length(); i++) {
                    JSONObject jTag = jArr.getJSONObject(i);
                    if (jTag.getString("class").equals("tag") && jTag.has("content")) {
                        tags.add(TagModel.create(null, jTag.getString("content")));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private URL getUrlFromPath(String path) throws JSONException {
        String link = (host.endsWith("/") || path.startsWith("/"))
                ? (host + path)
                : String.format("%s/%s", host, path);
        try {
            return new URL(link);
        } catch (MalformedURLException e) {
            throw new JSONException("Error parse URL - "+link, e);
        }
    }
}
