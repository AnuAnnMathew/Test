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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.project.ann.model.Fff0c3eb64db493ce9dc65971714a
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.Locale


var count = 1

const val harness = "9daef0d7-bf3c-4f50-921d-8e818c60fe61"
const val horse = "161d9be2-e909-4326-8c2c-35ed71fb460b"
const val greyhound = "4a2788f8-e825-4d36-9894-efd4baf1cfae"

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    var responseNEW: RacingApiService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val raceList = remember { mutableStateListOf<Fff0c3eb64db493ce9dc65971714a>() }

                    LaunchedEffect(Unit) {
                        while (true) {
                            fetchData(raceList)
                            delay(10000) // Delay for 5 seconds (adjust as needed)
                        }
                    }
                    GreetingPreview(raceList)
                }
            }
        }
    }


    private fun fetchData(raceList: MutableList<Fff0c3eb64db493ce9dc65971714a>) {

        val response = ServiceBuilder.buildService(RacingApiService::class.java)

        response.getNextRaces("nextraces", 10).enqueue(object : Callback<JsonObject> {

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val jsonObject = response.body()
                val data: JsonObject? = jsonObject?.getAsJsonObject("data")
                val nestedArray: JsonArray? = data?.getAsJsonArray("next_to_go_ids")

                if (nestedArray != null) {
                    val raceIdList = mutableListOf<String>()
                    for (jsonElement in nestedArray) {
                        raceIdList.add(jsonElement.asString)
                    }

                    for (raceId in raceIdList) {
                        val raceSummaries: JsonObject? = data.getAsJsonObject("race_summaries")
                        val obj: JsonObject? = raceSummaries?.getAsJsonObject(raceId)

                        if (obj != null) {
                            val gson = Gson()
                            val mMineUserEntity =
                                gson.fromJson(obj, Fff0c3eb64db493ce9dc65971714a::class.java)
                            raceList.add(mMineUserEntity)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "onFailure: ")
            }
        })
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GreetingPreview(raceList: List<Fff0c3eb64db493ce9dc65971714a>) {

    val showDialog = remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<String>() }
    val filteredRaceList = remember { mutableStateListOf<Fff0c3eb64db493ce9dc65971714a>() }

    for (i in raceList) {
        if (!filteredRaceList.contains(i)) {

            if (filteredRaceList.size < 5) {
                filteredRaceList.add(i)
            } else {
                filteredRaceList.add(i)
                filteredRaceList.removeFirst()
            }
        }
    }


    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Next to go Races", fontSize = 24.sp, fontWeight = FontWeight.Light
            )
            MyScreen(
                showDialog = showDialog,
                raceList = raceList,
                onFilterApplied = { filteredList ->

                    filteredRaceList.clear()
//                    filteredRaceList.addAll(filteredList)

                    if (filteredRaceList.size < 5) {
                        for (f in filteredList) {
                            if (!filteredRaceList.contains(f)) {
                                filteredRaceList.add(f)
                            }
                        }
                    }

                    Log.d(TAG, "updated: ${filteredList.size} items")
                    Log.d(TAG, "updated: ${filteredRaceList.size} items")
                    Log.d(TAG, "updated----------------------------------------")
                    Log.d(TAG, "updated----------------------------------------")
                    showDialog.value = false
                })
        }

        Modifier.padding(18.dp)

        if (!filteredRaceList.isEmpty()) {
            LazyColumn(modifier = Modifier.padding(8.dp)) {
                items(filteredRaceList) { item ->
                    ItemDesign(item)
                }
            }
        } else {
            Text(text = "No races available")
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyScreen(
    showDialog: MutableState<Boolean>,
    onFilterApplied: (List<Fff0c3eb64db493ce9dc65971714a>) -> Unit,
    raceList: List<Fff0c3eb64db493ce9dc65971714a>,
) {
    Icon(painter = painterResource(id = R.drawable.baseline_filter_alt_24),
        contentDescription = null,
        modifier = Modifier.clickable {
            showDialog.value = true
        })

    if (showDialog.value) {
        ShowListWithCheckboxesDialog(items = listOf(
            "Horse racing", "Harness racing", "Greyhound racing"
        ), onDismiss = { showDialog.value = false }, onItemsSelected = { selectedItems ->
            val filteredList = mutableListOf<Fff0c3eb64db493ce9dc65971714a>()

            filteredList.clear()
            if ("Horse racing" in selectedItems) {
                for (x in raceList) {
                    if (x.category_id == horse) {
                        filteredList.add(x)
                    }
                }
            }
            if ("Greyhound racing" in selectedItems) {
                for (x in raceList) {
                    if (x.category_id == greyhound) {
                        filteredList.add(x)
                    }
                }
            }
            if ("Harness racing" in selectedItems) {
                for (x in raceList) {
                    if (x.category_id == harness) {
                        filteredList.add(x)
                    }
                }
            }
            if (filteredList.size <= 5)
                onFilterApplied(filteredList)
            else {
                filteredList.removeLast()
                onFilterApplied(filteredList)
            }
        })
    }
}

@Composable
fun ShowListWithCheckboxesDialog(
    items: List<String>,
    onDismiss: () -> Unit,
    onItemsSelected: (List<String>) -> Unit,
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
                    Text(text = item, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }, confirmButton = {
        Button(onClick = {
            onItemsSelected(selectedItems)
            onDismiss()
        }) {
            Text(text = "Apply")
        }
    }, dismissButton = {
        Button(onClick = onDismiss) {
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

    Column {
        val raceName: String? = race.race_name
        val meetingName: String = race.meeting_name
        val raceNumber: String = race.race_number.toString()
        val raceTime: Long = race.advertised_start.seconds
        val remTime: String = getTimeRemaining(raceTime)

        val isRemContainsAgo = remTime.contains("ago")

        if (!isRemContainsAgo) {
            if (!raceName.equals(null) || raceName != "") {
                count++

                Text(
                    text = "#$raceNumber $meetingName",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                val s = getTimeRemaining(race.advertised_start.seconds)

                Text(text = s)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = unixTimeToHuman(raceTime))
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            if (remTime.contains("1")) {
                if (!raceName.equals(null) || raceName != "") {
                    count++

                    Text(
                        text = "#$raceNumber $meetingName",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    val s = getTimeRemaining(race.advertised_start.seconds)

                    Text(text = s)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = unixTimeToHuman(raceTime))
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
