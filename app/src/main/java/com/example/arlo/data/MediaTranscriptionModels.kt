package com.example.arlo.data

data class AudioStructuredNote(
    val title: String,
    val summary: String,
    val fullTranscript: String,
    val actionItems: List<String>,
    val sentiment: String = "Productive",
    val durationSeconds: Int = 0,
    val recordingType: String = "Live Audio" // "Live Audio", "Call Recording", "Voice Memo"
)

data class VideoTranscriptionResult(
    val videoUrl: String,
    val videoTitle: String,
    val channelOrSource: String,
    val summary: String,
    val fullTranscript: String,
    val keyTakeaways: List<String>,
    val actionItems: List<String>,
    val estimatedDuration: String = "5:00",
    val tags: List<String> = emptyList()
)
