package com.project.ann

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.gson.Gson
import com.project.ann.model.ResponseModel
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


var newList: List<String> = mutableListOf()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    fetchData()
                }
            }
        }
    }

    private fun fetchData() {
        val response = ServiceBuilder.buildService(RacingApiService::class.java)

        response.getNextRaces("nextraces", 5).enqueue(object : Callback<ResponseModel> {
            override fun onResponse(
                call: Call<ResponseModel>, response: Response<ResponseModel>,
            ) {
                Log.d(TAG, "onResponse: " + response.body().toString())
                newList = response.body()?.data?.next_to_go_ids ?: emptyList()

                setContent {
                    GreetingPreview()
                }

                try {
                    val jsonObject = JSONObject(Gson().toJson(response.body()))
                    Log.d(TAG, "onResponse: $jsonObject")

                    val msg = jsonObject.getString("data")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            }

            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Log.d(TAG, "onFailure: ")
            }
        })
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

    LazyColumn {
        items(newList) { item ->
            Text(text = item)
        }
    }
}
