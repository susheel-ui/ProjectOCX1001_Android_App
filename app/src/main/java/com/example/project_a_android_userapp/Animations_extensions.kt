package com.example.project_a_android_userapp

import android.view.View

object Animations_extensions{
    fun View.popClick(
        scaleDown: Float = 0.96f,
        duration: Long = 80,
        onClick: () -> Unit
    ) {
        this.setOnClickListener {
            this.animate()
                .scaleX(scaleDown)
                .scaleY(scaleDown)
                .setDuration(duration)
                .withEndAction {
                    this.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(duration)
                        .withEndAction { onClick() }
                        .start()
                }
                .start()
        }
    }

}