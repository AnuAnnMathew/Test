package com.project.ann

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.project.ann.model.ResponseModel
import com.project.ann.ui.theme.AnnTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnnTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {

                    val newList: List<String> = fetchData()
                    GreetingPreview(newList)
                }
            }
        }
    }

    private fun fetchData(): List<String> {
        var responseList: List<String> = listOf()
        val response = ServiceBuilder.buildService(RacingApiService::class.java)

        response.getNextRaces("nextraces", 5).enqueue(object : Callback<ResponseModel> {
            override fun onResponse(
                call: Call<ResponseModel>, response: Response<ResponseModel>,
            ) {
                Log.d(TAG, "onResponse: " + response.body().toString())
                val res: ResponseModel = response.body()!!
                responseList = res.data.next_to_go_ids
                Log.d(TAG, "onResponse: $res")

            }

            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_LONG).show()
            }

        })

        return responseList
    }
}

//@Preview(showBackground = true)
@Composable
fun GreetingPreview(newList: List<String>) {
    ListExample(items = newList)
}

@Composable
fun ListExample(items: List<String>) {
    LazyColumn {
        items(items) { item ->
            Text(text = item)
        }
    }
}