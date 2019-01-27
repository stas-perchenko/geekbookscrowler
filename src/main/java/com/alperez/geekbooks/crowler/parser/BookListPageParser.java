package com.alperez.geekbooks.crowler.parser;

import com.alperez.geekbooks.crowler.data.parsing.BookRefItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BookListPageParser {
    private final JSONObject contentJson;
    private final String urlHost;

    public BookListPageParser(JSONObject contentJson, String urlHost) {
        this.contentJson = contentJson;
        this.urlHost = urlHost;
    }

    public Collection<BookRefItem> parse() throws JSONException {
        List<BookRefItem> ret = new ArrayList<>();
        evaluateJson(ret);
        return ret;
    }


    private void evaluateJson(List<BookRefItem> dst) throws JSONException {

        Object abstrRow = contentJson.getJSONObject("table").getJSONObject("tbody").get("tr");


        if (abstrRow instanceof JSONArray) {
            JSONArray jRows = (JSONArray) abstrRow;
            for (int i = 0; i < jRows.length(); i++) {
                evaluateTableRow(jRows.getJSONObject(i), dst);
            }// for(i)
        } else if (abstrRow instanceof JSONObject) {
            evaluateTableRow((JSONObject) abstrRow, dst);
        }
    }


    private void evaluateTableRow(JSONObject jRow, List<BookRefItem> dst) {
        JSONArray jRowCells = jRow.getJSONArray("td");
        for (int j=0; j<jRowCells.length(); j++) {
            JSONObject jCell = jRowCells.getJSONObject(j);
            if ("title".equals(jCell.optString("class", ""))) {
                JSONArray jCellPayload = jCell.getJSONArray("div");
                for (int k=0; k<jCellPayload.length(); k++) {
                    JSONObject jObj = jCellPayload.getJSONObject(k);
                    if (!jObj.has("class") && jObj.has("a")) {
                        JSONObject jRef = jObj.getJSONObject("a");
                        if ("title".equals(jRef.optString("class", ""))) {
                            String bookTitle = jRef.getString("content");
                            String bookPath  = jRef.getString("href");
                            try {

                                String link = (urlHost.endsWith("/") || bookPath.startsWith("/"))
                                        ? (urlHost + bookPath)
                                        : String.format("%s/%s", urlHost, bookPath);

                                BookRefItem refModel = new BookRefItem(bookTitle, new URL(link));
                                dst.add(refModel);
                                //TODO Log this
                            } catch (MalformedURLException e) {
                                //TODO Log this
                            }
                        }
                        break; // for(k)
                    }
                } // for(k)
                continue; //for(j)
            }
        }// for(j)
    }
}
