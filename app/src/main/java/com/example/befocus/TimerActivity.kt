package com.example.befocus

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class TimerActivity: AppCompatActivity() {
    private lateinit var progress_Bar: ProgressBar
    private lateinit var edtTaskName: TextView
    private lateinit var txtTimeRemaining: TextView
    private lateinit var btnPlayResume: ImageView
    private lateinit var btnStop: ImageView

    private lateinit var sendTaskName: String
    private lateinit var sendTaskMinutes: String
    private lateinit var sendTaskSeconds: String

    private var countdownTimer: CountDownTimer? = null
    private var isRunning = false
    private var totalMiliSeconds: Long = 0
    private var timeLeftMiliSeconds: Long = 0

    private var mediaPlayer: MediaPlayer? = null

    val CHANNEL_ID = "BE_FOCUS_CHANNEL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_timer)

        // change color action bar
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffdecc")))
        supportActionBar?.elevation = 0f
        supportActionBar?.title = "" // Set to your app's name

        // declare variables
        edtTaskName = findViewById(R.id.txtTaskName)
        txtTimeRemaining = findViewById(R.id.txtTimeRemaining)
        btnPlayResume = findViewById(R.id.btnPlayResume)
        btnStop = findViewById(R.id.btnStop)
        progress_Bar = findViewById(R.id.barProgress)

        // notification
        createNotificationChannel()

        // receive
        val bundle: Bundle? = intent.extras
        val taskName_msg = bundle!!.getString("taskName")
        val taskMinutes_msg = bundle!!.getString("taskMinutes")
        val taskSeconds_msg = bundle!!.getString("taskSeconds")
        val taskNotification_msg = bundle!!.getString("taskNotification")

        sendTaskName = taskName_msg.toString()
        sendTaskMinutes = taskMinutes_msg.toString()
        sendTaskSeconds = taskSeconds_msg.toString()

        // edit layout
        edtTaskName.text = taskName_msg

        val taskLength_msg = sendTaskMinutes.toInt()*60 + sendTaskSeconds.toInt()

        if (taskNotification_msg == null){
            txtTimeRemaining.text =  "${formatTime(taskLength_msg.toLong())}"
        }
        else {
            txtTimeRemaining.text = taskNotification_msg.toString()
            var dialog_notification = PopupFragment()
            val args = Bundle().apply {
                putString("arg_message", "Congrats! Focus time for $sendTaskName is done.")
            }
            dialog_notification.arguments = args
            dialog_notification.show(supportFragmentManager,"Notification")
        }

        totalMiliSeconds = taskLength_msg.toString().toLong()*1000
        timeLeftMiliSeconds = totalMiliSeconds
        progress_Bar.max = (totalMiliSeconds/1000).toInt()
        progress_Bar.progress = (totalMiliSeconds/1000).toInt()

        // actions
        btnPlayResume.setOnClickListener{
            if (isRunning == false){
                startCountdown()
                playSound()
            }
            else {
                pauseCountdown()
            }
        }

        btnStop.setOnClickListener{
            stopCountdown()
        }
    }
    private fun startCountdown() {
        // Create a CountDownTimer that updates the progress every second
        countdownTimer = object : CountDownTimer(timeLeftMiliSeconds, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeftMiliSeconds = millisUntilFinished
                // Calculate the progress as a percentage of the total time
                val progress = (timeLeftMiliSeconds.toFloat() / 1000).toInt()
                // Update the progress bar
                progress_Bar.progress = progress

                // Update the TextView with the remaining time
                val secondsRemaining = millisUntilFinished / 1000
                txtTimeRemaining.text = "${formatTime(secondsRemaining)}"
            }

            override fun onFinish() {
                mediaPlayer?.reset()
                // Set the progress to 0 when the countdown is finished
                progress_Bar.progress = (totalMiliSeconds/1000).toInt()
                // Update the TextView to show that time is up
                txtTimeRemaining.text = "Time's up!"
                isRunning = false
                btnPlayResume.setBackgroundResource(R.drawable.play_icon)
                timeLeftMiliSeconds = totalMiliSeconds
                showNotification("Congrats!", "Focus time for $sendTaskName is done.")
                var dialog_notification = PopupFragment()
                val args = Bundle().apply {
                    putString("arg_message", "Congrats! Focus time for $sendTaskName is done.")
                }
                dialog_notification.arguments = args
                dialog_notification.show(supportFragmentManager,"Notification")
            }
        }.start()
        isRunning = true
        btnPlayResume.setBackgroundResource(R.drawable.pause_icon)
    }

    private fun pauseCountdown() {
        mediaPlayer?.reset()
        countdownTimer?.cancel()
        isRunning = false
        btnPlayResume.setBackgroundResource(R.drawable.play_icon)
    }

    private fun stopCountdown() {
        mediaPlayer?.reset()
        countdownTimer?.cancel()  // Cancel the countdown timer if it's running

        isRunning = false
        timeLeftMiliSeconds = totalMiliSeconds
        progress_Bar.progress = (totalMiliSeconds/1000).toInt()
        txtTimeRemaining.text = "${formatTime((totalMiliSeconds/1000))}"
        btnPlayResume.setBackgroundResource(R.drawable.play_icon)
    }

    fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.app_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuEdit -> {
                stopCountdown()
                val intent = Intent(this, EditTaskActivity::class.java)
                intent.putExtra("currentTaskName", sendTaskName)
                intent.putExtra("currentTaskMinutes", sendTaskMinutes)
                intent.putExtra("currentTaskSeconds", sendTaskSeconds)
                startActivity(intent)
                return true
            }
            R.id.menuHome -> {
                stopCountdown()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun playSound() {
        val soundUrl = Uri.parse("android.resource://com.example.befocus/" + R.raw.tick_timer)
        mediaPlayer = MediaPlayer.create(this,soundUrl)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }


    // notification
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "BeFocus", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Channel for BeFocus notifications"
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String) {
        // Create an Intent to open the MainActivity
        val intent = Intent(this, TimerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra("taskName", sendTaskName)
        intent.putExtra("taskMinutes", sendTaskMinutes)
        intent.putExtra("taskSeconds", sendTaskSeconds)
        intent.putExtra("taskNotification","Time's up!")

        // Create a PendingIntent
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.timer_img)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(1, notificationBuilder.build())
    }
}

