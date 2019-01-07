package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.data.BookCategoryModel;
import com.alperez.geekbooks.crowler.data.BookModel;
import com.alperez.geekbooks.crowler.data.BookRefItem;
import com.alperez.geekbooks.crowler.utils.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.management.modelmbean.XMLParseException;
import java.util.Random;

public class BookItemProcessor implements Runnable {


    public interface BookRefItemProvider {
        @Nullable BookRefItem getBookRefItem();
    }

    public interface OnBookDecodeListener {
        void onBookDecoded(@NonNull BookModel book);
    }

    private final BookRefItemProvider src;
    private final OnBookDecodeListener dst;

    public BookItemProcessor(@NonNull BookRefItemProvider src, @NonNull OnBookDecodeListener dst) {
        this.src = src;
        this.dst = dst;
    }

    @Override
    public void run() {
        BookRefItem ref;
        while((ref = src.getBookRefItem()) != null) {
            BookModel bm = evaluateBookReference(ref);
            if (bm != null) dst.onBookDecoded(bm);
        }
    }

    private Random rnd = new Random();


    public static final String BREADCRUMBS_TAG = "<ul class=\"breadcrumbs\">";
    public static final String CONTENT_TABLE_TAG = "<table class=\"book-info\">";

    @Nullable
    private BookModel evaluateBookReference(BookRefItem ref) {


        try {
            Log.d("START", "------------------------   %s   ------------------", ref);
            String pageHtml = new HtmlPageLoader(ref.getUrl()).load(50000);

            BookCategoryModel category = extractBookCategory(pageHtml);

            JSONObject jBookContent = extractJsonContent(pageHtml);

            Log.d("BOOK_CONT", jBookContent.toString());


            BookModel.Builder bookBuilder = BookModel.builder().setCategory(category);
            decodeBookContent(jBookContent, bookBuilder);
            return bookBuilder.build();
        } catch (Exception e) {
            Log.d("ERR", e.getMessage());
            e.printStackTrace(System.out);
            return null;
        }
    }

    private BookCategoryModel extractBookCategory(String html) throws JSONException {
        int iStart = html.indexOf(BREADCRUMBS_TAG);
        if (iStart < 0) throw new JSONException("Breadcrumbs tag not found");

        String htmlBreadcrumbs = new TagExtractor(html).getTag("ul", iStart);
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


    private JSONObject extractJsonContent(String html) throws XMLParseException {
        int iStart = html.indexOf(CONTENT_TABLE_TAG);
        if (iStart < 0) throw new JSONException("Breadcrumbs tag not found");

        String htmlTable = new TagExtractor(html).getTag("table", iStart);
        if (htmlTable == null) throw new JSONException("Error extract breadcrumbs tag");


        //--- Fix <img> tags closing ---
        StringBuilder sb = new StringBuilder(htmlTable);
        int startSearch = 0, iOpen;
        while ((iOpen = sb.indexOf("<img src=", startSearch)) >= 0) {
            int iClose = sb.indexOf(">", iOpen);
            if (iClose < iOpen) throw new XMLParseException("No close bracket for <img tag. position="+iOpen);

            sb.insert(iClose, '/');
            startSearch = iClose;
        }

        //--- Fix for &nbsp; ---
        htmlTable = sb.toString().replaceAll("&nbsp;", " ");

        Log.d("HTML", "---> %s", htmlTable);

        JSONObject json = org.json.XML.toJSONObject(htmlTable, false);

        JSONObject jTable = json.getJSONObject("table");
        if (!"book-info".equals(jTable.getString("class"))) {
            throw new JSONException("'class' property must have 'book-info' value");
        }

        return json;
    }

    private void decodeBookContent(JSONObject jContent, BookModel.Builder dst) throws JSONException {
        //TODO Implement further !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }
}
