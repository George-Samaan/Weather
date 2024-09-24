package com.example.iti.ui.favourites.view

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iti.R
import com.example.iti.databinding.ActivityFavouritesBinding
import com.example.iti.db.local.LocalDataSourceImpl
import com.example.iti.db.remote.RemoteDataSourceImpl
import com.example.iti.db.repository.RepositoryImpl
import com.example.iti.db.room.AppDatabase
import com.example.iti.db.sharedPrefrences.SettingsDataSourceImpl
import com.example.iti.model.WeatherEntity
import com.example.iti.network.ApiClient
import com.example.iti.ui.favourites.viewModel.FavouritesAdapter
import com.example.iti.ui.favourites.viewModel.FavouritesViewModel
import com.example.iti.ui.favourites.viewModel.FavouritesViewModelFactory
import com.example.iti.ui.googleMaps.GoogleMapsActivity
import com.example.iti.ui.homeScreen.view.HomeScreenActivity
import com.example.iti.ui.settings.viewModel.SettingsViewModel
import com.example.iti.ui.settings.viewModel.SettingsViewModelFactory
import kotlinx.coroutines.launch

class FavouritesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavouritesBinding
    private val favouritesViewModel: FavouritesViewModel by viewModels {
        FavouritesViewModelFactory(
            RepositoryImpl(
                remoteDataSource = RemoteDataSourceImpl(apiService = ApiClient.retrofit),
                settingsDataSource = SettingsDataSourceImpl(
                    this.getSharedPreferences("AppSettingPrefs", MODE_PRIVATE)
                ),
                localDataSource = LocalDataSourceImpl(AppDatabase.getDatabase(this).weatherDao())
            )
        )
    }
    private lateinit var favouritesAdapter: FavouritesAdapter

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            RepositoryImpl(
                remoteDataSource = RemoteDataSourceImpl(apiService = ApiClient.retrofit),
                settingsDataSource = SettingsDataSourceImpl(
                    this.getSharedPreferences("AppSettingPrefs", MODE_PRIVATE)
                ),
                localDataSource = LocalDataSourceImpl(AppDatabase.getDatabase(this).weatherDao())
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavouritesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpRecyclerViews()
        setUpObservers()
        onButtonClicks()
    }

    private fun setUpRecyclerViews() {
        favouritesAdapter = FavouritesAdapter(settingsViewModel, lifecycleScope) { cityName ->
            val intent = Intent(this, HomeScreenActivity::class.java)
            intent.putExtra("CITY_KEY", cityName)
            startActivity(intent)
        }

        binding.rvFavs.layoutManager = LinearLayoutManager(this)
        binding.rvFavs.adapter = favouritesAdapter

        val itemTouchHelperCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            private val deleteIcon =
                ContextCompat.getDrawable(this@FavouritesActivity, R.drawable.ic_delete)
            private val intrinsicWidth = deleteIcon?.intrinsicWidth ?: 0
            private val intrinsicHeight = deleteIcon?.intrinsicHeight ?: 0
            private val backgroundPaint = Paint().apply {
                color = Color.parseColor("#8AFF0000") // Red color
                style = Paint.Style.FILL
            }
            private val cornerRadius = 28f // Match the corner radius of the CardView

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val weatherEntity = favouritesAdapter.currentList[position]
                showDeleteConfirmationDialog(weatherEntity, position)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                val cardMarginHorizontal = 16 // The horizontal margin for the CardView
                val cardPaddingVertical = 50
                val isCancelled = dX == 0f && !isCurrentlyActive

                if (isCancelled) {
                    clearCanvas(
                        c,
                        itemView.right + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat()
                    )
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    return
                }

                // Adjust background to fit the CardView
                val left = itemView.right + dX.toInt() + cardMarginHorizontal
                val right = itemView.right - cardMarginHorizontal
                val top = itemView.top + cardPaddingVertical
                val bottom = itemView.bottom - itemView.paddingBottom
                val rectF = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

                // Draw red background with corner radius
                c.drawRoundRect(rectF, cornerRadius, cornerRadius, backgroundPaint)

                // Draw the delete icon
                val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
                val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
                val deleteIconRight = itemView.right - deleteIconMargin
                val deleteIconBottom = deleteIconTop + intrinsicHeight

                deleteIcon?.setBounds(
                    deleteIconLeft,
                    deleteIconTop,
                    deleteIconRight,
                    deleteIconBottom
                )
                deleteIcon?.draw(c)

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            private fun clearCanvas(
                c: Canvas?,
                left: Float,
                top: Float,
                right: Float,
                bottom: Float
            ) {
                c?.drawRect(left, top, right, bottom, Paint())
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvFavs)
    }

    private fun showDeleteConfirmationDialog(weatherEntity: WeatherEntity, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Location")
            .setMessage("Are you sure you want to delete this location?")
            .setPositiveButton("Yes") { dialog, _ ->
                favouritesViewModel.deleteWeatherData(weatherEntity)
//                favouritesAdapter.notifyItemRemoved(position)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                favouritesAdapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun setUpObservers() {
        lifecycleScope.launch {
            favouritesViewModel.allWeatherData.collect {
                favouritesAdapter.submitList(it)
            }
        }
    }

    private fun onButtonClicks() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnMaps.setOnClickListener {
            startActivity(Intent(this, GoogleMapsActivity::class.java))
        }
    }
}