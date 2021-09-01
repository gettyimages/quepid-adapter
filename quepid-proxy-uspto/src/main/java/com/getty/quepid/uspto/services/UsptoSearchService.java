/*
MIT License
Copyright (c) 2021 Getty Images

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.getty.quepid.uspto.services;

import com.getty.quepid.model.search.AbstractSearchService;
import com.getty.quepid.uspto.model.Grant;
import com.getty.quepid.uspto.model.UsptoSearchRequest;
import com.getty.quepid.uspto.model.UsptoSearchResponse;
import com.getty.quepid.model.solr.*;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Component
public class UsptoSearchService extends AbstractSearchService<UsptoSearchRequest, UsptoSearchResponse> {

    private final Logger LOGGER = LoggerFactory.getLogger(UsptoSearchService.class);

    private final UsptoService service;
    private final Gson gson;

    public UsptoSearchService(Gson gson) {

        final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(loggingInterceptor);

        final OkHttpClient client = httpClient.build();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://developer.uspto.gov/ibd-api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        this.service = retrofit.create(UsptoService.class);
        this.gson = gson;

    }

    @Override
    public Doc getDocument(String patentNumber) throws IOException {

        // This will return a single doc.
        final List<Grant> grants = service.getGrant(patentNumber).execute().body();

        final Doc doc = new Doc(patentNumber);

        // Just make sure we got a doc.
        if(!grants.isEmpty()) {

            // Create a Doc from the result.
            doc.setTitle(grants.get(0).getInventionTitle());
            doc.setDescription(grants.get(0).getDescriptionText());

        } else {

            // No patent exists for this patent number.
            LOGGER.warn("No patent found for patent number {}", patentNumber);

            doc.setTitle("Patent not found");
            doc.setDescription("Patent not found");

        }

        return doc;

    }

    @Override
    public UsptoSearchResponse search(UsptoSearchRequest usptoSearchRequest) throws IOException {

        final String searchText = usptoSearchRequest.getSearchText();
        final int start = usptoSearchRequest.getStart();
        final int rows = usptoSearchRequest.getRows();
        final String largeTextSearchFlag = usptoSearchRequest.getLargeTextSearchFlag();

        final long startTime = System.currentTimeMillis();
        final List<Grant> grants = service.getGrants(searchText, start, rows, largeTextSearchFlag).execute().body();
        final long searchTime = System.currentTimeMillis() - startTime;

        final List<Doc> docs = new LinkedList<>();

        for(final Grant grant : grants) {

            final Doc doc = new Doc(grant.getPatentNumber());
            doc.setTitle(grants.get(0).getInventionTitle());
            doc.setDescription(grants.get(0).getDescriptionText());

            docs.add(doc);

        }

        final Params params = new Params(usptoSearchRequest.getSearchText());
        final ResponseHeader responseHeader = new ResponseHeader(0, searchTime, params);

        final Response response = new Response(docs);
        final SolrResponse solrResponse = new SolrResponse(responseHeader, response);

        final UsptoSearchResponse usptoSearchResponse = new UsptoSearchResponse();
        usptoSearchResponse.setSearchTime(searchTime);
        usptoSearchResponse.setJson(gson.toJson(solrResponse));

        return usptoSearchResponse;

    }

}
