package com.alperez.geekbooks.crowler.parser;

import com.alperez.geekbooks.crowler.data.CategoryItem;
import com.alperez.geekbooks.crowler.utils.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CategoryIndexParser {

    private final JSONObject contentJson;
    private final String urlHost;

    public CategoryIndexParser(JSONObject contentJson, String urlHost) {
        this.contentJson = contentJson;
        this.urlHost = urlHost;
    }

    public Collection<CategoryItem> parse() {
        Set<CategoryItem> result = new HashSet<>();
        evaluateObject(contentJson, result);
        return result;
    }


    private void evaluateObject(JSONObject json, Collection<CategoryItem> dst) {

        if (testForCategoryItem(json)) {
            try {
                CategoryItem cItem = parseToCategoryItem(json);
                Log.d(Thread.currentThread().getName(), "---> New category item decoded: %s", cItem);
                dst.add(cItem);
            } catch (JSONException e) {
                Log.d(Thread.currentThread().getName(), "<~~~ JSON object was detected as a Category Item, but decoding failed - "+e.getMessage());
                e.printStackTrace();
            }
        } else if (json.has("div")) {
            Object child = json.get("div");
            if (child instanceof JSONObject) {
                evaluateObject((JSONObject) child, dst);
            } else if (child instanceof JSONArray) {
                JSONArray children = (JSONArray) child;
                for (int i=0; i<children.length(); i++) {
                    Object item = children.get(i);
                    if (item instanceof JSONObject) {
                        evaluateObject((JSONObject) item, dst);
                    } else {
                        Log.d(Thread.currentThread().getName(), "<~~~ JSON tree error - child JSON array contains not an object: "+item);
                    }
                }
            } else {
                Log.d(Thread.currentThread().getName(), "<~~~ JSON tree error - 'div' child has wrong format: "+child);
            }
        } else {
            Log.d(Thread.currentThread().getName(), "<~~~ JSON tree error - not 'div' child");
        }
    }

    private boolean testForCategoryItem(JSONObject json) {
        if (!"group".equalsIgnoreCase(json.optString("class"))) return false;
        Object oA = json.opt("a");
        if ((oA == null) || !(oA instanceof JSONObject)) return false;
        Object oS = json.opt("span");
        if ((oS == null) || !(oS instanceof JSONObject)) return false;
        return true;
    }

    private CategoryItem parseToCategoryItem(JSONObject json) throws JSONException {

        if (!"group".equals(json.getString("class"))) throw new JSONException("'class' property must be 'group'");
        JSONObject jA = json.getJSONObject("a");
        JSONObject jSpan = json.getJSONObject("span");
        if (!"count".equals(jSpan.getString("class"))) throw new JSONException("'span' property must have 'clas' attribute equals to 'count'");

        URL u = null;
        try {
            u = new URL(urlHost + jA.getString("href"));
        } catch (MalformedURLException e) {
            throw new JSONException("Wrong URL for Category Item - "+json.toString(), e);
        }
        String title = jA.getString("content");
        int count = jSpan.getInt("content");
        return new CategoryItem(title, u, count);
    }
}
