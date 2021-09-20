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

package com.getty.quepid.controllers;

import com.getty.quepid.uspto.model.UsptoSearchRequest;
import com.getty.quepid.uspto.model.UsptoSearchResponse;
import com.getty.quepid.uspto.services.UsptoSearchService;
import com.getty.quepid.model.solr.*;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CrossOrigin(origins = "*")
@RestController
public class QuepidSearchAdapter {

    private final Logger LOGGER = LoggerFactory.getLogger(QuepidSearchAdapter.class);

    @Autowired
    private Gson gson;

    @Autowired
    private UsptoSearchService searchService;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ResponseEntity<String> search(@RequestParam(value = "q") String query,
                                         @RequestParam(value = "json.wrf", defaultValue = "", required = false) String jsonWrf) throws Exception {

        final HttpHeaders httpResponseHeaders = getSolrHeaders(jsonWrf);

        if(StringUtils.equalsIgnoreCase(query, "*:*")) {

            // This is Quepid doing the initial ping.
            // We'll return an empty search list as a Solr response.

            final List<Doc> docs = Collections.emptyList();
            final UsptoSearchResponse searchResponse = formSearchResponse(query, docs, 1);

            return ResponseEntity.ok()
                    .headers(httpResponseHeaders)
                    .body(toTsonWrf(jsonWrf, searchResponse.getJson()));

        } else if(query.startsWith("id:(")) {

            // This is Quepid doing a snapshot comparison.
            // Quepid needs to look up specific documents by ID.
            // We need to parse the document IDs from the query so we can retrieve them.
            // q=id:(12697 OR 18645 OR 26965 OR 71714 OR 81899)

            final Pattern pattern = Pattern.compile("[0-9]+");
            final Matcher matcher = pattern.matcher(query);

            final List<String> documentIds = new LinkedList<>();

            while (matcher.find()) {
                documentIds.add(matcher.group());
            }

            final List<Doc> docs = new LinkedList<>();

            // Do a search for those documentIds.
            // TODO: Change this to a batch search if possible.

            final long startTime = System.currentTimeMillis();

            for (final String documentId : documentIds) {

                final Doc doc = searchService.getDocument(documentId);

                if (doc != null) {
                    docs.add(doc);
                }

            }

            // This isn't a fair representation of time because it's over multiple queries
            // but it is better than nothing.
            final long elapsedTime = System.currentTimeMillis() - startTime;

            final UsptoSearchResponse searchResponse = formSearchResponse(query, docs, elapsedTime);

            return ResponseEntity.ok()
                    .headers(httpResponseHeaders)
                    .body(toTsonWrf(jsonWrf, searchResponse.getJson()));

        } else if(query.startsWith("id:")) {

            // Quepid is wanting to show the details of a single document.
            final String documentId = query.replace("id:", "");

            final long startTime = System.currentTimeMillis();
            final Doc doc = searchService.getDocument(documentId);
            final long elapsedTime = System.currentTimeMillis() - startTime;

            // Returning a single document.
            final UsptoSearchResponse searchResponse = formSearchResponse(query, Arrays.asList(doc), elapsedTime);

            return ResponseEntity.ok()
                    .headers(httpResponseHeaders)
                    .body(toTsonWrf(jsonWrf, searchResponse.getJson()));

        } else {

            // This is Quepid doing a search.

            LOGGER.info("Received search for: " + query);

            final UsptoSearchRequest searchRequest = new UsptoSearchRequest(query);
            final UsptoSearchResponse searchResponse = searchService.search(searchRequest);

            return ResponseEntity.ok()
                    .headers(httpResponseHeaders)
                    .body(toTsonWrf(jsonWrf, searchResponse.getJson()));

        }

    }

    /**
     * Wraps the json response if necessary.
     * @param jsonWrf The jsonwrf parameter value.
     * @param json The response json.
     * @return The formatted json.
     */
    private String toTsonWrf(final String jsonWrf, final String json) {

        if(StringUtils.isEmpty(jsonWrf)) {

            return json;

        } else {

            return jsonWrf + "(" + json + ")";

        }

    }

    /**
     * Creates the Solr search response.
     * @param query The query.
     * @param docs The list of {@link Doc docs}.
     * @param qtime The time required for query execution.
     * @return The search response.
     */
    private UsptoSearchResponse formSearchResponse(final String query, final List<Doc> docs, final long qtime) {

        final Params params = new Params(query);
        final ResponseHeader responseHeader = new ResponseHeader(0, qtime, params);
        final Response response = new Response(docs);
        final SolrResponse solrResponse = new SolrResponse(responseHeader, response);

        final UsptoSearchResponse searchResponse = new UsptoSearchResponse();
        searchResponse.setJson(gson.toJson(solrResponse));

        return searchResponse;

    }

    /**
     * Builds the HTTP response headers Solr would provide.
     * @param jsonWrf The JSON-WRF string or <code>null</code>.
     * @return The Solr {@link HttpHeaders} for the response.
     */
    private HttpHeaders getSolrHeaders(final String jsonWrf) {

        final HttpHeaders solrResponseHeaders = new HttpHeaders();

        // These are Solr headers.
        solrResponseHeaders.set("Access-Control-Allow-Origin", "*");
        solrResponseHeaders.set("Content-Security-Policy", "default-src 'none'; base-uri 'none'; connect-src 'self'; form-action 'self'; font-src 'self'; frame-ancestors 'none'; img-src 'self'; media-src 'self'; style-src 'self' 'unsafe-inline'; script-src 'self'; worker-src 'self';");
        solrResponseHeaders.set("X-Content-Type-Options", "nosniff");
        solrResponseHeaders.set("X-XSS-Protection", "1; mode=block");
        solrResponseHeaders.set("X-Frame-Options", "SAMEORIGIN");

        if (StringUtils.isEmpty(jsonWrf)) {
            solrResponseHeaders.set("Content-Type", "application/json;charset=utf-8");
        } else {
            solrResponseHeaders.set("Content-Type", "text/javascript");
        }

        return solrResponseHeaders;

    }

}
