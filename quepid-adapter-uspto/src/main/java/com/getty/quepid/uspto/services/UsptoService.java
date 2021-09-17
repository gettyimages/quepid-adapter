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

import com.getty.quepid.uspto.model.Grant;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import java.util.List;

public interface UsptoService {

    // Subset implementation of https://developer.uspto.gov/ibd-api/#!/bulkdata/searchPublicationData
    @Headers({
            "Accept: application/json"
    })
    @GET("v1/application/grants")
    Call<List<Grant>> getGrants(@Query("searchText") String searchText,
                                @Query("start") int start,
                                @Query("rows") int rows,
                                @Query("largeTextSearchFlag") String largeTextSearchFlag);
    // https://developer.uspto.gov/ibd-api/v1/application/grants?searchText=dog&start=0&rows=100&largeTextSearchFlag=N

    @Headers({
            "Accept: application/json"
    })
    @GET("v1/application/grants")
    Call<List<Grant>> getGrant(@Query("patentNumber") String patentNumber);
    // https://developer.uspto.gov/ibd-api/v1/application/grants?patentNumber=08088540&start=0&rows=100&largeTextSearchFlag=N

}