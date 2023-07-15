package com.project.ann

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Timeout
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@RunWith(MockitoJUnitRunner::class)
class MainActivityTest {

    @Mock
    private lateinit var mockApiService: RacingApiService

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Create a mock web server
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Set the base URL for the mock API service
        ServiceBuilder.BASE_URL = mockWebServer.url("/").toString()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testFetchData_Success() {
        // Create a mock response
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(createMockResponseBody())

        // Enqueue the mock response to the mock web server
        mockWebServer.enqueue(mockResponse)

        // Create an instance of the MainActivity
        val activity = MainActivity()

        // Set the mock API service to the MainActivity
        activity.responseNEW = mockApiService

        // Call the fetchData() method
        activity.fetchData()

        // Verify that the API service's getNextRaces() method is called with the correct parameters
        Mockito.verify(mockApiService).getNextRaces("nextraces", 10)

        // Get the request from the mock web server
        val request = mockWebServer.takeRequest()

        // Assert that the request path is correct
        Assert.assertEquals("/nextraces?limit=10", request.path)

        // Create the expected response object
        val expectedResponse = createMockResponseObject()

        // Create a Call object with the expected response
        val call = object : Call<JsonObject> {
            override fun enqueue(callback: Callback<JsonObject>) {
                callback.onResponse(this, Response.success(expectedResponse))
            }

            override fun isExecuted(): Boolean = false

            override fun clone(): Call<JsonObject> = this

            override fun isCanceled(): Boolean = false

            override fun cancel() {}

            override fun execute(): Response<JsonObject> = Response.success(expectedResponse)

            override fun request(): okhttp3.Request? = null
            override fun timeout(): Timeout {
                TODO("Not yet implemented")
            }
        }

        // Mock the API service's createComment() method to return the mocked Call object
        Mockito.`when`(mockApiService.getNextRaces("nextRaces", 10)).thenReturn(call)

        // Verify the behavior of the MainActivity after the API response is received
        // You can add assertions or verify any behavior based on the response here
    }

    private fun createMockResponseBody(): String {
        // Create a sample JSON response body
        return """
            {
                "data": {
                    "next_to_go_ids": ["1", "2", "3"],
                    "race_summaries": {
                        "1": {
                            "race_name": "Race 1",
                            "meeting_name": "Meeting A",
                            "race_number": 1,
                            "advertised_start": 1626325200
                        },
                        "2": {
                            "race_name": "Race 2",
                            "meeting_name": "Meeting B",
                            "race_number": 2,
                            "advertised_start": 1626328800
                        },
                        "3": {
                            "race_name": "Race 3",
                            "meeting_name": "Meeting C",
                            "race_number": 3,
                            "advertised_start": 1626332400
                        }
                    }
                }
            }
        """.trimIndent()
    }

    private fun createMockResponseObject(): JsonObject {
        // Create a sample JsonObject for the response
        val jsonObject = JsonObject()
        val dataObject = JsonObject()
        val nextToGoIdsArray = JsonArray()
        val raceSummariesObject = JsonObject()

        // Add sample data to the JsonObject
        nextToGoIdsArray.add("1")
        nextToGoIdsArray.add("2")
        nextToGoIdsArray.add("3")

        val race1Object = JsonObject()
        race1Object.addProperty("race_name", "Race 1")
        race1Object.addProperty("meeting_name", "Meeting A")
        race1Object.addProperty("race_number", 1)
        race1Object.addProperty("advertised_start", 1626325200)

        val race2Object = JsonObject()
        race2Object.addProperty("race_name", "Race 2")
        race2Object.addProperty("meeting_name", "Meeting B")
        race2Object.addProperty("race_number", 2)
        race2Object.addProperty("advertised_start", 1626328800)

        val race3Object = JsonObject()
        race3Object.addProperty("race_name", "Race 3")
        race3Object.addProperty("meeting_name", "Meeting C")
        race3Object.addProperty("race_number", 3)
        race3Object.addProperty("advertised_start", 1626332400)

        raceSummariesObject.add("1", race1Object)
        raceSummariesObject.add("2", race2Object)
        raceSummariesObject.add("3", race3Object)

        dataObject.add("next_to_go_ids", nextToGoIdsArray)
        dataObject.add("race_summaries", raceSummariesObject)

        jsonObject.add("data", dataObject)

        return jsonObject
    }
}
