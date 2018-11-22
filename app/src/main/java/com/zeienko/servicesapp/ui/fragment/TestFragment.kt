package com.zeienko.servicesapp.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zeienko.servicesapp.R
import com.zeienko.servicesapp.domain.interactors.ControllerCallbackImp
import com.zeienko.servicesapp.ui.service.ImageLoader
import kotlinx.android.synthetic.main.test_fragment.*
import java.util.concurrent.atomic.AtomicInteger


class TestFragment : Fragment() {
    companion object {
        fun newInstance(): Fragment {
            return TestFragment()
        }

        val TAG = TestFragment::class.java.simpleName
    }

    private val handler = Handler()
    private var atomicInt = AtomicInteger(0)
    private val controllerCallbackImp = ControllerCallbackImp()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.test_fragment, container, false)
    }

    override fun onResume() {
        super.onResume()

        btnTestPress.setOnClickListener {
            atomicInt = AtomicInteger(0)
            imageLoader1?.resume()
            imageLoader2?.resume()
            controllerCallbackImp.isResumed = true

            ivImage1.setImageBitmap(null)
            ivImage2.setImageBitmap(null)
            createImageLoaderAndStartLoading1()
            createImageLoaderAndStartLoading2()
        }

        btnTestPress1.setOnClickListener {
            controllerCallbackImp.isResumed = false
        }

        btnTestPress2.setOnClickListener {
            controllerCallbackImp.isResumed = true
//            controllerCallbackImp.resume()
            imageLoader1?.resume()
            imageLoader2?.resume()
        }
    }

    private var imageLoader1: ImageLoader? = null
    private var imageLoader2: ImageLoader? = null

    private fun createImageLoaderAndStartLoading1() {
        imageLoader1 = ImageLoader(atomicInt.incrementAndGet(),
            { msg, id ->
                setText(msg, id)
            }, { path, id ->
                setImage(path, id)
            }, controllerCallbackImp
        )
        imageLoader1?.downloadImage()
    }

    private fun createImageLoaderAndStartLoading2() {
        imageLoader2 = ImageLoader(atomicInt.incrementAndGet(),
            { msg, id ->
                setText(msg, id)
            }, { path, id ->
                setImage(path, id)
            }, controllerCallbackImp
        )
        imageLoader2?.downloadImage()
    }

    private fun setText(msg: String, id: Int) {
        if (id == 1) {
            handler.post {
                tvTestText1.text = msg
            }
        } else if (id == 2) {
            handler.post {
                tvTestText2.text = msg
            }
        }
    }

    private fun setImage(path: String, id: Int = 0) {
        if (id == 1) {
            handler.post {
                ivImage1.setImageURI(Uri.parse(path))
            }
        } else if (id == 2) {
            handler.post {
                ivImage2.setImageURI(Uri.parse(path))
            }
        }
    }

}