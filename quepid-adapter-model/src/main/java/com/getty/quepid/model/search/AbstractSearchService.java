package com.getty.quepid.model.search;

import com.getty.quepid.model.solr.Doc;

import java.io.IOException;

public abstract class AbstractSearchService<T extends AbstractSearchRequest, V extends AbstractSearchResponse> {

    public abstract V search(T genericSearchRequest) throws IOException;

    public abstract Doc getDocument(String id) throws Exception;

}
