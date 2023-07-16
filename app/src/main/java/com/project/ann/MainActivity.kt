package com.project.ann

import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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

const val harness = "9daef0d7-bf3c-4f50-921d-8e818c60fe61"
const val horse = "161d9be2-e909-4326-8c2c-35ed71fb460b"
const val greyhound = "4a2788f8-e825-4d36-9894-efd4baf1cfae"

class MainActivity : ComponentActivity() {
    var responseNEW: RacingApiService? = null

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

    fun fetchData() {

        val response = ServiceBuilder.buildService(RacingApiService::class.java)
        responseNEW = response

        response.getNextRaces("nextraces", 10).enqueue(object : Callback<JsonObject> {
            @RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Next to go Races", fontSize = 24.sp, fontWeight = FontWeight.Light
            )
            MyScreen()
        }

        Modifier.padding(18.dp)

        LazyListView(raceSum)
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LazyListView(raceSum: MutableList<Fff0c3eb64db493ce9dc65971714a>) {

    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(raceSum) { item ->
            ItemDesign(item)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyScreen() {
    val showDialog = remember { mutableStateOf(false) }

    Icon(painter = painterResource(id = R.drawable.baseline_filter_alt_24),
        contentDescription = null,
        modifier = Modifier.clickable {
            showDialog.value = true
        })

    if (showDialog.value) {
        ShowListWithCheckboxesDialog(items = listOf("Horse racing", "Harness racing","Greyhound racing"),
            onDismiss = { showDialog.value = false },
            onItemsSelected = {
                // Handle selected items
                showDialog.value = false
            })
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShowListWithCheckboxesDialog(
    items: List<String>, onDismiss: () -> Unit, onItemsSelected: (List<String>) -> Unit,
) {
    val selectedItems = remember { mutableStateListOf<String>() }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(text = "Select Items") }, text = {
        Column {
            items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = selectedItems.contains(item),
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                selectedItems.add(item)
                            } else {
                                selectedItems.remove(item)
                            }
                        })
                    Text(
                        text = item, modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }, confirmButton = {
        Button(onClick = {
            // Retrieve selected items and call onItemsSelected callback


            if (selectedItems.size == 1) {

                if (selectedItems[0] == "Horse") {

                    for (x in raceSum) {
                        val newList = mutableListOf<Fff0c3eb64db493ce9dc65971714a>()

                        if (x.category_id == "") {

                        } else {

                        }

                    }



                    Log.d(TAG, "ShowListWithCheckboxesDialog: Horse Selected")
                } else {

                    Log.d(TAG, "ShowListWithCheckboxesDialog: other Selected")
                }

            }


//


            onDismiss()
        }) {
            Text(text = "Apply")
        }
    }, dismissButton = {
        Button(
            onClick = onDismiss
        ) {
            Text(text = "Cancel")
        }
    })
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

    var count = 0

    Column {
        val raceName: String? = race.race_name
        val meetingName: String = race.meeting_name
        val raceNumber: String = race.race_number.toString()
        val raceTime: Long = race.advertised_start.seconds
        val remTime: String = getTimeRemaining(raceTime)

        val isRemContainsAgo = remTime.contains("ago")
        //if 9daef0d7-bf3c-4f50-921d-8e818c60fe61 --> race hound
        //else horse

        if (count <= 5) {
            if (!isRemContainsAgo) {

                if (!raceName.equals(null) || raceName != "") {

                    Text(
                        text = "#$raceNumber $meetingName",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    val s = getTimeRemaining(race.advertised_start.seconds)

                    count++
                    Text(text = s)
                    Spacer(modifier = Modifier.height(4.dp))
//                Text(text = "Meeting Name: $meetingName")


                    Text(text = unixTimeToHuman(raceTime))

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

    }
}
