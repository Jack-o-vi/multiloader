package com.zeienko.servicesapp.ui.service

class ImageLoaderContainer() {

    private var size: Int = 5
        set(value) {
            if (value > 5) throw IllegalArgumentException()
            else field = value
        }

    private var curPos = 0

    val imageLoaders = ArrayList<ImageLoader?>(size)

    fun addImageLoader(imageLoader: ImageLoader) {
        imageLoaders.add(imageLoader)
    }

    fun runDownloads(){}

}