package com.example.iti.ui.alert.view

import SwipeToDeleteCallback
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iti.R
import com.example.iti.databinding.ActivityAlertBinding
import com.example.iti.db.local.alert.AlertDataSourceImpl
import com.example.iti.db.room.AppDatabase
import com.example.iti.model.AlarmEntity
import com.example.iti.ui.alert.viewModel.AlertViewModel
import com.example.iti.ui.alert.viewModel.AlertViewModelFactory
import java.util.Calendar

class AlertActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlertBinding
    private var selectedTimeInMillis: Long = 0
    private lateinit var alarmViewModel: AlertViewModel
    private lateinit var alarmAdapter: AlertAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        alarmViewModel = ViewModelProvider(
            this,
            AlertViewModelFactory(
                AlertDataSourceImpl(
                    this,
                    AppDatabase.getDatabase(this).alarmDao(),
                    getSystemService(Context.ALARM_SERVICE) as AlarmManager
                )
            )
        )[AlertViewModel::class.java]

        setUpRecyclerView()

        // Load alarms when activity starts
        alarmViewModel.loadAlarms()

        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnAddAlert.setOnClickListener {
            showDatePicker()
        }
    }

    private fun setUpRecyclerView() {
        alarmAdapter = AlertAdapter()
        binding.rvAlerts.layoutManager = LinearLayoutManager(this)
        binding.rvAlerts.adapter = alarmAdapter

        alarmViewModel.alarms.observe(this) { alarmList ->
            if (alarmList.isEmpty()) {
                binding.tvNoItems.visibility = View.VISIBLE
                binding.imcNoSaved.visibility = View.VISIBLE
                binding.rvAlerts.visibility = View.GONE
            } else {
                binding.tvNoItems.visibility = View.GONE
                binding.imcNoSaved.visibility = View.GONE
                binding.rvAlerts.visibility = View.VISIBLE
                alarmAdapter.submitList(alarmList)
            }
        }

        // Set up swipe-to-delete
        val swipeToDeleteCallback = SwipeToDeleteCallback(
            onSwipedAction = { position ->
                val alarm = alarmAdapter.currentList[position]
                showDeleteConfirmationDialog(alarm, position)
            },
            iconResId = R.drawable.ic_delete // Use the delete icon
        )

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvAlerts)
    }

    private fun showDeleteConfirmationDialog(alarm: AlarmEntity, position: Int) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_alert))
            .setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_alert))
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                alarmViewModel.deleteAlarm(alarm)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                alarmAdapter.notifyItemChanged(position) // Revert swipe if the user cancels
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val buttonOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val buttonCancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            buttonOk.setTextColor(resources.getColor(R.color.delete_color, null))
            buttonCancel.setTextColor(resources.getColor(R.color.buttons_, null))
        }
        dialog.show()
    }


    private fun showDatePicker() {

        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this, R.style.CustomDatePickerDialog,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                showTimePicker(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun showTimePicker(calendar: Calendar) {
        val timePickerDialog = TimePickerDialog(
            this, R.style.CustomTimePickerDialog,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                selectedTimeInMillis = calendar.timeInMillis
                checkOverlayPermission()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.show()
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivityForResult(intent, 2000)
            } else {
                setAlarm(selectedTimeInMillis)
            }
        } else {
            setAlarm(selectedTimeInMillis)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1000 -> {
                if (Settings.canDrawOverlays(this)) {
                    checkExactAlarmPermission()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.overlay_permission_is_required_to_display_the_alarm_screen),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            2000 -> {
                val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (alarmManager.canScheduleExactAlarms()) {
                    setAlarm(selectedTimeInMillis)
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.exact_alarm_permission_is_required_to_set_alarms),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + this.packageName)
                )
                startActivityForResult(intent, 1000)
            } else {
                checkExactAlarmPermission()
            }
        } else {
            checkExactAlarmPermission()
        }
    }

    private fun setAlarm(timeInMillis: Long) {
        alarmViewModel.setAlarm(timeInMillis)
        Toast.makeText(this, getString(R.string.alarm_set), Toast.LENGTH_SHORT).show()
        alarmViewModel.loadAlarms()
    }

}