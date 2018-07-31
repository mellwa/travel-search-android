package edu.usc.cs.travelsearch.result;

import java.util.ArrayList;
import java.util.List;

public class SearchResultList {

    List<SearchResult> results = new ArrayList<>();
    String nextPageToken;

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public void addResult(SearchResult searchResult) {
        results.add(searchResult);
    }

    public void removeResult(String placeId) {
        for(SearchResult searchResult : results) {
            if(searchResult.getId().equals(placeId)) {
                this.results.remove(searchResult);
            }
        }
    }

    public List<SearchResult> getResults() {
        return this.results;
    }
}
