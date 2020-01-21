package query;

import org.openstreetmap.gui.jmapviewer.Layer;

import java.util.*;

public class QueryManager implements Iterable<Query>{
    private List<Query> queries;

    public QueryManager() {
        this.queries = new ArrayList<>();
    }

    public void addQuery(Query query) {
        queries.add(query);
    }

    public void removeQuery(Query query) {
        queries.remove(query);
    }

    public Set<String> getQueryTerms() {
        Set<String> ans = new HashSet<>();
        for (Query q : queries) {
            ans.addAll(q.getFilter().terms());
        }
        return ans;
    }

    public Set<Layer> getVisibleLayers() {
        Set<Layer> ans = new HashSet<>();
        for (Query q : queries) {
            if (q.getVisible()) {
                ans.add(q.getLayer());
            }
        }
        return ans;
    }

    @Override
    public Iterator<Query> iterator() {
        return queries.iterator();
    }
}
