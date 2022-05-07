package com.thecattest.accents.Data;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET(Dictionary.SYNC_URL)
    Call<Dictionary> getDictionary();
}
