package com.priyanshumaurya8868.priyanshu_android_task

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.priyanshumaurya8868.priyanshu_android_task.databinding.ActivityMainBinding
import com.priyanshumaurya8868.priyanshu_android_task.models.Appointed
import com.priyanshumaurya8868.priyanshu_android_task.models.Appointment
import com.priyanshumaurya8868.priyanshu_android_task.util.Constant
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {


    private var morningRVAdapter = RVAdapter()
    private var noonRVAdapter = RVAdapter()
    private var eveRVAdapter = RVAdapter()
    private var nightRVAdapter = RVAdapter()
    private val viewModel: MainViewModel by viewModels()
    private var binding: ActivityMainBinding? = null
    private val REQUEST_CODE_PERMISSION = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.apply {
            initializeRV(
                rvMorning,
                morningRVAdapter,
                5,
                "10:00",
                2,
                Constant.DISABLE,
                Constant.MORNING_RV
            )
            initializeRV(rvAfternoon, noonRVAdapter, 5, "14:00", 2, rv_id = Constant.AFTERNOON_RV)
            initializeRV(rvEvening, eveRVAdapter, 5, "16:00", 2, rv_id = Constant.EVENING_RV)
            initializeRV(rvNight, nightRVAdapter, 5, "20:00", 2, rv_id = Constant.NIGHT_RV)
        }

        binding?.uploadImageBtn?.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                )
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSION
                    )

                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                )
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION
                    )

            } else {

                ImagePicker.with(this).start()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val uri: Uri = data?.data!!

            binding?.tvImgFileName?.text = queryName(contentResolver, uri)

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun queryName(resolver: ContentResolver, uri: Uri): String? {
        val returnCursor: Cursor = resolver.query(uri, null, null, null, null)!!
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    private fun initializeRV(
        rvMorning: RecyclerView,
        rvAdpter: RVAdapter,
        amount: Int,
        myTime: String,
        avalibilityOfHours: Int,
        state: Int = Constant.ENABLE,
        rv_id: Int
    ) {
        rvMorning.apply {
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = rvAdpter.also { rv_adapter ->
                rv_adapter.submitList(
                    appointmentList(timeList(amount, myTime, avalibilityOfHours), state)
                )
                rv_adapter.setOnClickListener({ fixAppointment(it, rv_adapter) }, rv_id)
            }

        }
    }


    private fun appointmentList(
        timeList: List<String>,
        state: Int = Constant.ENABLE
    ): List<Appointment> {
        val list = ArrayList<Appointment>()
        timeList.forEach {
            val appointment = Appointment(
                it,
                state
            )  // override this method  to change state I'm Keeping it  ENABLED  by default
            list.add(appointment)
        }
        return list
    }


    private fun timeList(amount: Int, myTime: String, avalibilityOfHours: Int): List<String> {
        val list = ArrayList<String>()
        val df = SimpleDateFormat("HH:mm")
        val d: Date = df.parse(myTime)
        val temp = (avalibilityOfHours * 60).div(amount)

        for (i in 1..temp) {
            val cal: Calendar = Calendar.getInstance()
            cal.time = d
            cal.add(Calendar.MINUTE, amount)
            val newTime: String = df.format(cal.time)
            list.add(newTime)
        }
        return list
    }

    private fun fixAppointment(appointed: Appointed, adapter: RVAdapter) {
        if (adapter.currentList[appointed.accessed_Index].state != Constant.DISABLE ||
            adapter.currentList[appointed.accessed_Index].state != Constant.BUSY
        ) { //fixing new appointment time
            adapter.currentList[appointed.accessed_Index].state = Constant.APPOINTED
            adapter.notifyItemChanged(appointed.accessed_Index)

            //undo last selected appointed time
            viewModel.previouslyAppointed?.let {
                when (it.rv_id) {
                    Constant.MORNING_RV -> {
                        morningRVAdapter.currentList[it.accessed_Index].state = Constant.ENABLE
                        morningRVAdapter.notifyItemChanged(it.accessed_Index)
                    }
                    Constant.AFTERNOON_RV -> {
                        noonRVAdapter.currentList[it.accessed_Index].state = Constant.ENABLE
                        noonRVAdapter.notifyItemChanged(it.accessed_Index)
                    }
                    Constant.EVENING_RV -> {
                        eveRVAdapter.currentList[it.accessed_Index].state = Constant.ENABLE
                        eveRVAdapter.notifyItemChanged(it.accessed_Index)
                    }
                    Constant.NIGHT_RV -> {
                        nightRVAdapter.currentList[it.accessed_Index].state = Constant.ENABLE
                        nightRVAdapter.notifyItemChanged(it.accessed_Index)
                    }
                }
            }
            viewModel.previouslyAppointed = appointed
        }
    }
}