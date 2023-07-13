package com.project.ann

import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.project.ann.model.Fff0c3eb64db493ce9dc65971714a
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.Locale


var newList: MutableList<String> = mutableListOf()
var raceSum: MutableList<Fff0c3eb64db493ce9dc65971714a> = mutableListOf()

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

        response.getNextRaces("nextraces", 10).enqueue(object : Callback<JsonObject> {
            override fun onResponse(
                call: Call<JsonObject>, response: Response<JsonObject>,
            ) {

                Log.d(TAG, "onResponse: " + response.body().toString())

                val jsonObject = response.body()
                val data: JsonObject? = jsonObject?.getAsJsonObject("data")
                val nestedArray: JsonArray? = data?.getAsJsonArray("next_to_go_ids")

                if (nestedArray != null) {
                    newList = mutableListOf()
                    for (jsonElement in nestedArray) {
                        newList.add(jsonElement.asString)
                    }
                }

                raceSum = mutableListOf()

                for (race_id in newList) {
                    val raceSummaries: JsonObject? = data?.getAsJsonObject("race_summaries")
                    val obj: JsonObject? = raceSummaries?.getAsJsonObject(race_id)

                    if (obj != null) {
                        val gson = Gson()
                        val mMineUserEntity =
                            gson.fromJson(obj, Fff0c3eb64db493ce9dc65971714a::class.java)
                        raceSum.add(mMineUserEntity)
                    }

                }

                setContent {
                    GreetingPreview()
                }

            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "onFailure: ")
            }
        })
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

    Column(modifier = Modifier.padding(16.dp)) {

        Text(text = "Next to go Races", fontSize = 24.sp, fontWeight = FontWeight.Light)

        Modifier.padding(18.dp)


        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(raceSum) { item ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ItemDesign(item)
                }
            }
        }
    }


}

fun unixTimeToHuman(unixTime: Long): String {
    val date = java.util.Date(unixTime * 1000)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(date)
}

@RequiresApi(Build.VERSION_CODES.O)
fun getTimeRemaining(unixTime: Long): String {
    val currentTime = Instant.now().epochSecond
    val remainingSeconds = unixTime - currentTime

    if (remainingSeconds <= 0) {
        val elapsedSeconds = -remainingSeconds
        val elapsedMinutes = elapsedSeconds / 60
        return "$elapsedMinutes minutes ago."
    }

    val duration = Duration.ofSeconds(remainingSeconds)
    val hours: Int = (duration.toHours() % 24).toInt()
    val minutes: Int = (duration.toMinutes() % 60).toInt()

    return if (hours == 0) {
        String.format("Time left: %d min", minutes)
    } else {
        String.format("Time left: %d hrs, %d min", hours, minutes)
    }

}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ItemDesign(race: Fff0c3eb64db493ce9dc65971714a) {

    Column {

        val raceName: String? = race.race_name
        val meetingName: String = race.meeting_name
        val raceNumber: String = race.race_number.toString()
        val raceTime: Long = race.advertised_start.seconds
        val remTime: String = getTimeRemaining(raceTime)

        val isRemContainsAgo = remTime.contains("ago")



        if (!isRemContainsAgo) {

            if (!raceName.equals(null) || raceName != "") {

                Text(
                    text = "#$raceNumber $raceName!!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                val s = getTimeRemaining(race.advertised_start.seconds)

                Text(text = s)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Meeting Name: $meetingName")


                Text(text = unixTimeToHuman(raceTime))

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

    }
}
